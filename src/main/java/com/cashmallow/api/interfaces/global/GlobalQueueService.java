package com.cashmallow.api.interfaces.global;

import com.cashmallow.api.application.FileService;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.vo.ApplyCurrencyType;
import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.refund.JpRefundAccountInfo;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.traveler.GlobalTravelerCertificationStep;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRequestSender;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.interfaces.authme.dto.TravelerImage;
import com.cashmallow.api.interfaces.authme.dto.TravelerImageSender;
import com.cashmallow.api.interfaces.global.dto.*;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.cashmallow.api.config.RabbitConfig.*;
import static com.cashmallow.api.domain.shared.Const.Y;
import static com.cashmallow.api.domain.shared.Const.getAuthFilePath;

@Slf4j
@Service
@RequiredArgsConstructor
public class GlobalQueueService {
    private final RabbitTemplate rabbitTemplate;
    private final JsonUtil jsonUtil;
    private final FileService fileService;

    public static final String GLOBAL_JP_TOPIC = "global-jp-topic";

    public static final String GLOBAL_JP_TRAVELER_RESULT_ROUTING = "global-jp.result.traveler.routing";
    public static final String GLOBAL_JP_SEND_CERTIFICATIONS_ROUTING = "global-jp.send.certifications.routing";
    public static final String GLOBAL_JP_SEND_LOGIN_INFO_ROUTING = "global-jp.send.login.info.routing";
    public static final String GLOBAL_JP_SEND_TRAVELER_LEAVE_ROUTING = "global-jp.send.traveler.leave.routing";

