package com.cashmallow.interceptor;

import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
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

import static com.cashmallow.common.HashUtil.getSha256;

@Slf4j
@AllArgsConstructor
@Component
public class DbsServiceApiInterceptor implements HandlerInterceptor {

    private final String ML_CLIENT_KEY = "ML-Client-Key";

    private final DbsProperties properties;

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

        return true;
    }

    private boolean isNotValidClientKey(CacheReadHttpServletRequest cachedRequest) {
        String clientName = cachedRequest.getHeader(ML_CLIENT_KEY);
        if (!properties.clientKey().equals(clientName)) {
            log.error("Invalid Client Name:{}", clientName);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
    }

}
