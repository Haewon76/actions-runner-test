package com.cashmallow.api.interfaces.scb.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.MLSCBWebhookService;
import com.cashmallow.api.auth.impl.AuthServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.cashout.WithdrawalLogRepository;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.WithdrawalResponse;
import com.cashmallow.api.interfaces.scb.model.dto.CashoutResponse;
import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import com.cashmallow.api.interfaces.scb.model.dto.request.RequestDto;
import com.cashmallow.api.interfaces.scb.service.RedisPubService;
import com.cashmallow.api.interfaces.scb.service.WebNotificationService;
import com.cashmallow.common.CRC16Util;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.ML_INVALID_QR_CODE;

@RestController
@Slf4j
@RequestMapping(value = "/scb")
@RequiredArgsConstructor
public class SCBController {

    public static final String X_API_KEY = "x-api-key";
    public static final String CORRELATION_ID = "correlationid";

    private final AuthServiceImpl authService;
    private final RedisPubService pubService;
    private final Gson gson;
    private final WebNotificationService webNotificationService;
    private final LocaleResolver localeResolver;
    private final MessageSource messageSource;

    private final CashoutRepositoryService cashoutRepositoryService;
    private final EnvUtil envUtil;
    private final AlarmService alarmService;
    private final RedisService redisService;
    private final MLSCBWebhookService mlscbWebhookService;

    private final MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;
    private final WithdrawalLogRepository withdrawalLogRepository;

    @Value("${scb.apiKey}")
    public String API_KEY;

    /**
     * 인출 신청
     * Web to API - called by webview
     *
     * @param requestDto
     * @param request
     * @return
     */
    @PostMapping(value = "/withdrawal/cashouts")
    public ResponseEntity<CashoutResponse> requestCashOutV2(@RequestBody RequestDto requestDto, HttpServletRequest request) throws CashmallowException {
        Long walletId = requestDto.getWalletId();
        long withdrawalPartnerId = requestDto.getWithdrawalPartnerId();
        Integer partnerId = requestDto.getWithdrawalAgencyId();
        Locale locale = localeResolver.resolveLocale(request);

        ResponseEntity<CashoutResponse> responseEntity;
        final String walletKey = RedisService.REDIS_KEY_TRAVELER_WALLET + walletId;
        try {
            // 키가 레디스에 있으면 중복 요청
            if (!redisService.putIfAbsent(walletKey, "wallet reserve", 5, TimeUnit.SECONDS)) {
                String message = messageSource.getMessage(MsgCode.CASHOUT_IN_PROGRESS, null, "There is an error with the server. Please try again later.", locale);
                CashoutResponse cashoutResponse = new CashoutResponse();
                cashoutResponse.setCode(400);
                cashoutResponse.setErrorMessage(message);
                return ResponseEntity.badRequest().body(cashoutResponse);
            }

            log.info("requestDto:{}", gson.toJson(requestDto));

            WithdrawalResponse withdrawalResponse = mallowlinkWithdrawalService.requestCashOut(requestDto.getUserId(), withdrawalPartnerId, walletId, partnerId);
            CashoutResponse cashoutResponse = new CashoutResponse();

            cashoutResponse.setCashoutId(withdrawalResponse.cashoutId());
            return ResponseEntity.ok().body(cashoutResponse);

        } catch (CashmallowException e) {
            String errorMsg = "유저ID: " + requestDto.getUserId() + ", walletId: " + walletId +
                    "\nError: " + e.getMessage();
            log.warn("예외 발생:{}", errorMsg);
            alarmService.i("SCB 인출 신청 에러", errorMsg);

            String message = messageSource.getMessage(e.getMessage(), null, "There is an error with the server. Please try again later.", locale);
            CashoutResponse cashoutResponse = new CashoutResponse();
            cashoutResponse.setCode(400);
            cashoutResponse.setErrorMessage(message);
            responseEntity = ResponseEntity.badRequest().body(cashoutResponse);
        }

        return responseEntity;
    }

    /**
     * SSE 웹페이지 갱신을 위한 구독 처리
     *
     * @param withdrawalRequestNo
     * @return
     */
    @GetMapping(value = "/withdraw/inbound/subscribe/{withdrawalRequestNo}", produces = "text/event-stream")
    public SseEmitter inboundSubscribe(@PathVariable String withdrawalRequestNo) {
        log.debug("withdrawalRequestNo:{}", withdrawalRequestNo);
        return webNotificationService.subscribe(withdrawalRequestNo);
    }

    /**
     * SSE 연결완료 처리
     *
     * @param withdrawalRequestNo
     * @return
     */
    @PostMapping(value = "/withdraw/inbound/connect/{withdrawalRequestNo}")
    public ResponseEntity<InboundMessage> inboundConnect(@PathVariable String withdrawalRequestNo) {
        log.debug("withdrawalRequestNo:{}", withdrawalRequestNo);
        withdrawalLogRepository.updateConnectionConfirm(withdrawalRequestNo);
        return new ResponseEntity<>(InboundMessage.builder().code(333).build(), HttpStatus.OK);
    }

    /**
     * 인출 성공여부 확인
     *
     * @param withdrawalRequestNo
     * @return
     */
    @PostMapping(value = "/withdraw/inbound/isComplete/{withdrawalRequestNo}")
    public boolean inboundIsComplete(@PathVariable String withdrawalRequestNo) throws CashmallowException {
        log.info("withdrawalRequestNo:{}", withdrawalRequestNo);
        CashOut cashOut = cashoutRepositoryService.getCashOutByCasmTxnId(withdrawalRequestNo)
                .orElseThrow(() -> new CashmallowException(INTERNAL_SERVER_ERROR));
        return CashOut.CoStatus.CF.equals(CashOut.CoStatus.valueOf(cashOut.getCoStatus()));
    }


    /**
     * QR코드 Validation
     *
     * @param withdrawalRequestNo
     * @return
     */
    @PostMapping(value = "/qr")
    public String qrCodeValidation(@RequestHeader("Authorization") String token,
                                   @RequestBody String requestBody,
                                   HttpServletResponse response,
                                   HttpServletRequest request) {

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_PARAMS);
        Long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            resultVO.setFailInfo(MSG_INVALID_TOKEN);
            log.info("MSG_INVALID_TOKEN: {}", resultVO);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        JSONObject body = new JSONObject(jsonStr);

        String qrCode = body.getString("qrCode");

        // QR 코드 SCB 호출 없이 Validation
        if (CRC16Util.isValidScbQR(qrCode)) {
            resultVO.setSuccessInfo();
        } else {
            Locale locale = localeResolver.resolveLocale(request);
            String scbInvalidQrData = messageSource.getMessage(ML_INVALID_QR_CODE, null, "Invalid QR Code", locale);
            resultVO.setFailInfo(scbInvalidQrData);
            resultVO.setCode("1220");
        }

        log.info("qrCodeValidation: {}", gson.toJson(resultVO));
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }
}