    // 회원 정보 업데이트 시 호출
    public void sendTravelerResult(User user, TravelerRequestSender globalTraveler) {
        log.debug("sendTravelerResult(): message={}", globalTraveler.toString());
        if (user.getCountryCode().equals(CountryCode.JP)) {
            rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, GLOBAL_JP_TRAVELER_RESULT_ROUTING, jsonUtil.toJson(globalTraveler));
        }
    }

    // 회원 가입 시 호출
    public void sendUserRegister(User user) {
        if (user.getCountryCode().equals(CountryCode.JP)) {
            rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, GLOBAL_JP_TRAVELER_RESULT_ROUTING, jsonUtil.toJson(new TravelerRequestSender(user)));
        }
    }

    // certification 파일 전송
    public void sendTravelerCertification(User user, TravelerImage image) {
        if (user.getCountryCode().equals(CountryCode.JP)) {
            String base64Image = fileService.getImageBase64(getAuthFilePath(user.getId()), image.getFileName());
            rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, GLOBAL_JP_SEND_CERTIFICATIONS_ROUTING, jsonUtil.toJson(new TravelerImageSender(image, base64Image)));
        }
    }

    // 로그인시 기기관련 정보 전송
    public void sendLoginInfo(User user) {
        if (user.getCountryCode().equals(CountryCode.JP)) {
            rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, GLOBAL_JP_SEND_LOGIN_INFO_ROUTING, jsonUtil.toJson(user));
        }
    }

    // 탈퇴에 대한 정보 전달
    public void sendTravelerLeave(User user) {
        if (user.getCountryCode().equals(CountryCode.JP)) {
            rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, GLOBAL_JP_SEND_TRAVELER_LEAVE_ROUTING, jsonUtil.toJson(user));
        }
    }

    /**
     * 7일뒤 계좌인증 만료 체크
     * 7일 이후에 결과값 체크, 계좌인증 하지 않으면 계좌인증 만료로 'R' 처리
     *
     * @param traveler
     */
    public void certificationExpirationUpdateAfter7Days(Traveler traveler) {
        int oneHour = 60 * 60 * 1000;
        int checkingDays = oneHour * 24 * 7; // 7일 이후에 결과값 체크, 계좌인증 하지 않으면 계좌인증 만료로 'R' 처리
        rabbitTemplate.convertAndSend(CERTIFICATION_TIMEOUT_TOPIC,
                CERTIFICATION_TIMEOUT_ROUTING_KEY,
                traveler,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", checkingDays);
                    log.info("certificationExpirationUpdateAfter7Days: send message");
                    return message;
                });
    }

    /**
     * traveler 수동인증 신청중 TimeOut(30분)
     *
     * @param userId
     */
    public void timeoutCertificationStep(Long userId) {
        int delayTimes = 30 * 60 * 1000; // 30분 이후에 타임아웃
        log.debug("timeoutCertificationStep() : userid={}", userId);
        rabbitTemplate.convertAndSend(CERTIFICATION_TIMEOUT_TOPIC,
                CERTIFICATION_STEP_TIMEOUT_ROUTING_KEY,
                userId,
                message -> {
                    message.getMessageProperties().setHeader("x-delay", delayTimes);
                    log.debug("timeoutCertificationStep: send message");
                    return message;
                });
    }

    public void sendExchange(Exchange exchange, Long couponIssueSyncId, String bankName) {
        final String routingKey = "global-jp.request.exchange.routing";

        GlobalExchangeDto globalExchangeDto = new GlobalExchangeDto(exchange, CountryCode.of(exchange.getFromCd()), CountryCode.of(exchange.getToCd()), bankName, couponIssueSyncId);

        String exchangeJson = JsonStr.toJson(globalExchangeDto);

        log.info("sendExchange: {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendWithdrawal(CashOut cashOut) {

        final String routingKey = "global-jp.request.withdrawal.routing";

        GlobalWithdrawalDto globalWithdrawalDto = new GlobalWithdrawalDto(cashOut, CountryCode.of(cashOut.getCountry()));

        String exchangeJson = JsonStr.toJson(globalWithdrawalDto);

        log.info("sendWithdrawal: {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendRemittance(Remittance remittance, Long couponIssueSyncId, String bankName, String addressStateProvince, RemittanceTravelerSnapshot snapshot) {

        final String routingKey = "global-jp.request.remittance.routing";

        String addressStateProvinceEn = "";
        if (snapshot.getAddressStateProvince() != null) {
            addressStateProvinceEn = snapshot.getAddressStateProvinceEn();
        }

        String remitRelationship = null;
        if (remittance.getRemitRelationship() != null) {
            remitRelationship = remittance.getRemitRelationship().name();
        }

        GlobalRemittanceDto globalRemittanceDto = new GlobalRemittanceDto(remittance, remitRelationship
                , CountryCode.of(remittance.getFromCd()), CountryCode.of(remittance.getToCd()), bankName, addressStateProvince, addressStateProvinceEn, couponIssueSyncId);

        String exchangeJson = JsonStr.toJson(globalRemittanceDto);

        log.info("sendRemittance: {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void globalJpSystemCouponManage(Coupon coupon, Set<String> applyCurrencyList) {
        final String routingKey = "global-jp.system.coupon.manage.routing";

        CouponRequest dto = new CouponRequest();
        dto.setCouponCode(coupon.getCouponCode());
        dto.setIsActive(Y);
        dto.setCouponDiscountType(DiscountType.fromCode(coupon.getCouponDiscountType()));
        dto.setCouponDiscountValue(coupon.getCouponDiscountValue());
        dto.setCouponStartDate(coupon.getCouponStartDate());
        dto.setCouponEndDate(coupon.getCouponEndDate());
        dto.setExpirePeriodDays(Math.toIntExact(coupon.getExpirePeriodDays()));
        dto.setExpireType(ExpireType.fromString(coupon.getExpireType()));
        dto.setMaxDiscountAmount(coupon.getMaxDiscountAmount());
        dto.setMinRequiredAmount(coupon.getMinRequiredAmount());
        dto.setCouponName(coupon.getCouponName());
        dto.setServiceType(ServiceType.fromString(coupon.getServiceType()));
        dto.setFromCountryCode(coupon.getFromCountryCode());
        dto.setSyncId(coupon.getId());
        dto.setIsSystem(coupon.getIsSystem());
        dto.setApplyCurrencyList(applyCurrencyList);
        dto.setApplyCurrencyType(ApplyCurrencyType.all);
        dto.setCouponDescription(coupon.getCouponDescription());
        dto.setCreatedId(coupon.getCreatedId());
        dto.setUpdatedId(coupon.getCreatedId());

        String couponJson = JsonStr.toJson(dto);

        log.info("JP globalJpSystemCouponManage: {}", couponJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, couponJson);
    }

    public void sendIssueSystemCoupon(CouponIssue couponIssue, String fromCountryCode, List<Long> userIds, Long inviteUserId) {
        final String routingKey = "global-jp.system.coupon.routing";

        GlobalSystemCouponDto dto = new GlobalSystemCouponDto(couponIssue, fromCountryCode, userIds, inviteUserId);

        String issueSystemCouponJson = JsonStr.toJson(dto);

        log.info("JP sendIssueSystemCoupon: {}", issueSystemCouponJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, issueSystemCouponJson);
    }

    public void sendApplyCurrencySystemCoupon(Long couponId, List<String> countrySetIso3166) {
        final String routingKey = "global-jp.system.coupon.apply.currency.routing";

        GlobalSystemCouponApplyCurrencyDto dto = new GlobalSystemCouponApplyCurrencyDto(couponId, countrySetIso3166);

        String applyCurrencySystemCouponJson = JsonStr.toJson(dto);

        log.info("JP sendApplyCurrencySystemCoupon: {}", applyCurrencySystemCouponJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, applyCurrencySystemCouponJson);
    }


    public void setUpdateStatusUserCoupon(List<UpdateStatusUserCoupon> userCouponList, String availableStatus) {
        final String routingKey = "global-jp.coupon.update.status.routing";

        GlobalUpdateStatusUserCouponDto dto = new GlobalUpdateStatusUserCouponDto(availableStatus, userCouponList);

        String updateStatusUserCoupon = JsonStr.toJson(dto);

        log.info("JP setUpdateStatusUserCoupon: {}", updateStatusUserCoupon);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, updateStatusUserCoupon);
    }

    public void failRemittance(Long remittanceId) {

        final String routingKey = "global-jp.fail.remittance.routing";

        Map<String, Long> message = new HashMap<>();
        message.put("remittanceId", remittanceId);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, message);
    }

    public void reRegisterRemittance(Remittance remittance) {

        final String routingKey = "global-jp.re-register.remittance.routing";

        GlobalReRegisterRemittanceDto globalReRegisterRemittanceDto = new GlobalReRegisterRemittanceDto(remittance);

        String exchangeJson = JsonStr.toJson(globalReRegisterRemittanceDto);

        log.info("sendRemittance: {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendRefund(NewRefund newRefund, JpRefundAccountInfo jpRefundAccountInfo) {

        final String routingKey = "global-jp.request.refund.routing";
        String method = "sendRefund()";

        TransactionRecord.RelatedTxnType originalTransactionType = null;
        Long originalCmTransactionId = 0L;
        log.info("{} : refundId={}, exchangeId={}, remitId={}", method, newRefund.getId(), newRefund.getExchangeId(), newRefund.getRemitId());
        if (ObjectUtils.isNotEmpty(newRefund.getExchangeId())) {
            originalTransactionType = TransactionRecord.RelatedTxnType.EXCHANGE;
            originalCmTransactionId = newRefund.getExchangeId();
        } else if (ObjectUtils.isNotEmpty(newRefund.getRemitId())) {
            originalTransactionType = TransactionRecord.RelatedTxnType.REMITTANCE;
            originalCmTransactionId = newRefund.getRemitId();
        }

        GlobalRefundDto globalRefundDto = new GlobalRefundDto(newRefund, CountryCode.of(newRefund.getFromCd()), CountryCode.of(newRefund.getToCd()),
                jpRefundAccountInfo, originalTransactionType, originalCmTransactionId);

        String exchangeJson = JsonStr.toJson(globalRefundDto);
        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void reRegisterRefundAccount(Long newRefundId, JpRefundAccountInfo jpRefundAccountInfo) {

        final String routingKey = "global-jp.re-register.refund.account.routing";

        GlobalRefundAccountDto globalRefundDto = new GlobalRefundAccountDto(newRefundId, jpRefundAccountInfo);

        String exchangeJson = JsonStr.toJson(globalRefundDto);

        log.info("reRegisterRefundAccount() : {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendTransactionCancel(TransactionRecord.RelatedTxnType txnType, Long transactionId, String cancelStatus, Long couponIssueSyncId) {

        final String routingKey = "global-jp.cancel.transaction.routing";

        GlobalTransactionCancelDto cancelDto = new GlobalTransactionCancelDto(txnType, transactionId, cancelStatus, couponIssueSyncId);

        String exchangeJson = JsonStr.toJson(cancelDto);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendRemittanceCompleted(Long remittanceId) {

        final String routingKey = "global-jp.complete.remittance.routing";

        Map<String, Long> message = new HashMap<>();
        message.put("remittanceId", remittanceId);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, message);
    }

    public void sendWithdrawalCompleted(Long withdrawalId) {

        final String routingKey = "global-jp.complete.withdrawal.routing";

        Map<String, Long> message = new HashMap<>();
        message.put("withdrawalId", withdrawalId);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, message);
    }

    public void sendUnpaidTransactionList(List<GlobalUnpaidTransactionDto> unpaidTransactionDtoList) {

        final String routingKey = "global-jp.request.unpaid.routing";

        String exchangeJson = JsonStr.toJson(unpaidTransactionDtoList);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendCouponIssueReservationCompletedAt(Long id, LocalDateTime completedAt) {

        final String routingKey = "global.jp.complete.coupon.issue.reservation.routing";

        Map<String, Object> message = new HashMap<>();
        message.put("id", id);
        message.put("completedAt", completedAt);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, message);
    }

    public void sendMallowlinkTransactionId(TransactionRecord.RelatedTxnType txnType, Long transactionId, String mallowlinkTransactionId) {
        final String routingKey = "global-jp.ml.transaction.routing";

        GlobalMallowlinkTransactionDto mlTransactionDto = new GlobalMallowlinkTransactionDto(txnType, transactionId, mallowlinkTransactionId);

        String exchangeJson = JsonStr.toJson(mlTransactionDto);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

    public void sendTravelerCertificationStep(GlobalTravelerCertificationStep globalTravelerCertificationStep) {
        final String routingKey = "global-jp.request.traveler.certification-step.routing";

        GlobalTravelerCertificationStepDto globalRefundDto = new GlobalTravelerCertificationStepDto(globalTravelerCertificationStep);

        String exchangeJson = JsonStr.toJson(globalRefundDto);

        log.info("sendTravelerCertificationStep() : {}", exchangeJson);

        rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, exchangeJson);
    }

}
