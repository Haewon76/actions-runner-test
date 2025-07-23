package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.CouponMobileV2Mapper;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.vo.*;
import com.cashmallow.api.domain.model.coupon.vo.TargetType;
import com.cashmallow.api.domain.model.system.JobPlan;
import com.cashmallow.api.domain.model.system.SystemMapper;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.batch.client.BatchClient;
import com.cashmallow.api.interfaces.coupon.dto.req.*;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueQueueServiceImpl implements CouponIssueQueueService {

    private final GlobalQueueService globalQueueService;

    private final CouponUserService couponUserService;
    private final CouponIssueServiceV2 couponIssueService;
    private final CouponJobPlanService couponJobPlanService;
    private final CouponMobileV2Mapper couponMobileV2Mapper;
    private final AlarmService alarmService;
    private final SystemMapper systemMapper;

    private static final int issueDays = 7;           // 생일 쿠폰 7일 전 발급
    private static final int beforeExpireDays = 2;    // 만료 임박 알림 2일 전 발송
    private static final int hour = 11;                // 생일 쿠폰 발급 시각 시
    private static final int minute = 0;             // 생일 쿠폰 발급 시각 분
    private static final int second = 0;              // 생일 쿠폰 발급 시각 초

    private final BatchClient batchClient;

    private final CouponMobileServiceV2 couponMobileService;
    private final UserRepositoryService userRepositoryService;

    /**
     * 임의 쿠폰 예약 발급
     * - 모든 여행자 중에서 예약된 쿠폰을 발급받은 대상자
     * - coupon_issue 테이블 send_type 컬럼에 RESERVATION 으로 등록되어있는 쿠폰들 발급 상태 변경
     * - RESERVATION 으로 발급되어 있는 쿠폰은 유저가 조회할 수 없도록 해야 한다.
     **/
    @Override
    public void couponIssueListByReservation(String issueId) throws CashmallowException {
        String method = "couponIssueListByReservation()";
        log.info("{}: 예약 쿠폰 발급(임의 쿠폰, 예약 등록 생일 쿠폰)", method);

        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                null,
                null,
                "",
                "",
                "",
                AvailableStatus.RESERVATION.name(),
                null,
                Long.valueOf(issueId),
                null,
                null
        );

        List<CouponMobileUser> couponList
                =  couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest);

        // 1-2. 조회 데이터 있을 때만 진행
        if (!couponList.isEmpty()) {
            // 2. 쿠폰 발급 ID 추출
            List<Long> couponIssueIds = couponList.stream().map(CouponMobileUser::getCouponIssueId).collect(Collectors.toUnmodifiableList());

            // 3. 쿠폰 발급 ID 에 해당하는 coupon_user 테이블 available_status 컬럼 상태값을 RESERVATION -> AVAILABLE 로 변경
            Long updated = couponIssueService.updateReservedCouponIssueUsers(AvailableStatus.AVAILABLE.name(), couponIssueIds);
            if (updated < 1) {
                log.error("updateReservedCouponIssueUsers(): 쿠폰 예약 발급 오류");
                try {
                    throw new CashmallowException("쿠폰 예약 발급 오류");
                } catch (CashmallowException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

            List<String> jobKeyList = couponList.stream().map(CouponMobileUser::getJobKey).collect(Collectors.toUnmodifiableList());

            // 4. Job 성공 여부 Update
            Long successJob = couponJobPlanService.updateSuccessJobPlan(jobKeyList);
            if (successJob < 1) {
                log.error("Job 성공 여부 Update 오류");

                // 중복 출력되지 않도록 Set 에 담아준 후, 출력
                Set<Long> couponPushIssueIds = couponIssueIds.stream().collect(Collectors.toSet());
                Set<String> jobKeyPushList = jobKeyList.stream().collect(Collectors.toSet());

                String msg = "\n1시간 뒤에 재시도 합니다.\n쿠폰 발송 ID: "+couponPushIssueIds+"\nJobKey: "+jobKeyPushList;
                alarmService.aAlert("예약 쿠폰 발송 오류", msg, null);

                // 실패 시 1시간 후 재시도
                //  - Job 은 쿠폰 발급 ID 를 기준으로 등록되므로 무조건 0번째 index 만 존재함
                JobPlan jobPlan = systemMapper.getJobPlanByJobKey(couponList.get(0).getJobKey());
                if(!jobPlan.getIsExecuted()) {
                    // Job 삭제
                    batchClient.deleteJobSchedule(jobPlan.getId());
                    int deleted = systemMapper.deleteJobPlan(jobPlan.getJobKey());

                    // %d %d %d %d %d ? %d : 초 분 시 일 월 요일 년도
                    String[] cronIssueDateTime = jobPlan.getCronExpression().split(" ");
                    int sec = Integer.parseInt(cronIssueDateTime[0]); // 초
                    int min = Integer.parseInt(cronIssueDateTime[1]); // 분
                    int hour = Integer.parseInt(cronIssueDateTime[2]); // 시
                    int day = Integer.parseInt(cronIssueDateTime[3]); // 일
                    int month = Integer.parseInt(cronIssueDateTime[4]); // 월
                    // 요일은 필요하지 않으므로 5번째 index 는 뺀다
                    int year = Integer.parseInt(cronIssueDateTime[6]); // 년도

                    String cronExpression = couponJobPlanService.getCronExpression(LocalDateTime.of(year, month, day, hour, min, sec).plusHours(1));
                    jobPlan.setCronExpression(cronExpression);
                    if ( deleted > 0 ) {
                        log.info("1시간 후 실행되도록 Job 업데이트");
                        couponJobPlanService.insertJobPlan(jobPlan.getFromCountryCode(), jobPlan.getJobKey(), jobPlan.getCronExpression());
                    } else {
                        // 중복 출력되지 않도록 Set 에 담아준 후, 출력
                        msg = "\n쿠폰 발송 ID: "+couponPushIssueIds+"\nJobKey: "+jobKeyPushList;
                        alarmService.aAlert("예약 쿠폰 발송 재시도 성공", msg, null);
                    }
                }

            } else {

                // 중복 출력되지 않도록 Set 에 담아준 후, 출력
                Set<Long> couponPushIssueIds = couponIssueIds.stream().collect(Collectors.toSet());
                Set<String> jobKeyPushList = jobKeyList.stream().collect(Collectors.toSet());
                // jobKey 는 하나만 존재하므로 0번째 index 가져옴
                String[] jobKeySplit = couponList.get(0).getJobKey().split("_");
                String iso3166 = jobKeySplit[0]; // HK, JP
                String type = jobKeySplit[4]; // BIRTHDAY, ISSUE

                // 6-3. 일본 임의쿠폰 발급 데이터 전송
                // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
                CountryCode countryCode = CountryCode.fromIso3166(iso3166);
                if (CountryCode.JP.getCode().equals(countryCode.getCode())) {

                    List<CouponIssueUser> couponIssueUser
                            = couponIssueService.getUsersByCouponIssueId(Long.valueOf(issueId), null, null);

                    List<UpdateStatusUserCoupon> userCouponList = couponIssueUser.stream()
                                    .map(UpdateStatusUserCoupon::ofAvailable).toList();

                    globalQueueService.setUpdateStatusUserCoupon(userCouponList, AvailableStatus.AVAILABLE.name());
                }

                List<Long> userIds = couponList.stream().map(CouponMobileUser::getUserId).toList();
                List<User> userList = userRepositoryService.getUsersByUserIds(userIds);

                // 7. 예약 발급 성공하면 push 알림
                if(CouponJobKeyType.ISSUE.name().equals(type)) {
                    // 임의 쿠폰 발급 성공하면 push 알림
                    couponUserService.sendCouponPushMessage(userList, FcmEventCode.COUPON_ISSUE, FcmEventValue.CF);
                    String msg = "\n쿠폰 발송 ID: " + couponPushIssueIds + "\nJobKey: " + jobKeyPushList;
                    alarmService.aAlert("예약 쿠폰 발송", msg, null);
                }
            }
        }
    }


    /**
     * 생일 쿠폰 예약 등록 (시스템 쿠폰)
     * - 모든 여행자 중에서 생일자
     * - RabbitMQ 에서 호출 시에 JP/HK 각각 다른 배치가 0시 10분에 호출하여 RESERVATION 으로 등록
     **/
    @Override
    public void issueBirthDaySystemCoupon(String fromCountryCode) throws CashmallowException {
        String method = "issueBirthDaySystemCoupon()";
        // fromCountryCode: 캐시멜로에서 관리하는 국가코드

        // 1. 해당 국가의 LocalDate
        LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);
        log.info("{}: 해당 국가의 LocalDateTime 기준으로 조회: fromCountryCode={}, currentDate={}", method, fromCountryCode, currentDate);

        // 생일 쿠폰 형식
        // - 홍콩: Birthday(HK)
        // - 일본: Birthday(JP)
        CountryCode countryCode = CountryCode.of(fromCountryCode);
        String iso3166 = countryCode.name();

        // 현재 사용 중인 쿠폰 조회
        CouponSystemManagementRequest couponSystemManagementRequest = CouponSystemManagementRequest.builder()
                .fromCountryCode(countryCode.getCode())
                .couponType(SystemCouponType.birthday.getCode()) // 아래에서 welcome, birthday, thankYouMyFriend, thankYouToo 에 따라 바인딩해줌 (시스템 쿠폰만 해당)
                .currentDate(currentDate)
                .build();
        // 현재 적용 중인 생일 쿠폰 조회
        Coupon coupon = couponMobileService.isApplyingSystemCoupon(iso3166, couponSystemManagementRequest);
        if (coupon == null) {
            log.error("issueBirthDaySystemCoupon(): couponSystemManagementRequest={}", couponSystemManagementRequest);
            throw new CashmallowException(INVALID_COUPON);
        }

        // 시스템 쿠폰에 해당하는 통화 추가될 시, 업데이트 (서비스 종료할시 어차피 앱에서 표시 안되므로 삭제 X. 추가만 함)
        couponMobileService.addSystemCouponApplyCurrency(fromCountryCode, coupon);

        // 3. 올해 생일 쿠폰 발급받은 유저 ID 목록 조회
        List<Long> couponIssuedUserList = couponIssueService.getCouponIssuedBirthdayUserByCouponId(coupon.getId(), currentDate.getYear());

        // 4. 해당 국가의 LocalDateTime 기준으로 생일 n일 전 날짜 세팅 (자정부터 발급되어야 하므로 시간 필요 없음)
        // issueDays: 며칠 전 발급되는 지 (당일 포함이므로 -1)
        LocalDate beforeBirthday = DateUtil.beforeDay(currentDate, issueDays-1);

        // 5. 활성화된 모든 유저 중에서 7일 후 생일인 유저 조회
        List<User> userList = couponIssueService.getUserListByBirthday(fromCountryCode, beforeBirthday);

        // 6. 이미 생일 쿠폰을 발급 받은 유저는 제외
        for (Long userId : couponIssuedUserList) {
            userList.stream().filter(f -> userId.equals(f.getId()))
                             .findFirst().ifPresent(userList::remove);
        }

        log.info("생일 쿠폰 예약 등록할 유저 count: {}, 오늘날짜={}, 생일={}", userList.size(), currentDate, beforeBirthday);

        // 생일 쿠폰은 오전 11시에 발급하므로 발급 일자를 오전 11시로 설정
        LocalDateTime birthdayLocalDateTime = DateUtil.toLocalDateTimeAtTime(currentDate, hour, minute, second);
        String birthdayIssueDate = DateUtil.fromLocalDateTime(birthdayLocalDateTime);

        // 생일 쿠폰 대상자 있을 때만 진행
        if(!userList.isEmpty()) {
            List<Long> userIds = userList.stream().map(User::getId).toList();
            CouponIssueCreateRequest request = CouponIssueCreateRequest.withExceptOfIssueDate(
                    fromCountryCode
                    , coupon.getId()
                    , TargetType.SPECIFIC.getCode()
                    , SendType.RESERVATION.getCode()
                    , birthdayIssueDate
                    , -1L
                    , userIds
                    , null
            );

            log.info("생일 쿠폰 예약 등록: {}", userIds);

            // 6. 생일 쿠폰 예약 등록
            CouponIssue savedCouponIssueUser = couponIssueService.createCouponIssue(request);
            if(savedCouponIssueUser != null) {
                // 6-1. 생일 쿠폰 예약 등록할 때 Job Plan 생성
                String cronExpress = couponJobPlanService.getCronExpression(savedCouponIssueUser.getIssueDate());
                // 6-2. Job Plan 등록: 쿠폰 발급 저장 트랜잭션이 끝난 후에 실행되어야 하므로 여기서 실행
                couponJobPlanService.insertJobPlan(request.fromCountryCode(), savedCouponIssueUser.getJobKey(), cronExpress);
                // 6-3. 일본 생일쿠폰 발급 데이터 전송
                // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
                if (CountryCode.JP.getCode().equals(fromCountryCode)) {
                    globalQueueService.sendIssueSystemCoupon(savedCouponIssueUser, fromCountryCode, userIds, null);
                }

            } else {
                 log.error("생일 쿠폰 예약 등록 오류 fromCountryCode={}, currentDate={}", fromCountryCode, currentDate);
                 throw new CashmallowException("생일 쿠폰 예약 등록 오류: fromCountryCode="+fromCountryCode+", currentDate="+currentDate);
            }
        }
    }

    @Override
    @Transactional
    public void updateExpireCoupon(String fromCountryCode) throws CashmallowException {
        String method = "updateExpireCoupon()";

        // 1. 해당 국가의 LocalDate
        LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);
        log.info("{}: 해당 국가의 LocalDateTime 기준으로 조회: fromCountryCode={}, currentDate={}", method, fromCountryCode, currentDate);

        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                null,
                null,
                fromCountryCode,
                "",
                "",
                AvailableStatus.AVAILABLE.name(),
                null,
                null,
                null,
                null
        );

        // 2-1. 쿠폰 사용 가능한 모든 유저 조회
        List<CouponMobileUser> couponUserList = couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest);
        List<CouponIssueUserExpire> expireCouponList = new ArrayList<>();

        // 2-2. 사용 가능한 쿠폰을 보유하고 있는 유저 없으면 stop
        if(!couponUserList.isEmpty()) {
            for (CouponMobileUser couponMobileUser : couponUserList) {
                if (couponMobileUser.getIssueDate() == null || couponMobileUser.getCouponCalEndDate() == null) {
                    log.error("{}: issueDate()={}, couponCalEndDate()={},couponIssueId={}, couponUserId={}"
                            , method, couponMobileUser.getIssueDate(), couponMobileUser.getCouponCalEndDate(), couponMobileUser.getCouponIssueId(), couponMobileUser.getCouponUserId());
                    throw new CashmallowException(STATUS_INVALID_PARAMS);
                }
                // 현재 날짜(currentDate) 보다 쿠폰사용종료일(couponCalEndDate-계산된 날짜)이 이전일 경우
                if (couponMobileUser.getCouponCalEndDate().isBefore(currentDate)) {
                    expireCouponList.add(CouponIssueUserExpire.toLocalDate(couponMobileUser));
                }
            }

            log.info("유저 쿠폰 만료 갯수: {}, userExpireList={}", expireCouponList.size(), expireCouponList);

            // 3-3. 유저 있을 때만 진행
            if(!expireCouponList.isEmpty()) {
                // 4. 유저 쿠폰 상태값 EXPIRED 로 업데이트
                Long updated = couponIssueService.updateExpireCoupon(expireCouponList);
                if (updated < 1) {
                    log.error("[fromCountryCode:{}] EXPIRED 업데이트에 실패했습니다.", fromCountryCode);
                    throw new CashmallowException(COUPON_CANNOT_UPDATE_EXPIRED);
                }

                List<UpdateStatusUserCoupon> userCouponList = expireCouponList.stream()
                        .map(UpdateStatusUserCoupon::ofExpire).toList();

                // 일본 생일쿠폰 발급 데이터 전송
                // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
                if (CountryCode.JP.getCode().equals(fromCountryCode)) {
                    globalQueueService.setUpdateStatusUserCoupon(userCouponList, AvailableStatus.EXPIRED.name());
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public void pushExpireCoupon(String fromCountryCode) throws CashmallowException {
        String method = "pushExpireCoupon()";

        // 1. 해당 국가의 LocalDate
        LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);

        // 1-2. 만료 n일 후 계산: 만료일 2일 전(만료되는 당일 포함아여 총 3일)
        LocalDate expireDate = DateUtil.beforeDay(currentDate, beforeExpireDays);
        log.info("{}: 해당 국가의 LocalDateTime 기준으로 조회: fromCountryCode={}, currentDate={}, expireDate={}", method, fromCountryCode, currentDate, expireDate);

        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                null,
                null,
                fromCountryCode,
                "",
                "",
                AvailableStatus.AVAILABLE.name(),
                null,
                null,
                currentDate,
                null
        );
        // 2-1. 발급된 쿠폰 중 사용가능(coupon_user 테이블 available_status 컬럼이 AVAILABLE 인 유저 쿠폰)한 유저 쿠폰 조회
        //    - 쿠폰은 비활성화되었어도 만료일자가 지나면 EXPIRED 처리 해야 하므로 쿠폰의 활성화여부 체크하지 않음
        List<CouponMobileUser> couponList = couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest);
        List<CouponIssueUserExpire> pushCouponList = new ArrayList<>();

        // 2-2. 만료 임박 쿠폰있는 유저 없으면 stop
        if(!couponList.isEmpty()) {
            for (CouponMobileUser couponMobileUser : couponList) {
                if (couponMobileUser.getIssueDate() == null || couponMobileUser.getCouponCalEndDate() == null) {
                    log.error("{}: issueDate()={}, couponCalEndDate()={},couponIssueId={}, couponUserId={}"
                            , method, couponMobileUser.getIssueDate(), couponMobileUser.getCouponCalEndDate(), couponMobileUser.getCouponIssueId(), couponMobileUser.getCouponUserId());
                    throw new CashmallowException(STATUS_INVALID_PARAMS);
                }
                // 만료일 2일 전(expireDate: 만료되는 당일 포함아여 총 3일)이 쿠폰사용종료일(couponCalEndDate-계산된 날짜)과 같을 경우
                if (couponMobileUser.getCouponCalEndDate().equals(expireDate)) {
                    pushCouponList.add(CouponIssueUserExpire.toLocalDate(couponMobileUser));
                }
            }

            // 발급일자로부터 만료일수를 더한 날짜(쿠폰 사용기한은 발급당일 포함이므로 -1 해줌)가 만료 2일 전과 같을 때
            // 발급일자: 2025-02-27, 만료일수:3, push 알림: 27 + (3-1) = 2025-02-27
            if (!pushCouponList.isEmpty()) {
                // 3-1. Welcome / Not Welcome 으로 리스트 분리
                List<CouponIssueUserExpire> welcome = pushCouponList.stream().filter(f-> f.getCouponCode().startsWith(SystemCouponType.welcome.getCode())).toList();
                List<CouponIssueUserExpire> notWelcome = pushCouponList.stream().filter(f-> !f.getCouponCode().startsWith(SystemCouponType.welcome.getCode())).toList();

                // 3-2. userId 만 추출
                List<Long> welcomeUserIds = welcome.stream().map(CouponIssueUserExpire::getTargetUserId).collect(Collectors.toList());
                List<Long> notWelcomeUserIds = notWelcome.stream().map(CouponIssueUserExpire::getTargetUserId).collect(Collectors.toList());

                // 3-3. Welcome 인지 아닌지 구분 후, 만료 PUSH
                isWelcomeCoupon(welcomeUserIds, fromCountryCode);
                isNotWelcomeCoupon(notWelcomeUserIds, fromCountryCode);
            }

        }
    }

    public void isWelcomeCoupon(List<Long> userIds, String fromCountryCode) {
        log.info("가입 쿠폰 만료 알림 count: {}", userIds.size());

        // 유저 있을 때만 진행
        if(!userIds.isEmpty()) {
            // 4. fromCountryCode 과 만료 n일전 사용가능한 쿠폰을 보유하고 있는 유저 ID 로 유저 리스트 조회
            List<User> userList = couponIssueService.getUserListByExpire(fromCountryCode, userIds);

            // 5. push 보내기
            couponUserService.sendCouponPushMessage(userList, FcmEventCode.COUPON_WELCOME_EXPIRE, FcmEventValue.CF);
        }
    }

    public void isNotWelcomeCoupon(List<Long> userIds, String fromCountryCode) {
        log.info("가입 쿠폰 아닌 쿠폰 만료 알림 count: {}", userIds.size());

        //  유저 있을 때만 진행
        if(!userIds.isEmpty()) {
            // 4. fromCountryCode 과 만료 n일전 사용가능한 쿠폰을 보유하고 있는 유저 ID 로 유저 리스트 조회
            List<User> userList = couponIssueService.getUserListByExpire(fromCountryCode, userIds);

            // 5. push 보내기
            couponUserService.sendCouponPushMessage(userList, FcmEventCode.COUPON_EXPIRE, FcmEventValue.CF);
        }
    }

}