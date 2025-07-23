package com.cashmallow.api.interfaces.openbank.service;


import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.openbank.OpenbankLogMapper;
import com.cashmallow.api.domain.model.openbank.OpenbankLogRequest;
import com.cashmallow.api.domain.model.openbank.OpenbankLogResponse;
import com.cashmallow.api.domain.model.openbank.OpenbankRequestType;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.openbank.OpenbankConst;
import com.cashmallow.api.interfaces.openbank.dto.client.*;
import com.cashmallow.common.RandomUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenbankClient {
    private final OpenbankConst openbankConst;
    private final OpenbankLogMapper openbankLogMapper;
    private final SecurityService securityService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisService redisService;
    private final Gson gsonSnakeCase;

    private WebClient webClient;
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String scope;
    private String cashmallowCd;
    private String cntrAccountNum;
    private String cntrBankCode;
    private String cntrAccountName;


    @PostConstruct
    public void init() {
        cashmallowCd = openbankConst.getCashmallowCd();
        baseUrl = openbankConst.getURL();
        clientId = openbankConst.getClientId();
        clientSecret = openbankConst.getSecret();
        redirectUri = openbankConst.getRedirectUri();
        scope = openbankConst.getScope();
        cntrAccountNum = openbankConst.getCntrAccountNum();
        cntrBankCode = openbankConst.getCntrBankCode();
        cntrAccountName = openbankConst.getCntrAccountName();

        webClient = WebClient.create(baseUrl);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OpenbankTokenResponse issueToken(String authorizationCode) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String code = "code";
        params.add(code, authorizationCode);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        String paramsToString = String.format("%s:%s", code, authorizationCode);
        log.info("{}", paramsToString);
        OpenbankLogRequest openbankLogRequest = new OpenbankLogRequest(
                OpenbankRequestType.ISSUE_TOKEN,
                "",
                paramsToString);
        openbankLogMapper.saveRequest(openbankLogRequest);


        ResponseEntity<String> response = webClient
                .post()
                .uri("oauth/2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankTokenResponse tokenResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankTokenResponse.class);

        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                openbankLogRequest.getId(),
                response.getStatusCode().toString(),
                "",
                tokenResponse.toString(),
                tokenResponse.getRspCode(),
                tokenResponse.getRspMessage()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", tokenResponse.getRspCode(), tokenResponse.getRspMessage());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return tokenResponse;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OpenbankTokenResponse reissueToken(String refreshToken) {

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        params.add("scope", scope);
        params.add("grant_type", "refresh_token");

        String paramsToString = String.format("%s:%s", "refresh_token", refreshToken);
        OpenbankLogRequest openbankLogRequest = new OpenbankLogRequest(
                OpenbankRequestType.REISSUE_TOKEN,
                "",
                paramsToString);
        openbankLogMapper.saveRequest(openbankLogRequest);

        ResponseEntity<String> response = webClient
                .post()
                .uri("oauth/2.0/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(params)
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankTokenResponse tokenResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankTokenResponse.class);

        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                openbankLogRequest.getId(),
                response.getStatusCode().toString(),
                "",
                tokenResponse.toString(),
                tokenResponse.getRspCode(),
                tokenResponse.getRspMessage()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", tokenResponse.getRspCode(), tokenResponse.getRspMessage());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return tokenResponse;
    }

    /**
     * 오픈뱅킹 유저 정보 및 계좌 정보 가져오기
     *
     * @param accessToken 오픈뱅킹 엑세스토큰
     * @param userSeqNo
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public UserMe getAccountInfo(String accessToken, String userSeqNo) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_seq_no", userSeqNo);

        OpenbankLogRequest openbankLogRequest = new OpenbankLogRequest(
                OpenbankRequestType.USER_ME,
                "",
                params.toString()
        );
        openbankLogMapper.saveRequest(openbankLogRequest);
        log.debug("reissueToken params{}", params);

        log.info("request:{}", params);
        ResponseEntity<String> response = webClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path("/v2.0/user/me")
                                .queryParams(params)
                                .build()
                ).headers(httpHeaders -> httpHeaders.setBearerAuth(accessToken))
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        UserMe userMe = gsonSnakeCase.fromJson(response.getBody(), UserMe.class);
        log.info("response:{}", userMe);

        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                openbankLogRequest.getId(),
                response.getStatusCode().toString(),
                userMe.getApi_tran_id(),
                userMe.toString(),
                userMe.getRsp_code(),
                userMe.getRsp_message()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", userMe.getRsp_code(), userMe.getRsp_message());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return userMe;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OpenbankTransferWithdrawalResponse transferWithdrawal(TransactionRecord.RelatedTxnType txnType, String accessToken, String fintechUseNum, long fromMoney, String reqClientName, String travelerId) {
        String bankTranId = getBankTranId();
        String tranDtime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));


        String dpsPrintContent;
        if (txnType.equals(TransactionRecord.RelatedTxnType.EXCHANGE)) {
            dpsPrintContent = "환전";
        } else if (txnType.equals(TransactionRecord.RelatedTxnType.REMITTANCE)) {
            dpsPrintContent = "송금";
        } else {
            throw new RuntimeException("INTERNAL_SERVER_ERROR");
        }

        OpenbankTransferWithdrawalReqeust transferWithdrawalRequest = new OpenbankTransferWithdrawalReqeust(
                bankTranId,
                cntrAccountNum,
                dpsPrintContent,
                fintechUseNum,
                fromMoney,
                tranDtime,
                reqClientName,
                fintechUseNum,
                travelerId,
                cntrAccountName,
                cntrBankCode,
                cntrAccountNum
        );

        log.info("request:{}", transferWithdrawalRequest);
        OpenbankLogRequest openbankLogRequest = new OpenbankLogRequest(
                OpenbankRequestType.TRANSFER_WITHDRAW,
                bankTranId,
                transferWithdrawalRequest.toString()
        );
        openbankLogMapper.saveRequest(openbankLogRequest);

        ResponseEntity<String> response = webClient.post()
                .uri("/v2.0/transfer/withdraw/fin_num")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .bodyValue(transferWithdrawalRequest)
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankTransferWithdrawalResponse withdrawalResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankTransferWithdrawalResponse.class);
        log.info("response: {}", withdrawalResponse);
        openbankLogMapper.saveResponse(
                new OpenbankLogResponse(
                        openbankLogRequest.getId(),
                        response.getStatusCode().toString(),
                        withdrawalResponse.getApiTranId(),
                        withdrawalResponse.toString(),
                        withdrawalResponse.getRspCode(),
                        withdrawalResponse.getRspMessage()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", withdrawalResponse.getRspCode(), withdrawalResponse.getRspMessage());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return withdrawalResponse;
    }

    /**
     * 오픈뱅크 계좌 해지
     *
     * @param accessToken
     * @param fintechUseNum
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OpenbankCancelAccountResponse cancelAccount(String accessToken, String fintechUseNum) {
        Map<String, String> body = new HashMap<>();
        String bankTranId = getBankTranId();
        body.put("bank_tran_id", bankTranId);
        body.put("scope", "transfer");
        body.put("fintech_use_num", fintechUseNum);

        log.info("request:{}", body.toString());
        OpenbankLogRequest request = new OpenbankLogRequest(
                OpenbankRequestType.ACCOUNT_CANCEL,
                bankTranId,
                body.toString()
        );
        openbankLogMapper.saveRequest(request);

        ResponseEntity<String> response = webClient
                .post()
                .uri("v2.0/account/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> headers.setBearerAuth(accessToken))
                .body(Mono.just(body), Map.class)
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankCancelAccountResponse cancelAccountResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankCancelAccountResponse.class);
        log.info("response:{}", cancelAccountResponse);
        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                request.getId(),
                response.getStatusCode().toString(),
                cancelAccountResponse.getApiTranId(),
                cancelAccountResponse.toString(),
                cancelAccountResponse.getRspCode(),
                cancelAccountResponse.getRspMessage()
        ));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", cancelAccountResponse.getRspCode(), cancelAccountResponse.getRspMessage());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return cancelAccountResponse;
    }

    /**
     * 오픈뱅킹 계정 삭제 API
     *
     * @param accessToken 오픈뱅킹 엑세스 토큰
     * @param userSeqNo   오픈뱅킹 사용자 일련번호
     * @return
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public OpenbankCloseUserResponse closeUser(String accessToken, String userSeqNo) {
        String userCloseUrl = "/v2.0/user/close";

        Map<String, String> body = new HashMap<>();
        body.put("client_use_code", cashmallowCd);
        body.put("user_seq_no", userSeqNo);

        OpenbankLogRequest request = new OpenbankLogRequest(
                OpenbankRequestType.USER_CLOSE,
                "",
                body.toString()
        );
        openbankLogMapper.saveRequest(request);
        log.info("request:{}", body.toString());

        ResponseEntity<String> response = webClient.post()
                .uri(userCloseUrl)
                .headers(header -> header.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankCloseUserResponse closeUserResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankCloseUserResponse.class);
        log.info("response:{}", closeUserResponse);
        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                request.getId(),
                response.getStatusCode().toString(),
                closeUserResponse.getApiTranId(),
                closeUserResponse.toString(),
                closeUserResponse.getRspCode(),
                closeUserResponse.getRspMessage()
        ));


        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", closeUserResponse.getRspCode(), closeUserResponse.getRspMessage());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return closeUserResponse;
    }


    // 유저 일일 한도 조회
    public OpenbankUserRemainAmtResponse getUserRemainAmtUrl(String openbankToken, String userSeqNo) {
        String userRemainAmtUrl = "/v2.0transfer/user_remain_amt";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("user_seq_no", userSeqNo);

        OpenbankLogRequest openbankLogRequest = new OpenbankLogRequest(
                OpenbankRequestType.USER_REMAIN_AMT,
                "",
                params.toString()
        );
        openbankLogMapper.saveRequest(openbankLogRequest);
        log.debug("userRemainAmt params{}", params);

        log.info("request:{}", params);
        ResponseEntity<String> response = webClient.get().uri(
                        uriBuilder -> uriBuilder
                                .path(userRemainAmtUrl)
                                .queryParams(params)
                                .build()
                ).headers(httpHeaders -> httpHeaders.setBearerAuth(openbankToken))
                .retrieve()
                .toEntity(String.class)
                .block();

        log.info("encrypt Full Response:{}", securityService.encryptAES256(response.getBody()));
        OpenbankUserRemainAmtResponse userRemainAmtResponse = gsonSnakeCase.fromJson(response.getBody(), OpenbankUserRemainAmtResponse.class);
        log.info("response:{}", userRemainAmtResponse);

        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                openbankLogRequest.getId(),
                response.getStatusCode().toString(),
                userRemainAmtResponse.getApi_tran_id(),
                userRemainAmtResponse.toString(),
                userRemainAmtResponse.getRsp_code(),
                userRemainAmtResponse.getRsp_message()));

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("{}:{}", userRemainAmtResponse.getRsp_code(), userRemainAmtResponse.getRsp_message());
            throw new RuntimeException("OPENBANK_SERVER_ERROR");
        }

        return userRemainAmtResponse;
    }


    private String getBankTranId() {
        // 날짜로 레디스 키 생성.
        String yyyyMMdd = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.BASIC_ISO_DATE);
        String redisSetKey = redisService.generateRedisKey("bankTranId", yyyyMMdd, RedisService.REDIS_KEY_OPENBANK);
        redisTemplate.expire(redisSetKey, 1, TimeUnit.DAYS);
        SetOperations<String, String> redisSetOperations = redisTemplate.opsForSet();

        // 겹치는 랜덤 문자열의 경우 재생성.
        String randomString = RandomUtil.generateRandomString(RandomUtil.CAPITAL_ALPHA_NUMERIC, 9);
        while (Boolean.TRUE.equals(redisSetOperations.isMember(redisSetKey, randomString))) {
            randomString = RandomUtil.generateRandomString(RandomUtil.CAPITAL_ALPHA_NUMERIC, 9);
        }
        redisSetOperations.add(redisSetKey, randomString);

        return cashmallowCd + "U" + randomString;
    }

}
