package com.cashmallow.interceptor;

import com.cashmallow.api.config.GlobalProperties;
import com.cashmallow.api.domain.shared.AuthException;
import com.cashmallow.api.domain.shared.Const;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalInterceptor implements HandlerInterceptor {

    private final GlobalProperties globalProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String xAuthKey = request.getHeader("x-auth-key");

        boolean isJp = request.getRequestURI().contains("global/jp");
        boolean isKr = request.getRequestURI().contains("global/kr");
        if (isJp) {
            GlobalProperties.GlobalData jp = globalProperties.jp();
            if (!xAuthKey.equals(jp.xAuthKey())) {
                throw new AuthException(Const.CODE_INVALID_TOKEN);
            }

            MDC.put(AuthInterceptor.SERVICE_COUNTRY, "JP");
            return true;
        } else if (isKr) {
            GlobalProperties.GlobalData kr = globalProperties.kr();
            if (!xAuthKey.equals(kr.xAuthKey())) {
                throw new AuthException(Const.CODE_INVALID_TOKEN);
            }

            MDC.put(AuthInterceptor.SERVICE_COUNTRY, "KR");
            return true;
        }

        throw new AuthException(Const.CODE_INVALID_TOKEN);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(AuthInterceptor.SERVICE_COUNTRY);
    }
}

