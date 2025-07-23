package com.cashmallow.interceptor;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkProperties;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.RsaUtil;
import com.cashmallow.filter.CacheReadHttpServletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.interfaces.RSAPublicKey;
import java.time.ZonedDateTime;

import static com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig.ML_CLIENT_ID;
import static com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig.ML_SIGNATURE;
import static com.cashmallow.common.HashUtil.getSha256;

@Slf4j
@AllArgsConstructor
@Component
public class MallowlinkWebhookInterceptor implements HandlerInterceptor {

    private final MallowlinkProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 0. 캐쉬된 Request
        if (!(request instanceof CacheReadHttpServletRequest)) {
            return false;
        }

        CacheReadHttpServletRequest cachedRequest = (CacheReadHttpServletRequest) request;

        // 1. ML_CLIENT_KEY 체크
        if (isNotValidClientKey(cachedRequest)) {
            throw new MallowlinkException(MallowlinkExceptionType.INVALID_CLIENT);
        }

        // 2. ML_SIGNATURE 체크
        if (isNotValidSignature(cachedRequest)) {
            throw new MallowlinkException(MallowlinkExceptionType.INVALID_PARAMETER_EXCEPTION);
        }

        return true;
    }

    private boolean isNotValidClientKey(CacheReadHttpServletRequest cachedRequest) {
        String clientName = cachedRequest.getHeader(ML_CLIENT_ID);
        if (!properties.getClientId().equals(clientName) && !properties.getClientIdJp().equals(clientName)) {
            log.error("Invalid Client Name:{}", clientName);
            return true;
        } else {
            return false;
        }
    }

    private boolean isNotValidSignature(CacheReadHttpServletRequest cachedRequest) {
        String mlSignature = cachedRequest.getHeader(ML_SIGNATURE);
        log.debug("mlSignature={}", mlSignature);

        String requestJsonBody = CommonUtil.getRequestBodyString(cachedRequest);
        log.debug("requestJsonBody:{}", requestJsonBody);

        RSAPublicKey mlPublicKey = properties.getMlPublicKey();
        String decryptedSignature = RsaUtil.decryptRsa2048(mlPublicKey, mlSignature);

        return !StringUtils.equals(decryptedSignature, getSha256(requestJsonBody));
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

    @Data
    private static class MallowlinkBody {
        private final String transactionId;
        private final ZonedDateTime requestTime;

        public Long getTimestamp() {
            return requestTime.toEpochSecond();
        }
    }
}
