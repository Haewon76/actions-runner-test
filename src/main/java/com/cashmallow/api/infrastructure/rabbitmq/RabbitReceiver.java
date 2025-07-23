package com.cashmallow.api.infrastructure.rabbitmq;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.CurrencyService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.traveler.TravelerVerificationStatusRequest;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.api.interfaces.coupon.CouponIssueQueueService;
import com.cashmallow.api.interfaces.dbs.DbsService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.global.GlobalService;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.api.interfaces.statistics.MoneyTransferStatisticsService;
import com.cashmallow.api.interfaces.traveler.web.TravelerJpService;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.retry.annotation.Retryable;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Map;

import static com.cashmallow.api.config.RabbitConfig.*;

@Slf4j
public class RabbitReceiver extends RejectAndDontRequeueRecoverer {

    private static class MessageKey {
        static final String FUNCTION = "function";
        static final String DATA = "data";
    }

    @Value("${host.url}")
    private String hostUrl;

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private SevenBankServiceImpl sevenBankService;

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private InactiveUserServiceImpl inactiveUserService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private PaygateServiceImpl paygateService;

    @Autowired
    private RemittanceServiceImpl remittanceService;

    @Autowired
    private MoneyTransferStatisticsService moneyTransferStatisticsService;

    @Autowired
    private CouponIssueQueueService couponIssueQueueService;

    @Autowired
    private DbsService dbsService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;

    @Autowired
    private AuthMeService authMeService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private GlobalService globalService;

    @Autowired
    private TravelerMapper travelerMapper;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private TravelerJpService travelerJpService;

