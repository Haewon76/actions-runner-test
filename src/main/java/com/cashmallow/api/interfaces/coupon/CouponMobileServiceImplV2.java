package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.*;
import com.cashmallow.api.domain.model.coupon.entity.CouponUserInviteCode;
import com.cashmallow.api.domain.model.coupon.entity.*;
import com.cashmallow.api.domain.model.coupon.vo.*;
import com.cashmallow.api.domain.model.coupon.vo.TargetType;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.coupon.dto.*;
import com.cashmallow.api.interfaces.coupon.dto.req.*;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueUserResponse;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponMobileServiceImplV2 implements CouponMobileServiceV2 {

    private final CouponUserInviteCodeService couponUserInviteCodeService;
    private final CouponValidationService couponValidationService;

    private final CouponServiceV2 couponServiceV2;
    private final CouponIssueServiceV2 couponIssueServiceV2;

    private final CouponV2Mapper couponV2Mapper;
    private final CouponIssueV2Mapper couponIssueV2Mapper;
    private final CouponMobileV2Mapper couponMobileV2Mapper;
    private final CouponUserMapper couponUserMapper;

    private final GlobalQueueService globalQueueService;

    private final CountryService countryService;
    private final UserRepositoryService userRepositoryService;
    private final CouponSystemManagementService couponSystemManagementService;
    private final CouponUserService couponUserService;
    private final TravelerRepositoryService travelerRepositoryService;

    @Value("${host.cdn.url}")
    private String hostUrl;

    @Value("${coupon.thumbnail.url}")
    private String thumbnailUrl;

    @Value("${short.url}")
    private String shortUrl;

    private final MessageSource messageSource;

    private final static int month = 1; // 탈퇴일자로 부터 1개월 내
    private final static int days = 6; // 인플루언서 쿠폰 등록기한. 가입일로부터 6일이 지났을때까지 (당일포함 등록기한 7일)


    @Override
    public CouponIssueMobileResponse getMobileSystemCoupon(String currency, String couponCodePrefix, Long userId) throws CashmallowException {
        // country code 정보 조회
        CountryCode countryCode = CountryCode.fromCurrency(currency);
        String fromCountryCode = countryCode.getCode();
        String iso3166 = countryCode.name();

        // 초대 코드 조회하여 없으면 생성
        CouponUserInviteCode inviteCode = couponUserInviteCodeService.getInviteCodeByUserId(userId);
        if (inviteCode == null) {
            CouponUserInviteCodeRequest inviteCodeRequest
                    = CouponUserInviteCodeRequest.builder()
                    .userId(userId)
                    .abbreviation(SystemCouponType.thankYouMyFriend.getAbbreviation())
                    .iso3166(iso3166)
                    .build();

            // 초대코드 생성하여 insert
            couponUserInviteCodeService.getCouponUserInviteCodeV3(inviteCodeRequest);
            inviteCode = couponUserInviteCodeService.getInviteCodeByUserId(userId);
        }

        // 쿠폰 코드로 쿠폰 정보 조회
        String couponCode = getCouponCode(couponCodePrefix, iso3166, null);
        Coupon coupon = couponServiceV2.getCouponByCouponCode(fromCountryCode, Y, Y, couponCode);

        CouponIssueMobileResponse couponIssueMobileResponse = CouponIssueMobileResponse.inviteCouponValue(coupon, currency, inviteCode.getInviteCode());
        couponIssueMobileResponse.setLink(makeCouponLinkUrl());

        return couponIssueMobileResponse;
    }

    private String makeCouponLinkUrl() {
        return shortUrl + "/a";
    }

    /** 쿠폰함 조회는 userId 만 필요함 **/
    // userId 로 조회하는 것이기 때문에 fromCurrency 굳이 필요하지 않으나 기존 코드에서 사용했었기에 혹시 몰라 남겨둠.
    // 한 유저가 fromCurrency 를 2가지 이상 사용할 수 있을 경우에는 사용할 수도 있음.
    @Override
    @Transactional(readOnly = true)
    public List<CouponIssueUserResponse> getCouponIssueUserV2(Long userId, ServiceType serviceType, String fromCurrency, BigDecimal fromMoney, BigDecimal fee, Locale locale, boolean amountWithFeeFlag) throws CashmallowException {

        if (fromMoney.compareTo(BigDecimal.ZERO) < 0) {
            log.error("getCouponIssueUserV2(): fromMoney less than zero fromMoney={}", fromMoney);
            throw new CashmallowException(CODE_INVALID_PARAMS);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        LocalDate currentDate = DateUtil.toLocalDate(user.getCountry());

        // 1. 특정 서비스 타입만 이용 가능한 것이 아닐 시, 전부 조회
        //   - EXCHANGE, REMITTANCE 가 아닐 때에는 all 로 들어옴
        String serviceTypeCode = "";
        if (ServiceType.ALL.getCode().equals(serviceType.getCode())) {
            serviceTypeCode = ServiceType.ALL.getCode();
        } else {
            serviceTypeCode = serviceType.getCode();
        }

        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                userId,
                null,
                "",
                serviceTypeCode,
                "",
                AvailableStatus.AVAILABLE.name(),
                fee,
                null,
                currentDate,
                Const.Y
        );

        if(fee == null) {
            fee = BigDecimal.ZERO;
        }

        // 2. userId 와 거래유형(serviceType)에 해당하는 사용가능한 쿠폰 조회 후, 재계산
        // 정렬 조건 2가지 (1) 만료 임박한 쿠폰 순으로 정렬 (2) 최대 할인금액 높은 순으로 정렬
        //   - (1) 만료 임박한 쿠폰순으로 정렬
        BigDecimal finalFee = fee;
        List<CouponMobileUser> couponMobileUser
                =  couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest)
                .stream().map(data -> {
                    // 2-2. Locale 에 따라 시스템 쿠폰은 다국어처리
                    data.setCouponName(getSystemCouponNameByLocale(data.getCouponCode(), data.getCouponName(), locale));
                    data.setCouponDescription(getSystemCouponDescriptionByLocale(data.getCouponCode(), data.getCouponDescription(), locale));
                    return data;
                }).map(data -> {
                    // fromMoney 에 따른 할인 금액 계산 (환전,송금 선택 시에 목록 호출하므로 계산 로직 추가함.
                    CouponCalcResponse calcResponse = couponCalcMoneyMobileV2(data, fromMoney, finalFee);
                    data.setCouponCalDiscountValue(calcResponse.getDiscountAmount() == null ? BigDecimal.ZERO : calcResponse.getDiscountAmount());
                    return data;
                }).toList();

        // 3. 앱으로 Response
        //   - (2) 최대 할인금액 높은 순으로 정렬. 사용가능한 걸 상위에 보여줌
        return couponMobileUser.stream()
                .map(coupon ->
                        CouponIssueUserResponse.ofMobile(
                                coupon,
                                finalFee,
                                validationMinRequiredAmount(coupon, finalFee, fromMoney, amountWithFeeFlag),
                                getApplyCurrencyList(coupon.getCouponId())
                        )
                )
                .sorted(Comparator.comparing(CouponIssueUserResponse::isAvailability).reversed()
                        .thenComparing(CouponIssueUserResponse::getEndDate)
                        .thenComparing(CouponIssueUserResponse::getDiscountValue, Comparator.reverseOrder())
                ).toList();
    }

    /** 쿠폰함 조회는 userId 만 필요함 **/
    @Override
    @Transactional(readOnly = true)
    public List<CouponIssueUserResponse> getCouponIssueUserMyPageV2(Long userId, String fromCurrency, Locale locale) {
        User user = userRepositoryService.getUserByUserId(userId);
        LocalDate currentDate = DateUtil.toLocalDate(user.getCountry());

        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                userId,
                null,
                "",
                null,
                "",
                AvailableStatus.AVAILABLE.name(),
                null,
                null,
                currentDate,
                Const.Y
        );

        // 2. userId 와 거래유형(serviceType)에 해당하는 사용가능한 쿠폰 조회 후, 재계산
        // 정렬 조건 2가지 (1) 만료 임박한 쿠폰 순으로 정렬 (2) 최대 할인금액 높은 순으로 정렬
        //   - (1) 만료 임박한 쿠폰 순으로 정렬(쿼리에서 처리)
        List<CouponMobileUser> couponMobileUser
                =  couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest)
                .stream().map(data -> {
                    // 2-2. Locale 에 따라 시스템 쿠폰은 다국어처리
                    data.setCouponName(getSystemCouponNameByLocale(data.getCouponCode(), data.getCouponName(), locale));
                    data.setCouponDescription(getSystemCouponDescriptionByLocale(data.getCouponCode(), data.getCouponDescription(), locale));
                    return data;
                }).toList();

        // 3. 앱으로 Response
        //   - (2) 최대 할인금액 높은 순으로 정렬
        return couponMobileUser.stream()
                .map(coupon ->
                        CouponIssueUserResponse.ofMoney(
                                coupon,
                                BigDecimal.ZERO, // 쿠폰함은 수수료를 알지 못하므로 0으로 고정 (null 들어가면 정렬할때 에러남)
                                Const.TRUE,
                                getApplyCurrencyList(coupon.getCouponId())
                        )
                )
                .sorted(Comparator.comparing(CouponIssueUserResponse::getEndDate)
                        .thenComparing(CouponIssueUserResponse::getDiscountValue, Comparator.reverseOrder())
                ).toList();
    }

    public String getCouponCode(String couponCodePrefix, String iso3166, String couponCodeBody) {
        // couponCodePrefix + iso3166 = Welcome + HK
        // result: Welcome(HK)
        log.info("couponCodePrefix={}, iso3166={}, couponCodeBody={}", couponCodePrefix, iso3166, couponCodeBody);
        String result = SystemCouponType.valueOf(couponCodePrefix + iso3166).getCode();
        if(couponCodeBody != null) {
            result += couponCodeBody;
        }
        return result;
    }

    @Override
    public int updateCouponStatus(Long couponUserId, AvailableStatus availableStatus) {
        return couponMobileV2Mapper.updateCouponStatus(couponUserId, availableStatus.name());
    }

    @Override
    public int updateListCouponStatus(List<Long> couponUserIds, AvailableStatus availableStatus) {
        return couponMobileV2Mapper.updateListCouponStatus(couponUserIds, availableStatus.name());
    }

    // 적용중인 시스템 쿠폰 조회
    @Override
    public Coupon isApplyingSystemCoupon(String iso3166, CouponSystemManagementRequest couponSystemManagementRequest) {
        CouponSystemManagement couponSystemManagement = couponSystemManagementService.getUsingCouponDateRange(couponSystemManagementRequest);
        if (couponSystemManagement == null) {
            couponSystemManagement = couponSystemManagementService.getUsingCoupon(couponSystemManagementRequest);
        }

        String systemCouponCode = getCouponCode(couponSystemManagementRequest.getCouponType(), iso3166, couponSystemManagement.getCouponCodeBody());
        return couponServiceV2.getCouponByCouponCode(couponSystemManagementRequest.getFromCountryCode(), Y, Y, systemCouponCode);
    }

    @Override
    @Transactional
    public ApiResultVO issueMobileCouponsV3(String couponCode, String iso3166, String fromCurrency, Long userId, ZoneId zoneId, Locale locale, Long couponUserId) {
        ApiResultVO apiResultVO = new ApiResultVO();
        try{
            // 쿠폰은 무조건 평생 1회 발급 가능하므로 과거 가입 내역까지 체크함
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            CountryCode countryCode = CountryCode.fromIso3166(iso3166);

            // 현재 사용 중인 쿠폰 조회
            LocalDate currentDate = DateUtil.toLocalDate(countryCode.getCode());
            CouponSystemManagementRequest couponSystemManagementRequest = CouponSystemManagementRequest.builder()
                    .fromCountryCode(countryCode.getCode())
                    .couponType(null) // 아래에서 welcome, birthday, thankYouMyFriend, thankYouToo 에 따라 바인딩해줌 (시스템 쿠폰만 해당)
                    .currentDate(currentDate)
                    .build();

            // Welcome 쿠폰일 때 발급 가능한 유저 대상인지 체크 후에 발급 (본인인증하기 전 이므로 identificationNumber 는 null)
            if (couponCode.startsWith(SystemCouponType.welcome.getCode())
                    && !couponValidationService.hasUsedSystemCoupon(userId, SystemCouponType.welcome, null, month)) {

                // 기간 한정 적용 쿠폰 있으면 해당 쿠폰으로 발급하기 위해 쿠폰 코드 조회해옴
                couponSystemManagementRequest.setCouponType(SystemCouponType.welcome.getCode());
                // 현재 적용 중인 가입 쿠폰 조회
                Coupon coupon = isApplyingSystemCoupon(iso3166, couponSystemManagementRequest);
                if (coupon == null) {
                    log.error("issueWelcomeV3(): couponSystemManagementRequest={}", couponSystemManagementRequest);
                    throw new CashmallowException(INVALID_COUPON);
                }

                // couponCode ex: Welcome(HK), Welcome(JP),  Welcome(HK)_50_250531 ...
                // Welcome 쿠폰은 가입과 동시에 생성하므로 return 하지 않음
                issueWelcomeV3(coupon, iso3166, fromCurrency, userId, zoneId, locale);

                // couponCode 가 FR 로 시작할시 친구 초대 쿠폰으로 인식
            } else if (couponCode.startsWith(SystemCouponType.thankYouMyFriend.getAbbreviation())) {

                // 기간 한정 적용 쿠폰 있으면 해당 쿠폰으로 발급하기 위해 쿠폰 코드 조회해옴
                couponSystemManagementRequest.setCouponType(SystemCouponType.thankYouMyFriend.getCode());

                // 현재 적용 중인 초대 쿠폰 조회
                Coupon coupon = isApplyingSystemCoupon(iso3166, couponSystemManagementRequest);
                if (coupon == null) {
                    log.error("issueThankYouMyFriendV3(): couponSystemManagementRequest={}", couponSystemManagementRequest);
                    throw new CashmallowException(INVALID_COUPON);
                }
                // 친구 초대 쿠폰은 couponCode 가 inviteCode(ex: FR83D4DGHK)로 들어옴
                // couponCode = FR + [(숫자 + 영문 대문자) Random 6자리] + [iso3166]
                return issueThankYouMyFriendV3(couponCode, coupon, fromCurrency, userId, locale, traveler);

            } else if (couponCode.startsWith(SystemCouponType.thankYouToo.getCode())) {
                // 기간 한정 적용 쿠폰 있으면 해당 쿠폰으로 발급하기 위해 쿠폰 코드 조회해옴
                couponSystemManagementRequest.setCouponType(SystemCouponType.thankYouToo.getCode());
                // 현재 적용 중인 가입 쿠폰 조회
                Coupon coupon = isApplyingSystemCoupon(iso3166, couponSystemManagementRequest);
                if (coupon == null) {
                    log.error("issueThankYouTooV3(): couponSystemManagementRequest={}", couponSystemManagementRequest);
                    throw new CashmallowException(INVALID_COUPON);
                }
                // ex: ThankYouToo(HK), ThankYouToo(JP), ThankYouToo(HK)_3000 ...
                issueThankYouTooV3(couponUserId, userId, coupon);

            } else if (couponCode.startsWith(SystemCouponType.influencer.getAbbreviation())) {
                // couponCode = IN + [(숫자 + 영문 대문자) Random 6자리] + [iso3166]
                return issueInfluencerV3(couponCode, currentDate, fromCurrency, userId, locale, traveler);

            } else {
                // 일반 쿠폰 처리
                // EV + [(숫자 + 영문 대문자) Random 6자리] + [iso3166]
                // 쿠폰 코드 체계 생기기 전 발급된 과거 쿠폰들의 경우 코드가 다를 수 있음
                return issueCouponForMobileV3(couponCode, currentDate, iso3166, userId, locale, traveler);
            }
        } catch (CashmallowException e) {
            log.error("issueMobileCoupons error", e);
            apiResultVO.setFailInfo(e.getMessage());
        }
        return apiResultVO;
    }

    public void addSystemCouponApplyCurrency(String fromCountryCode, Coupon coupon) {
        Map<String, Object> params  = new HashMap<>();
        params.put("service", "Y");
        // 전체 서비스 통화
        List<Country> countryList = countryService.getCountryList(params);
        List<String> iso3166List = countryList.stream().map(Country::getIso3166).collect(Collectors.toSet()).stream().collect(Collectors.toList());
        // 현재 서비스되고 있는 통화
        List<String> nowServiceIso3166 = couponServiceV2.getApplyCurrencyListByCouponId(coupon.getId());

        if(iso3166List.size() != nowServiceIso3166.size()){
            // 이미 등록된 통화는 제외
            for (String iso3166 : nowServiceIso3166) {
                iso3166List.removeIf(f-> f.equals(iso3166));
            }
            log.debug("addSystemCouponApplyCurrency() 시스템 쿠폰 추가될 적용 통화 업데이트 count={}, couponId={}, iso3166List={}",
                    iso3166List.size(), coupon.getId(), iso3166List);

            // iso3166List 가 empty 일 경우, insert 쿼리가 생성되지 않아 syntax 에러 나므로 방지
            if (!iso3166List.isEmpty()) {
                Long saved = couponV2Mapper.insertCouponApplyCurrency(coupon.getId(), iso3166List);
                // fromCountryCode = 004 일 때 (일본 DB에 적재)
                // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
                // 시스템 쿠폰은 통화를 직접 넣어줘야 하므로 가입자가 있을 시, trigger 로 업데이트 함
                if (saved > 0 && CountryCode.JP.getCode().equals(fromCountryCode)) {
                    globalQueueService.sendApplyCurrencySystemCoupon(coupon.getId(), iso3166List);
                }
            }
        }
    }

    /**
     * 가입 쿠폰 개선 V3
     **/
    public void issueWelcomeV3(Coupon coupon, String iso3166, String fromCurrency, Long userId, ZoneId zoneId, Locale locale)
            throws CashmallowException {
        log.debug("Issue welcome v3 for userId={}", userId);

        try{
            ApiResultVO apiResultVO = new ApiResultVO();

            // 가입 쿠폰 형식
            // - 홍콩: Welcome(HK)
            // - 일본: Welcome(JP)
            String fromCountryCode = CountryCode.fromIso3166(iso3166).getCode();

            // 시스템 쿠폰에 해당하는 통화 추가될 시, 업데이트 (서비스 종료할시 어차피 앱에서 표시 안되므로 삭제 X. 추가만 함)
            addSystemCouponApplyCurrency(fromCountryCode, coupon);

            List<Long> userIds = new ArrayList<>();
            userIds.add(userId);

            ZoneId zone = ZoneId.of(zoneId.getId());
            Instant instant = ZonedDateTime.now(zone).toInstant();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(zoneId);
            String localDateTime = dateTimeFormatter.format(instant);

            // 가입할 때 자동 생성되는 쿠폰이므로 createdId 는 -1
            CouponIssueCreateRequest couponRequest = CouponIssueCreateRequest.builder()
                    .fromCountryCode(fromCountryCode)
                    .couponId(coupon.getId())
                    .targetType(TargetType.SPECIFIC.getCode())
                    .sendType(SendType.DIRECT.getCode())
                    .issueDate(localDateTime)
                    .createdId(-1L)
                    .users(userIds)
                    .build();

            CouponUserInviteCodeRequest inviteCodeRequest
                    = CouponUserInviteCodeRequest.builder()
                    .userId(userId)
                    .abbreviation(SystemCouponType.thankYouMyFriend.getAbbreviation())
                    .iso3166(iso3166)
                    .build();

            // 초대코드 생성하여 insert
            couponUserInviteCodeService.getCouponUserInviteCodeV3(inviteCodeRequest);

            // Welcome 쿠폰 발급
            CouponIssue savedCouponIssue = couponIssueServiceV2.createCouponIssue(couponRequest);

            // fromCountryCode = 004 일 때 (일본 DB에 적재)
            // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
            if(savedCouponIssue != null && CountryCode.JP.getCode().equals(fromCountryCode)){
                globalQueueService.sendIssueSystemCoupon(savedCouponIssue, fromCountryCode, userIds, null);
            }

            apiResultVO.setSuccessInfo();
        } catch (Exception e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    // 초대한 유저 (inviterUserCode), 초대된 유저 (inviteeNewUser)
    @Transactional(rollbackFor = CashmallowException.class)
    public ApiResultVO issueThankYouMyFriendV3(String inviterUserCode, Coupon coupon, String fromCurrency, Long userId, Locale locale, Traveler traveler) throws CashmallowException {
        ApiResultVO thankYouMyFriendResultVO = new ApiResultVO();

        // country code 정보 조회
        CountryCode countryCode = CountryCode.fromCurrency(fromCurrency);
        String fromCountryCode = countryCode.getCode();
        // 초대된 유저의 초대 코드 조회하여 자기의 초대 코드인지 체크
        CouponUserInviteCode inviteeNewUser = couponUserInviteCodeService.getInviteCodeByUserId(userId);
        if (inviterUserCode.equals(inviteeNewUser.getInviteCode())) {
            thankYouMyFriendResultVO.setResult(Const.CODE_SUCCESS, Const.CANNOT_ADD_INVITE_COUPON, messageSource.getMessage("CANNOT_ADD_INVITE_COUPON", null, "It is not possible to register an invitation coupon using your own invitation code.", locale));
            return thankYouMyFriendResultVO;
        }

        // 동일 쿠폰 중복 등록 방지
        List<Long> duplicatedCouponUserId = couponUserService.getUserCouponLikeCouponCode(userId, null, SystemCouponType.thankYouMyFriend.getCode());
        if (!duplicatedCouponUserId.isEmpty()) {
            thankYouMyFriendResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("COUPON_ALREADY_REGISTERED", null, "Coupons cannot be registered repeatedly.", locale));
            return thankYouMyFriendResultVO;
        }

        // 인플루언서 쿠폰과 초대 쿠폰은 평생 1번 등록 가능하으므로 중복 등록여부 체크. 등록한 적 있으면 true 없으면 false
        List<Long> couponUserId = couponUserService.getUserCouponLikeCouponCode(userId, null, SystemCouponType.influencer.getAbbreviation());
        if (!couponUserId.isEmpty()) {
            thankYouMyFriendResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("FRIEND_COUPON_ALREADY_REGISTERED", null, "The invitation code can only register once.", locale));
            return thankYouMyFriendResultVO;
        }

        // 시스템 쿠폰에 해당하는 통화 추가될 시, 업데이트 (서비스 종료할시 어차피 앱에서 표시 안되므로 삭제 X. 추가만 함)
        addSystemCouponApplyCurrency(fromCountryCode, coupon);

        // 초대한 유저 (inviterUser) 조회
        CouponUserInviteCodeRequest inviterUserRequest = CouponUserInviteCodeRequest.builder()
                .inviteCode(inviterUserCode)
                .build();
        CouponUserInviteCode couponUserInviteCode= couponUserInviteCodeService.getUserIdByInviteCode(inviterUserRequest);

        // 올바르지 않은 친구초대코드 입력했을 시 예외처리
        if(couponUserInviteCode == null) {
            thankYouMyFriendResultVO.setResult(Const.CODE_SUCCESS, Const.NOT_FOUND_INVITE_CODE, messageSource.getMessage("NOT_FOUND_INVITE_CODE", null, "This coupon cannot be registered.", locale));
            return thankYouMyFriendResultVO;
        }

        // 초대된 유저 (inviteeNewUser) 초대 쿠폰 생성
        List<Long> users = new ArrayList<>();
        users.add(userId);
        CouponIssueCreateRequest request = CouponIssueCreateRequest.builder()
                .fromCountryCode(fromCountryCode)
                .couponId(coupon.getId())
                .targetType(TargetType.SPECIFIC.getCode())
                .sendType(SendType.DIRECT.getCode())
                .issueDate(DateUtil.fromLocalDateTime(DateUtil.toLocalDateTime(fromCountryCode)))
                .createdId(-1L)
                .users(users)
                .inviteUserId(couponUserInviteCode.getUserId())
                .build();

        CouponIssue savedCouponIssue = couponIssueServiceV2.createCouponIssue(request);

        // fromCountryCode = 004 일 때 (일본 DB에 적재)
        // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
        if(savedCouponIssue != null && CountryCode.JP.getCode().equals(fromCountryCode)){
            globalQueueService.sendIssueSystemCoupon(savedCouponIssue, fromCountryCode, users, couponUserInviteCode.getUserId());
        }

        thankYouMyFriendResultVO.setSuccessInfo();
        thankYouMyFriendResultVO.setMessage(messageSource.getMessage("COUPON_REGISTERED_SUCCESSFULLY", null, "Registered successfully.", locale));
        return thankYouMyFriendResultVO;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public ApiResultVO issueInfluencerV3(String couponCode, LocalDate currentDate, String fromCurrency, Long userId, Locale locale, Traveler traveler) throws CashmallowException {
        ApiResultVO influencerResultVO = new ApiResultVO();

        String identificationNumber = null;
        if (traveler != null) {
            identificationNumber = traveler.getIdentificationNumber();
        }

        CountryCode countryCode = CountryCode.fromCurrency(fromCurrency);
        // 쿠폰 조회
        Coupon coupon = couponServiceV2.getCouponByCouponCode(countryCode.getCode(), Const.N, Const.Y, couponCode);
        if (coupon == null) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("INVALID_COUPON", null, "Coupon is Invalid.", locale));
            return influencerResultVO;
        }

        // 기간 정함 쿠폰의 경우, 쿠폰 사용기한 체크
        if (ExpireType.DATE_RANGE.getCode().equals(coupon.getExpireType())
                && (currentDate.plusDays(1).isBefore(coupon.getCouponStartDate()) || currentDate.minusDays(1).isAfter(coupon.getCouponEndDate()))) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, Const.NOT_REGISTRATION_PERIOD, messageSource.getMessage("NOT_REGISTRATION_PERIOD", null, "It is not the registration period.", locale));
            return influencerResultVO;
        }

        // 회원가입 후 7일 이내 등록 가능(당일 포함)
        User user = userRepositoryService.getUserByUserId(userId);
        LocalDate localDate = DateUtil.toLocalDateTime(countryCode.getCode()).toLocalDate();
        LocalDate singUpDate = DateUtil.fromY_M_D_H_M_S(DateUtil.fromTimestampToY_M_D_H_M_S(countryCode.getCode(), user.getCreatedDate())).toLocalDate();
        // 해당 국가 기준 현재 날짜가 가입일로부터 6일이 지났을때 (등록기한 7일)
        if (singUpDate.plusDays(days).isBefore(localDate)) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, Const.NOT_REGISTRATION_PERIOD, messageSource.getMessage("NOT_REGISTRATION_PERIOD", null, "It is not the registration period.", locale));
            return influencerResultVO;
        }

        log.debug("localDate={}, 회원가입 후 7일 내={}, singUpDate={}", localDate, singUpDate.plusDays(days), singUpDate);

        // 동일 쿠폰 중복 등록 방지
        List<Long> duplicatedCouponUserId = couponUserService.getUserCouponLikeCouponCode(userId, null, couponCode);
        if (!duplicatedCouponUserId.isEmpty()) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("COUPON_ALREADY_REGISTERED", null, "Coupons cannot be registered repeatedly.", locale));
            return influencerResultVO;
        }

        // 인플루언서 쿠폰과 초대 쿠폰은 평생 1번 등록 가능하으므로 중복 등록여부 체크. 등록한 적 있으면 true 없으면 false
        List<Long> thankYouMyFriendCouponUserId = couponUserService.getUserCouponLikeCouponCode(userId, null, SystemCouponType.thankYouMyFriend.getCode());
        if (!thankYouMyFriendCouponUserId.isEmpty()
                || couponValidationService.hasRegisteredCoupon(userId, SystemCouponType.influencer.getCode(), identificationNumber)) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("USING_INFLUENCER_COUPON", null, "Could not  register kol coupons or friends invitation code repeatedly.", locale));
            return influencerResultVO;
        }

        // 인플루언서 쿠폰 평생 1번 등록 가능하므로 다른 인플루언서 쿠폰 등록한 이력 있으면 체크함
        List<Long> influenceCouponUserId = couponUserService.getUserCouponLikeCouponCode(userId, null, SystemCouponType.influencer.getAbbreviation());
        if (!influenceCouponUserId.isEmpty()
                || couponValidationService.hasRegisteredCoupon(userId, SystemCouponType.influencer.getAbbreviation(), identificationNumber)) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("FRIEND_COUPON_ALREADY_REGISTERED", null, "Could not  register kol coupons or friends invitation code repeatedly.", locale));
            return influencerResultVO;
        }

        // 인플루언서는 쿠폰을 가장 먼저 발급 받으므로 최초에 발급받은 userId 를 가져옴
        // 인플루언서에게 admin 에서 발급 후, 유저는 쿠폰 등록을 통해서 쿠폰을 발급 받으므로 admin 발급이 없을 경우에는 등록할 수 없음
        CouponUser firstIssuedCouponUser
                = couponUserMapper.getFirstIssuedUserCouponByCouponId(coupon.getId());
        if (firstIssuedCouponUser == null) {
            influencerResultVO.setResult(Const.CODE_SUCCESS, STATUS_SUCCESS, messageSource.getMessage("THIS_COUPON_CANNOT_BE_REGISTERED", null, "This coupon cannot be registered.", locale));
            return influencerResultVO;
        }

        List<Long> users = new ArrayList<>();
        users.add(userId);
        CouponIssueCreateRequest request = CouponIssueCreateRequest.builder()
                .fromCountryCode(countryCode.getCode())
                .couponId(coupon.getId())
                .targetType(TargetType.SPECIFIC.getCode())
                .sendType(SendType.DIRECT.getCode())
                .issueDate(DateUtil.fromLocalDateTime(DateUtil.toLocalDateTime(countryCode.getCode())))
                .createdId(coupon.getCreatedId()) // 인플루언서 쿠폰 생성한 운영자 id
                .users(users)
                .build();

        CouponIssue savedCouponIssue = couponIssueServiceV2.createCouponIssue(request);

        // fromCountryCode = 004 일 때 (일본 DB에 적재)
        // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
        if(savedCouponIssue != null && CountryCode.JP.getCode().equals(countryCode.getCode())){
            globalQueueService.sendIssueSystemCoupon(savedCouponIssue, countryCode.getCode(), users, firstIssuedCouponUser.getTargetUserId());
        }

        influencerResultVO.setSuccessInfo();
        influencerResultVO.setMessage(messageSource.getMessage("COUPON_REGISTERED_SUCCESSFULLY", null, "Registered successfully.", locale));
        return influencerResultVO;
    }

    // 초대한 유저 (inviterUser), 초대된 유저 (inviteeNewUser, couponUser)
    @Transactional(rollbackFor = CashmallowException.class)
    public void issueThankYouTooV3(Long couponUserId, Long userId, Coupon coupon) throws CashmallowException {
        // 초대코드로 가입하여 거래하는 신규 유저의 초대 쿠폰 조회 (검증용)
        CouponUser couponUser = couponMobileV2Mapper.getUserCouponById(couponUserId);
        if (couponUser == null) {
            throw new CashmallowException(INVALID_COUPON);
        }

        // 신규 유저를 초대한 유저 조회
        Long inviterUserId = couponUser.getInviteUserId();
        log.info("inviterUserId: {}", inviterUserId.toString());
        User inviteeUser = userRepositoryService.getUserByUserId(inviterUserId);

        // 시스템 쿠폰에 해당하는 통화 추가될 시, 업데이트 (서비스 종료할시 어차피 앱에서 표시 안되므로 삭제 X. 추가만 함)
        addSystemCouponApplyCurrency(coupon.getFromCountryCode(), coupon);

        // 초대한 유저의 id 바인딩
        List<Long> users = new ArrayList<>();
        users.add(inviterUserId);

        CouponIssueCreateRequest request = CouponIssueCreateRequest.builder()
                .fromCountryCode(inviteeUser.getCountry())
                .couponId(coupon.getId())
                .targetType(TargetType.SPECIFIC.getCode())
                .sendType(SendType.DIRECT.getCode())
                .issueDate(DateUtil.fromLocalDateTime(DateUtil.toLocalDateTime(inviteeUser.getCountry())))
                .users(users)
                .inviteUserId(userId)   // 초대완료 쿠폰 받는 유저는 초대한 유저가 inviteUser
                .createdId(-1L)         // 시스템 쿠폰이므로 createdId 는 -1
                .build();

        CouponIssue savedCouponIssue = couponIssueServiceV2.createCouponIssue(request);

        // fromCountryCode = 004 일 때 (일본 DB에 적재)
        // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
        if(savedCouponIssue != null && CountryCode.JP.getCode().equals(inviteeUser.getCountry())){
            globalQueueService.sendIssueSystemCoupon(savedCouponIssue, inviteeUser.getCountry(), users, userId);
        }

    }

    @Transactional(rollbackFor = CashmallowException.class)
    public ApiResultVO issueCouponForMobileV3(String couponCode, LocalDate currentDate, String iso3166, Long userId, Locale locale, Traveler traveler) throws CashmallowException {
        ApiResultVO eventResultVO = new ApiResultVO();

        CountryCode countryCode = CountryCode.fromIso3166(iso3166);
        Coupon coupon = couponServiceV2.getCouponByCouponCode(countryCode.getCode(), Const.N, Const.Y, couponCode);
        // 유저가 쿠폰 코드 입력을 통해서 추가 가능한 쿠폰인지 확인 필요
        if (coupon == null) {
            eventResultVO.setResult(Const.CODE_SUCCESS, Const.INVALID_COUPON
                    , messageSource.getMessage("INVALID_COUPON", null, "Coupon is Invalid.", locale));
            return eventResultVO;
        }

        // 기간 정함 쿠폰의 경우, 쿠폰 사용기한 체크
        if (ExpireType.DATE_RANGE.getCode().equals(coupon.getExpireType())
                && (currentDate.isBefore(coupon.getCouponStartDate()) || currentDate.isAfter(coupon.getCouponEndDate()))) {
            eventResultVO.setResult(Const.CODE_SUCCESS, Const.NOT_REGISTRATION_PERIOD, messageSource.getMessage("NOT_REGISTRATION_PERIOD", null, "It is not the registration period.", locale));
            return eventResultVO;
        }

        // 해당 쿠폰 코드로 유저의 쿠폰이 등록되어 있는 지 확인
        CouponUser couponUser = couponMobileV2Mapper.getUserCouponByUserIdAndCouponId(userId, coupon.getId());

        // 쿠폰이 이미 등록되어 있는 경우
        if (couponUser != null) {
            eventResultVO.setResult(Const.CODE_SUCCESS, Const.COUPON_ALREADY_REGISTERED, messageSource.getMessage("COUPON_ALREADY_REGISTERED", null, "You cannot register coupons multiple times.", locale));
            return eventResultVO;
        }

        List<Long> users = new ArrayList<>();
        users.add(userId);
        CouponIssueCreateRequest request = CouponIssueCreateRequest.builder()
                .fromCountryCode(countryCode.getCode())
                .couponId(coupon.getId())
                .targetType(TargetType.SPECIFIC.getCode())
                .sendType(SendType.DIRECT.getCode())
                .issueDate(DateUtil.fromLocalDateTime(DateUtil.toLocalDateTime(countryCode.getCode())))
                .createdId(coupon.getCreatedId()) // 이벤트 쿠폰 생성한 운영자 id
                .users(users)
                .build();

        CouponIssue savedCouponIssue = couponIssueServiceV2.createCouponIssue(request);

        // fromCountryCode = 004 일 때 (일본 DB에 적재)
        // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
        if(savedCouponIssue != null && CountryCode.JP.getCode().equals(countryCode.getCode())){
            globalQueueService.sendIssueSystemCoupon(savedCouponIssue, countryCode.getCode(), users, null);
        }

        eventResultVO.setSuccessInfo();
        eventResultVO.setMessage(messageSource.getMessage("COUPON_REGISTERED_SUCCESSFULLY", null, "Registered successfully.", locale));
        return eventResultVO;
    }

    @Override
    public CouponCalcResponse calcCouponV2(Country fromCountry, Country toCountry, BigDecimal fromMoney, BigDecimal feePerAmt, Long couponUserId, Long userId, ServiceType serviceType, boolean amountWithFeeFlag) throws CashmallowException {

        if (fromMoney.compareTo(BigDecimal.ZERO) < 0) {
            log.error("calcCouponV2(): fromMoney less than zero fromMoney={}", fromMoney);
            throw new CashmallowException(CODE_INVALID_PARAMS);
        }

        CouponCalcResponse couponCalcResponse = new CouponCalcResponse();

        // pinValue 에러 방지 위해 계산 검증 시, 미리 쿠폰 존재하는지 체크
        CouponUser couponUser = couponMobileV2Mapper.getUserCouponById(couponUserId);
        if (couponUser == null) {
            throw new CashmallowException(INVALID_COUPON);
        }

        // 1. 쿠폰 조회 시, 조회 조건
        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                userId,
                couponUserId,
                fromCountry.getCode(),
                serviceType.getCode(),
                "",
                AvailableStatus.AVAILABLE.name(),
                feePerAmt,
                null,
                null,
                Const.Y
        );

        List<CouponIssueUserResponse> couponMobileUsers;
        if (couponUserId != null && couponUserId > 0) {

            // 2. 유저가 사용가능한 쿠폰 조회
            couponMobileUsers = couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest)
                    .stream()
                    .map(coupon ->
                            CouponIssueUserResponse.ofMoney(
                                    coupon,
                                    feePerAmt,
                                    validationMinRequiredAmount(coupon, feePerAmt, fromMoney, amountWithFeeFlag),  // 최소결제 금액 Valid
                                    getApplyCurrencyList(coupon.getCouponId())     // coupon 에 해당하는 적용가능 통화 목록 조회
                            )
                    )
                    .collect(Collectors.toUnmodifiableList());

            if (!CollectionUtils.isEmpty(couponMobileUsers)) {
                // 특정 쿠폰 하나만 선택 했으므로 0번째 가져옴
                CouponIssueUserResponse couponIssueUserResponse = couponMobileUsers.get(0);

                return couponCalcMoneyV2(couponIssueUserResponse, fromMoney, feePerAmt, amountWithFeeFlag);
            }
        } else {
            throw new CashmallowException(INVALID_COUPON);
        }

        return couponCalcResponse;
    }

    private CouponCalcResponse couponCalcMoneyV2(CouponIssueUserResponse couponResponse, BigDecimal fromMoney, BigDecimal feePerAmt, boolean amountWithFeeFlag) throws CashmallowException {

        if (fromMoney.compareTo(BigDecimal.ZERO) < 0) {
            log.error("couponCalcMoneyV2(): fromMoney less than zero fromMoney={}", fromMoney);
            throw new CashmallowException(CODE_INVALID_PARAMS);
        }

        CouponCalcResponse couponCalcResponse = new CouponCalcResponse();

        log.debug("실제 환전(계산) 및 송금(계산) 쿠폰 할인 금액 조회 couponCalcMoneyV2(): CouponIssueUserResponse={}, fromMoney={}, feePerAmt={}, amountWithFeeFlag={}"
                , couponResponse.toString(), fromMoney, feePerAmt, amountWithFeeFlag);
        // null 이거나 0 의 경우 쿠폰 정보 계산
        // fromMoney 가 최소 필수 금액을 넘겼을 시 쿠폰 정보 계산
        if (couponResponse.getMinRequiredAmount() == null ||
                couponResponse.getMinRequiredAmount().compareTo(BigDecimal.ZERO) == 0 ||
                fromMoney.compareTo(couponResponse.getMinRequiredAmount()) >= 0) {

            BigDecimal totAmt;

            if (amountWithFeeFlag) {
                totAmt = fromMoney;
            } else {
                totAmt = fromMoney.add(feePerAmt);
            }

            if (DiscountType.FIXED_AMOUNT.equals(couponResponse.getDiscountType())) {

                couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());
                couponCalcResponse.setDiscountAmount(couponResponse.getDiscountValue());
                couponCalcResponse.setPaymentAmount(totAmt.subtract(couponResponse.getDiscountValue()));

                return couponCalcResponse;

            } else if (DiscountType.RATE_AMOUNT.equals(couponResponse.getDiscountType())) {

                couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());

                BigDecimal discountAmount = totAmt.multiply(couponResponse.getDiscountValue().divide(BigDecimal.valueOf(100)));
                discountAmount = discountAmount.setScale(0, RoundingMode.HALF_UP);

                // max discount 금액보다 discount 금액이 클때는 max discount 금액으로 셋팅
                if (couponResponse.getMaxDiscountAmount() != null &&
                        discountAmount.compareTo(couponResponse.getMaxDiscountAmount()) > 0) {
                    discountAmount = couponResponse.getMaxDiscountAmount();
                }

                couponCalcResponse.setDiscountAmount(discountAmount);
                couponCalcResponse.setPaymentAmount(totAmt.subtract(discountAmount));

                return couponCalcResponse;

            } else if (DiscountType.FEE_WAIVER.equals(couponResponse.getDiscountType())) {

                BigDecimal fee = feePerAmt == null ? BigDecimal.ZERO : feePerAmt;

                couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());
                couponCalcResponse.setDiscountAmount(fee);

                // 실제 환전 및 송급할 떄에는 수수료면제 할인쿠폰 사용시, 쿠폰 선택하면 할인금액 제외하고 total 금액 출력
                if(amountWithFeeFlag) {
                    couponCalcResponse.setPaymentAmount(totAmt.subtract(fee));
                    // 계산할 떄에는 수수료면제 할인쿠폰 사용시, 쿠폰 선택하면 할인금액 제외하지 않고 total 금액 출력
                } else {
                    couponCalcResponse.setPaymentAmount(fromMoney);
                }

                return couponCalcResponse;

            }
        }

        return couponCalcResponse;
    }

    private CouponCalcResponse couponCalcMoneyMobileV2(CouponMobileUser couponResponse, BigDecimal fromMoney, BigDecimal feePerAmt) {
        CouponCalcResponse couponCalcResponse = new CouponCalcResponse();

        BigDecimal totAmt = fromMoney.add(feePerAmt);

        if (DiscountType.FIXED_AMOUNT.getCode().equals(couponResponse.getCouponDiscountType())) {

            couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());
            couponCalcResponse.setDiscountAmount(couponResponse.getCouponDiscountValue());
            couponCalcResponse.setPaymentAmount(totAmt.subtract(couponResponse.getCouponDiscountValue()));

            log.debug("환전 및 송금 전 쿠폰 할인 금액 조회 discountType={}, fromMoney={}, feePerAmt={}, couponCalcResponse={}"
                    , couponResponse.getCouponDiscountType(), fromMoney, feePerAmt, couponCalcResponse);

            return couponCalcResponse;

        } else if (DiscountType.RATE_AMOUNT.getCode().equals(couponResponse.getCouponDiscountType())) {

            couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());

            BigDecimal discountAmount = fromMoney.multiply(couponResponse.getCouponDiscountValue().divide(BigDecimal.valueOf(100)));
            discountAmount = discountAmount.setScale(0, RoundingMode.HALF_UP);

            // max discount 금액보다 discount 금액이 클때는 max discount 금액으로 셋팅
            if (couponResponse.getMaxDiscountAmount() != null &&
                    discountAmount.compareTo(couponResponse.getMaxDiscountAmount()) > 0) {
                discountAmount = couponResponse.getMaxDiscountAmount();
            }

            couponCalcResponse.setDiscountAmount(discountAmount);
            couponCalcResponse.setPaymentAmount(totAmt.subtract(discountAmount));

            log.debug("환전 및 송금 전 쿠폰 할인 금액 조회 discountType={}, fromMoney={}, feePerAmt={}, couponCalcResponse={}"
                    , couponResponse.getCouponDiscountType(), fromMoney, feePerAmt, couponCalcResponse);

            return couponCalcResponse;

        } else if (DiscountType.FEE_WAIVER.getCode().equals(couponResponse.getCouponDiscountType())) {

            couponCalcResponse.setCouponUserId(couponResponse.getCouponUserId());
            couponCalcResponse.setDiscountAmount(feePerAmt);
            couponCalcResponse.setPaymentAmount(fromMoney);

            log.debug("환전 및 송금 전 쿠폰 할인 금액 조회 discountType={}, fromMoney={}, feePerAmt={}, couponCalcResponse={}"
                    , couponResponse.getCouponDiscountType(), fromMoney, feePerAmt, couponCalcResponse);

            return couponCalcResponse;

        }

        return couponCalcResponse;
    }

    // 최소결제금액에 따른 사용가능한 쿠폰 구분자 추가
    private boolean validationMinRequiredAmount(CouponMobileUser coupon, BigDecimal finalFee, BigDecimal fromMoney, boolean amountWithFeeFlag) {

        BigDecimal withoutFeeFromMoney;
        if(amountWithFeeFlag) {
            withoutFeeFromMoney = fromMoney.subtract(finalFee);
        } else {
            withoutFeeFromMoney = fromMoney;
        }

        log.debug("최소결제금액기준: minRequiredAmount={}, withoutFeeFromMoney={}", coupon.getMinRequiredAmount(), withoutFeeFromMoney);

        boolean availability = true;
        if (coupon.getMinRequiredAmount() != null &&
                coupon.getMinRequiredAmount().compareTo(BigDecimal.ZERO) > 0 &&
                fromMoney != null && fromMoney.compareTo(BigDecimal.ZERO) > 0 &&
                coupon.getMinRequiredAmount().compareTo(withoutFeeFromMoney) > 0) {
            availability = Const.FALSE;
        }
        return availability;
    }

    // 사용가능 통화 목록 조회
    // iso3166 코드를 통화로 바꾼 후, 중복 제거함
    private List<String> getApplyCurrencyList(Long couponId) {
        // 2-1. 쿠폰 ID 로 사용가능 통화 조회하기 위해 바인딩
        List<Long> couponIdList = new ArrayList<>();
        couponIdList.add(couponId);
        return couponIssueV2Mapper.applyCurrencyByCouponList(couponIdList).stream()
                .filter(applyCurrency -> applyCurrency.getCouponId().equals(couponId))
                .map(ApplyCurrency::getTargetIso3166)
                .map(iso3166 -> CountryCode.fromIso3166(iso3166).getCurrency())
                .collect(Collectors.toSet()).stream().collect(Collectors.toUnmodifiableList());
    }


    // 캐시멜로 관리 국가코드 fromCountryCode ex: 001(홍콩)
    @Override
    @Transactional
    public void useCouponUser(String fromCountryCode, Long userId, Long couponUserId, BigDecimal discountAmount, String serviceType) throws CashmallowException {
        // fromCountryCode 기준 LocalDateTime
        LocalDateTime couponUsedDate = DateUtil.toLocalDateTime(fromCountryCode);
        // Validation 체크
        couponValidationService.validCouponUser(fromCountryCode, userId, couponUserId, serviceType, AvailableStatus.AVAILABLE.name());
        // 사용 여부 Update
        CouponUseUpdateRequest couponUseRequest = new CouponUseUpdateRequest(
                couponUserId,
                discountAmount,
                AvailableStatus.USED.name(),
                couponUsedDate
        );

        int usedCoupon = couponMobileV2Mapper.useCouponUser(couponUseRequest);
        if(usedCoupon < 1) {
            throw new CashmallowException("쿠폰 사용 오류");
        }
    }

    @Override
    @Transactional
    public Long cancelCouponUserV2(Long couponUserId, String exchangeOrRemittanceStatus) throws CashmallowException {
        Long thankYouTooCouponUserId = null;

        CouponIssueUser usedCouponUser = couponUserService.getCouponUserByIdAndStatus(couponUserId, AvailableStatus.USED);
        if (usedCouponUser == null) {
            log.error("[couponUserId:{}] {}", couponUserId, INVALID_COUPON);
            throw new CashmallowException(INVALID_COUPON);
        }

        // 1. 쿠폰 원복 전, 만료 일자 및 기한 체크한 쿠폰인지 조회
        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                null,                                // 유저 ID
                couponUserId,                               // 유니크한 유저 쿠폰 ID
                "",                                         // 캐시멜로에서 관리하는 국가 코드  ex) 001: 홍콩
                "",                                         // 거래 유형(remittance-송금, exchange-환전, all-모두(송금, 환전))
                "",                                         // 발급 유형(direct-즉시, reservation-예약)
                AvailableStatus.USED.name(),                // 사용가능 여부(AVAILABLE-사용가능, RESERVATION-예약, EXPIRED-만료됨, USED-사용됨, REVOKED-회수됨)
                null,                                       // 수수료(할인 정책 중 수수료면제 feeWaiver 일때만 사용)
                null,                                       // 쿠폰 발급 ID
                null,                                       // 현재 로컬 날짜
                Const.Y                                     // 쿠폰 활성화여부
        );
        List<CouponMobileUser> couponListMobileUser = couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest);
        if(!couponListMobileUser.isEmpty()) {
            List<CouponIssueUserExpire> userList = couponListMobileUser.stream().map(CouponIssueUserExpire::toLocalDate).toList();
            CouponMobileUser couponMobileUser = couponListMobileUser.get(0);

            LocalDate now = DateUtil.toLocalDate(couponMobileUser.getFromCountryCode());
            // 2. 사용가능 쿠폰이면 원복(AVAILABLE)
            int canceledCouponUser = couponMobileV2Mapper.cancelCouponUser(couponUserId);
            if(canceledCouponUser < 1) {
                log.error("[couponUserId:{}] 쿠폰 원복 오류", couponUserId);
                throw new CashmallowException(COUPON_CANNOT_RESTORE);
            }
            // 3. 쿠폰 사용시작일(couponCalStartDate)이 오늘보다 미래이거나 쿠폰 만료일(couponCalEndDate)이 오늘보다 과거일 경우 사용불가
            if (couponMobileUser.getCouponCalStartDate().isAfter(now) || couponMobileUser.getCouponCalEndDate().isBefore(now)) {
                // 5. 유저 쿠폰 상태값 EXPIRED 로 업데이트 - 만료 일자 및 기한 지난 쿠폰이면 사용됨(USED)에서 그대로 만료됨(EXPIRED) 처리
                Long updated = couponIssueServiceV2.updateExpireCoupon(userList);
                if (updated < 1) {
                    log.error("[couponUserId:{}]  EXPIRED 업데이트에 실패했습니다.", couponUserId);
                    throw new CashmallowException(COUPON_CANNOT_UPDATE_EXPIRED);
                }
            }
            // 4. 초대 쿠폰의 경우, OP가 아닐 때 사용하고 환불해도 다시 취소하지 않고 회수함
            if (couponMobileUser.getCouponCode().startsWith(SystemCouponType.thankYouMyFriend.getCode())
                    && !"OP".equals(exchangeOrRemittanceStatus) ){ // 환전, 송금 둘다 해당하므로 enum 대신 string "OP" 사용함
                int inviteUpdated = updateCouponStatus(couponUserId, AvailableStatus.REVOKED);
                if (inviteUpdated < 1) {
                    log.error("[couponUserId:{}]  초대 쿠폰 or 인플루언서 쿠폰 REVOKED 업데이트에 실패했습니다.", couponUserId);
                    throw new CashmallowException(COUPON_CANNOT_UPDATE_REVOKED);
                }

                // 5. 초대된 유저가 거래 취소 시, 초대완료 쿠폰 회수
                // 초대완료 쿠폰을 받은 유저가 target_user_id, 초대쿠폰 사용한 유저가 invite_user_id
                Long targetUserId = couponMobileUser.getInviteUserId();
                Long inviteUserId = couponMobileUser.getUserId();
                // 마지막 등록된 쿠폰이 최근 것이므로 0번째 index 에서 가져옴
                List<Long> completeCouponUserId = couponUserService.getUserCouponLikeCouponCode(targetUserId, inviteUserId, SystemCouponType.thankYouToo.getCode());
                if (!completeCouponUserId.isEmpty()) {
                    thankYouTooCouponUserId = completeCouponUserId.get(0);
                    if (thankYouTooCouponUserId != null) {
                        int completeUpdated = updateCouponStatus(completeCouponUserId.get(0), AvailableStatus.REVOKED);
                        if (completeUpdated < 1) {
                            log.error(COUPON_CANNOT_UPDATE_REVOKED+": {}", completeCouponUserId);
                        }
                    }
                }

            } else if (couponMobileUser.getCouponCode().startsWith(SystemCouponType.influencer.getAbbreviation())
                    && !"OP".equals(exchangeOrRemittanceStatus) ){ // 환전, 송금 둘다 해당하므로 enum 대신 string "OP" 사용함

                // 6. 인플루언서 쿠폰의 경우 초대받은 유저의 쿠폰만 회수 처리
                int completeUpdated = updateCouponStatus(couponMobileUser.getCouponUserId(), AvailableStatus.REVOKED);
                if (completeUpdated < 1) {
                    log.error("[inviteCompleteCouponUser={}]  인플루언서 쿠폰 REVOKED 업데이트에 실패했습니다.", couponMobileUser.getCouponUserId());
                    throw new CashmallowException(COUPON_CANNOT_UPDATE_REVOKED);
                }

            }
        } else {
            log.error("[couponUserId:{}] {}", couponUserId, INVALID_COUPON);
            throw new CashmallowException(INVALID_COUPON);
        }
        log.debug("thankYouTooCouponUserId final:{}", thankYouTooCouponUserId);
        return thankYouTooCouponUserId;
    }

    public String getSystemCouponNameByLocale(String couponCode, String couponName, Locale locale) {
        if (locale.equals(Locale.CHINA) || locale.equals(Locale.CHINESE)) {
            locale = Locale.ENGLISH;
        }

        if (couponCode.contains(SystemCouponType.welcome.getCode())) {
            return messageSource.getMessage(WELCOME_COUPON_NAME, null, couponName, locale);
        } else if (couponCode.contains(SystemCouponType.birthday.getCode())) {
            return messageSource.getMessage(BIRTHDAY_COUPON_NAME, null, couponName, locale);
        } else if (couponCode.contains(SystemCouponType.thankYouMyFriend.getCode())) {
            return messageSource.getMessage(THANK_YOU_MY_FRIEND_COUPON_NAME, null, couponName, locale);
        } else if (couponCode.contains(SystemCouponType.thankYouToo.getCode())) {
            return messageSource.getMessage(THANK_YOU_TOO_COUPON_NAME, null, couponName, locale);
        }
        return couponName;
    }

    public String getSystemCouponDescriptionByLocale(String couponCode, String couponDescription, Locale locale) {
        if (locale.equals(Locale.CHINA) || locale.equals(Locale.CHINESE)) {
            locale = Locale.ENGLISH;
        }

        if (couponCode.contains(SystemCouponType.welcome.getCode())) {
            return messageSource.getMessage(WELCOME_COUPON_DESCRIPTION, null, couponDescription, locale);
        } else if (couponCode.contains(SystemCouponType.birthday.getCode())) {
            return messageSource.getMessage(BIRTHDAY_COUPON_DESCRIPTION, null, couponDescription, locale);
        } else if (couponCode.contains(SystemCouponType.thankYouMyFriend.getCode())) {
            return messageSource.getMessage(THANK_YOU_MY_FRIEND_COUPON_DESCRIPTION, null, couponDescription, locale);
        } else if (couponCode.contains(SystemCouponType.thankYouToo.getCode())) {
            return messageSource.getMessage(THANK_YOU_TOO_COUPON_DESCRIPTION, null, couponDescription, locale);
        }
        return couponDescription;
    }

}


