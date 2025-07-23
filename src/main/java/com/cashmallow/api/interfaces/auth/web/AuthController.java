package com.cashmallow.api.interfaces.auth.web;

import com.cashmallow.api.application.AdminService;
import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.impl.CaptchaServiceImpl;
import com.cashmallow.api.application.impl.InactiveUserServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.application.impl.UserServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserAgreeTerms;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.OtpService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.CMDEF;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cashmallow.api.application.impl.TravelerServiceImpl.TRAVELER_LOGIN_DEVICE_RESET;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.common.CommonUtil.getDeviceType;


/**
 * Handles requests for OAuth.
 */
@RequestMapping("/auth")
@Controller
@SuppressWarnings({"unchecked", "deprecation"})
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private InactiveUserServiceImpl inactiveUserService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private OtpService otpService;

    @Autowired
    private CaptchaServiceImpl captchaService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private EnvUtil envUtil;

    @PostMapping(value = {"/deviceResetCodeValid"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deviceResetCode(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                  @RequestBody String requestBody,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setFailInfo(messageSource.getMessage("TRAVELER_RESET_NEW_DEVICE_FAIL", null, "Invalid verification code", locale));


        if (!authService.isHexaStr(token)) {
            logger.info("{}: NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        try {
            String jsonStr = CustomStringUtil.decode(token, requestBody);

            JSONObject body = new JSONObject(jsonStr);

            String code = body.getString("code");
            String tokenId = body.getString("tokenId");
            String fcmToken = "";
            try {
                fcmToken = body.getString("fcmToken");
            } catch (Exception ignored) {
            }

            logger.info("code={}, tokenId={}, fcmToken={}", code, tokenId, fcmToken);

            // dev 디버깅용 알람
            if (envUtil.isDev()) {
                alarmService.i("deviceResetCode", "code=" + code + ", tokenId=" + tokenId + ", fcmToken=" + fcmToken);
            }

            if (travelerService.isDeviceResetCodeValid(tokenId, code, fcmToken, locale, getDeviceType())) {
                resultVO.setSuccessInfo();
                logger.debug("deviceResetCode(): resultVO={}", resultVO);
                return CustomStringUtil.encryptJsonString(token, resultVO, response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }


    /**
     * login with ID and password and create tokens(access token, refresh token)
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = {"/login", "/admin/login", "/admin/login/otp"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String login(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                        @RequestBody String requestBody,
                        HttpServletRequest request, HttpServletResponse response) {

        String method = "login()";
        String result = null;
        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (!authService.isHexaStr(token)) {
            logger.info("{}: NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        try {
            String languageHeader = request.getHeader("Accept-Language");

            logger.info("loginLocale={}, languageHeader={}", locale, languageHeader);

            if (ObjectUtils.isEmpty(locale)) {
                //
                locale = new Locale(languageHeader.substring(0, 1));
                logger.info("second loginLocale={}, languageHeader={}", locale, languageHeader);
            }

            String jsonStr = CustomStringUtil.decode(token, requestBody);

            JSONObject body = new JSONObject(jsonStr);

            String loginId = body.getString("username");
            String password = body.getString("password");
            String instanceId = body.getString("instance_id");  // 기기 S/N, UUID

            String remoteInstanceId = null;
            if (body.has("remote_instance_id")) {
                remoteInstanceId = body.getString("remote_instance_id"); // 기존 instanceId(FCM token)
            }

            String deviceInfo = body.getString("device_info");
            String ip = body.getString("ip");
            String cls = body.getString("cls");

            loginId = loginId.replaceAll("[^A-Za-z0-9]", "");

            String deviceType = null;
            if (body.has("device_type")) {
                deviceType = body.getString("device_type");
            }

            String appbuild = null;
            if (body.has("appbuild")) {
                appbuild = body.getString("appbuild");
            }

            String deviceOsVersion = null;
            if (body.has("device_os_version")) {
                deviceOsVersion = body.getString("device_os_version");
            }

            logger.info("{}: loginId={}, instanceId={}, deviceInfo={}, ip={}, cls={}, deviceType={}, appbuild={}, deviceOSVersion={}",
                    method, loginId, instanceId, deviceInfo, ip, cls, deviceType, appbuild, deviceOsVersion);

            String refreshToken = null;
            String sessionTimeout = Const.SESSION_TIMEOUT_10_MIN; // milliseconds

            switch (cls) {
                case Const.CLS_TRAVELER:
                    refreshToken = travelerService.travelerLogin(loginId, password, instanceId, deviceInfo, ip, locale, deviceType, appbuild, deviceOsVersion, remoteInstanceId);
                    break;

                case Const.CLS_ADMIN:
                    refreshToken = adminService.loginAdmin(loginId, password, instanceId, deviceInfo, ip, locale);
                    sessionTimeout = "1800000"; // 30 minutes
                    break;

                default:
                    break;
            }

            String accessToken = authService.issueAccessToken(refreshToken);

            String notification = "";

            // Recover a dormant user
            UserAgreeTerms user = userRepositoryService.getUserAgreeTermsByLoginId(loginId);
            if (Boolean.FALSE.equals(user.getActivated())) {
                InactiveUser inaUser = inactiveUserService.getInactiveUser(user.getUserId());
                if (inaUser.getInactiveType().equals(InactiveUser.InactiveType.DOR)) {
                    inactiveUserService.activateUser(user.getUserId(), user.getUserId());
                    notification = messageSource.getMessage("USER_LOGIN_RECOVERED_DORMANT_USER", null, "", locale);

                } else {
                    // prevent a deleted user to login
                    refreshToken = null;
                }
            }


            if (StringUtils.isEmpty(refreshToken)) {
                resultVO.setResult(Const.CODE_LOGIN_FAILURE, Const.STATUS_LOGIN_FAILURE, Const.MSG_LOGIN_FAILURE);

            } else {
                logger.debug("{}: accessToken={}, loginId={}", method, accessToken, loginId);

                Jws<Claims> jws = authService.parseJWT(accessToken);

                Claims claims = jws.getBody();
                String expiresIn = String.valueOf(claims.getExpiration().getTime());

                Map<String, String> obj = new HashMap<>();
                obj.put("access_token", accessToken);
                obj.put("token_type", "bearer");
                obj.put("expires_in", expiresIn);
                obj.put("refresh_token", refreshToken);
                obj.put("session_timeout", sessionTimeout);
                obj.put("notification", notification);

                // TODO: delete following 2 lines after deploy new APPs. after 2020-01-01
                resultVO.setSuccessInfo(refreshToken);
                CMDEF.storeTokenIntoSession(request, resultVO);

                // TODO: delete following a line if you use 2 servers. It needs check and test.
                CMDEF.storeTokenIntoSession(request, accessToken);

                List<TermsHistoryVO> termsReAgreement = userService.getUnreadTermsList(user.getUserId(), user.getCountryCode(), locale);
                if (Const.CLS_TRAVELER.equals(cls) && !termsReAgreement.isEmpty()) {
                    obj.put("status", Const.SHOW_THE_TERMS);
                    obj.put("title", getMessageByLocale("UNREAD_TO_TERMS_OF_SERVICE", locale));
                    obj.put("message", getMessageByLocale("UNREAD_TO_TERMS_OF_SERVICE_BODY", locale));
                    obj.put("iso3166", user.getIso3166());
                    resultVO.setResult(Const.CODE_SUCCESS, Const.SHOW_THE_TERMS, null, obj);
                } else {
                    obj.put("status", Const.STATUS_SUCCESS);
                    resultVO.setSuccessInfo(obj);
                }

                // OTP 인증에 대한 처리
                if (request.getRequestURI().contains("otp") && body.has("otp")) {
                    final boolean validOtp = otpService.isValidOtp(loginId, body.getString("otp"));
                    if (!validOtp) {
                        resultVO.setFailInfo("OTP 인증 실패");
                    }
                }
            }

            logger.info("{}: appbuild={}", method, appbuild);
            CMDEF.setValueIntoSession(request, "appbuild", appbuild);


        } catch (CashmallowException e) {
            if (INTERNAL_SERVER_ERROR.equals(e.getMessage())) {
                logger.error(e.getMessage(), e);
            } else {
                logger.warn(e.getMessage());
            }

            // error message localization
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);

            // 기기 변경시 오류 코드 처리
            if (TRAVELER_LOGIN_DEVICE_RESET.equalsIgnoreCase(e.getMessage()) && CommonUtil.isValidPinCodeDeviceReset()) {
                resultVO.setResult(
                        Const.CODE_FAILURE,
                        e.getMessage(),
                        errMsg,
                        travelerService.getResetDeviceEmailToken(Long.parseLong(e.getOption()))
                );
            } else {
                resultVO.setFailInfo(errMsg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);
        }

        result = CustomStringUtil.encryptJsonString(token, resultVO, response);

        logger.debug("{}: result={}", method, result);

        return result;
    }

    /**
     * issue access token
     *
     * @param token    "Bearer <refresh token>"
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/access-token", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String issueAccessToken(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        logger.debug("issueAccessToken(): token={}", token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Locale locale = localeResolver.resolveLocale(request);

        Long userId = authService.getUserId(token);

        if (userId != Const.NO_USER_ID) {
            String refreshToken = token;
            if (refreshToken.startsWith(AuthService.TOKEN_PREFIX)) {
                refreshToken = refreshToken.replaceFirst(AuthService.TOKEN_PREFIX, "").trim();
            }

            String accessToken = authService.issueAccessToken(refreshToken);

            if (!StringUtils.isEmpty(accessToken)) {

                CMDEF.storeTokenIntoSession(request, accessToken);

                Map<String, String> obj = new HashMap<>();
                obj.put("access_token", accessToken);
                resultVO.setSuccessInfo(obj);
            }
        }

        // error message localization
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        String result = CustomStringUtil.encryptJsonString(token, resultVO, response);

        logger.debug("issueAccessToken(): result={}", result);

        return result;
    }

    /**
     * Delete refresh token
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @DeleteMapping(value = "/refresh-token", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deleteRefreshToken(@RequestHeader("Authorization") String token,
                                     HttpServletRequest request, HttpServletResponse response) {

        String method = "deleteRefreshToken()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        if (userId == Const.NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        authService.deleteRefreshToken(userId);
        resultVO.setSuccessInfo();

        logger.info("{}: resultVO={}", method, resultVO);

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * 사용자 패스워드 리셋 후 메일 발송
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = {"/admin/password-reset"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String resetAdminPassword(@RequestHeader("Authorization") String token,
                                     @RequestBody String requestBody,
                                     HttpServletRequest request, HttpServletResponse response) {
        String method = "resetAdminPassword()";

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (!authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String email = (String) map.get("email");

        logger.info("{}: email={}", method, email);

        try {
            userService.passwordResetAndSendEmailForAdmin(email);
            resultVO.setSuccessInfo("");
        } catch (CashmallowException e) {
            if (INTERNAL_SERVER_ERROR.equals(e.getMessage())) {
                logger.error(e.getMessage(), e);
            } else {
                logger.warn(e.getMessage());
            }

            Locale locale = localeResolver.resolveLocale(request);
            String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            resultVO.setFailInfo(message);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * 사용자 패스워드 리셋 후 메일 발송
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = {"/password-reset"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String resetPassword(@RequestHeader("Authorization") String token,
                                @RequestBody String requestBody,
                                HttpServletRequest request, HttpServletResponse response) {

        String method = "resetPassword()";

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (!authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String email = (String) map.get("email");

        logger.info("{}: email={}", method, email);

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String result = userService.passwordResetAndSendEmail(email, locale);
            resultVO.setSuccessInfo(result);
        } catch (CashmallowException e) {
            if (INTERNAL_SERVER_ERROR.equals(e.getMessage())) {
                logger.error(e.getMessage(), e);
            } else {
                logger.warn(e.getMessage());
            }

            String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            resultVO.setFailInfo(message);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping(value = {"/email/verify"}, produces = GlobalConst.PRODUCES)
    public String resetPasswordValidation(final @RequestParam String token,
                                          final @RequestParam(required = false) String captcha,
                                          final HttpServletRequest request,
                                          final HttpServletResponse response,
                                          final Model model) {

        boolean robot = captchaService.isRobot(captcha);
        final EmailTokenVerity emailTokenVerity = userService.passwordResetAndVerity(token);
        if (robot || emailTokenVerity == null) {
            logger.debug("robot={}, emailTokenVerity={}", robot, emailTokenVerity);
            return CommonUtil.getTemplateByCountry(CountryCode.HK, "reset_password_request_invalid");
        }

        model.addAttribute("newPassword", emailTokenVerity.getPassword());

        final User user = userRepositoryService.getUserByUserId(emailTokenVerity.getUserId());
        return CommonUtil.getTemplateByCountry(user.getCountryCode(), "reset_password");
    }

    @GetMapping(value = "/email/verify-captcha", produces = GlobalConst.PRODUCES)
    public String resetPasswordCaptcha(final @RequestParam String token,
                                       final HttpServletRequest request,
                                       final HttpServletResponse response,
                                       final Model model) {


        logger.info("captchaPasswordReset(): token={}", token);
        final EmailTokenVerity emailTokenVerity = notificationService.getVerifiedEmailPassword(token);
        if (emailTokenVerity == null) {
            return CommonUtil.getTemplateByCountry(CountryCode.HK, "reset_password_request_invalid");
        }

        logger.info("captchaPasswordReset(): userId={}, emailTokenVerity={}", emailTokenVerity.getUserId(), emailTokenVerity);
        final User user = userRepositoryService.getUserByUserId(emailTokenVerity.getUserId());

        String url = String.format("%s%s?token=%s", request.getContextPath(), "/auth/email/verify", token);
        String resetPassword = messageSource.getMessage("RESET_PASSWORD_TEXT", null, user.getCountryLocale());
        model.addAttribute("resetURL", url);
        model.addAttribute("locale", user.getCountryLocale().getLanguage());
        model.addAttribute("resetPassword", resetPassword);

        return "captcha/reset_password_captcha";
    }

    // 기능: 10.2.1. e-mail 중복체크
    @GetMapping(value = "/unused-email", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String isUnusedEmail(@RequestHeader("Authorization") String token,
                                @RequestParam String email,
                                HttpServletRequest request, HttpServletResponse response) {

        String method = "[GET]isUnusedEmail()";

        if (!authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: email={}", method, email);

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        Map<String, String> obj = new HashMap<>();

        String loginId = email.replaceAll("[^A-Za-z0-9]", "");
        User user = userRepositoryService.getUserByLoginId(loginId);
        if (user != null) {
            obj.put("email", email);
            voResult.setSuccessInfo(obj);
        } else {
            voResult.setSuccessInfo();
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * @param token
     * @param email
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/cert-num-email", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String sendCertNumEmailToAuth(@RequestHeader("Authorization") String token,
                                         @RequestParam String email,
                                         @RequestParam(value = "country_code", required = false) String countryCode,
                                         HttpServletRequest request, HttpServletResponse response) {

        String method = "sendCertNumEmailToAuth()";

        if (!authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: email={}, countryCode={}", method, email, countryCode);

        ApiResultVO resultVO = new ApiResultVO();
        try {
            CountryCode country = CountryCode.HK;
            if (!StringUtils.isEmpty(countryCode)) {
                country = CountryCode.of(countryCode);
            }

            String value = notificationService.sendEmailToAuth(email, country);
            Map<String, String> obj = new HashMap<>();
            obj.put("cert-num", value);
            resultVO.setSuccessInfo(obj);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * 이메일 인증 코드 확인
     * validation check
     *
     * @param token
     * @param email
     * @param code
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/cert-num-email/valid", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String validateEmailCertNum(@RequestHeader("Authorization") String token,
                                       @RequestParam String email,
                                       @RequestParam String code,
                                       HttpServletRequest request, HttpServletResponse response) {

        String method = "validateEmailCertNum()";

        if (!authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: code={}", method, code);

        Locale locale = localeResolver.resolveLocale(request);

        ApiResultVO resultVO = new ApiResultVO();
        try {
            // validate email with code
            userService.validatesEmailCertNum(email, code, locale);
            resultVO.setSuccessInfo();
        } catch (CashmallowException e) {
            if (StringUtils.isNotBlank(code)
                    && org.apache.commons.lang3.StringUtils.equals(e.getMessage(), "MAIL_SUBJECT_EMAIL_AUTH_FAIL")) {
                logger.warn(e.getMessage());
            } else {
                logger.error(e.getMessage(), e);
            }

            String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            resultVO.setFailInfo(message);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    private String getMessageByLocale(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
}
