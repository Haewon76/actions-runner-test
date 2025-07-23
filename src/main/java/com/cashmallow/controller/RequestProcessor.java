package com.cashmallow.controller;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.common.annotation.dto.CustomUserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Component
@Slf4j
@RequiredArgsConstructor
public class RequestProcessor {

    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;
    private final JsonUtil jsonUtil;

    public <T> T decode(CustomUserInfo userInfo, String requestBody, Class<T> clazz) {
        try {
            String json = CustomStringUtil.decode(userInfo.token(), requestBody);
            return objectMapper.readValue(json, clazz);
        } catch (Exception ignored) {
            log.error("Failed to decode request body: {}", requestBody);
        }

        return null;
    }

    public <T> T toObject(String json, Class<T> clazz) {
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception ignored) {
            log.error("Failed to decode request body: {}", json);
        }

        return null;
    }

    public String encryptedProcess(CustomUserInfo user,
                                   RequestCommand command) {
        return process(user, true, command);
    }

    public String process(CustomUserInfo user,
                          RequestCommand command) {
        return process(user, false, command);

    }

    private String process(CustomUserInfo user,
                           boolean encrypted,
                           RequestCommand command) {

        Locale locale = user.locale();
        String token = user.token();
        HttpServletResponse response = getResponse();

        if (user.isInvalidUser()) {
            return invalidToken(token, response);
        }

        ApiResultVO voResult = new ApiResultVO(CODE_FAILURE);

        try {
            voResult.setSuccessInfo(command.process());
        } catch (CashmallowException e) {
            String message = cashmallowException(locale, messageSource, e);
            voResult.setFailInfo(message);

            if (STATUS_TRAVELER_INFO_MODIFY_FAIL.equals(e.getMessage())) {
                voResult.setStatus(e.getMessage());
            }

            log.error(message, e);
        } catch (Exception e) {
            String message = exception(locale, messageSource);
            voResult.setFailInfo(message);
            log.error(message, e);
        }

        return toJsonString(locale, token, messageSource, voResult, response, encrypted);
    }


    private String toJsonString(Locale locale, String token, MessageSource messageSource, ApiResultVO voResult, HttpServletResponse response, boolean encrypted) {
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        log.debug("result: {}", jsonUtil.toJsonPretty(voResult));

        if (encrypted) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // start of error response
    private String invalidToken(String token, HttpServletResponse response) {
        return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
    }

    private String cashmallowException(Locale locale, MessageSource messageSource, Exception e) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }

    private String exception(Locale locale, MessageSource messageSource) {
        return messageSource.getMessage(INTERNAL_SERVER_ERROR, null, locale);
    }
    // end of error response

    private HttpServletResponse getResponse() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes != null) {
            return requestAttributes.getResponse();
        }

        return null;
    }
}