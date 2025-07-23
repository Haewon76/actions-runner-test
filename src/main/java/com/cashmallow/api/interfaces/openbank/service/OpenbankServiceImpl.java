package com.cashmallow.api.interfaces.openbank.service;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.domain.model.bankinfo.BankInfo;
import com.cashmallow.api.domain.model.bankinfo.BankInfoMapper;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.Exchange.ExStatus;
import com.cashmallow.api.domain.model.exchange.ExchangeMapper;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.openbank.Openbank;
import com.cashmallow.api.domain.model.openbank.OpenbankToken;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.openbank.dto.*;
import com.cashmallow.api.interfaces.openbank.dto.client.*;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.RandomUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

import static com.cashmallow.api.domain.model.country.enums.CountryCode.SG;
import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_PINCODE;


@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class OpenbankServiceImpl {

    @Value("${openbank.clientId}")
    private String clientId;
    @Value("${openbank.redirectUri}")
    private String redirectUri;
    @Value("${openbank.scope}")
    private String scope;

    private static final String FIRST_AUTHENTICATION = "0";  // 0:최초인증, 2:인증생략
    private static final String SKIP_AUTHENTICATION = "2";

    // 토큰 만료일 30일 전에 리프래쉬
    private final long refreshLimit = 30 * 24 * 60 * 60;
    private final int EXPIRE_AUTH = 5;

    private final RedisService redisService;
    private final UserRepositoryService userRepositoryService;
    private final OpenbankRepositoryImpl openbankRepository;
    private final ExchangeMapper exchangeMapper;
    private final TravelerRepositoryService travelerRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final OpenbankClient openbankClient;
    private final ExchangeRepositoryService exchangeRepositoryService;
    private final RemittanceRepositoryService remittanceRepositoryService;
    private final CompanyServiceImpl companyService;
    private final BankInfoMapper bankInfoMapper;
    private final EnvUtil envUtil;
    private final AlarmService alarmService;
    private final Gson gson;

    private final NotificationService notificationService;
    private final CountryService countryService;

    /**
     * 여행자의 오픈뱅킹 가입여부를 반환
     *
     * @param travelerId
     * @return 가입: Y, 미가입, 탈퇴: N, 동의 만료: E
     * @throws CashmallowException
     */
    public String getOpenbankSignYn(long travelerId) throws CashmallowException {
        Openbank openbank = openbankRepository.getOpenbank(travelerId);

        log.debug("openbank={}", openbank);

        if (openbank != null && openbank.isSigned()) {
            ZonedDateTime signDate = openbank.getSignDate();
            LocalDateTime localDateTime = signDate.toLocalDateTime();
            LocalDateTime expirationDate = localDateTime.plus(1, ChronoUnit.YEARS);

            if (LocalDateTime.now().isAfter(expirationDate)) {
                log.info("travelerId={} OPENBANK_ACCOUNT_EXPIRED", travelerId);
                return "E";
            } else {
                return "Y";
            }
        }
        return "N";
    }

    public OpenbankAuthResponse getUserOAuth(long userId,
                                             String deviceType,
                                             String deviceIp,
                                             String deviceId,
                                             String deviceVersion) throws CashmallowException, UnsupportedEncodingException {
        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null) {
            log.error("올바르지 않은 userId 입니다.");
            throw new CashmallowException("INTERNAL_SERVER_ERROR");
        }
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            log.error("userId로 여행자 정보를 찾을 수 없습니다.");
            throw new CashmallowException("INTERNAL_SERVER_ERROR");
        }

        // state 저장
        String state = RandomUtil.generateRandomString(RandomUtil.ALPHA_NUMERIC, 32);
        String redisKey = redisService.generateRedisKey("travelerId", traveler.getId().toString(), RedisService.REDIS_KEY_OPENBANK);
        log.info("created state={} redisKey={}", state, redisKey);
        redisService.put(redisKey, state, 10);

        ClientInfoVO clientInfoVO = new ClientInfoVO(traveler.getId(), deviceType);
        String clientInfoJson = Base64.getUrlEncoder().encodeToString(gson.toJson(clientInfoVO).getBytes(StandardCharsets.UTF_8));

        String korPhoneNumber = CommonUtil.getKorPhoneNumber(user);

        String authType = FIRST_AUTHENTICATION;
        Openbank openbank = openbankRepository.getOpenbank(traveler.getId());
        OpenbankAuthResponse.Header header = null;

        // todo 인증 생략
        // if (openbank != null && isOpenbankAuth(openbank)) {
        //     authType = SKIP_AUTHENTICATION;
        //     header = new OpenbankAuthResponse.Header(openbank.getUserSeqNo(), openbank.getUserCi(), openbank.getAccessToken());
        // }

        UriComponents openBankUrl = UriComponentsBuilder
                .fromHttpUrl("https://testapi.openbanking.or.kr")
                .path("/oauth/2.0/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", "{clientId}")
                .queryParam("redirect_uri", "{redirectUri}")
                .queryParam("scope", "{scope}")
                .queryParam("state", "{state}")
                .queryParam("client_info", "{clientInfoVO}")
                .queryParam("client_device_type", "{deviceType}")
                .queryParam("client_device_ip", "{deviceIp}")
                .queryParam("client_device_id", "{deviceId}")
                .queryParam("client_device_num", "{phoneNumber}")
                .queryParam("client_device_version", "{deviceVersion}")
                .queryParam("auth_type", "{authType}")
                .encode()
                .buildAndExpand(clientId, redirectUri, scope, state, clientInfoJson, deviceType, deviceIp, deviceId, korPhoneNumber, deviceVersion, authType);

        log.info("openBank Auth Url: {}", openBankUrl.toUri());

        return new OpenbankAuthResponse(openBankUrl.toUri().toString(), header);
    }


    private boolean isOpenbankAuth(Openbank openbank) {
        LocalDateTime signDate = openbank.getSignDate().toLocalDateTime();
        LocalDateTime expireDate = signDate.plusYears(EXPIRE_AUTH);
        String userCi = openbank.getUserCi();

        return openbank.isSigned()
                && LocalDateTime.now().isBefore(expireDate)
                && StringUtils.isNotBlank(userCi);
    }

    @Transactional
    public OpenbankTokenResponse issueOpenBankToken(String code, long travelerId) {

        OpenbankTokenResponse openbankTokenResponse = openbankClient.issueToken(code);

        if (openbankTokenResponse.isFail()) {
            log.error("{} : {}", openbankTokenResponse.getRspCode(), openbankTokenResponse.getRspMessage());
            throw new RuntimeException(openbankTokenResponse.getRspMessage());
        }

        ZonedDateTime tokenIssueDate = ZonedDateTime.now();
        String accessToken = openbankTokenResponse.getAccessToken();
        String refreshToken = openbankTokenResponse.getRefreshToken();
        String userSeqNo = openbankTokenResponse.getUserSeqNo();

        // 토큰 테이블 저장 insertTable
        Openbank openbank = openbankRepository.getOpenbank(travelerId);
        OpenbankToken openbankToken = new OpenbankToken(travelerId, accessToken, refreshToken, userSeqNo, tokenIssueDate, ZonedDateTime.now());
        if (openbank == null) {
            int effectedRow = openbankRepository.insertOpenbankToken(openbankToken);
        } else {
            int effectedRow = openbankRepository.updateOpenbankToken(openbankToken);
        }
        return openbankTokenResponse;
    }

    public boolean reissueToken(long travelerId, String code, String refreshToken) throws Exception {
        Openbank openbank = openbankRepository.getOpenbank(travelerId);

        ZonedDateTime expiredAccessToken = openbank.getTokenIssueDate().plus(90, ChronoUnit.DAYS);
        ZonedDateTime expiredRefreshToken = openbank.getTokenIssueDate().plus(100, ChronoUnit.DAYS);
        ZonedDateTime now = ZonedDateTime.now();
        if (now.isBefore(expiredAccessToken)) {
            log.error("Access 토큰 만료 이전");
            return false;
        }
        if (now.isAfter(expiredRefreshToken)) {
            log.error("Refresh 토큰 만료");
            return false;
        }

        OpenbankTokenResponse openbankTokenResponse = openbankClient.reissueToken(refreshToken);

        if (openbankTokenResponse.isSuccess()) {
            OpenbankToken openbankToken = new OpenbankToken(travelerId,
                    openbankTokenResponse.getAccessToken(),
                    openbankTokenResponse.getRefreshToken(),
                    openbankTokenResponse.getUserSeqNo(),
                    ZonedDateTime.now(),
                    openbank.getSignDate());
            int i = openbankRepository.updateOpenbankToken(openbankToken);
            if (i != 1) {
                throw new RuntimeException("INTERNAL_SERVER_ERROR");
            }
            return true;
        } else {
            log.info("{}: {}", openbankTokenResponse.getRspCode(), openbankTokenResponse.getRspMessage());
            return false;
        }
    }

    @Transactional(readOnly = false, rollbackFor = CashmallowException.class)
    public ApiResultVO transferExchange(long exchangeId, Traveler traveler, String otp) throws CashmallowException {
        log.info("exchangeId: {}, travelerId={}, otp={}", exchangeId, traveler.getId(), otp);

        checkPincodeOtp(traveler, otp);

        ApiResultVO apiResultVO = new ApiResultVO(Const.CODE_FAILURE);
        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);
        if (exchange == null) {
            throw new RuntimeException("NOT FOUND EXCHANGE");
        }

        Exchange latestExchangeInProgress = exchangeRepositoryService.getLatestExchangeInProgress(traveler);

        // 송금 소유자 확인
        if (latestExchangeInProgress != null && !exchange.getId().equals(latestExchangeInProgress.getId())) {
            log.error("해당 유저가 진행중인 다른 환전건이 있습니다. exchange={}, latestExchangeInProgress={}", exchange, latestExchangeInProgress);
            log.error("exchange.getTravelerId():{}, travelerId:{}", exchange.getTravelerId(), traveler.getId());
            throw new CashmallowException("해당 유저가 진행중인 다른 환전건이 있습니다.");
        }

        long travelerId = traveler.getId();
        Openbank openbankUserInfo = openbankRepository.getOpenbank(travelerId);
        log.debug("openbankUserInfo={}", openbankUserInfo);

        if (openbankUserInfo == null) {
            throw new RuntimeException("NOT FOUND OPENBANK USER INFO");
        }

        BigDecimal fromAmt = exchange.getFromAmt();
        afterBankTestAddTravelerWallet(exchangeId,
                travelerId,
                openbankUserInfo.getBankName(),
                openbankUserInfo.getAccountNumMasked(),
                openbankUserInfo.getAccountHolderName(),
                fromAmt);

        OpenbankTransferWithdrawalResponse withdrawalResponse = openbankClient.transferWithdrawal(
                TransactionRecord.RelatedTxnType.EXCHANGE,
                openbankUserInfo.getAccessToken(),
                openbankUserInfo.getFintechUseNum(),
                fromAmt.longValue(),
                openbankUserInfo.getAccountHolderName(),
                String.valueOf(travelerId));

        log.info("response:{}", withdrawalResponse);

        String rspCode = withdrawalResponse.getRspCode();
        String rspMassage = withdrawalResponse.getRspMessage();
        if (withdrawalResponse.isSuccess()) {
            apiResultVO.setSuccessInfo();
            companyService.sendMappingResultNotification(TransactionRecord.RelatedTxnType.EXCHANGE, exchangeId, false);

        } else if ("400".equals(rspCode) || "A0003".equals(rspCode) || "A0007".equals(rspCode)) {
            // 입금 처리중
            // todo 잠시 후 이체결과 조회
            log.info("transfer rspCode: {}, 잠시 후 이체 결과 조회 필요.", rspCode);
            apiResultVO.setFailInfo("잠시 후 이체 결과 조회 필요.");
            throw new RuntimeException("INVALID_TRANSFER_INFO");

        } else if (!"A0000".equals(rspCode)) {
            alarmService.i("openbank-transfer-fail", withdrawalResponse.toString());
            apiResultVO.setFailInfo("INVALID_TRANSFER_INFO");
            throw new RuntimeException("INVALID_TRANSFER_INFO");
        }

        TransferExchangeResponse response = TransferExchangeResponse.builder()
                .exchangeId(exchangeId)
                .fromAmt(fromAmt)
                .rspCode(rspCode)
                .rspMessage(rspMassage)
                .build();
        apiResultVO.setObj(response);
        return apiResultVO;
    }

    /**
     * Pincode 검증으로 받은 otp를 상용해 정상 요청인지 검증
     *
     * @param traveler
     * @param otp
     */
    private void checkPincodeOtp(Traveler traveler, String otp) {
        boolean isMatch = redisService.isMatch(REDIS_KEY_PINCODE, String.valueOf(traveler.getUserId()), otp);
        if (!isMatch) {
            log.info("pinCode 인증을 거치지 않은 이체 시도, otp={}", otp);
            throw new RuntimeException("NOT INVALID REQUEST");
        }
    }

    @Transactional(readOnly = false, rollbackFor = CashmallowException.class)
    public ApiResultVO transferRemittance(long remittanceId, Traveler traveler, String otp) throws CashmallowException {
        log.info("remittanceId:{}, traveler:{}, otp:{}", remittanceId, traveler.getId(), otp);

        checkPincodeOtp(traveler, otp);

        ApiResultVO apiResultVO = new ApiResultVO(Const.CODE_FAILURE);

        Remittance remittance = remittanceValidate(remittanceId, traveler);

        // 오픈뱅킹 가입 검증
        long travelerId = traveler.getId();
        Openbank openbankUserInfo = openbankRepository.getOpenbank(travelerId);
        log.debug("openbankUserInfo={}", openbankUserInfo);
        if (openbankUserInfo == null) {
            throw new RuntimeException("NOT FOUND OPENBANK USER INFO");
        }

        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        OpenbankTransferWithdrawalResponse withdrawalResponse = openbankClient.transferWithdrawal(TransactionRecord.RelatedTxnType.REMITTANCE,
                openbankUserInfo.getAccessToken(),
                openbankUserInfo.getFintechUseNum(),
                remittance.getFromAmt().longValue(),
                openbankUserInfo.getAccountHolderName(),
                String.valueOf(travelerId));

        log.info("response:{}", withdrawalResponse);

        String rspCode = withdrawalResponse.getRspCode();
        String rspMassage = withdrawalResponse.getRspMessage();
        if (withdrawalResponse.isSuccess()) {
            if (SG.getCode().equals(remittance.getToCd())) {
                log.info("송금 국가={}", remittance.getToCd());
                // sentbeService.requestSentbeRemittance(remittance);
                // TODO : CF 건 AML 거래 정보 전송(한국 오픈시 반드시 필요)

            } else {
                alarmService.i("openbank-transfer-fail", "올바르지 않은 to국가:" + remittance.getToCd());
                throw new RuntimeException("INVALID_TRANSFER_INFO");
            }

            if (envUtil.isDev()) {
                remittance.setRemitStatus(Remittance.RemittanceStatusCode.CF);
                remittanceRepositoryService.updateRemittance(remittance);

                remittanceRepositoryService.insertRemitStatus(remittance.getId(), Remittance.RemittanceStatusCode.CF);

                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.RM, FcmEventValue.CF, 0L, "");

                // Country fromCountry = countryService.getCountry(remittance.getFromCd());
                // Country toCountry = countryService.getCountry(remittance.getToCd());
                //
                // notificationService.sendEmailConfirmRemittance(user, traveler, remittance, fromCountry, toCountry);
                //
                // companyService.preparePaygateExchangeForRemit(remittance);
            }

            apiResultVO.setSuccessInfo();
            companyService.sendMappingResultNotification(TransactionRecord.RelatedTxnType.REMITTANCE, remittanceId, false);

        } else if ("400".equals(rspCode) || "A0003".equals(rspCode) || "A0007".equals(rspCode)) {
            // 입금 처리중
            // todo 잠시 후 이체결과 조회
            log.info("transfer rspCode: {}, 잠시 후 이체 결과 조회 필요.", rspCode);
            // apiResultVO.setFailInfo("잠시 후 이체 결과 조회 필요.");
            throw new RuntimeException("INVALID_TRANSFER_INFO");

        } else if (!"A0000".equals(rspCode)) {
            alarmService.i("openbank-transfer-fail", withdrawalResponse.toString());
            // apiResultVO.setFailInfo("INVALID_TRANSFER_INFO");
            throw new RuntimeException("INVALID_TRANSFER_INFO");
        }

        TransferRemittanceResponse response = TransferRemittanceResponse.builder()
                .remittanceId(remittanceId)
                .fromAmt(remittance.getFromAmt())
                .rspCode(rspCode)
                .rspMessage(rspMassage)
                .build();
        apiResultVO.setObj(response);
        return apiResultVO;
    }

    private Remittance remittanceValidate(long remittanceId, Traveler traveler) throws CashmallowException {
        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remittanceId);
        Remittance inProgressRemittance = remittanceRepositoryService.getRemittanceInprogress(traveler.getId());

        // 송금 소유자 확인
        if (inProgressRemittance != null && !remittance.getId().equals(inProgressRemittance.getId())) {
            log.error("해당 유저가 진행중인 다른 송금건이 있습니다. remittance={}, inProgressRemittance={}", remittance, inProgressRemittance);
            log.error("remittance.getTravelerId():{}, travelerId:{}", remittance.getTravelerId(), traveler.getId());
            throw new CashmallowException("해당 유저가 진행중인 다른 송금건이 있습니다.");
        }

        if (SG.getCode().equals(remittance.getToCd())) {
            // Map<String, Object> sentbeBalanceMap = sentbeService.getSentbeBalance();
            // BigDecimal sentbeUSDBalance = new BigDecimal(sentbeBalanceMap.get("USD").toString());
            //
            // BigDecimal toAmtForUSD = remittance.getToAmt().multiply(new BigDecimal("1.37"));
            // if (sentbeUSDBalance.compareTo(toAmtForUSD) < 0) {
            //     log.info("Sentbe 지갑의 잔액이 부족합니다. 잔액={} USD, 신청금액={} USD", sentbeUSDBalance, toAmtForUSD);
            //     // todo 슬렉알람 추가?
            //     throw new CashmallowException("Sentbe 지갑의 잔액이 부족해서 매핑이 불가능합니다. 잔액=" + sentbeUSDBalance + "USD ,신청 금액=" + toAmtForUSD + "USD");
            // }
        } else {
            throw new CashmallowException("송금 신청이 불가능한 수취 국가입니다. toCd:{}", remittance.getToCd());
        }

        if (envUtil.isPrd() && !StringUtils.equals("Y", remittance.getIsConfirmedReceiverAml())) {
            log.info("AML 조회 결과가 존재하는 수취인. travelerId:{}, remittanceId:{}", traveler, remittance.getId());
            // todo 슬렉알람 추가?
            throw new CashmallowException("수취인 AML 조회가 완료되지 않았습니다.");
        }
        return remittance;
    }

    // 지갑 생성 함수 호출
    @Transactional(rollbackFor = CashmallowException.class)
    public boolean afterBankTestAddTravelerWallet(Long exchangeId, Long travelerId, String bankName, String accountNumber, String accountName, BigDecimal fromAmt) throws CashmallowException {
        Exchange exchange = exchangeMapper.getExchangeByExchangeId(exchangeId);
        log.info("exchangeId={}, travelerId={}", exchangeId, travelerId);
        if (exchange == null || !ExStatus.OP.name().equalsIgnoreCase(exchange.getExStatus())) {
            log.debug(gson.toJson(exchange));
            log.debug("환전정보(exchange)가 없거나 OP상태가 아닙니다.");
            throw new CashmallowException("환전정보(exchange)의 상태를 변경할 수 없습니다");
        }

        exchange.setTrAccountNo(accountNumber);
        exchange.setTrAccountName(accountName);
        exchange.setTrBankName(bankName);
        exchange.setTrDepositDate(Timestamp.valueOf(LocalDateTime.now()));
        exchange.setTrFromAmt(fromAmt);

        exchange.setExStatus(ExStatus.CF.toString());
        exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRow = exchangeRepositoryService.updateExchange(exchange);
        if (affectedRow != 1) {
            log.debug("환전정보(exchange)의 상태를 변경할 수 없습니다");
            throw new CashmallowException("환전정보(exchange)의 상태를 변경할 수 없습니다");
        }

        // 정상 : Traveler_wallet 입금
        walletRepositoryService.addTravelerWallet(travelerId,
                exchange.getFromCd(),
                exchange.getToCd(),
                exchange.getToAmt(),
                travelerId,
                exchange.getId());

        log.info("지갑생성 exchangeId={}, travelerId={}", exchangeId, travelerId);
        return true;
    }

    /**
     * 오픈뱅킹 계정 삭제
     *
     * @param travelerId
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public boolean closeUser(Long travelerId) throws CashmallowException {
        Openbank openbank = openbankRepository.getOpenbank(travelerId);
        if (openbank == null) {
            return true;
        }

        log.info("closeUser travelerId={}", travelerId);
        try {
            int isDelete = openbankRepository.deleteOpenbankUser(travelerId);
            log.debug("deleteOpenbankUser={}", isDelete);
            if (isDelete != 1) {
                throw new CashmallowException("FAIL: delete Openbank User");
            }
        } catch (Exception e) {
            log.info("deleteOpenbankUser={}", e.getMessage());
            throw new CashmallowException(e.getMessage(), e);
        }

        OpenbankCloseUserResponse closeUserResponse = openbankClient.closeUser(openbank.getAccessToken(), openbank.getUserSeqNo());

        if (closeUserResponse.isFail()) {
            log.info("rsp_message={}", closeUserResponse.getRspMessage());
            alarmService.i("openbank", closeUserResponse.toString());
            throw new CashmallowException(closeUserResponse.getRspMessage());
        }
        return true;
    }

    /**
     * 오픈뱅킹 계좌 해지
     *
     * @param traveler
     * @param otp
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public boolean cancelAccount(Traveler traveler, String otp) throws CashmallowException {
        if (!redisService.isMatch(REDIS_KEY_PINCODE, String.valueOf(traveler.getUserId()), otp)) {
            log.error("OTP가 일치 하지 않음. 올바르지 않은 요청. travelerId:{}", traveler.getId());
            throw new CashmallowException(MsgCode.INTERNAL_SERVER_ERROR);
        }

        long travelerId = traveler.getId();
        Openbank openbank = openbankRepository.getOpenbank(travelerId);

        int deleteOpenbankAccountRows = openbankRepository.deleteOpenbankAccount(travelerId);
        if (deleteOpenbankAccountRows != 1) {
            throw new CashmallowException("FAIL: delete Openbank User Account");
        }

        String accessToken = openbank.getAccessToken();
        String fintechUseNum = openbank.getFintechUseNum();
        OpenbankCancelAccountResponse cancelAccountRes = openbankClient.cancelAccount(accessToken, fintechUseNum);

        if (cancelAccountRes.isFail()) {
            log.info("rsp_code={}, rsp_message={}", cancelAccountRes.getRspCode(), cancelAccountRes.getRspMessage());
            log.info("cancelAccountRes={}", cancelAccountRes);
            throw new CashmallowException(cancelAccountRes.getRspMessage());
        }

        return true;
    }

    /**
     * @param travelerId
     * @param openbankToken "Bearer " 없이
     * @param userSeqNo
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public void updateOpenbankUserInfo(Long travelerId, String openbankToken, String userSeqNo) throws CashmallowException {
        UserMe userMe = openbankClient.getAccountInfo(openbankToken, userSeqNo);

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        String nameByTravelerId = travelerRepositoryService.getNameByTravelerId(travelerId);

        List<UserMeDetail> resList = userMe.getRes_list();
        resList.sort((m1, m2) -> m2.getTransfer_agree_dtime().compareTo(m1.getTransfer_agree_dtime()));
        UserMeDetail lastUserMeDetail = userMe.getRes_list().stream().findFirst().orElseThrow();

        if (userMe.isNotMatchUserName(nameByTravelerId)) {
            String message = "고객명이 일치하지 않습니다. 본인 계좌로 신청해주세요.";
            log.error("{} traveler.name:{}, openbank.userName:{}", message, nameByTravelerId, userMe.getUser_name());

            // todo 인증 생략 방어
            closeUser(travelerId);

            alarmService.i("openbank", message + " traveler.name:" + nameByTravelerId + ", openbank.userName:" + userMe.getUser_name());
            throw new RuntimeException(message);
        }

        // 계좌번호 일치
        String bankCodeStd = lastUserMeDetail.getBank_code_std();
        String bankCode = traveler.getBankCode();

        String accountNumMasked = lastUserMeDetail.getAccount_num_masked();
        String travelerAccountNo = traveler.getAccountNo();
        if (!StringUtils.equals(bankCodeStd, bankCode) || !CustomStringUtil.matchMaskedString(accountNumMasked, travelerAccountNo)) {
            String message = "계좌가 일치하지 않습니다. 인증 받은 계좌로 신청 해주세요.";
            String errorMsg = MessageFormat.format("{0} traveler.accountNo:[{1}]{2}, openbank.accountNo:[{3}]{4}", message, bankCode, travelerAccountNo, bankCodeStd, accountNumMasked);
            log.error(errorMsg);

            alarmService.i("openbank", errorMsg);
            throw new RuntimeException(message);
        }

        // 성공
        if (userMe.isSuccess()) {
            openbankRepository.updateOpenbankAccount(userMe.getUser_ci(), travelerId.toString(), lastUserMeDetail);
        }
    }

    public OpenbankUserResponse getOpenbankUser(long userId) {

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            return null;
        }

        Openbank openbank = openbankRepository.getOpenbank(traveler.getId());
        if (openbank == null) {
            return null;
        }

        String bankIconPath = "";
        if ("Y".equalsIgnoreCase(openbank.getAccountSignYn())) {
            BankInfo bankInfoKrByCode = bankInfoMapper.getBankInfoKrByCode(openbank.getBankCodeStd());
            bankIconPath = bankInfoKrByCode.getIconPath();
        }

        return OpenbankUserResponse.of(openbank, bankIconPath);
    }

    public void ReissueExpiredToken() {
        List<Openbank> expiredTokenUser = openbankRepository.getExpiredTokenUser();
        if (expiredTokenUser.isEmpty()) {
            return;
        }

        log.info("만료된 토큰을 가진 사용자 수: {}", expiredTokenUser.size());

        int count = 0;
        for (var openbank : expiredTokenUser) {
            OpenbankTokenResponse openbankTokenResponse = openbankClient.reissueToken(openbank.getRefreshToken());
            OpenbankToken openbankToken = new OpenbankToken(openbank.getTravelerId(),
                    openbankTokenResponse.getAccessToken(),
                    openbankTokenResponse.getRefreshToken(),
                    openbankTokenResponse.getUserSeqNo(),
                    ZonedDateTime.now(),
                    openbank.getSignDate());
            int effectedRow = openbankRepository.updateOpenbankToken(openbankToken);
            count += effectedRow;
        }

        log.info("토큰 재발급한 사용자 수 : {}/{}", count, expiredTokenUser.size());
    }
}
