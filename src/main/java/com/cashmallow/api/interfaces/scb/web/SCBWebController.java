package com.cashmallow.api.interfaces.scb.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.UserService;
import com.cashmallow.api.auth.impl.AuthServiceImpl;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.QrResponse;
import com.cashmallow.common.CRC16Util;
import com.cashmallow.common.CustomStringUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import static com.cashmallow.api.config.RedisConfig.SCB_INBOUND_TOPIC;
import static com.cashmallow.api.domain.shared.MsgCode.*;

@Controller
@Slf4j
@RequestMapping(value = "/scb")
@RequiredArgsConstructor
public class SCBWebController {

    private final AuthServiceImpl authService;
    private final Gson gson;
    private final TravelerRepositoryService travelerRepositoryService;
    private final WalletRepositoryService walletRepositoryService;
    private final UserService userService;
    private final LocaleResolver localeResolver;
    private final MessageSource messageSource;
    private final AlarmService alarmService;
    private final MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;


    /**
     * App to API - called by app
     *
     * @param data
     * @param token
     * @param model
     * @return
     * @ref. https://drive.google.com/drive/folders/14f4Gt-kj-sLgLLq-AKlTpM_leH3t2BSs
     * 출금하기 Button
     */
    @GetMapping(value = "/withdraw/request/view")
    public String requestWithdrawView(
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authToken,
            @RequestParam String data,
            @RequestParam(value = "token", required = false) String token,
            Model model,
            HttpServletRequest request) {
        // data는 암호화된 인코딩 데이터x
        String walletId = "";
        String atmLocationId = "-1";
        Long withdrawalPartnerId = -1L;
        String osType = "";
        long price = 0;
        String currency = "THB";

        log.info("authToken : {}", authToken);
        log.info("token : {}", token);

        String newToken = StringUtils.isEmpty(authToken) ? token : authToken;

        Long userId = authService.getUserId(newToken);

        log.info("newToken: {}, 유저ID: {}", newToken, userId);
        if (userId == Const.NO_USER_ID) {
            log.info("Invalid token. token={}", newToken);
            alarmService.e("requestWithdrawView", "requestWithdrawView::Invalid token. token=" + newToken + ", data=" + data);
            return getErrorHTML(request, model, osType);
        }

        try {
            newToken = newToken.replaceFirst("Bearer ", "");
            String jsonStr = CustomStringUtil.decode(newToken, data);

            JSONObject body = new JSONObject(jsonStr);
            log.info("/withdraw/request/view body: {}", gson.toJson(body));
            walletId = body.getString("walletId");
            atmLocationId = body.getString("atmLocationId");
            withdrawalPartnerId = body.getLong("partnerId");
            osType = body.getString("osType");

            // 모든 request 취소
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            mallowlinkWithdrawalService.cancelAllWithdrawal(traveler, Long.valueOf(withdrawalPartnerId));

            price = getWithdrawPrice(walletId);
            log.info("지갑 현재 금액: {}", price);
        } catch (Exception e) {
            log.error("requestWithdrawView() get data & 인출신청 금액 가져오기");
            alarmService.e("requestWithdrawView", "requestWithdrawView::walletId - " + walletId + ", atmLocationId=" + atmLocationId + ", withdrawalPartnerId=" + withdrawalPartnerId + ", osType=" + osType + ", price=" + price + ", userId=" + userId);
            return getErrorHTML(request, model, osType);
        }

        // 1. checking our db in waiting status
        // 2. request cancel scb api
        // scbService.cancel();
        model.addAttribute("string", "Hello, message!!");
        model.addAttribute("walletId", walletId);
        model.addAttribute("token", newToken);
        model.addAttribute("data", data);
        model.addAttribute("atmLocationId", atmLocationId);
        model.addAttribute("partnerId", withdrawalPartnerId);
        model.addAttribute("userId", userId);
        model.addAttribute("osType", osType);
        model.addAttribute("currency", currency);
        model.addAttribute("priceWithComma", NumberFormat.getInstance().format(price));
        model.addAttribute("isAndroid", "ANDROID".equalsIgnoreCase(osType));

        Locale locale = localeResolver.resolveLocale(request);
        userService.addSCBMessage(model, locale);

        return "scb/withdrawal_request";
    }

    /**
     * 인출신청 금액 갖여오기
     *
     * @param walletId
     * @return
     */
    private long getWithdrawPrice(String walletId) {
        //
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(Long.parseLong(walletId));
        BigDecimal money = travelerWallet.geteMoney();
        return money.longValue();
    }

