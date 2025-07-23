package com.cashmallow.api.interfaces.global;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.CustomerCenterServiceImpl;
import com.cashmallow.api.application.impl.InactiveUserServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.application.impl.UserServiceImpl;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.CountryFee;
import com.cashmallow.api.domain.model.country.CurrencyLimit;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import com.cashmallow.api.domain.model.customercenter.NoticeContent;
import com.cashmallow.api.domain.model.edd.UserEdd;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.*;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.coupon.CouponIssueServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponJobPlanService;
import com.cashmallow.api.interfaces.coupon.CouponServiceV2;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponApplyCurrency;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponIssueCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUpdateRequest;
import com.cashmallow.api.interfaces.edd.UserEddService;
import com.cashmallow.api.interfaces.global.dto.*;
import com.cashmallow.api.interfaces.traveler.web.TravelerJpService;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalService {
    private final CustomerCenterServiceImpl customerCenterService;
    private final CountryService countryService;
    private final UserEddService userEddService;
    private final TravelerServiceImpl travelerService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final UserRepositoryService userRepositoryService;
    private final CouponServiceV2 couponService;
    private final InactiveUserServiceImpl inactiveUserService;
    private final UserServiceImpl userService;
    private final AddressEnglishServiceImpl addressEnglishService;
    private final WalletRepositoryService walletRepositoryService;
    private final RemittanceRepositoryService remittanceRepositoryService;
    private final GlobalQueueService globalQueueService;
    private final SecurityService securityService;
    private final MessageSource messageSource;
    private final CouponIssueServiceV2 couponIssueService;
    private final CouponJobPlanService couponJobPlanService;
    private final TravelerJpService travelerJpService;

    @Value("${short.url}")
    private String linkUrl;

    public Long addNotice(NoticeRequest request) {
        Long syncId = request.id();
        NoticeContent noticeContent = new NoticeContent();
        noticeContent.setBeginDate(String.valueOf(request.beginDate()));
        noticeContent.setEndDate(String.valueOf(request.endDate()));
        noticeContent.setIsPopup(request.popup());
        // noticeContent.setModifiedDate(Timestamp.valueOf(request.updatedAt()));
        noticeContent.setModifier(-1L);
        for (NoticeRequest.NoticeContentDto noticeContentDto : request.noticeContents()) {
            noticeContent.setId(syncId);
            noticeContent.setLanguageType(noticeContentDto.getLanguageType());
            noticeContent.setTitle(noticeContentDto.getTitle());
            noticeContent.setContent(noticeContentDto.getContent());
            try {
                NoticeContent noticeContent1 = customerCenterService.addNoticeContent(noticeContent);
                syncId = noticeContent1.getId();
                log.info("Notice content added successfully : {}", syncId);
            } catch (Exception e) {
                log.error("Failed to add notice content", e);
            }
        }
        return syncId;
    }

    public Long syncCountryFee(CountryFeeRequest request) {
        Long managerId = -1L;
        String ip = "0.0.0.0";
        CountryFee countryFee = new CountryFee();
        countryFee.setId(request.getSyncId());
        countryFee.setFromCd(request.getFromCd());
        countryFee.setToCd(request.getToCd());
        countryFee.setFee(request.getFee());
        countryFee.setMin(request.getMin());
        countryFee.setMax(request.getMax());
        countryFee.setSort(request.getSort().intValue());
        countryFee.setUseYn(request.isUseYn() ? "Y" : "N");

        if (request.getSyncId() == null) {
            countryService.registerCountryFee(countryFee, managerId, ip);
        } else {
            countryService.updateCountryFee(countryFee, managerId, ip);
        }
        return countryFee.getId();
    }

    public Long syncExchangeFee(ExchangeConfigRequest request) {
        Long managerId = -1L;
        String ip = "0.0.0.0";

        ExchangeConfig exchangeConfig = new ExchangeConfig();
        exchangeConfig.setId(request.getSyncId());
        exchangeConfig.setFromCd(request.getFromCd());
        exchangeConfig.setToCd(request.getToCd());
        exchangeConfig.setFeeRateExchange(request.getFeeRateExchange());
        exchangeConfig.setMinFee(request.getMinFee());
        exchangeConfig.setCanExchange(request.isCanExchange() ? "Y" : "N");
        exchangeConfig.setEnabledExchange(request.isEnabledExchange() ? "Y" : "N");
        exchangeConfig.setExchangeNotice(request.getExchangeNotice());
        exchangeConfig.setRefundFeePer(request.getRefundFeePer());
        exchangeConfig.setFeePerExchange(request.getFeePerExchange());
        exchangeConfig.setCanRemittance(request.isCanRemittance() ? "Y" : "N");
        exchangeConfig.setEnabledRemittance(request.isEnabledRemittance() ? "Y" : "N");
        exchangeConfig.setFeePerRemittance(request.getFeePerRemittance());
        exchangeConfig.setFeeRateRemittance(request.getFeeRateRemittance());
        exchangeConfig.setRemittanceNotice(request.getRemittanceNotice());
        exchangeConfig.setCreator(managerId);

        if (request.getSyncId() == null) {
            countryService.insertExchangeConfig(exchangeConfig, managerId, ip);
        } else {
            countryService.updateExchangeConfig(exchangeConfig, managerId, ip);
        }
        return exchangeConfig.getId();
    }

    /**
     * start of Traveler 관련 엔드포인트 처리
     */
    public String getTravelerResetPasswordUrl(Long travelerId) throws CashmallowException {
        User user = userRepositoryService.getUserByUserId(travelerId);
        return userService.passwordResetAndSendEmailForAdmin(user.getEmail());
    }

    // update traveler
    @Transactional
    public TravelerUpdateResponseDto travelerUpdate(TravelerUpdateDto request) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(request.travelerId());
        if (traveler == null) {
            throw new CashmallowException("travelerId=" + request.travelerId() + "에 해당하는 사용자가 없습니다.");
        }

        // 일본어 주소 영문 주소 변환
        List<GoogleAddressResultResponse> searchResultForGlobal = addressEnglishService.getSearchResultForGlobal(request.addressFull());
        if (searchResultForGlobal.isEmpty()) {
            throw new CashmallowException("영문 주소 변환 실패. address=" + request.addressFull());
        }

        GoogleAddressResultResponse address = searchResultForGlobal.get(0);
        traveler.updateGlobalTraveler(request, address, securityService);
        travelerRepositoryService.updateTraveler(traveler);

        // user update
        User user = userRepositoryService.getUserByTravelerId(request.travelerId());
        user.setPhoneCountry(Country3.ofCallingCode(request.callingCode()).getAlpha3());
        user.setPhoneNumber(request.phoneNumber());
        userRepositoryService.updateUser(user);

        return new TravelerUpdateResponseDto(request.addressFull(), address);
    }

    // update traveler ekyc
    @Transactional
    public void travelerEkycUpdate(TravelerEkycUpdateDto request) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(request.travelerId());
        if (traveler == null) {
            throw new CashmallowException("travelerId=" + request.travelerId() + "에 해당하는 사용자가 없습니다.");
        }

        traveler.updateGlobalTravelerEkyc(request, securityService);
        travelerRepositoryService.updateTraveler(traveler);

        // user update
        User user = userRepositoryService.getUserByTravelerId(request.travelerId());
        userRepositoryService.updateUser(user);
    }


    public void travelerLeave(Long travelerId) throws CashmallowException {
        inactiveUserService.deactivateUser(travelerId, travelerId, InactiveUser.InactiveType.DEL);
    }

    public void cancelCertificationTraveler(Long travelerId, String managerName) throws CashmallowException {
        final User user = userRepositoryService.getUserByUserId(travelerId);
        final String message = messageSource.getMessage("FCM_AU_AI_1", null, user.getCountryLocale());
        travelerService.verifyIdentityByAdmin(travelerId, managerName, "N", message, false);
    }

    public void travelerCertificationApprove(Long travelerId, GlobalTravelerCertificationRequest request) throws CashmallowException {
        try {
            final User user = userRepositoryService.getUserByUserId(travelerId);
            final String message = messageSource.getMessage("FCM_AU_AI_0", null, user.getCountryLocale());
            travelerService.verifyIdentityByAdmin(travelerId, request.managerName(), "Y", message, false);
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
            travelerService.verifyBankAccountByAdmin(traveler, "Y", null);
        } catch (CashmallowException e) {
            throw new CashmallowException(e.getMessage());
        }
    }

    public void travelerCertificationReject(Long travelerId, GlobalTravelerCertificationRequest request) throws CashmallowException {
        try {
            travelerService.verifyIdentityByAdmin(travelerId, request.managerName(), "R", request.rejectReason(), request.needAccount());
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
            travelerService.verifyBankAccountByAdmin(traveler, "N", request.rejectReason());
            if (ObjectUtils.isNotEmpty(request.travelerCertificationStepId())) {
                travelerJpService.deactivateGlobalTravelerCertificationStep(request.travelerCertificationStepId());
            }
        } catch (CashmallowException e) {
            throw new CashmallowException(e.getMessage());
        }
    }

    public Long syncAddTravelerEdd(Long travelerId) throws CashmallowException {
        Long managerId = -1L;
        String ip = "0.0.0.0";
        Calendar cal = Calendar.getInstance();
        Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());
        UserEdd userEdd = UserEdd.builder()
                .userId(travelerId)
                .limited(Const.USER_EDD_LIMITED_Y)
                .amount(BigDecimal.ZERO)
                .count(0)
                .creatorId(managerId)
                .createdAt(toDayTimestamp)
                .updatedAt(toDayTimestamp)
                .initIp(ip)
                .build();

        userEddService.registerUserEdd(userEdd, managerId, ip);
        return userEdd.getId();
    }

    // end of Traveler 관련 엔드포인트 처리

    public Long syncCoupon(CouponCreateRequest request) throws CashmallowException {
        return couponService.createCoupon(request).getId();
    }

    public Long syncCouponIssue(CouponIssueCreateRequest request) throws CashmallowException {
        CouponIssue couponIssue = couponIssueService.createCouponIssue(request);
        Long couponIssueId = couponIssue.getId();
        if (couponIssueId != null && SendType.RESERVATION.getCode().equals(couponIssue.getSendType())) {
            // 예약 발급일 때 Job Plan 생성
            String cronExpress = couponJobPlanService.getCronExpression(couponIssue.getIssueDate());
            // Job Plan 등록: 쿠폰 발급 저장 트랜잭션이 끝난 후에 실행되어야 하므로 여기서 실행
            couponJobPlanService.insertJobPlan(request.fromCountryCode(), couponIssue.getJobKey(), cronExpress);
        }
        return couponIssueId;
    }

    public void updateCouponIssuePossible(CouponUpdateRequest request) throws CashmallowException {
        couponService.updateCouponActive(CountryCode.JP.getCode(), request.getUpdateList());
    }

    public List<GlobalTravelerWalletBalance> getTravelerBalance(Long travelerId) {
        return travelerService.getTravelerBalance(travelerId);
    }

    /**
     * 지불되지 않은 금액들 집계
     * 지불되지 않은 조건 - 환전 - 환전 완료 후 인출(환불)되지 않음.(wallet이 존재하면서 금액(e_money, c_money, r_money)이 0보다 클때)
     * 지불되지 않은 조건 - 송금 - 매핑이 완료 됐으면서, 송금이 완료되지 않거나, 환불이 완료되지 않았을 경우(DP, RR, RC, RP)
     */
    public void calculateUnpaidListForGlobalJP() {
        List<TravelerWallet> unpaidExchangeList = walletRepositoryService.getUnpaidListForGlobal(CountryCode.JP.getCode());

        List<Remittance> unpaidRemittanceList = remittanceRepositoryService.getUnpaidListForGlobal(CountryCode.JP.getCode());

        List<GlobalUnpaidTransactionDto> unpaidTransactionDtoList = new ArrayList<>();

        unpaidTransactionDtoList.addAll(unpaidExchangeList.stream().map(wallet
                -> new GlobalUnpaidTransactionDto(TransactionRecord.RelatedTxnType.EXCHANGE, wallet.getExchangeId())).toList());
        unpaidTransactionDtoList.addAll(unpaidRemittanceList.stream().map(remit
                -> new GlobalUnpaidTransactionDto(TransactionRecord.RelatedTxnType.REMITTANCE, remit.getId())).toList());

        log.info(unpaidTransactionDtoList.toString());

        globalQueueService.sendUnpaidTransactionList(unpaidTransactionDtoList);
    }

    public Long saveCurrencyLimit(CurrencyLimit currencyLimitRequest) {
        return countryService.saveCurrencyLimit(currencyLimitRequest);
    }

    public void deleteCoupons(List<Long> couponIds) throws CashmallowException {
        couponService.deleteCoupon(couponIds);
    }

    @Transactional
    public boolean couponCompensatingTransaction(Long couponId) throws CashmallowException {
        List<Long> couponIds = new ArrayList<>();
        couponIds.add(couponId);

        int deleted = couponService.deleteCoupon(couponIds);
        if (deleted < 1) {
            log.error("JP 신규 시스템 쿠폰 생성, 보상 트랜잭션 실패");
        } else {
            log.info("JP 신규 시스템 쿠폰 생성, 보상 트랜잭션 성공");
            return true;
        }
        return false;
    }

    @Transactional
    public boolean couponCompensatingTransactionIssue(Long issueId) {
        couponIssueService.getCouponIssuedById(issueId);

        int deletedIssuedCoupon = couponIssueService.deleteCouponIssuedById(issueId);
        log.info("deletedIssuedCoupon: {}", deletedIssuedCoupon);

        int deletedIssuedCouponUser = couponIssueService.deleteCouponIssuedUserByCouponIssueId(issueId);
        log.info("deletedIssuedCouponUser: {}", deletedIssuedCouponUser);

        if( deletedIssuedCoupon < 1 || deletedIssuedCouponUser < 1 ) {
            log.error("JP 시스템 쿠폰 발급 실패, 보상 트랜잭션 실패");
        } else {
            log.info("JP 시스템 쿠폰 발급 실패, 보상 트랜잭션 성공");
            return true;
        }
        return false;
    }

    @Transactional
    public boolean couponCompensatingTransactionApplyCurrency(CouponApplyCurrency couponApplyCurrency) {
        int deletedApplyCurrency = couponIssueService.deleteApplyCurrencyByCouponId(couponApplyCurrency.getCouponId(), couponApplyCurrency.getApplyCurrencyList());
        log.info("deletedApplyCurrency: {}", deletedApplyCurrency);

        if( deletedApplyCurrency < 1 ) {
            log.error("JP 시스템 쿠폰 통화 업데이트 실패, 보상 트랜잭션 실패");
        } else {
            log.info("JP 시스템 쿠폰 통화 업데이트 실패, 보상 트랜잭션 성공");
            return true;
        }
        return false;
    }

    @Transactional
    public boolean couponCompensatingTransactionUpdateStatus(List<UpdateStatusUserCoupon> userCouponList, String availableStatus) {
        int updatedStatus = couponIssueService.updateStatusByCouponIssueUserSyncIds(userCouponList, availableStatus);
        log.info("updatedStatus: {}", updatedStatus);

        if( updatedStatus < 1 ) {
            log.error("JP 시스템 쿠폰 사용여부 상태 업데이트(EXPIRED, AVAILABLE) 실패, 보상 트랜잭션 실패");
        } else {
            log.info("JP 시스템 쿠폰 사용여부 상태 업데이트(EXPIRED, AVAILABLE) 실패, 보상 트랜잭션 성공");
            return true;
        }
        return false;
    }
}