    /**
     * Batch 에 의해 요청된 환전 신청 취소 처리
     *
     * @param message
     */
    @RabbitListener(queues = "exchange-cancel-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenExchangeCancel(Map<String, String> message) throws CashmallowException {

        String method = "listenExchangeCancel()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("cancelExchangeByTimeout".equals(function)) {
            Long exchangeId = data.getLong("id");
            Long travelerId = data.getLong("traveler_id");

            exchangeService.cancelExchangeByBatch(exchangeId, travelerId);

        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. fucntion=" + function);
        }
    }

    @RabbitListener(queues = "remittance-cancel-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenRemittanceCancel(Map<String, String> message) throws CashmallowException {

        String method = "listenRemittanceCancel()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("cancelRemittanceByTimeout".equals(function)) {
            Long remittanceId = data.getLong("id");

            remittanceService.cancelRemittanceByBatch(remittanceId);
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. fucntion=" + function);
        }
    }

    /**
     * Batch 에 의해 요청된 인출 예약 취소 처리
     *
     * @param message
     */
    @RabbitListener(queues = "cashout-cancel-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenCashoutCancel(Map<String, String> message) throws CashmallowException {

        String method = "listenCashoutCancel()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("cancelCashoutByTimeout".equals(function)) {
            Long cashoutId = data.getLong("id");
            Long withdrawalPartnerId = data.getLong("withdrawal_partner_id");

            WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);

            CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);

            log.info("cancelCashoutByTimeout partner={}, cashoutId={}, amount={} ", withdrawalPartner.getShopName(), cashoutId, cashout.getTravelerCashOutAmt());

            switch (withdrawalPartner.getStorekeeperType()) {
                // QBC
                case M001 -> sevenBankService.cancelCashOutSevenBankByTimeout(cashout, CashOut.CoStatus.CC.name());

                // COATM, Socash
                case C, A, M002, P001 -> {
                    cashOutService.processCancelCashout(cashout.getId(), CashOut.CoStatus.CC);
                    User sUser = userRepositoryService.getUserByUserId(withdrawalPartner.getUserId());
                    notificationService.sendFcmNotificationMsgAsync(sUser, FcmEventCode.CO, FcmEventValue.CC, cashout.getId());
                }

                // SCB, BNI, RCBC ....
                default -> mallowlinkWithdrawalService.cancelByTimeout(cashoutId);
            }

            User user = userRepositoryService.getUserByUserId(cashout.getTravelerId());

            if (user != null && CountryCode.JP.getCode().equals(user.getCountry())) {
                // 인출 취소에서는 쿠폰 롤백을 할 수 없으므로 -1L(사용안함)로 지정함
                globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.CASH_OUT, cashoutId, CashOut.CoStatus.CC.name(), -1L);
            }

        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. fucntion=" + function);
        }

    }

    /**
     * Batch 에 의해 요청된 환율 수집 처리
     *
     * @param message
     */
    @RabbitListener(queues = "currency-collect-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenCurrencyCollect(Map<String, String> message) throws CashmallowException {

        String method = "listenCurrencyCollect()";
        // cmLogger.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("collectCurrency".equals(function)) {
            String sourceCurrency = data.getString("source");
            currencyService.collectCurrencyRate(sourceCurrency);
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. fucntion=" + function);
        }

    }

    /**
     * Batch 에 의해 요청된 오래된 fcm notification 삭제 처리
     *
     * @param message
     */
    @RabbitListener(queues = "notification-delete-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenNotificationDelete(Map<String, String> message) {

        String method = "listenNotificationDelete()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        if ("deleteOldFcmNotification".equals(function)) {
            notificationService.removeFcmNotification();
        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }

    }

    /**
     * 휴면계정 처리
     *
     * @param message
     */
    @RabbitListener(queues = "user-dormant-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenUserDormant(Map<String, String> message) throws CashmallowException {

        String method = "listenUserDormant()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("sendMail".equals(function)) {
            long userId = data.getLong("id");
            String email = data.getString("email");

            User user = userRepositoryService.getUserByUserId(userId);
            if (user == null || !user.getEmail().equals(email)) {
                throw new CashmallowException("Invalid userId");
            }

            notificationService.sendEmailToNotifyDormantUser(userId, email);

        } else if ("dormantUser".equals(function)) {
            long userId = data.getLong("id");

            User user = userRepositoryService.getUserByUserId(userId);
            if (user == null) {
                throw new CashmallowException("Invalid userId");
            }

            inactiveUserService.deactivateUser(userId, 0, InactiveUser.InactiveType.DOR);

        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }

    }

    /**
     * Message 처리 중 Exception 발생한 경우. 정해진 재시도 횟수 만큼 재시도 후 실행된다.
     * 이때 message queue 의 message 는 삭제된다.
     * 설정된 prefetch count(default:250) 에 도달하면 listener 에게 더이상 메시지를 주지 않아 장애가 발생하므로 처리 중 최종적으로 에러가 난 메시지는 queue 에서 삭제되도록 한다.
     */
    @Override
    public void recover(Message message, Throwable cause) {

        // log.error("recover(): RabbitMQ Listener Error. message={}, cause={}", new String(message.getBody()), cause);

        String msg = String.format("message=%s, URL_HOST=%s, RabbitMQ 에러 입니다.(cashmallow-api)", new String(message.getBody()), hostUrl);

        alarmService.i("에러 RabbitMQ", msg);

    }

    @RabbitListener(queues = "money-statistics-collect-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenMoneyStatisticsCollect(Map<String, String> message) throws CashmallowException {
        String method = "listenMoneyStatisticsCollect()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        JSONObject data = new JSONObject(message.get(MessageKey.DATA));

        if ("addMoneyTransferStatistics".equals(function)) {
            CountryCode fromCd = CountryCode.of(data.getString("fromCd"));
            ZonedDateTime date = LocalDate.parse(data.getString("date")).atStartOfDay(fromCd.getZoneId());

            moneyTransferStatisticsService.addMoneyTransferStatistics(fromCd, date.toLocalDate());
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. function=" + function);
        }
    }

    /**
     * 예약, 생일 등 시스템 쿠폰 로직 추가
     **/
    @RabbitListener(queues = "coupon-issue-system-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenCouponIssueSystem(Map<String, String> message) throws Exception {
        String method = "listenCouponIssueSystem()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        String data = message.get(MessageKey.DATA);

        // 예약 발급 쿠폰: data = issueId
        if ("issueReservedSystemCoupons".equals(function)) {
            log.info("issueReservedSystemCoupons data={}", data);
            couponIssueQueueService.couponIssueListByReservation(data);
        // 생일 쿠폰
        } else if ("issueBirthDaySystemCouponJP".equals(function)) {
            couponIssueQueueService.issueBirthDaySystemCoupon(CountryCode.JP.getCode());
        } else if ("issueBirthDaySystemCouponHK".equals(function)) {
            couponIssueQueueService.issueBirthDaySystemCoupon(CountryCode.HK.getCode());
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. function=" + function);
        }
    }

    @RabbitListener(queues = "coupon-update-expire-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenUpdateExpireCoupon(Map<String, String> message) throws CashmallowException {
        String method = "listenUpdateExpireCoupon()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);

        // 해당 국가 자정 기준으로 만료된 쿠폰 EXPIRED 로 상태값 변경
        if ("updateExpireCouponJP".equals(function)) {
            couponIssueQueueService.updateExpireCoupon("004");
        } else if("updateExpireCouponHK".equals(function)) {
            couponIssueQueueService.updateExpireCoupon("001");
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. function=" + function);
        }
    }

    @RabbitListener(queues = "coupon-push-expire-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenPushExpireCoupon(Map<String, String> message) throws CashmallowException {
        String method = "listenPushExpireCoupon()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);

        // 만료 2일 전 쿠폰 보유한 유저 대상 푸시 알림
        if ("pushExpireCouponJP".equals(function)) {
            couponIssueQueueService.pushExpireCoupon("004");
        } else if ("pushExpireCouponHK".equals(function)) {
            couponIssueQueueService.pushExpireCoupon("001");
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. function=" + function);
        }
    }

    ////////////////// ////////////////// ////////////////// //////////////////

    @RabbitListener(queues = "calculate-unpaid-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenCalculateUnpaidByJP(Map<String, String> message) throws CashmallowException {
        String method = "listenCalculateUnpaidByJP()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);

        if ("calculateUnpaidByJP".equals(function)) {
            globalService.calculateUnpaidListForGlobalJP();
        } else {
            log.error("{}: invalid function name. function={}", method, function);
            throw new CashmallowException("invalid function name. function=" + function);
        }
    }

    @RabbitListener(queues = "wallet-expire-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenExpireWallet(Map<String, String> message) throws CashmallowException {

        String method = "listenExpireWallet()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        String data = message.get(MessageKey.DATA);

        if ("expiredWallet".equals(function)) {
            Long walletId = Long.valueOf(data);
            try {
                walletRepositoryService.expireWallet(walletId);
            } catch (Exception e) {
                log.error("{} : {}", method, e.getMessage());
            }
        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }
    }

    @RabbitListener(queues = "refund-expire-wallet-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenStandbyRefundOfExpireWallet(Map<String, String> message) throws CashmallowException {

        String method = "listenStandbyRefundOfExpireWallet()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        String data = message.get(MessageKey.DATA);

        if ("standbyRefundOfExpiredWallet".equals(function)) {
            Long walletId = Long.valueOf(data);
            try {
                refundService.standbyRefundOfExpiredWallet(walletId);
            } catch (Exception e) {
                log.error("{} : {}", method, e.getMessage(), e);
            }
        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }
    }

    @RabbitListener(queues = "request-refund-standby-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenRequestRefundForStandbyRefund(Map<String, String> message) throws CashmallowException {

        String method = "listenRequestRefundForStandbyRefund()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        String data = message.get(MessageKey.DATA);

        if ("requestRefundForStandbyRefund".equals(function)) {
            Long refundId = Long.valueOf(data);
            try {
                refundService.requestRefundForStandbyRefund(refundId);
            } catch (Exception e) {
                log.error("{} : {}", method, e.getMessage());
            }
        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }
    }

    @RabbitListener(queues = "wallet-expire-before-7day-queue", priority = "${rabbitmq.listener.priority}")
    @Retryable(interceptor = "retryInterceptor", value = RemoteAccessException.class)
    public void listenWarningExpireBefore7DayWallet(Map<String, String> message) throws CashmallowException {

        String method = "listenWarningExpireBefore7DayWallet()";
        log.info("{}: RabbitMQ received message={}", method, message);

        String function = message.get(MessageKey.FUNCTION);
        String data = message.get(MessageKey.DATA);

        if ("expireBefore7DayWallet".equals(function)) {
            Long walletId = Long.valueOf(data);

            travelerService.warningExpireWalletBefore7Day(walletId);
        } else {
            log.error("{}: invalid function name. function={}", method, function);
        }
    }

    @RabbitListener(queues = DELAY_TEST_QUEUE, priority = "${rabbitmq.listener.priority}")
    public void handleMessage(String message) {
        log.info("Received message: {}", message);
    }

    @RabbitListener(queues = AUTHME_TIMEOUT_QUEUE, priority = "${rabbitmq.listener.priority}")
    public void listenAuthmeCheckReceiver(String authmeCustomerId) {
        String country = authmeCustomerId.substring(0, 2);
        log.info("Country: {}, Received message: {}", country, authmeCustomerId);

        authMeService.checkTimeoutAndUpdateStatus(authmeCustomerId);
    }

    @RabbitListener(queues = CERTIFICATION_TIMEOUT_QUEUE, priority = "${rabbitmq.listener.priority}")
    public void listenCertificationCheckReceiver(Traveler traveler) {
        // 고객이 신분증 인증 후 상태를 Y로 변경하지만 7일내(인증완료시점+7일),
        // 계좌인증을 “완료Y” 상태로 변경하지 못하면 신분증인증 상태값 “R”로 변경.
        if ("Y".equals(traveler.getCertificationOk()) && !"Y".equals(traveler.getAccountOk())) {
            // certification 'R' 로 변경
            traveler.setCertificationOk("R");
            traveler.setCertificationOkDate(null);

            String slackMessage = """
                        [%s] 인증 후 7일간 계좌인증을 진행하지 않아 'R'상태로 자동변경
                        사용자ID: %s
                    """.formatted(traveler.getCertificationType().name(),
                    traveler.getUserId());
            alarmService.aAlert("승인", slackMessage, userRepositoryService.getUserByUserId(traveler));

            travelerMapper.updateTraveler(traveler);

            travelerService.insertTravelerVerificationStatus(
                    new TravelerVerificationStatusRequest(
                            traveler.getId(),
                            Traveler.VerificationType.CERTIFICATION.name(),
                            traveler.getCertificationOk(),
                            "Your account authentication has expired as it was not completed within 7 days.",
                            traveler.getCertificationPhoto(),
                            MDC.get("userId")
                    )
            );
        }
    }

    @RabbitListener(queues = CERTIFICATION_STEP_TIMEOUT_QUEUE, priority = "${rabbitmq.listener.priority}")
    public void listenCertificationStepTimeOut(Long userId) {
        log.debug("listenCertificationStepTimeOut() : userId={}", userId);
        // JP 수동인증시 최초 신청후 30분안에 최종 단계까지 신청하지 않으면, 모두 inactivate
        try {
            travelerJpService.timeoutCertificationStep(userId);
        } catch (CashmallowException e) {
            log.warn(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

