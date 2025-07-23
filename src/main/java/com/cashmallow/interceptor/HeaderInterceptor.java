package com.cashmallow.interceptor;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.auth.UserAuth;
import com.cashmallow.api.domain.shared.AuthException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.cashmallow.common.EnvUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final AlarmService alarmService;
    private final SecurityService securityService;
    private final EnvUtil envUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String cmDeviceId = request.getHeader("cm-device-id");
        final String token = request.getHeader("Authorization");

        String host = request.getHeader("host");
        if (host.contains("localhost") || host.contains("cm-api-rollout")) {
            return true;
        }

        if (token != null && !authService.isHexaStr(token)) {
            if (StringUtils.isEmpty(cmDeviceId)) {
                log.error("cm-device-id가 null인 GET 요청");
                alarmService.i("cm-device-id가 null인 GET 요청", "해당 엔드포인트 확인 필요");
                throw new AuthException(Const.CODE_INVALID_TOKEN);
            }

            UserAuth userAuth = checkIfTokenExpired(request);

            String deviceId = securityService.encryptSHA2(cmDeviceId);
            String instanceId = userAuth.getInstanceId();
            if (!StringUtils.equals(instanceId, deviceId)) {
                log.info("deviceId 불일치");
                alarmService.i("deviceId 불일치", "");
                throw new AuthException(Const.CODE_INVALID_TOKEN);
            }

        }

        return true;
    }

    /**
     * 토큰이 만료되었는지 체크
     *
     * @param request
     * @throws AuthException
     */
    private UserAuth checkIfTokenExpired(HttpServletRequest request) throws AuthException {
        final String token = request.getHeader("Authorization");
        // jwtToken 검증 결과값 리턴

        if (StringUtils.isEmpty(token)) {
            log.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            throw new AuthException(Const.CODE_INVALID_TOKEN);
        }

        UserAuth userInfo = authService.getUserInfo(token);
        if (userInfo == null) {
            log.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            throw new AuthException(Const.CODE_INVALID_TOKEN);
        }

        long userId = userInfo.getUserId();
        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            log.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            throw new AuthException(Const.CODE_INVALID_TOKEN);
        }

        if (authService.isHexaStr(token)) {
            alarmService.i("임시 토큰 API 추적", "추적용");
        }

        return userInfo;
    }


}

