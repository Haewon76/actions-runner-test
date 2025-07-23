package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.*;
import com.cashmallow.api.domain.model.coupon.entity.ApplyCurrency;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.*;
import com.cashmallow.api.domain.model.coupon.vo.TargetType;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponIssueCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueReadResponse;
import com.cashmallow.api.interfaces.user.dto.UserSearchRequest;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueServiceImplV2 implements CouponIssueServiceV2 {

    private final CouponIssueV2Mapper couponIssueMapper;

    private final CouponServiceV2 couponService;
    private final CouponUserService couponUserService;

    private final CouponValidationService couponValidationService;
    private final CouponJobPlanService couponJobPlanService;

    private final UserRepositoryService userRepositoryService;
    private final TravelerRepositoryService travelerRepositoryService;

    private static final String COUPON_ISSUE_RESERVATION_JOB_KEY = "%s_%s_%s_COUPON_ISSUE_RESERVATION";
    private static final String COUPON_BIRTHDAY_RESERVATION_JOB_KEY = "%s_%s_%s_COUPON_BIRTHDAY_RESERVATION";

    @Override
    @Transactional(readOnly = true)
    public List<CouponIssueReadResponse> getCouponIssueList(CouponSearchRequest couponSearchRequest) {

        // 1. 쿠폰 정보 및 쿠폰 발급 목록 조회 (페이징 및 조회조건 걸려있음)
        List<CouponIssueManagement> couponIssueList = couponIssueMapper.getCouponIssueList(couponSearchRequest);

        if(couponIssueList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2-1. 쿠폰 ID 만 추출
        List<Long> couponIds = couponIssueList.stream()
                .map(CouponIssueManagement::getCouponId).toList();

        // 2-2. 중복 제거하고 couponIdList List 로 추출
        Set<Long> set = new HashSet<>(couponIds);
        List<Long> couponIdList = set.stream().toList();

        // 3. 쿠폰 ID 에 해당하는 적용통화 조회
        List<ApplyCurrency> applyCurrencyByCouponList = couponIssueMapper.applyCurrencyByCouponList(couponIdList);

        List<Long> createdIds = couponIssueList.stream().map(CouponIssueManagement::getCreatedId).toList();
        List<User> createdUsers = userRepositoryService.getUsersByUserIds(createdIds);

        // 4-1. 쿠폰 발급 ID 로 조합하여 response 로 return
        return couponIssueList.stream()
                .map(data ->
                    CouponIssueReadResponse.of(data,
                        // userId 로 Name 필터링
                        createdUsers.stream().filter(f -> f.getId().equals(data.getCreatedId())).findFirst(),
                        // 4-2. 쿠폰 ID 에 해당하는 통화 목록 조회
                        applyCurrencyByCouponList.stream()
                            .filter(currency -> currency.getCouponId().equals(data.getCouponId()))
                            .map(ApplyCurrency::getTargetIso3166)
                            .collect(Collectors.toList())
                    )
                ).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CouponIssue createCouponIssue(CouponIssueCreateRequest couponIssueCreateRequest) throws CashmallowException {
        // 1. 쿠폰 ID 조회
        Coupon coupon = couponService.getCouponById(couponIssueCreateRequest.couponId());

        if (coupon == null) {
            throw new CashmallowException(INVALID_COUPON);
        }

        if (N.equals(coupon.getIsActive())) {
            throw new CashmallowException("비활성화 쿠폰입니다. 활성화 여부를 변경해주세요.");
        }

        List<Long> users = null;
        // 2. 발급 대상 유저 ID 를 List 에 바인딩 (본인인증을 완료한 활성화된 유저)
        if (TargetType.EVERYONE.getCode().equals(couponIssueCreateRequest.targetType())) {
            // 쿠폰 발급할 fromCountry 고객 조회
            users = couponService.getUsersByFromCountryCode(coupon.getFromCountryCode());
            if (users == null || users.isEmpty()) {
                throw new CashmallowException(CountryCode.of(coupon.getFromCountryCode()).getKorName() + " 대상 고객이 없습니다.");
            }
            couponIssueCreateRequest = couponIssueCreateRequest.withUsers(users);
        } else {
            users = couponIssueCreateRequest.users();
        }

        if (users.isEmpty()) {
            throw new CashmallowException("발급할 대상이 없습니다.");
        }

        // 3. 쿠폰 발급: coupon_issue 테이블에 데이터 생성
        CouponIssue couponIssue = couponIssueCreateRequest.toEntity();

        // 4. 발급 받은 유저 있는지 확인 : 시스템 쿠폰은 admin 에서 발급할 수 없으므로 따로 체크하지 않음
        List<CouponUser> existedCouponUserList
                = couponUserService.getUserListCouponByUserIdsAndCouponId(users, coupon.getId());
        // 4-1. userId List 로 재조합
        List<Long> existedCouponUserIds = existedCouponUserList.stream().map(CouponUser::getTargetUserId).toList();
        // 4-4. 해당 유저가 발급 받은 적 있거나 과거에 발급 받은 이력 있을 경우 체크
        // ThankYouToo 쿠폰이나 Birthday 쿠폰은 계속 발급 받아도 되므로 제외
        if (!coupon.getCouponCode().startsWith(SystemCouponType.birthday.getCode())
                && !coupon.getCouponCode().startsWith(SystemCouponType.thankYouToo.getCode())
                && !existedCouponUserIds.isEmpty()) {
            log.error("해당 쿠폰을 이미 발급받은 유저가 있습니다. userIds={}, couponCode={}", existedCouponUserIds, coupon.getCouponCode());
            throw new CashmallowException("해당 쿠폰을 이미 발급받은 유저가 있습니다. userIds=" + existedCouponUserIds + ", couponCode=" + coupon.getCouponCode());
        }

        // 4-2. 인증 정보 필요하므로 userId 로 traveler List 조회
        List<Traveler> travelerList = travelerRepositoryService.getTravelersByUserIds(users);
        // 4-3. 이벤트 쿠폰 아닐 시 중복 체크
        if (!coupon.getCouponCode().startsWith(SystemCouponType.event.getAbbreviation())) {
            // 4-3-1.  influencer, thankYouMyFriend(평생 1회) 쿠폰일 경우 유저의 과거 탈퇴 계정 등록 및 발급이력 체크
            List<Long> hasThankYouMyFriendUserIds = couponValidationService.hasRegisteredCouponByUserList(users, SystemCouponType.thankYouMyFriend.getCode());
            List<Long> hasInfluencerUserIds = couponValidationService.hasRegisteredCouponByUserList(users, SystemCouponType.influencer.getCode());
            // 탈퇴 이력 조회하여  해당 유저가 평생 1회 발급받은 기록있는지 체크함
            if (!hasThankYouMyFriendUserIds.isEmpty() || !hasInfluencerUserIds.isEmpty()) {
                String duplicatedCouponUserIds = null;
                if (!hasThankYouMyFriendUserIds.isEmpty()) {
                    duplicatedCouponUserIds = hasThankYouMyFriendUserIds.toString();
                } else {
                    duplicatedCouponUserIds = hasInfluencerUserIds.toString();
                }
                log.error("평생 1회 발급 가능한 쿠폰을 이미 발급받은 유저가 있습니다. userIds={}, couponCode={}", duplicatedCouponUserIds, coupon.getCouponCode());
                throw new CashmallowException("평생 1회 발급 가능한 쿠폰을 이미 발급받은 유저가 있습니다. userIds=" + duplicatedCouponUserIds + ", couponCode=" + coupon.getCouponCode());
            }
            // 4-3-2. influencer, thankYouMyFriend 쿠폰일 경우 유저의 과거 탈퇴 계정 거래내역 체크
            List<Long> hasHistoriesUserIds = couponValidationService.hasInactiveTransactionHistoryByUserList(users);
            // 탈퇴 이력 조회하여  해당 유저가 평생 1회 발급받은 기록있는지 체크함
            if (!hasHistoriesUserIds.isEmpty()) {
                log.error("과거에 가입했던 계정에서 거래내역이 이미 존재하는 유저가 있습니다. userIds={}, couponCode={}", hasHistoriesUserIds, coupon.getCouponCode());
                throw new CashmallowException("과거에 가입했던 계정의 거래내역이 이미 존재하는 유저가 있습니다. userIds=" + hasHistoriesUserIds + ", couponCode=" + coupon.getCouponCode());
            }
        // 4-4. 이벤트 쿠폰 일 시 중복 체크
        } else {
            List<Long> hasEvenCouponUserIds = couponValidationService.hasRegisteredCouponByUserList(users, coupon.getCouponCode());
            if (!hasEvenCouponUserIds.isEmpty()) {
                log.error("해당 쿠폰을 이미 발급받은 유저가 있습니다. users={}, couponCode={}", hasEvenCouponUserIds, coupon.getCouponCode());
                throw new CashmallowException("해당 쿠폰을 이미 발급받은 유저가 있습니다. users=" + hasEvenCouponUserIds + ", couponCode=" + coupon.getCouponCode());
            }
        }

        couponIssueMapper.createCouponIssue(couponIssue);
        log.debug("couponIssue.getId(): {}", couponIssue.getId());

        // 5. 예약 발급일 때와 즉시 발급일 때 쿠폰 상태 분류
        String availableStatus = "";
        if (SendType.DIRECT.getCode().equals(couponIssueCreateRequest.sendType())) {
            // 5-1. 즉시발급일때 coupon_user 테이블 available_status 컬럼값 AVAILABLE
            availableStatus = AvailableStatus.AVAILABLE.name();
        } else {
            // 5-2-1. 예약발급일때 coupon_user 테이블 available_status 컬럼값 RESERVATION
            availableStatus = AvailableStatus.RESERVATION.name();

            // 5-2-2. jobKey, cronExpress 설정
            CountryCode countryCode = CountryCode.of(couponIssueCreateRequest.fromCountryCode());
            String iso3166 = countryCode.name();
            String jobKey = null;

            // 생일쿠폰일시 jobKey
            if (SystemCouponType.BirthdayHK.getCode().equals(coupon.getCouponCode()) || SystemCouponType.BirthdayJP.getCode().equals(coupon.getCouponCode())) {
                jobKey = couponJobPlanService.getJobKey(COUPON_BIRTHDAY_RESERVATION_JOB_KEY, iso3166, couponIssue.getId());
            // 임의쿠폰일시 jobKey
            } else {
                jobKey = couponJobPlanService.getJobKey(COUPON_ISSUE_RESERVATION_JOB_KEY, iso3166, couponIssue.getId());
            }

            couponIssue = couponIssue.withJobKey(jobKey);

            // 5-2-3. jobKey update
            Long updatedJobKey = couponJobPlanService.updateJobKeyCouponIssue(couponIssue.getId(), couponIssue.getJobKey());
            if(updatedJobKey == 0) {
                throw new CashmallowException("[CouponIssueId: "+couponIssue.getId()+", JobKey: "+couponIssue.getJobKey()+"]예약 쿠폰 JobKey 등록에 실패하였습니다.");
            }
        }

        CouponUser couponUser = CouponUser.builder()
                .couponIssueId(couponIssue.getId())
                .couponId(coupon.getId())
                .inviteUserId(couponIssueCreateRequest.inviteUserId())
                .couponUsedAmount(BigDecimal.ZERO)
                .couponUsedDate(null)
                .couponUsedDateUtc(null)
                .availableStatus(availableStatus)
                .build();

        // 6. coupon_user 테이블에 발급 쿠폰 데이터 생성
        Long savedUsersCoupon = couponIssueMapper.createUsersCoupon(couponUser, users);
        if(savedUsersCoupon == 0) {
            // IsSystem = 'Y' 일 시에는 RabbitMQ 로 Insert
            // IsSystem = 'N' 일 시에는 admin 에서 직접 Insert
            throw new CashmallowException("[IsSystem: "+coupon.getIsSystem()+", CouponCode: "+coupon.getCouponCode()+"]일부 유저의 쿠폰 발급에 실패하였습니다.");
        }
        // 7. 즉시 발급 쿠폰 유저에게 알림 보냄
        if (SendType.DIRECT.getCode().equals(couponIssueCreateRequest.sendType())) {
            // 7-1. 유저 있을 때만 진행
            if(!users.isEmpty()) {
                UserSearchRequest request = new UserSearchRequest();
                request.setIds(users);
                // 7-2. 유저 조회
                List<User> userList = userRepositoryService.getUsers(request);
                log.debug("쿠폰 즉시 발급 userList: {}", userList.toString());
                // 7-3. push 보내기
                couponUserService.sendCouponPushMessage(userList, FcmEventCode.COUPON_ISSUE, FcmEventValue.CF);
            }
        }
        return couponIssue;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCouponTotalCount(CouponSearchRequest couponSearchRequest) {
        return couponIssueMapper.getCouponIssueCountTotal(couponSearchRequest);
    }

    @Override
    public List<CouponIssueUser> getUsersByCouponIssueId(Long couponIssueId, String sortColumnCode, String sortColumnOrder) throws CashmallowException {

        if (sortColumnOrder != null) {
            sortColumnOrder = sortColumnOrder.toUpperCase();
        }

        List<CouponIssueUser> couponIssueUserList
                = couponIssueMapper.getUsersByCouponIssueId(couponIssueId, sortColumnCode, sortColumnOrder);

        if(couponIssueUserList == null || couponIssueUserList.isEmpty()) {
            throw new CashmallowException("발급된 쿠폰에 해당하는 유저가 존재하지 않습니다.");
        }

        return couponIssueUserList;
    }




    /**
     * 예약 발급
     * - 모든 여행자 대상
     * - coupon_issue 테이블 send_type 컬럼에 RESERVATION 으로 등록되어있는 쿠폰들 발급 상태 변경
     * - RESERVATION 으로 발급되어 있는 쿠폰은 유저가 조회할 수 없도록 해야 한다.
     **/
    @Override
    public Long updateReservedCouponIssueUsers(String availableStatus, List<Long> couponIssueIds) {
        return couponIssueMapper.updateReservedCouponIssueUsers(availableStatus, couponIssueIds);
    }

    /**
     * 시스템 쿠폰 발급
     * 7일 후 생일자 대상
     **/
    @Override
    public List<User> getUserListByBirthday(String fromCountryCode, LocalDate beforeBirthday) {
        List<User> usersByBirthday = couponIssueMapper.getUserListByEvent(fromCountryCode, DateUtil.fromLocalDateToYMD(beforeBirthday), null);

        if (usersByBirthday == null || usersByBirthday.isEmpty()) {
            return Optional.ofNullable(usersByBirthday).orElse(new ArrayList<>());
        }
        return usersByBirthday;
    }

    @Override
    public List<Long> getCouponIssuedBirthdayUserByCouponId(Long couponId, int currentYear) {
        return couponIssueMapper.getCouponIssuedBirthdayUserByCouponId(couponId, currentYear);
    }

    /**
     * 쿠폰 만료 알림
     * 사용가능한 쿠폰이 2일 남은 유저 대상
     **/
    @Override
    public List<User> getUserListByExpire(String fromCountryCode, List<Long> userIds) {
        List<User> usersByBirthday = couponIssueMapper.getUserListByEvent(fromCountryCode, null, userIds);

        if (usersByBirthday == null || usersByBirthday.isEmpty()) {
            return Optional.ofNullable(usersByBirthday).orElse(new ArrayList<>());
        }
        return usersByBirthday;
    }

    @Override
    @Transactional
    public Long updateExpireCoupon(List<CouponIssueUserExpire> userList) {
        return couponIssueMapper.updateExpireCoupon(userList);
    }

    @Override
    public CouponIssue getCouponIssuedById(Long issueId) {
        return couponIssueMapper.getCouponIssuedById(issueId);
    }

    @Override
    public int deleteCouponIssuedById(Long issueId) {
        return couponIssueMapper.deleteCouponIssuedById(issueId);
    }

    @Override
    public int deleteCouponIssuedUserByCouponIssueId(Long issueId) {
        return couponIssueMapper.deleteCouponIssuedUserByCouponIssueId(issueId);
    }

    @Override
    public int deleteApplyCurrencyByCouponId(Long couponId, List<String> applyCurrencyList) {
        return couponService.deleteApplyCurrencyByCouponId(couponId, applyCurrencyList);
    }

    @Override
    public int updateStatusByCouponIssueUserSyncIds(List<UpdateStatusUserCoupon> userCouponList, String availableStatus) {
        return couponIssueMapper.updateStatusByCouponIssueUserSyncIds(userCouponList, availableStatus);
    }
}