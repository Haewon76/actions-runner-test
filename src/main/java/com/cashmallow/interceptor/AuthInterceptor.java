package com.cashmallow.interceptor;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.auth.UserAuth;
import com.cashmallow.api.domain.shared.AuthException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.AuthVO;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.filter.CacheReadHttpServletRequest;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.cashmallow.common.HashUtil.getMd5Hash;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final RedisService redisService;
    private final Gson gson;
    private final AlarmService alarmService;
    private final SecurityService securityService;

    private final long INTERVAL_TIME = 3 * 60;  // 3분
    public static final String SERVICE_COUNTRY = "serviceCountry";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String fullUrl = CommonUtil.getFullUrl(request);

        if (isExcludeRequest(request)) {
            return true;
        }

        if (isNotCachedRequest(request)) {
            return true;
        }

        String requestJsonBody = CommonUtil.getRequestBodyString(request);

        // 1. 토큰이 만료되었는지 체크
        long userId = -1;
        UserAuth userAuth = new UserAuth(-1, "", List.of(), "");
        if (!isExcludeRequest(request)) {
            userAuth = checkIfTokenExpired(request);
            log.debug("userAuth={}", gson.toJson(userAuth));
            userId = userAuth.getUserId();
        }
        MDC.put(SERVICE_COUNTRY, userAuth.getServiceCountry());

        // 2. 캐쉬 된 바디일 경우 가져오기, 레디스에 저장(타임스템프가 포함되어야함), 캐쉬 된 바디일 경우
        if (StringUtils.isEmpty(requestJsonBody)) {
            log.warn("Empty body POST request={}", fullUrl);
            return true;
        }

        // 3. 암호화면 복호화
        final String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String decode = CustomStringUtil.decode(token, requestJsonBody);

        if (decode != null) {
            log.debug("decode={}", decode);
            requestJsonBody = decode;
        } else {
            log.info("not encrypted url={}", fullUrl);
        }

        // 4. 3분 지나지 않았는지
        log.debug("requestJsonBody={}", requestJsonBody);
        final AuthVO authBodyVo = gson.fromJson(requestJsonBody, AuthVO.class);
        if (authBodyVo.getTimestamp() == null || StringUtils.isBlank(authBodyVo.getDeviceId())) {
            alarmService.i("INVALID REQUEST", "timestamp 또는 deviceId 누락");
            throw new AuthException(Const.CODE_INVALID_PARAMS);
        }

        final LocalDateTime requestTime = LocalDateTime.ofEpochSecond(authBodyVo.getTimestamp(), 0, ZoneOffset.UTC);
        if (requestTime.plusSeconds(INTERVAL_TIME).isBefore(LocalDateTime.now())) {
            throw new AuthException(Const.CODE_INVALID_PARAMS);
        }

        // 5. 기기가 일치하는지, jwt와 비교
        if (!isExcludeRequest(request)) {
            log.debug("userAuth={}", userAuth);
            String encryptedDeviceId = securityService.encryptSHA2(authBodyVo.getDeviceId());
            if (!StringUtils.equals(encryptedDeviceId, userAuth.getInstanceId())) {
                // 기기인증 오류 throw
                log.warn("DeviceID 불일치. cm-device-id={}, authBodyVo.getDeviceId()={}, user.getInstanceI()={}", request.getHeader("cm-device-id"), authBodyVo.getDeviceId(), userAuth.getInstanceId());
                throw new AuthException(Const.CODE_INVALID_PARAMS);
            }
        }

        // 6. 중복인지
        final String key = userId + getMd5Hash(request.getMethod() + fullUrl + requestJsonBody);

        // 레디스에 3분 시간 만료 (삭제)
        if (!redisService.putIfAbsent(key, "0", INTERVAL_TIME, TimeUnit.SECONDS)) {
            // 레디스에 저장된 토큰이 있으면, 중복 요청으로 판단
            log.warn("중복 요청. userId={}, request_body={}", userId, gson.toJson(requestJsonBody));
            throw new AuthException(Const.CODE_INVALID_PARAMS);
        }

        return true;
    }

    private boolean isExcludeRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        if (StringUtils.equals(requestURI, "/api/auth/login") ||
                StringUtils.equals(requestURI, "/api/auth/deviceResetCodeValid") ||
                StringUtils.equals(requestURI, "/api/traveler/v3/kyc/token/webhook")) {
            return true;
        }
        return false;
    }

    private boolean isNotCachedRequest(HttpServletRequest request) {
        return !isCachedRequest(request);
    }

    private boolean isCachedRequest(HttpServletRequest request) {
        return request instanceof CacheReadHttpServletRequest || request instanceof MultipartHttpServletRequest;
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

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(SERVICE_COUNTRY);
    }
}
