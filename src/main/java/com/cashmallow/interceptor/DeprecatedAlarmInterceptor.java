package com.cashmallow.interceptor;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.cashmallow.common.CommonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@RequiredArgsConstructor
@Component
public class DeprecatedAlarmInterceptor implements HandlerInterceptor {

    private final AlarmService alarmService;
    private final AuthService authService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            // Controller에 맵핑되는 것이 아니면 패스.
            if (!(handler instanceof HandlerMethod)) {
                return true;
            }

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (!handlerMethod.hasMethodAnnotation(Deprecated.class)) {
                return true;
            }

            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            long userId = authService.getUserId(token);

            String uri = request.getMethod() + " " + request.getRequestURI() + "\n유저ID: " + userId + "\nUserAgent: " + CommonUtil.getUserAgent(request);

            log.info("레거시 API 호출:{}", uri.replaceAll("\n", " "));
            alarmService.i("레거시 API 호출", uri);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

}
