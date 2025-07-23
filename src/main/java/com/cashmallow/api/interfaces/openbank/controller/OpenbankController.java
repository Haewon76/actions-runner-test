package com.cashmallow.api.interfaces.openbank.controller;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.openbank.dto.OpenbankAuthResponse;
import com.cashmallow.api.interfaces.openbank.dto.OpenbankUserResponse;
import com.cashmallow.api.interfaces.openbank.dto.TransferExchangeRequest;
import com.cashmallow.api.interfaces.openbank.dto.TransferRemittanceRequest;
import com.cashmallow.api.interfaces.openbank.service.OpenbankServiceImpl;
import com.cashmallow.common.CustomStringUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.cashmallow.api.domain.shared.Const.OPENBANK_DELETED;
import static com.cashmallow.api.domain.shared.Const.OPENBANK_NOT_REGISTERED;


@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("openbank")
public class OpenbankController {

    private final TravelerRepositoryService travelerRepositoryService;
    private final OpenbankServiceImpl openBankService;
    private final AuthService authService;
    private final Gson gson;

    @GetMapping("oauth")
    public String getUserOAuth(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam("device_type") String deviceType,
            @RequestParam("device_ip") String deviceIp,
            @RequestParam("device_id") String deviceId,
            @RequestParam("device_version") String deviceVersion,
            HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO apiResultVO = new ApiResultVO();

        // 토큰으로 userId 받기
        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("Invalid token");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        log.info("유저ID:{}", userId);

        OpenbankAuthResponse userOAuth = openBankService.getUserOAuth(userId, deviceType, deviceIp, deviceId, deviceVersion);

        log.info("Success");
        apiResultVO.setSuccessInfo(userOAuth);
        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @PostMapping("transfer/exchange")
    public String doTransferExchange(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody String requestBody,
            HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            log.info("invalid token. userId={}", userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        String decode = CustomStringUtil.decode(token, requestBody);
        TransferExchangeRequest body = gson.fromJson(decode, TransferExchangeRequest.class);


        ApiResultVO voResult = new ApiResultVO();
        try {
            voResult = openBankService.transferExchange(body.getExchange_id(), traveler, body.getOtp());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        log.info(gson.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping("transfer/remittance")
    public String doTransferRemittance(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestBody String requestBody,
            HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            log.info("invalid token. userId={}", userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        String decode = CustomStringUtil.decode(token, requestBody);
        TransferRemittanceRequest body = gson.fromJson(decode, TransferRemittanceRequest.class);

        ApiResultVO voResult = new ApiResultVO();
        try {
            voResult = openBankService.transferRemittance(body.getRemittanceId(), traveler, body.getOtp());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        log.info(gson.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping("me")
    public String getOpenbankUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                  HttpServletRequest request, HttpServletResponse response) throws Exception {
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("Invalid token");
            voResult.setCode(Const.CODE_INVALID_TOKEN);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        OpenbankUserResponse openbankUser = openBankService.getOpenbankUser(userId);

        if (openbankUser == null) {
            // 한번도 등록하지 않는 경우
            voResult.setStatus(OPENBANK_NOT_REGISTERED);
        } else if (openbankUser.isDeleted()) {
            // 계정을 삭제한 경우
            voResult.setStatus(OPENBANK_DELETED);
        } else {
            // 정상 케이스
            voResult.setSuccessInfo(openbankUser);
        }

        log.info(gson.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @DeleteMapping("me/account")
    public String cancelOpenbankAccount(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                        @RequestParam String otp,
                                        HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("Invalid token");
            voResult.setCode(Const.CODE_INVALID_TOKEN);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            log.error("traveler 정보가 없습니다. userId={}", userId);
            voResult.setCode(Const.CODE_INVALID_TOKEN);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        try {
            boolean cancelAccount = openBankService.cancelAccount(traveler, otp);
            log.info("cancelAccount={}", cancelAccount);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo("INTERNAL_SERVER_ERROR");
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 0시 UTC
    private void reissueExpiredToken() {
        log.debug("start");
        openBankService.ReissueExpiredToken();
    }
}