    /**
     * Callback Event Ver.2
     * App to API - called by app
     *
     * @param data
     * @param token
     * @param model
     * @param request
     * @return
     * @ref. https://drive.google.com/drive/folders/14f4Gt-kj-sLgLLq-AKlTpM_leH3t2BSs
     * 출금 완료 요청 after reading QR
     * 출금 대기중..... inbound 들어 올때 까지 대기 후 처리
     */
    @GetMapping(value = "/withdraw/wait/callback")
    public String waitCallbackWithdraw(@RequestParam String data,
                                       @RequestParam(value = "token", required = false) String token,
                                       @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authToken,
                                       Model model,
                                       HttpServletRequest request) {
        String qrCode = "";
        String walletId = "";
        String osType = "";
        Locale locale = localeResolver.resolveLocale(request);

        String newToken = StringUtils.isEmpty(authToken) ? token : authToken;

        Long userId = Const.NO_USER_ID;
        try {
            userId = authService.getUserId(newToken);
            if (userId == Const.NO_USER_ID) {
                log.info("Invalid token. token={}", newToken);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        Long cashoutId = -1L;
        try {
            newToken = newToken.replaceFirst("Bearer ", "");
            String jsonStr = CustomStringUtil.decode(newToken, data);
            JSONObject body = new JSONObject(jsonStr);
            log.info("/withdraw/wait/callback body: {}", gson.toJson(body));
            qrCode = body.getString("qrCode");
            walletId = body.getString("walletId");
            osType = body.getString("osType");
            if (body.has("cashoutId")) {
                cashoutId = body.getLong("cashoutId");
            }

            // QR 코드 SCB 호출 없이 Validation
            if (StringUtils.isEmpty(walletId)
                    || StringUtils.isEmpty(osType)
                    || StringUtils.isEmpty(qrCode)
                    || CRC16Util.isInvalidScbQR(qrCode)) {
                String scbInvalidQrData = messageSource.getMessage(ML_INVALID_QR_CODE, null, "Invalid QR Code", locale);
                alarmService.e("waitCallbackWithdraw", "waitCallbackWithdraw::SCB_INVALID_QR_CODE From Cashmallow - " + walletId + ", qrCode=" + qrCode + ",osType=" + osType + ", userId=" + userId + ", cashoutId=" + cashoutId);

                return getErrorHTML(model, osType, scbInvalidQrData, "1220");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            QrResponse qr = mallowlinkWithdrawalService.qr(userId, Long.parseLong(walletId), qrCode);

            model.addAttribute("success", true);
            model.addAttribute("qrCode", qrCode);
            model.addAttribute("data", data);
            model.addAttribute("token", newToken);
            model.addAttribute("osType", osType);
            model.addAttribute("withdrawalRequestNo", qr.clientTransactionId());
            model.addAttribute("sseType", SCB_INBOUND_TOPIC);
            model.addAttribute("isAndroid", "ANDROID".equalsIgnoreCase(osType));

            String scbWithdrawalCompleted = messageSource.getMessage("SCB_WITHDRAWAL_COMPLETED", null, "Withdrawal Completed", locale);
            model.addAttribute("successMessage", scbWithdrawalCompleted);
            String scbWithdrawalFailed = messageSource.getMessage("SCB_WITHDRAWAL_FAILED", null, "Withdrawal Failed", locale);
            model.addAttribute("failMessage", scbWithdrawalFailed);

            userService.addSCBMessage(model, locale);
        } catch (Exception e) {
            if (StringUtils.equals(e.getMessage(), ML_INVALID_QR_CODE)) {
                String scbInvalidQrData = messageSource.getMessage(ML_INVALID_QR_CODE, null, "Invalid QR Code", locale);
                return getErrorHTML(model, osType, scbInvalidQrData, "1220");
            } else if (StringUtils.equals(e.getMessage(), INVALID_ATM)) {
                String scbInvalidAtm = messageSource.getMessage(INVALID_ATM, null, "Unable withdrawal in VTM machine.<br>Please use ATM/CDM machine.", locale);
                return getErrorHTML(model, osType, scbInvalidAtm, MallowlinkExceptionType.valueOf(INVALID_ATM).getCode());
            }

            log.warn(e.getMessage(), e);
            return getErrorHTML(request, model, osType);
        }

        return "scb/withdrawal_callback";
    }

    private String getErrorHTML(HttpServletRequest request, Model model, String osType) {
        Locale locale = localeResolver.resolveLocale(request);
        final String message = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, locale);
        return getErrorHTML(model, osType, message, "400");
    }

    private String getErrorHTML(Model model, String osType, String message, String code) {
        model.addAttribute("isAndroid", "ANDROID".equalsIgnoreCase(osType));
        model.addAttribute("message", message);
        model.addAttribute("code", code);
        return "scb/error";
    }
}
