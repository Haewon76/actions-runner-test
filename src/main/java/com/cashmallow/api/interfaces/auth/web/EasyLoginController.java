package com.cashmallow.api.interfaces.auth.web;

import com.cashmallow.api.application.EasyLoginService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.common.CustomStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Handles requests for EasyLogin.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(value = "/easyLogin")
public class EasyLoginController {

    private final AuthService authService;

    private final LocaleResolver localeResolver;

    private final EasyLoginService easyLoginService;

    private final MessageSource messageSource;

    /**
     * token validation
     *
     * @param refreshToken
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/validToken")
    @ResponseBody
    public String validToken(@RequestHeader("Authorization") String refreshToken,
                             @RequestBody String requestBody,
                             HttpServletRequest request, HttpServletResponse response) {
        String oriRefreshToken = refreshToken;
        String result = null;
        String method = "EasyLogin validToken()";
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);


        long userId = authService.getUserId(refreshToken);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token.", method);
            return CustomStringUtil.encryptJsonString(refreshToken, resultVO, response);
        }

        if (refreshToken.startsWith(AuthService.TOKEN_PREFIX)) {
            refreshToken = refreshToken.replaceFirst(AuthService.TOKEN_PREFIX, "").trim();
        }

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String jsonStr = CustomStringUtil.decode(oriRefreshToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);

            resultVO = easyLoginService.validToken(refreshToken, userId, body, locale);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_VALIDATION", null, "PIN CODE verification failed.", locale));
        }

        result = CustomStringUtil.encryptJsonString(oriRefreshToken, resultVO, response);
        log.info("{}: result={}", method, result);

        return result;
    }

    /**
     * 간편 로그인 등록
     *
     * @param accessToken
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registration(@RequestHeader("Authorization") String accessToken,
                               @RequestBody String requestBody,
                               HttpServletRequest request, HttpServletResponse response) {

        String method = "EasyLogin registration()";
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        long userId = authService.getUserId(accessToken);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token.", method);
            return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
        }

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String jsonStr = CustomStringUtil.decode(accessToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);

            resultVO = easyLoginService.addEasyLogin(accessToken, userId, body.getString("pinCode"), body.getString("pinCodeRe"), locale);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_REGISTERED_FAILED", null, "You have registered for Easy Login.", locale));
        }

        return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
    }


    /**
     * 핀코드 Validation Api
     *
     * @param accessToken
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/pinCodeValidation")
    @ResponseBody
    public String pinCodeValidation(@RequestHeader("Authorization") String accessToken,
                                    @RequestBody String requestBody,
                                    HttpServletRequest request, HttpServletResponse response) {

        String method = "EasyLogin pinCodeValidation()";
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        long userId = authService.getUserId(accessToken);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token.", method);
            return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
        }

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String jsonStr = CustomStringUtil.decode(accessToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);

            resultVO = easyLoginService.pinCodeValidation(userId, body.getString("pinCode"), locale);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_VALIDATION", null, "PIN CODE verification failed.", locale));
        }

        return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
    }


    @PostMapping(value = "/confirm", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String confirm(@RequestHeader("Authorization") String refreshToken,
                          @RequestBody String requestBody,
                          HttpServletRequest request, HttpServletResponse response) {

        String oriRefreshToken = refreshToken;
        String method = "EasyLogin confirm()";
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);


        if (refreshToken.startsWith(AuthService.TOKEN_PREFIX)) {
            refreshToken = refreshToken.replaceFirst(AuthService.TOKEN_PREFIX, "").trim();
        }


        Locale locale = localeResolver.resolveLocale(request);

        try {
            String jsonStr = CustomStringUtil.decode(oriRefreshToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);

            resultVO = easyLoginService.confirm(refreshToken, body, request);
        } catch (CashmallowException e) {
            log.info("{}: confirm(): EAZY_LOGIN_FAIL  !!!!!", method);
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL", null, "Easy Login Failed.", locale));
        }

        return CustomStringUtil.encryptJsonString(oriRefreshToken, resultVO, response);
    }

    @PostMapping(value = "/checkPincodeMatch")
    @ResponseBody
    public String checkPincodeMatch(@RequestHeader("Authorization") String refreshToken,
                                    @RequestBody String requestBody,
                                    HttpServletRequest request, HttpServletResponse response) {

        String oriRefreshToken = refreshToken;
        String method = "EasyLogin checkPincodeMatch()";
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        long userId = authService.getUserId(refreshToken);
        if (userId == Const.NO_USER_ID) {
            log.info("{}: Invalid token.", method);
            return CustomStringUtil.encryptJsonString(refreshToken, resultVO, response);
        }

        if (refreshToken.startsWith(AuthService.TOKEN_PREFIX)) {
            refreshToken = refreshToken.replaceFirst(AuthService.TOKEN_PREFIX, "").trim();
        }

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String jsonStr = CustomStringUtil.decode(oriRefreshToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);

            resultVO = easyLoginService.checkPincodeMatch(refreshToken, body, locale);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_VALIDATION", null, "PIN CODE verification failed.", locale));
        }

        return CustomStringUtil.encryptJsonString(oriRefreshToken, resultVO, response);
    }
}
