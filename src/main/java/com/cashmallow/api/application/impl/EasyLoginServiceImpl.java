package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.EasyLoginService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.UserService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.user.*;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.CMDEF;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.RandomUtil;
import com.cashmallow.common.RegularExpressionUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.cashmallow.api.domain.shared.Const.LOGIN_PASSWORD_MISMATCHES_5;
import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_PINCODE;

@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"unused", "deprecation"})
public class EasyLoginServiceImpl implements EasyLoginService {

    private final EasyLoginMapper easyLoginMapper;

    private final MessageSource messageSource;

    private final SecurityService securityService;

    private final UserService userService;

    private final UserRepositoryService userRepositoryService;

    private final RefreshTokenMapper refreshTokenMapper;

    private final AuthService authService;

    private final EasyLoginHistMapper easyLoginHistMapper;

    private final TravelerServiceImpl travelerService;

    private final InactiveUserServiceImpl inactiveUserService;

    private final LocaleResolver localeResolver;

    private final RedisService redisService;

    /**
     * 핀코드 validation
     *
     * @param userId
     * @param pinCode
     * @param locale
     * @return
     * @throws CashmallowException
     */
    @Override
    public ApiResultVO pinCodeValidation(Long userId, String pinCode, Locale locale) throws CashmallowException {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        // 연속된 숫자 체크. 123, 234 등등
        if (RegularExpressionUtil.sequenceNumbers(pinCode)) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_SEQUENCE_NUMBERS", null, "You can't write consecutive numbers like \"123\".", locale));
            return resultVO;
        }
        // 숫자 빈도수 체크 3개 이상일 경우
        if (RegularExpressionUtil.frequencyNumber(pinCode, 3)) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_SAME_NUMBERS", null, "You can't write the same number as \"111\".", locale));
            return resultVO;
        }
        // 생년월일, 핸드폰 번호에 매칭 된다면
        User user = userRepositoryService.getUserByUserId(userId);

        if (RegularExpressionUtil.isPinCodeContainsPhoneNumber(pinCode, user.getPhoneNumber())) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_SAME_PHONE_NUMBER", null, "Cannot use the phone number.", locale));
            return resultVO;
        }

        if (RegularExpressionUtil.isPinCodeContainsBirthDate(pinCode, user.getBirthDate())) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_SAME_BIRTH_DATE", null, "You cannot write your date of birth.", locale));
            return resultVO;
        }

        resultVO.setSuccessInfo();
        resultVO.setMessage(Const.STATUS_SUCCESS);
        return resultVO;
    }


    /**
     * Token이 유효한지 확인
     *
     * @param token
     * @param userId
     * @param body
     * @param locale
     * @return
     * @throws CashmallowException
     */
    @Override
    public ApiResultVO validToken(String token, Long userId, JSONObject body, Locale locale) throws CashmallowException {

        ApiResultVO resultVO = new ApiResultVO();

        EasyLogin easyLogin = easyLoginMapper.getEasyLoginByUserId(userId);

        resultVO = validationRefreshToken(easyLogin, body, resultVO, token, locale);

        if (StringUtils.equals(Const.CODE_FAILURE, resultVO.getCode())) {
            return resultVO;
        }

        resultVO.setSuccessInfo();
        resultVO.setMessage(messageSource.getMessage("EAZY_LOGIN_REGISTERED", null, "Complete.", locale));

        return resultVO;

    }


    /**
     * @param token
     * @param userId
     * @param pinCode
     * @param pinCodeRe
     * @param locale
     * @return
     * @throws CashmallowException
     */
    @Override
    @Transactional
    public ApiResultVO addEasyLogin(String token, Long userId, String pinCode, String pinCodeRe, Locale locale) throws CashmallowException {
        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        if (!StringUtils.equals(pinCode, pinCodeRe)) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_SAME_PIN_CODE", null, "password mismatch.", locale));
            return resultVO;
        }

        RefreshToken refreshToken = refreshTokenMapper.getRefreshTokenByUserId(userId);

        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        String rToken = refreshToken.getToken();

        EasyLogin easyLogin = easyLoginMapper.getEasyLoginByToken(rToken);

        EasyLogin easyLoginParams = EasyLogin.builder()
                .refreshToken(rToken)
                .userId(userId)
                .pinCodeHash(securityService.encryptSHA2(pinCode))
                .refreshTime(now)
                .failCount(0)
                .createdAt(now)
                .build();

        int succeseFlag = 0;
        if (easyLogin == null) {
            succeseFlag = easyLoginMapper.insertEasyLogin(easyLoginParams);
        } else {
            easyLoginParams.setId(easyLogin.getId());
            succeseFlag = easyLoginMapper.updateEasyLogin(easyLoginParams);
        }

        if (succeseFlag == 1) {
            resultVO.setSuccessInfo();
            resultVO.setMessage(messageSource.getMessage("EAZY_LOGIN_REGISTERED", null, "Complete.", locale));
        }

        try {
            EasyLoginHist easyLoginHistParams = new EasyLoginHist();

            BeanUtils.copyProperties(easyLoginParams, easyLoginHistParams);
            easyLoginHistParams.setLoginSuccess(Const.Y);
            easyLoginHistParams.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

            easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return resultVO;
    }

    /**
     * 간편 로그인
     *
     * @param refreshToken
     * @param body
     * @param request
     * @return
     * @throws CashmallowException
     */
    @Override
    @Transactional
    public ApiResultVO confirm(String refreshToken, JSONObject body, HttpServletRequest request) throws CashmallowException {
        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO resultVO = new ApiResultVO();

        EasyLogin easyLogin = easyLoginMapper.getEasyLoginByToken(securityService.encryptSHA2(refreshToken));
        resultVO = validationRefreshToken(easyLogin, body, resultVO, refreshToken, locale);

        if (StringUtils.equals(Const.CODE_FAILURE, resultVO.getCode())) {
            return resultVO;
        }

        String pinCodeEncrypt = securityService.encryptSHA2(body.getString("pinCode"));

        if (!StringUtils.equals(easyLogin.getPinCodeHash(), pinCodeEncrypt)) {

            ArrayList<String> array = new ArrayList<>();
            array.add(String.valueOf(easyLogin.getFailCount() + 1));
            array.add(String.valueOf(Const.LOGIN_PASSWORD_MISMATCHES_5));

            easyLogin.setFailCount(easyLogin.getFailCount() + 1);

            // 5회 로그인 실패시 상태값 다르게 보냄
            if (Objects.equals(easyLogin.getFailCount(), LOGIN_PASSWORD_MISMATCHES_5)) {
                resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_NOT_MATCH", array.toArray(), "Pin code does not match.", locale));
                resultVO.setStatus(Const.STATUS_LOGIN_PIN_CODE_NOT_MATCH_MAX);
            } else {
                resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_NOT_MATCH", array.toArray(), "Pin code does not match.", locale));
                resultVO.setStatus(Const.STATUS_LOGIN_PIN_CODE_NOT_MATCH);
            }

            easyLoginMapper.updateEasyLogin(easyLogin);

            try {
                EasyLoginHist easyLoginHistParams = new EasyLoginHist();

                BeanUtils.copyProperties(easyLogin, easyLoginHistParams);
                easyLoginHistParams.setLoginSuccess(Const.N);
                easyLoginHistParams.setPinCodeHash(pinCodeEncrypt);
                easyLoginHistParams.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return resultVO;
        } else {

            try {
                UserAgreeTerms user = userRepositoryService.getUserAgreeTermsByUserId(easyLogin.getUserId());

                refreshToken = travelerService.travelerLoginForEasyLogin(user.getLogin(), user.getPasswordHash(), locale, body);
                String accessToken = authService.issueAccessToken(refreshToken);

                String notification = "";

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

                    Jws<Claims> jws = authService.parseJWT(accessToken);

                    Claims claims = jws.getBody();
                    String expiresIn = String.valueOf(claims.getExpiration().getTime());

                    Map<String, String> obj = new HashMap<>();
                    obj.put("access_token", accessToken);
                    obj.put("token_type", "bearer");
                    obj.put("expires_in", expiresIn);
                    obj.put("refresh_token", refreshToken);
                    obj.put("session_timeout", Const.SESSION_TIMEOUT_10_MIN);
                    obj.put("notification", notification);

                    CMDEF.storeTokenIntoSession(request, resultVO);
                    CMDEF.storeTokenIntoSession(request, accessToken);

                    List<TermsHistoryVO> unreadTermsList = userService.getUnreadTermsList(user.getUserId(), user.getCountryCode(), locale);
                    if (!unreadTermsList.isEmpty()) {
                        obj.put("status", Const.SHOW_THE_TERMS);
                        obj.put("title", getMessageByLocale("UNREAD_TO_TERMS_OF_SERVICE", locale));
                        obj.put("message", getMessageByLocale("UNREAD_TO_TERMS_OF_SERVICE_BODY", locale));
                        obj.put("iso3166", user.getIso3166());
                        resultVO.setResult(Const.CODE_SUCCESS, Const.SHOW_THE_TERMS, null, obj);
                    } else {
                        obj.put("status", Const.STATUS_SUCCESS);
                        resultVO.setSuccessInfo(obj);
                    }

                    resultVO.setSuccessInfo(obj);
                    resultVO.setMessage(messageSource.getMessage("EAZY_LOGIN_SUCCESS", null, "Simple login success.", locale));
                }
                String appbuild = null;
                if (body.has("appbuild")) {
                    appbuild = body.getString("appbuild");
                }

                CMDEF.setValueIntoSession(request, "appbuild", appbuild);

                Timestamp now = Timestamp.valueOf(LocalDateTime.now());

                // EasyLogin refreshToken 새롭게 저장
                easyLogin.setFailCount(0);
                easyLogin.setRefreshTime(now);
                easyLogin.setRefreshToken(securityService.encryptSHA2(refreshToken));
                easyLogin.setUpdatedAt(now);
                easyLogin.setPinCodeHash(pinCodeEncrypt);
                easyLoginMapper.updateEasyLogin(easyLogin);

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new CashmallowException(Const.MSG_LOGIN_FAILURE, e);
            }

            try {
                EasyLoginHist easyLoginHistParams = new EasyLoginHist();

                BeanUtils.copyProperties(easyLogin, easyLoginHistParams);
                easyLoginHistParams.setLoginSuccess(Const.Y);
                easyLoginHistParams.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return resultVO;
        }
    }

    private ApiResultVO validationRefreshToken(EasyLogin easyLogin, JSONObject body, ApiResultVO resultVO, String refreshToken, Locale locale) {

        if (easyLogin == null) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_VALID_TOKEN", null, "Token not registered.", locale));
            resultVO.setStatus(Const.STATUS_UNREGISTERED_ACCOUNT);
            return resultVO;
        }

        if (easyLogin != null) {
            User user = userRepositoryService.getUserByUserId(easyLogin.getUserId());

            if (!body.has("instance_id")) {
                resultVO.setFailInfo(messageSource.getMessage("MAIL_SUBJECT_RESET_DEVICE", null, "Identification re-authentication due to change of mobile device.", locale));
                resultVO.setStatus(Const.INSTANCE_ID_IS_EMPTY);
                return resultVO;
            }
            // user의 InstanceId와 remote_instance_id가 같으면 instance_id로 변경.
            if (body.has("remote_instance_id") && StringUtils.equals(user.getInstanceId(), securityService.encryptSHA2(body.getString("remote_instance_id")))) {
                user.setInstanceId(securityService.encryptSHA2(body.getString("instance_id")));
                userRepositoryService.updateUser(user);
            }
            if (!StringUtils.equals(user.getInstanceId(), securityService.encryptSHA2(body.getString("instance_id")))) {
                resultVO.setFailInfo(messageSource.getMessage("MAIL_SUBJECT_RESET_DEVICE", null, "Identification re-authentication due to change of mobile device.", locale));
                resultVO.setStatus(Const.INSTANCE_ID_HAS_CHANGED);
                return resultVO;
            }

        }

        if (easyLogin.getFailCount() >= 5) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_5_COUNT", null, "Token not registered.", locale));
            resultVO.setStatus(Const.STATUS_LOGIN_PASSWORD_5_COUNT_FAIL);
            return resultVO;
        }

        Timestamp expireTimestamp = CommDateTime.addTimestamp(easyLogin.getRefreshTime(), Const.EXPIRE_DAYS);
        Timestamp nowTimestamp = Timestamp.valueOf(LocalDateTime.now());

        if (nowTimestamp.after(expireTimestamp)) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_VALID_TOKEN_EXPIRE", null, "Easy login retry is required.", locale));
            resultVO.setStatus(Const.STATUS_EXPIRE_TOKEN);
            return resultVO;
        }

        // 토큰 정보가 있으나 Token 정보가 다를 경우
        if (!StringUtils.equals(easyLogin.getRefreshToken(), securityService.encryptSHA2(refreshToken))) {
            resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_VALID_TOKEN_EXPIRE", null, "Easy login retry is required.", locale));
            resultVO.setStatus(Const.STATUS_EXPIRE_TOKEN);
            return resultVO;
        }

        return resultVO;
    }

    @Override
    @Transactional
    public ApiResultVO checkPincodeMatch(String refreshToken, JSONObject body, Locale locale) throws CashmallowException {

        ApiResultVO resultVO = new ApiResultVO();

        EasyLogin easyLogin = easyLoginMapper.getEasyLoginByToken(securityService.encryptSHA2(refreshToken));

        resultVO = validationRefreshToken(easyLogin, body, resultVO, refreshToken, locale);

        if (StringUtils.equals(Const.CODE_FAILURE, resultVO.getCode())) {
            return resultVO;
        }

        if (!StringUtils.equals(easyLogin.getPinCodeHash(), securityService.encryptSHA2(body.getString("pinCode")))) {
            // 실패, Pincode 불일치
            ArrayList<String> array = new ArrayList<>();
            array.add(String.valueOf(easyLogin.getFailCount() + 1));
            array.add(String.valueOf(Const.LOGIN_PASSWORD_MISMATCHES_5));

            easyLogin.setFailCount(easyLogin.getFailCount() + 1);

            // 5회 로그인 실패시 상태값 다르게 보냄
            if (Objects.equals(easyLogin.getFailCount(), LOGIN_PASSWORD_MISMATCHES_5)) {
                resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_NOT_MATCH", array.toArray(), "Pin code does not match.", locale));
                resultVO.setStatus(Const.STATUS_LOGIN_PIN_CODE_NOT_MATCH_MAX);
            } else {
                resultVO.setFailInfo(messageSource.getMessage("EAZY_LOGIN_FAIL_PIN_CODE_NOT_MATCH", array.toArray(), "Pin code does not match.", locale));
                resultVO.setStatus(Const.STATUS_LOGIN_PIN_CODE_NOT_MATCH);
            }

            easyLoginMapper.updateEasyLogin(easyLogin);

            try {
                EasyLoginHist easyLoginHistParams = new EasyLoginHist();

                BeanUtils.copyProperties(easyLogin, easyLoginHistParams);
                easyLoginHistParams.setLoginSuccess(Const.N);
                easyLoginHistParams.setPinCodeHash(securityService.encryptSHA2(body.getString("pinCode")));
                easyLoginHistParams.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            return resultVO;
        } else {
            // 성공시 otp값 생성, Pincode 일치.
            easyLogin.setFailCount(0);
            easyLogin.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
            easyLoginMapper.updateEasyLogin(easyLogin);

            String otp = RandomUtil.generateRandomString(RandomUtil.ALPHA_NUMERIC, 32);
            redisService.put(REDIS_KEY_PINCODE, String.valueOf(easyLogin.getUserId()), otp, 15, TimeUnit.SECONDS);
            Map<String, String> otpMap = new HashMap<>();
            otpMap.put("otp", otp);

            resultVO.setSuccessInfo(otpMap);
            resultVO.setMessage(Const.STATUS_SUCCESS);
            return resultVO;
        }
    }

    private String getMessageByLocale(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }
}
