package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.TravelerKrServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.hyphen.HyphenServiceImpl;
import com.cashmallow.api.interfaces.hyphen.dto.BankAccountInfoVo;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerKrRequest;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/kr/travelers")
public class TravelerKrController {
    private static final String TRAVELER_CANNOT_FIND_BY_USER_ID = "TRAVELER_CANNOT_FIND_BY_USER_ID";

    private final AuthService authService;
    private final TravelerKrServiceImpl travelerKrService;
    private final TravelerServiceImpl travelerService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final AlarmService alarmService;
    private final MessageSource messageSource;
    private final HyphenServiceImpl hyphenService;
    private final LocaleResolver localeResolver;

    private final EnvUtil envUtil;
    private final Validator validator;
    private final Gson gson;


    @PostMapping(value = "", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registerTravelerKr(@RequestHeader("Authorization") String token,
                                     @RequestBody String requestBody,
                                     HttpServletRequest request, HttpServletResponse response) throws CashmallowException {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.debug("decodeBody={}", jsonStr);

        if (envUtil.isDev()) {
            alarmService.i("registerTraveler", "userId: " + userId + ", " + jsonStr);
        }

        // body mapping & validation
        RegisterTravelerKrRequest registerRequest = gson.fromJson(jsonStr, RegisterTravelerKrRequest.class);
        log.info("registerRequest={}", registerRequest.toString());
        List<String> errorString = validRegisterTravelerKrRequest(registerRequest);
        if (errorString != null) {
            voResult.setFailInfo(errorString.toString());
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        try {
            Traveler resultTraveler = travelerKrService.registerTravelerKrV3(userId, registerRequest);
            voResult.setSuccessInfo(resultTraveler);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        log.debug("JsonStr.toJson(voResult):{}", JsonStr.toJson(voResult));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * 계좌 인증 요청(1원 인증 1/2)
     *
     * @param token
     * @param travelerId
     * @param requestBody encrypted {bank_code, bank_name}
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/{travelerId}/bank-account/request", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestBankAccount1(@RequestHeader("Authorization") String token,
                                      @PathVariable Long travelerId,
                                      @RequestBody String requestBody,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "requestBankAccount()";
        String body = CustomStringUtil.decode(token, requestBody);

        if (StringUtils.isEmpty(body)) {
            log.info("{}: Invalid token, Encryption errors", method);
            ApiResultVO apiResultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);
            apiResultVO.setFailInfo("Encryption errors");
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO voResult = new ApiResultVO();

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        if (traveler == null) {
            log.info("{}: Traveler id null", method);
            String message = messageSource.getMessage(TRAVELER_CANNOT_FIND_BY_USER_ID, null,
                    "Passport information is not registered.", locale);
            voResult.setFailInfo(message);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (traveler.getUserId() != userId) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject jo = new JSONObject(body);
        String bankCode = jo.getString("bank_code");
        String bankName = jo.getString("bank_name"); // 은행 이름
        String accountNo = jo.getString("account_no").replaceAll("[^0-9]", "");
        log.debug("requestBody={}", jo);

        // 계좌주 이름과 여행자 이름 일치여부
        String accountName = traveler.getLocalLastName() + traveler.getLocalFirstName();
        if (envUtil.isPrd()) {
            accountName = hyphenService.checkFCS(travelerId, bankCode, accountNo);
        }

        // 1원 이체
        BankAccountInfoVo bankAccountInfoVo = new BankAccountInfoVo(bankCode, bankName, accountNo, accountName);
        ApiResultVO apiResultVO;
        apiResultVO = hyphenService.checkAccount1(travelerId, bankAccountInfoVo);

        log.info("계좌 인증 1/2 {}, userId:{}", apiResultVO.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    /**
     * 계좌 인증 요청(1원 인증 2/2)
     *
     * @param token
     * @param travelerId
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/{travelerId}/bank-account/verify", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestBankAccount2(@RequestHeader("Authorization") String token,
                                      @PathVariable String travelerId,
                                      @RequestBody String requestBody,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "requestBankAccount()";
        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO voResult = new ApiResultVO();

        String body = CustomStringUtil.decode(token, requestBody);
        log.debug("decrypted body:{}", body);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(Long.valueOf(travelerId));
        if (traveler == null) {
            log.info("{}: Traveler id null", method);
            String message = messageSource.getMessage(TRAVELER_CANNOT_FIND_BY_USER_ID, null,
                    "Passport information is not registered.", locale);
            voResult.setFailInfo(message);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (traveler.getUserId() != userId) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject jo = new JSONObject(body);
        String oriSeqNo = jo.getString("oriSeqNo");
        String inPrintContent = jo.getString("inPrintContent");

        // 하이픈
        ApiResultVO apiResultVO = hyphenService.registerBankAccount(Long.parseLong(travelerId), oriSeqNo, inPrintContent);
        log.info("계좌인증 2/2 {}, userId:{}", apiResultVO.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    public List<String> validRegisterTravelerKrRequest(RegisterTravelerKrRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);

        if (errors.hasErrors()) {
            List<FieldError> fieldErrors = errors.getFieldErrors();
            fieldErrors.forEach(e -> log.info("{}: {}", e.getField(), e.getDefaultMessage()));

            return fieldErrors
                    .stream()
                    .map(e -> e.getField() + ":" + e.getDefaultMessage())
                    .toList();
        }
        return null;
    }

}
