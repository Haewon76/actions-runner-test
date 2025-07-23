package com.cashmallow.api.interfaces.mallowlink.withdrawal;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.CashOutServiceImpl;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashOutAjOtp;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.agency.MallowlinkAgencyClient;
import com.cashmallow.api.interfaces.mallowlink.agency.dto.AgencyResponse;
import com.cashmallow.api.interfaces.mallowlink.agency.dto.MallowlinkAgencyRequest;
import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkNotFoundEnduserException;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookRefundRequest;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookResultRequest;
import com.cashmallow.api.interfaces.mallowlink.enduser.MallowlinkEnduserServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.*;
import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import com.cashmallow.api.interfaces.scb.service.RedisPubService;
import com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyService;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType.MAINTENANCE_TIME;
import static com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType.TRANSACTION_USER_EXCEED_AMOUNT;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkWithdrawalServiceImpl {

    private final MallowlinkServiceImpl mallowlinkService;
    private final MallowlinkEnduserServiceImpl enduserService;
    private final MallowlinkWithdrawalClient withdrawalClient;
    private final MallowlinkAgencyClient mallowlinkAgencyClient;

    private final UserRepositoryService userRepositoryService;
    private final ExchangeRepositoryService exchangeRepositoryService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final CashOutServiceImpl cashOutService;
    private final CashoutRepositoryService cashoutRepositoryService;
    private final PartnerServiceImpl partnerService;
    private final AlarmService alarmService;

    private final RedisPubService pubService;

    private final JsonUtil jsonUtil;
    private final SecurityService securityService;
    private final PartnerCancelService partnerCancelService;
    private final CashoutAgencyService cashoutAgencyService;
    private final NotificationService notificationService;

    public List<WithdrawalPartnerCashpoint> getAtmList(Long withdrawalPartnerId, Double latitude, Double longitude) {
        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
        User userByUserId = userRepositoryService.getUserByUserId(withdrawalPartner.getUserId());
        CountryCode countryCode = CountryCode.of(userByUserId.getCountry());

        List<AgencyResponse> agencies = mallowlinkAgencyClient.agencies(new MallowlinkAgencyRequest(
                countryCode,
                latitude,
                longitude
        )).getData();

        log.debug("agencies:{}", agencies);

        List<WithdrawalPartnerCashpoint> list = new ArrayList<>();
        for (AgencyResponse agency : agencies) {
            WithdrawalPartnerCashpoint withdrawalPartnerCashpoint = WithdrawalPartnerCashpoint.of(agency, withdrawalPartnerId);
            list.add(withdrawalPartnerCashpoint);
        }
        return list;
    }


    /**
     * 인출 신청
     *
     * @param userId
     * @param withdrawalPartner
     * @param walletId
     * @return
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public WithdrawalResponse requestCashOut(Long userId,
                                             Long withdrawalPartnerId,
                                             Long walletId,
                                             Integer agencyId) throws CashmallowException {
        // 초기값 설정 및 검증
        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
        if (withdrawalPartner == null) {
            log.error("withdrawalPartnerId={}", withdrawalPartnerId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return requestCashOut(userId, withdrawalPartner, walletId, agencyId);
    }


    /**
     * 인출 신청
     *
     * @param userId
     * @param withdrawalPartner
     * @param walletId
     * @return
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public WithdrawalResponse requestCashOut(Long userId,
                                             WithdrawalPartner withdrawalPartner,
                                             Long walletId,
                                             Integer agencyId) throws CashmallowException {
        // 초기값 설정 및 검증
        User user = userRepositoryService.getUserByUserId(userId);

        if (user == null || withdrawalPartner == null) {
            log.error("userId={}, withdrawalPartnerId={}", userId, withdrawalPartner != null ? withdrawalPartner.getId() : -1);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            log.error("userId로 여행자 정보를 찾을 수 없습니다.");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cancelAllWithdrawal(traveler, withdrawalPartner.getId());

        try {
            return executeRequest(traveler, user, withdrawalPartner, walletId, agencyId);

        } catch (MallowlinkNotFoundEnduserException e) {
            log.info("Mallowlink에 등록 되지 않은 traveler:{}", traveler.getId());
            enduserService.register(user, traveler);

            try {
                return executeRequest(traveler, user, withdrawalPartner, walletId, agencyId);
            } catch (MallowlinkNotFoundEnduserException ex) {
                log.error("Mallowlink Enduser 등록 후 다시 인출시 실패", ex);
                throw new CashmallowException(INTERNAL_SERVER_ERROR, ex);
            }
        } catch (MallowlinkException e) {
            log.error("Mallowlink 인출 실패:{}", e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, e.getStatus().getMessage());
        }
    }

    @NotNull
    private WithdrawalResponse executeRequest(Traveler traveler,
                                              User user,
                                              WithdrawalPartner withdrawalPartner,
                                              Long walletId,
                                              Integer aId) throws CashmallowException, MallowlinkNotFoundEnduserException {
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(walletId);
        if (travelerWallet == null || travelerWallet.geteMoney().compareTo(BigDecimal.ZERO) <= 0) {
            log.error("인출 가능한 여행자 지갑을 찾지 못했습니다. eMoney:{}", traveler != null ? travelerWallet.geteMoney() : "NULL");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        int agencyId = aId != null ? aId : 0;
        BigDecimal travelerCashoutAmt = travelerWallet.geteMoney();
        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(travelerWallet.getExchangeId());
        String countryCode = travelerWallet.getCountry();

        BigDecimal cashoutFee = cashOutService.calcCashoutFee(countryCode, travelerCashoutAmt, withdrawalPartner.getId());

        // 현지 시각
        LocalDateTime localNow = LocalDateTime.now(CountryCode.of(travelerWallet.getCountry()).getZoneId());
        String cashoutReservedDate = localNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Integer requestTime = Integer.valueOf(localNow.format(DateTimeFormatter.ofPattern("HHmm")));

        cashOutService.checkCashOutRequestV2(traveler, cashoutReservedDate, travelerWallet, withdrawalPartner, travelerCashoutAmt, requestTime, countryCode);

        // 유효한 OTP가 있는지 확인
        WithdrawalResponse cachedWithdrawalResponse = partnerCancelService.getCachedWithdrawalResponse(withdrawalPartner, walletId);
        if (cachedWithdrawalResponse != null) {
            return cachedWithdrawalResponse;
        }

        // 인출 준비
        walletRepositoryService.updateWalletForWithdrawalV2(travelerCashoutAmt, travelerWallet);
        String transactionId = mallowlinkService.increaseAndGetTransactionId();

        CashOut cashOut = new CashOut(
                traveler.getId(),
                withdrawalPartner.getId(),
                countryCode,
                travelerCashoutAmt,
                BigDecimal.ZERO,
                travelerCashoutAmt,
                cashoutFee,
                null,
                null,
                travelerWallet.getExchangeIds(),
                travelerWallet.getId());

        cashOut.setCashoutReservedDate(cashoutReservedDate);
        cashOut.setPrivacySharingAgreement(user.getAgreePrivacy().equalsIgnoreCase("Y"));
        cashOut.setCoStatus(CashOut.CoStatus.OP.name());
        cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

        cashOut.setCasmTxnId(transactionId);

        long cashOutId = cashOutService.registerCashOut(cashOut);
        cashOut.setId(cashOutId);

        // mallowlink request
        MallowlinkWithdrawalRequest request = MallowlinkWithdrawalRequest.of(
                transactionId,
                traveler,
                travelerWallet,
                travelerCashoutAmt,
                exchange.getFromAmt(),
                CommonUtil.getRequestIp(),
                withdrawalPartner.getStorekeeperType().getPartnerId(),
                agencyId
        );
        MallowlinkWithdrawalResponse withdrawalResponse;
        try {
            withdrawalResponse = MallowlinkWithdrawalResponse.of(agencyId, withdrawalClient.withdrawal(request).getData());
        } catch (MallowlinkException e) {
            if (e.getStatus().equals(TRANSACTION_USER_EXCEED_AMOUNT)) {
                log.error("인출 일일 한도 초과 : 유저ID: {}, cashoutId:{}", user.getId(), cashOutId, e);
                throw new CashmallowException("WITHDRWAL_" + e.getMessage(), e);
            } else if (e.getStatus().equals(MAINTENANCE_TIME)) {
                // 서비스 점검시간 추가
                throw new CashmallowException("CASHOUT_ERROR_REQUEST_MAINTENANCE");
            } else {
                throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
            }
        }

        // updateCashOut QR or OTP
        switch (withdrawalResponse.confirmType()) {
            case QR -> cashOut.setQrCodeSource("QR");
            case OTP -> {
                cashOut.setQrCodeValue(securityService.encryptAES256(jsonUtil.toJson(withdrawalResponse)));
                cashOut.setQrCodeSource("OTP");
            }
            case QRGEN -> {
                cashOut.setQrCodeValue(securityService.encryptAES256(withdrawalResponse.qrCode()));
                cashOut.setQrCodeSource("QRGEN");
            }
        }

        if (cashOut.getCountry().equals(CountryCode.ID.getCode())) {
            log.info("executeRequest() : AJ Credentials={}", withdrawalResponse.credentials());
            String bankCodeVaNumber = getAjBankCodeVaNumber(withdrawalResponse.credentials());
            String otpCode = withdrawalResponse.credentials().stream().filter(credential -> "WITHDRAW_CODE".equals(credential.key()))
                    .map(Credential::code).findFirst().orElse("");
            // AJ 인출만료 시간 60분을 생성시간에 추가
            CashOutAjOtp cashOutAjOtp = new CashOutAjOtp(cashOut.getId(), bankCodeVaNumber,
                    otpCode, new Timestamp (cashOut.getCoStatusDate().getTime() + 60 * 60 * 1000));
            cashoutRepositoryService.insertCashOutAjOtp(cashOutAjOtp);
        }

        cashoutRepositoryService.updateCashOut(cashOut);

        // send slack message
        String msg = "인출 거래번호: " + cashOutId +
                "\n유저ID:" + user.getId() + ", 신청국:" + travelerWallet.getCountry() + ", 금액:" + travelerCashoutAmt +
                "\n가맹점ID:" + withdrawalPartner.getUserId() + ", 가맹점이름:" + withdrawalPartner.getShopName() +
                "\nWithdrawalRequestNo: " + cashOut.getCasmTxnId();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }
        alarmService.aAlert("인출신청", msg, user);

        return WithdrawalResponse.of(cashOutId, withdrawalResponse);
    }

    private String getAjBankCodeVaNumber(List<Credential> credentials) {
        // Key-Value 형태로 변환
        Map<String, String> credentialMap = credentials.stream()
                .collect(Collectors.toMap(Credential::key, Credential::code));

        // "BANK_CODE_VA_NUMBER" 키가 있으면 바로 사용, 없으면 "BANK_CODE" + "VA_NUMBER" 결합
        return Optional.ofNullable(credentialMap.get("BANK_CODE_VA_NUMBER"))
                .orElse(credentialMap.getOrDefault("BANK_CODE", "") +
                        credentialMap.getOrDefault("VA_NUMBER", ""));
    }


    public void cancelByTimeout(Long cashoutId) throws CashmallowException {
        CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);
        if (cashout == null) {
            log.error("인출정보가 올바르지 않습니다. cashout=null");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cancel(cashout, CashOut.CoStatus.CC);
    }

    public void cancel(Long userId, Long cashoutId, CashOut.CoStatus coStatus) throws CashmallowException {
        CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);
        if (cashout == null) {
            log.error("인출정보가 올바르지 않습니다. cashout=null");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null || !traveler.getId().equals(cashout.getTravelerId())) {
            log.error("TravelerId가 일치 하지 않음. TravelerId:{}, Cashout.travelerId:{}", traveler != null ? traveler.getId() : "null", cashout.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cancel(cashout, coStatus);
    }

    private void cancel(CashOut cashout, CashOut.CoStatus coStatus) throws CashmallowException {

        if (partnerCancelService.isNonCancelableStatePartner(cashout)) {
            return;
        }

        log.info("start cashout id:{}, getWithdrawalPartnerId:{}, coStatus:{} to coStatus:{}", cashout.getId(), cashout.getWithdrawalPartnerId(), cashout.getCoStatus(), coStatus);

        switch (cashout.getCoStatusEnum()) {
            case OP -> log.debug("start cashout id:{}, getWithdrawalPartnerId:{}, coStatus:{} to coStatus:{}", cashout.getId(), cashout.getWithdrawalPartnerId(), cashout.getCoStatus(), coStatus);
            case CF -> throw new CashmallowException(Const.ALREADY_COMPLETE);
            case CC, TC, SC -> {
                log.info("ALREADY_CANCELD cashoutId={}, coStatus={}", cashout.getId(), cashout.getCoStatus());
                alarmService.i("인출 취소", "ALREADY_CANCELD cashoutId=%s, coStatus=%s".formatted(cashout.getId(), cashout.getCoStatus()));
                return;
            }
            default -> {
                log.warn("진행 중인 인출이 아니므로 취소할 수 없습니다. cashoutId={}, coStatus={}", cashout.getId(), cashout.getCoStatus());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        // 캐시멜로 데이터는 성공 여부 상관없이 취소 처리 한다.
        cashout.setCoStatus(String.valueOf(coStatus));
        cashout.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

        String clientTransactionId = cashout.getCasmTxnId();
        MallowlinkCancelRequest request = new MallowlinkCancelRequest(clientTransactionId);

        Long cashoutId = cashout.getId();
        try {
            withdrawalClient.cancel(request);
            cashOutService.processCancelCashout(cashoutId, coStatus);

        } catch (MallowlinkException e) {
            switch (e.getStatus()) {
                case TRANSACTION_ALREADY_COMPLETED -> {
                    log.info("취소 실패, cashoutId:{} 이미 완료된 인출", cashoutId);
                    cashOutService.completeCashOutConfirm(cashout);
                    // throw new CashmallowException(Const.ALREADY_COMPLETE);
                }
                case TRANSACTION_ALREADY_REVERTED -> {
                    log.info("인출 취소 중, cashoutId:{} 승인 거절 상태", cashoutId);
                    // 인출 취소 및 지갑 복원 로직
                    cashOutService.processCancelCashout(cashoutId, coStatus);
                }
                case TRANSACTION_ALREADY_CANCELED -> {
                    log.info("인출 취소 중, cashoutId:{} 이미 취소된 인출", cashoutId);
                    // 인출 취소 및 지갑 복원 로직
                    cashOutService.processCancelCashout(cashoutId, coStatus);
                }
                case UNSUPPORTED_API -> {
                    log.info("지원하지 않는 API, cashoutId:{}", cashoutId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
                default -> log.info("인출 취소 실패, cashsoutId:{}, error:{}", cashoutId, e.getMessage());
            }
        }
    }

    public void cancelAllWithdrawal(Traveler traveler) {
        List<CashOut> cashoutOpList = cashOutService.getCashOutOpListByTravelerId(traveler.getId()).stream()
                .filter(co -> co.getCoStatus().equals("OP"))
                .toList();

        for (CashOut cashOut : cashoutOpList) {
            try {
                cancel(cashOut, CashOut.CoStatus.TC);
            } catch (CashmallowException e) {
                if (e.getMessage().equals(Const.ALREADY_COMPLETE)) {
                    log.info("이미 완료된 인출 cashout id:{}", cashOut.getId());
                } else {
                    log.error("지갑 가져오기 일괄 취소 시 에러, {}", e.getMessage(), e);
                }
            }
        }
    }

    public void cancelAllWithdrawal(Traveler traveler, Long withdrawalPartnerId) {
        List<CashOut> cashoutOpList = cashOutService.getCashOutOpListByTravelerId(traveler.getId()).stream()
                .filter(co -> co.getWithdrawalPartnerId().equals(withdrawalPartnerId))
                .filter(co -> co.getCoStatus().equals("OP"))
                .toList();

        for (CashOut cashOut : cashoutOpList) {
            try {
                cancel(cashOut, CashOut.CoStatus.TC);
            } catch (CashmallowException e) {
                if (e.getMessage().equals(Const.ALREADY_COMPLETE)) {
                    log.info("이미 완료된 인출 cashout id:{}", cashOut.getId());
                } else {
                    log.error("지갑 가져오기 일괄 취소 시 에러, {}", e.getMessage(), e);
                }
            }
        }
    }


    public QrResponse qr(long userId, long walletId, String qrData) throws CashmallowException {
        log.info("userId:{}, walletId:{}", userId, walletId);

        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(walletId);
        if (travelerWallet == null) {
            log.error("인출 가능한 여행자 지갑을 찾지 못했습니다.");
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CashOut cashOut = cashoutRepositoryService.getCashOutLastOpByWalletId(walletId);
        if (cashOut == null) {
            String errorMsg = String.format("CashOut 테이블에서 OP 상태인 값을 찾지 못했습니다. 유저ID: %s, walletId:%s", userId, walletId);
            alarmService.e("SCB confirmWithdrawal", errorMsg);
            log.error(errorMsg);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String clientTransactionId = cashOut.getCasmTxnId();
        log.info("ml clientTransactionId:{}, walletId:{}, cashoutId:{}", clientTransactionId, walletId, cashOut.getId());

        try {
            withdrawalClient.qr(MallowlinkQrRequest.of(clientTransactionId, qrData));
        } catch (MallowlinkException e) {
            log.error("e:{}", e.getMessage(), e);
            switch (e.getStatus()) {
                case WITHDRAWAL_INVALID_QR -> {
                    throw new CashmallowException("ML_INVALID_QR_CODE", e);
                }
                case INVALID_ATM -> {
                    throw new CashmallowException("INVALID_ATM", e);
                }
                default -> throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
            }
        }
        return new QrResponse(clientTransactionId);
    }

    /**
     * 결과 inbound 처리
     *
     * @param request
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public String webhookResult(WebhookResultRequest request) throws CashmallowException {

        String transactionId = request.clientTransactionId();
        CashOut cashOut = cashoutRepositoryService.getCashOutByCasmTxnId(transactionId)
                .orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));

        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        if (withdrawalPartner == null || "MALLOWLINK".equals(withdrawalPartner.getKindOfStorekeeper())) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CashOut.CoStatus coStatus = CashOut.CoStatus.valueOf(cashOut.getCoStatus());

        log.info("transactionId:{}", transactionId);
        log.info("withdrawalPartner:{}", withdrawalPartner.getStorekeeperType());
        log.info("request:{}", request);

        switch (request.status()) {
            case SUCCESS -> {
                if (!CashOut.CoStatus.OP.equals(coStatus)) {
                    log.error("The status is not operation. coStatus={}", coStatus);
                    throw new CashmallowException("DATA_NOT_FOUND_ERROR");
                }

                cashOutService.completeCashOutConfirm(cashOut);

                // scb 앱 알림
                if (withdrawalPartner.getStorekeeperType().equals(WithdrawalPartner.KindOfStorekeeper.SCB)) {
                    InboundMessage success = InboundMessage.success(transactionId);
                    pubService.sendMessage(success);
                    log.debug("inboundMessage: {}", jsonUtil.toJson(success));
                }

                return "Success";
            }
            case REVERT -> {
                if (!CashOut.CoStatus.CF.equals(coStatus)) {
                    log.error("The status is not complete. coStatus={}", coStatus);
                    throw new CashmallowException("The status is not complete.", CoatmServiceImpl.ResultCode.INVALID_CASHOUT);
                }

                cashOutService.rollbackCashoutWithNotification(cashOut);

                // scb 앱 알림
                if (withdrawalPartner.getStorekeeperType().equals(WithdrawalPartner.KindOfStorekeeper.SCB)) {
                    InboundMessage fail = InboundMessage.fail(transactionId);
                    pubService.sendMessage(fail);
                    log.debug("inboundMessage: {}", jsonUtil.toJson(fail));
                }

                return "Success";
            }
            case FAIL -> {
                // 이미 취소된 인출건 처리.
                if (CashOut.CoStatus.TC.equals(coStatus) || CashOut.CoStatus.CC.equals(coStatus) || CashOut.CoStatus.SC.equals(coStatus)) {
                    log.warn("Already Canceled. coStatus={}", coStatus);
                    return "Success";
                }

                if (!CashOut.CoStatus.OP.equals(coStatus)) {
                    log.error("The status is not operation. coStatus={}", coStatus);
                    throw new CashmallowException("DATA_NOT_FOUND_ERROR");
                }

                cashOutService.processCancelCashout(cashOut.getId(), CashOut.CoStatus.SC);

                switch (withdrawalPartner.getStorekeeperType()) {
                    case SCB -> {
                        // scb 앱 알림
                        InboundMessage fail = InboundMessage.fail(transactionId);
                        pubService.sendMessage(fail);
                        log.debug("inboundMessage: {}", jsonUtil.toJson(fail));
                    }
                    case AJ -> {
                        // AJ OTP 만료시 알람 전송
                        // FCM Notification 으로 앱에서 메인으로 이동하기 위해 보낸다.
                        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashOut.getTravelerId());
                        User tUser = userRepositoryService.getUserByUserId(traveler.getUserId());
                        notificationService.sendFcmNotificationMsgAsync(tUser, FcmEventCode.CO, FcmEventValue.SC, cashOut.getId());
                    }
                }

                return "Success";
            }
            default -> {
                return "Fail";
            }
        }
    }

    /**
     * 환불 inbound 처리
     *
     * @param request
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public String webhookRefundResult(WebhookRefundRequest request) throws CashmallowException {

        String transactionId = request.clientTransactionId();
        CashOut cashOut = cashoutRepositoryService.getCashOutByCasmTxnId(transactionId)
                .orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));

        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        if (withdrawalPartner == null || "MALLOWLINK".equals(withdrawalPartner.getKindOfStorekeeper())) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        CashOut.CoStatus coStatus = CashOut.CoStatus.valueOf(cashOut.getCoStatus());
        if (request.status() == WebhookRefundRequest.Status.SUCCESS) {
            switch (request.refundType()) {
                case REFUND, REVERT -> {
                    if (!CashOut.CoStatus.CF.equals(coStatus)) {
                        log.error("The status is not complete. coStatus={}", coStatus);
                        throw new CashmallowException("The status is not complete.", CoatmServiceImpl.ResultCode.INVALID_CASHOUT);
                    }

                    // slack 에 인출 취소 알림
                    User user = userRepositoryService.getUserByUserId(cashOut.getTravelerId());
                    String message = "ML BO를 통한 인출 롤백(전액환불처리) 처리 (" + request.refundType() + ")" + "\nUser ID: " + user.getId() + "\n트랜잭션 ID: " + transactionId + "\n화폐: " + request.currency().name();
                    alarmService.aAlert("인출롤백", message, user);

                    cashOutService.rollbackCashoutWithNotification(cashOut);
                    return "Success";
                }
                default -> {
                    return "Fail";
                }
            }
        }

        return "Fail";
    }

}
