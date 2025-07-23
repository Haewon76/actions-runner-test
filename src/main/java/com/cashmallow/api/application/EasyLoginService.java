package com.cashmallow.api.application;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

public interface EasyLoginService {

    ApiResultVO addEasyLogin(String token, Long userId, String pinCode, String pinCodeRe, Locale locale) throws CashmallowException;

    ApiResultVO pinCodeValidation(Long userId, String pinCode, Locale locale) throws CashmallowException;

    ApiResultVO validToken(String token, Long userId, JSONObject body, Locale locale) throws CashmallowException;

    ApiResultVO confirm(String token, JSONObject body, HttpServletRequest request) throws CashmallowException;

    ApiResultVO checkPincodeMatch(String refreshToken, JSONObject body, Locale locale) throws CashmallowException;

}