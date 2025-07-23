package com.cashmallow.interceptor;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
@Component
public class LogInterceptor implements HandlerInterceptor {

    // public static final String X_REQUEST_ID = "x-request-id";
    public static final String USER_ID = "userId";
    public static final String ELAPSED_SECONDS = "elapsedSeconds";
    public static final String ENV = "env";
    public static final String START_AT = "startAt";

    private final AuthService authService;
    private final AlarmService alarmService;
    private final EnvUtil envUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            String token = request.getHeader(HttpHeaders.AUTHORIZATION);
            // String xRequestId = request.getHeader(X_REQUEST_ID);
            // if (StringUtils.isBlank(xRequestId)) {
            //     xRequestId = UUID.randomUUID().toString();
            // }
            // MDC.put(X_REQUEST_ID, xRequestId);
            MDC.put(USER_ID, String.valueOf(authService.getUserId(token)));
            MDC.put(ENV, envUtil.getEnv());

            request.setAttribute(START_AT, System.nanoTime());

            // String uri = CommonUtil.getUri(request);
            // log.info("========= START [{}]: {} {} ========== x-request-id:{}", MDC.get("userId"), request.getMethod(), uri, xRequestId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

        // String xRequestId = MDC.get(X_REQUEST_ID);
        String uri = CommonUtil.getUri(request);

        long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - (long) request.getAttribute("startAt"));
        String secondsStr = (float) millis / 1_000 + "s";
        MDC.put(ELAPSED_SECONDS, secondsStr);

        // 10초 이상 걸린 리퀘스트는 알람
        if (millis > 10_000 && !StringUtils.contains(uri, "/admin")) {
            alarmService.i("너무 오래 걸린 요청", ELAPSED_SECONDS + ":" + secondsStr);
        }

        // log.info("========= END [{}]: {} {} ========== elapsedSeconds:{}, x-request-id:{}", MDC.get("userId"), request.getMethod(), uri, secondsStr, xRequestId);

        // MDC.remove(X_REQUEST_ID);
        MDC.remove(USER_ID);
        MDC.remove(ELAPSED_SECONDS);
    }
}