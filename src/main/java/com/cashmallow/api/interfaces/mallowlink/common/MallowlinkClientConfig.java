package com.cashmallow.api.interfaces.mallowlink.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.mallowlink.MallowlinkLog;
import com.cashmallow.api.domain.model.mallowlink.MallowlinkMapper;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkErrorResponse;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkNotFoundEnduserException;
import com.cashmallow.common.RsaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.*;
import feign.codec.ErrorDecoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.cashmallow.common.HashUtil.getSha256;

@Slf4j
@RequiredArgsConstructor
@SuppressWarnings({"unchecked", "deprecation"})
public class MallowlinkClientConfig {

    public static final String ML_CLIENT_ID = "ML-Client-Id";
    public static final String ML_SIGNATURE = "ML-Signature";

    private final ObjectMapper objectMapper;

    private final MallowlinkMapper mallowlinkMapper;
    private final UserMapper userMapper;

    @Bean
    Feign.Builder clientConfig() {
        // FETCH를 위해 OkHttpClient를 사용
        return Feign.builder()
                .client(new OkHttpClient())
                .logLevel(Logger.Level.FULL);
    }

    @Bean
    Logger dbLogger() {
        return new DbLogger(MallowlinkClientConfig.class, mallowlinkMapper);
    }

    @Bean
    public RequestInterceptor requestInterceptor(MallowlinkProperties mallowlinkProperties) {
        return requestTemplate -> {
            if (!requestTemplate.method().equals("POST") || requestTemplate.requestBody() == null) {
                return;
            }

            // requestTime 추가
            ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));
            // ML-Client-Key 추가
            requestTemplate.header(ML_CLIENT_ID, isJpUser(objectMapper, requestTemplate, now) ? mallowlinkProperties.getClientIdJp() : mallowlinkProperties.getClientId());

            String requestBody = getRequestBodyInjectedRequestTime(objectMapper, requestTemplate, now);

            requestTemplate.body(requestBody);

            // ML-signature 추가
            final RSAPrivateKey privateKey = mallowlinkProperties.getPrivateKey();
            final String sha256 = getSha256(requestBody);
            final String signature = RsaUtil.encryptRsa2048(privateKey, sha256);

            log.debug("sha256={}", sha256);
            log.debug("signature={}", signature);

            requestTemplate.header(ML_SIGNATURE, signature);

            try {
                log.debug("[ML-BodyInterceptor] jsonBody: {}", requestTemplate.requestBody().asString());
            } catch (Exception e) {
                log.error("[ML-BodyInterceptor] Error. " + e.getMessage(), e);
            }
        };
    }

    private static String getRequestBodyInjectedRequestTime(ObjectMapper objectMapper, RequestTemplate requestTemplate, ZonedDateTime requestTime) {
        String requestBody = requestTemplate.requestBody().asString();
        log.debug("requestBody={}", requestBody);
        try {
            Map<String, Object> map = objectMapper.readValue(requestBody, LinkedHashMap.class);
            map.put("requestTime", requestTime);
            requestBody = objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.debug("requestBody={}", requestBody);
        return requestBody;
    }

    private boolean isJpUser(ObjectMapper objectMapper, RequestTemplate requestTemplate, ZonedDateTime requestTime) {
        String requestBody = requestTemplate.requestBody().asString();
        String url = requestTemplate.url();
        try {
            // withdrawal request
            Map<String, Object> map = objectMapper.readValue(requestBody, LinkedHashMap.class);
            String userId = (String) map.get("userId");
            if (StringUtils.isNotBlank(userId)) {
                long uId = Long.parseLong(userId);
                User user = userMapper.getUserByUserId(uId);
                return user.getCountryCode() == CountryCode.JP;
            }

            // withdrawal cancel, qr, inquiry
            String transactionId = (String) map.get("transactionId");
            if (StringUtils.isNotBlank(transactionId)) {
                User user = userMapper.getUserByWithdrawalTransactionId(transactionId);
                return user.getCountryCode() == CountryCode.JP;
            }

            // remittance request
            String clientEndUserId = (String) map.get("clientEndUserId");
            if (StringUtils.isNotBlank(clientEndUserId)) {
                // CM24102100000023
                User user = userMapper.getUserByUserId(Long.parseLong(clientEndUserId));
                return user.getCountryCode() == CountryCode.JP;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {

            log.info("response.status()={}", response.status());

            String responseBodyString = "";

            try {
                String requestBody = new String(response.request().requestTemplate().body(), StandardCharsets.UTF_8);
                log.info("Mallowlink requestBody:{}", requestBody);

                responseBodyString = IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8));
                log.info("Mallowlink responseBody: {}", responseBodyString);
                log.warn("Mallowlink ErrorDecoder error:: method: {}, code: {}, body: {}", methodKey, response.status(), responseBodyString);

                MallowlinkErrorResponse mallowlinkErrorResponse = objectMapper.readValue(responseBodyString, MallowlinkErrorResponse.class);

                MallowlinkExceptionType mallowlinkExceptionType = MallowlinkExceptionType.byCode(mallowlinkErrorResponse.getCode());
                return switch (mallowlinkExceptionType) {
                    case USER_NOT_FOUND -> new MallowlinkNotFoundEnduserException();
                    default -> MallowlinkException.of(mallowlinkExceptionType);
                };

            } catch (Exception e) {
                log.error("ErrorDecoder UnexpectedPartnerException", e);
                log.error("method: {}, code: {}, body: {}", methodKey, response.status(), responseBodyString);
            }

            String errorMessage = String.format("UnexpectedPartnerException method: %s, code: %d, body: %s", methodKey, response.status(), responseBodyString);
            log.error(errorMessage);

            return new MallowlinkException(MallowlinkExceptionType.INTERNAL_SERVER_ERROR);
        };
    }

    public static class DbLogger extends Slf4jLogger {
        private final MallowlinkMapper mallowlinkMapper;

        public DbLogger(Class<?> clazz, MallowlinkMapper mallowlinkMapper) {
            super(clazz);
            this.mallowlinkMapper = mallowlinkMapper;
        }

        @Override
        protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime) throws IOException {
            String requestText = "";
            Request request = response.request();
            if (request.body() != null) {
                requestText = request.isBinary()
                        ? "Binary data:" + request.body().length + "bytes"
                        : new String(request.body(), request.charset());
            }

            int status = response.status();
            String responseText = "";
            byte[] bodyData = null;
            if (response.body() != null && !(status == 204 || status == 205)) {
                bodyData = Util.toByteArray(response.body().asInputStream());
                responseText = Util.decodeOrDefault(bodyData, Util.UTF_8, "Binary data:" + bodyData.length + "bytes");
            }

            if (!StringUtils.containsAny(configKey, "getBank", "getBankBranches", "agencies")) {
                log.info("[MallowlinkDbLogger] request: {}, status: {}, response: {}, elapsedTime: {}", requestText, status, responseText, elapsedTime);
                mallowlinkMapper.insertMallowlinkLog(new MallowlinkLog(configKey, requestText, status, responseText, elapsedTime));
            }
            return super.logAndRebufferResponse(configKey, logLevel, response.toBuilder().body(bodyData).build(), elapsedTime);
        }
    }


}
