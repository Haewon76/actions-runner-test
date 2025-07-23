// package com.cashmallow.aop;
//
// import com.cashmallow.api.auth.impl.AuthServiceImpl;
// import com.cashmallow.api.domain.model.geo.GeoLocation;
// import com.cashmallow.api.domain.shared.Const;
// import com.cashmallow.common.CommDateTime;
// import com.cashmallow.common.geoutil.GeoUtils;
// import com.google.gson.GsonBuilder;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.lang3.StringUtils;
// import org.apache.commons.lang3.time.StopWatch;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.security.core.Authentication;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.stereotype.Component;
// import org.springframework.web.context.request.RequestContextHolder;
// import org.springframework.web.context.request.ServletRequestAttributes;
//
// import javax.servlet.http.HttpServletRequest;
// import javax.servlet.http.HttpServletResponse;
// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.time.ZonedDateTime;
// import java.util.Enumeration;
// import java.util.HashMap;
// import java.util.Locale;
// import java.util.Map;
//
// @Component
// @Aspect
// @Slf4j
// public class ControllerLoggingAspect {
//
//     @Autowired
//     public AuthServiceImpl authService;
//
//     @Autowired
//     private  GeoUtils geoUtils;
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping)")
//     private void postController() {
//     }
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping)")
//     private void getController() {
//     }
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.PutMapping)")
//     private void putController() {
//     }
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
//     private void deleteController() {
//     }
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.PatchMapping)")
//     private void patchController() {
//     }
//
//     @Pointcut("@annotation(org.springframework.web.bind.annotation.RestController)")
//     private void restController() {
//     }
//
//
//     @Around("restController() || postController() || getController() || putController() || deleteController() || patchController()")
//     public Object controllerLogging(ProceedingJoinPoint pjp) throws Throwable {
//         Map<String, Object> map = new HashMap<>();
//
//         StopWatch stopWatch = new StopWatch();
//
//         final HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
//         final HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
//
//
//         Map<String, Object> reqMap = requestMap(request);
//         Map<String, Object> resMap = responseMap(response);
//
//         Object proceed = null;
//
//         try {
//             map.put("userAgent", request.getHeader("User-Agent"));
//             map.put("referer", request.getHeader("Referer"));
//             map.put("clientIP", getClientIPAddress(request));
//             map.put("userId", getUserId(reqMap));
//             map.put("method", request.getMethod());
//             map.put("uri", request.getRequestURI());
//             map.put("url", getUrl(request, getQueryString(request.getQueryString())));
//             map.put("locale", request.getLocale());
//             map.put("scheme", request.getScheme());
//             map.put("secure", request.isSecure());
//             map.put("serverName", request.getServerName());
//             map.put("serverPort", request.getServerPort());
//             map.put("protocol", request.getProtocol());
//             map.put("loginId", getUserId());
//
//             if (pjp != null) {
//                 reqMap.put("controller", pjp.getSignature().getDeclaringType().getSimpleName());
//                 reqMap.put("function", pjp.getSignature().getName());
//             }
//
//             stopWatch.start();
//             proceed = pjp.proceed(pjp.getArgs());
//         } finally {
//             stopWatch.stop();
//
//             resMap.put("body", proceed);
//             map.put("durationByMilliseconds", stopWatch.getTime());
//             map.put("request", reqMap);
//             map.put("response", resMap);
//             map.put("location", geoUtils.getMyCountryCode(getClientIPAddress(request)));
//         }
//
//         // 헬스 체크 무시
//         if (!"/api/health".equalsIgnoreCase(request.getRequestURI())) {
//             log.info(new GsonBuilder().disableHtmlEscaping().disableInnerClassSerialization().create().toJson(map));
//         }
//         return proceed;
//     }
//
//     private String getUrl(HttpServletRequest request, String queryString) {
//         return String.format("%s%s", request.getRequestURL().toString().replaceAll(request.getScheme(), ""), (StringUtils.isEmpty(queryString) ? "" : "?".concat(queryString)));
//     }
//
//     private Map<String, Object> requestMap(HttpServletRequest request) {
//         Map<String, Object> map = new HashMap<>();
//         GeoLocation geoLocation = geoUtils.getMyCountryCode(getClientIPAddress(request));
//         LocalDateTime localDateTime = LocalDateTime.now();
//         ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.of("+00:00"));
//         map.put("param", getParamsMap(request));
//         map.put("session", getSession(request));
//         map.put("header", getHeader(request));
//         map.put("dateToKST", CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("+09:00")));
//         map.put("dateToLocal", CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of(geoLocation.getTimezoneToZoneId())));
//         map.put("dateToUTC", CommDateTime.zonedDateTimeToString(ZonedDateTime.now(), ZoneId.of("+00:00")));
//         map.put("timestamp", zdt.toInstant().toEpochMilli());
//         return map;
//     }
//
//     private Map<String, Object> responseMap(HttpServletResponse response) {
//         Map<String, Object> map = new HashMap<>();
//         map.put("code", response.getStatus());
//         return map;
//     }
//
//     private String getUserId() {
//         try {
//             Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//             UserDetails userDetails = (UserDetails) authentication.getPrincipal();
//             return userDetails.getUsername();
//         } catch (Exception e) {
//         }
//
//         return null;
//     }
//
//     private Long getUserId(Map<String, Object> requestMap) {
//         try {
//             Map<String, Object> header = (Map<String, Object>) requestMap.get("header");
//             return authService.getAccessUserId(header.get("authorization"));
//         } catch (Exception e) {
//         }
//         return Const.NO_USER_ID;
//     }
//
//     private Map<String, Object> getSession(HttpServletRequest request) {
//         Map<String, Object> map = new HashMap<>();
//         map.put("sessionId", request.getRequestedSessionId());
//         map.put("isRequestedSessionIdFromCookie", request.isRequestedSessionIdFromCookie());
//         map.put("isRequestedSessionIdFromURL", request.isRequestedSessionIdFromURL());
//         map.put("isRequestedSessionIdValid", request.isRequestedSessionIdValid());
//         return map;
//     }
//
//     private Map<String, Object> getHeader(HttpServletRequest request) {
//         Map<String, Object> map = new HashMap<>();
//         Enumeration<String> headerNames = request.getHeaderNames();
//         if (headerNames != null) {
//             while (headerNames.hasMoreElements()) {
//                 String key = headerNames.nextElement().toLowerCase(Locale.ROOT);
//                 String value = request.getHeader(key);
//                 map.put(key, value);
//             }
//         }
//         return map;
//     }
//
//     private Map<String, Object> getParamsMap(HttpServletRequest request) {
//         Enumeration names = request.getParameterNames();
//         Map<String, Object> paramMap = new HashMap<>();
//         while (names.hasMoreElements()) {
//             String key = (String) names.nextElement();
//             String value = request.getParameter(key);
//             paramMap.put(key, value);
//         }
//         return paramMap;
//     }
//
//     public static String getQueryString(String query) {
//         if (StringUtils.isEmpty(query)) {
//             return Strings.EMPTY;
//         }
//         return query.replaceAll("\\\\u003d", "=");
//     }
//
//     public static String getClientIPAddress(final HttpServletRequest request) {
//         String ipAddress = request.getHeader("X-FORWARDED-FOR");
//         if (ipAddress == null) {
//             ipAddress = request.getRemoteAddr();
//         }
//
//         return ipAddress;
//     }
// }