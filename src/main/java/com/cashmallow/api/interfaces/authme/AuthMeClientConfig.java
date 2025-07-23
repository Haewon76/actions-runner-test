package com.cashmallow.api.interfaces.authme;

import com.cashmallow.api.application.AlarmService;
import feign.Feign;
import feign.Logger;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
public class AuthMeClientConfig {

    // private final ObjectMapper objectMapper;
    private final AlarmService alarmService;

    @Bean
    Feign.Builder clientConfig(ObjectFactory<HttpMessageConverters> converters) {
        // FETCH를 위해 OkHttpClient를 사용
        return Feign.builder()
                .client(new OkHttpClient())
                .logLevel(Logger.Level.FULL);
    }

    // @Bean
    // public ErrorDecoder errorDecoder() {
    //     return (methodKey, response) -> {
    //
    //         log.info("response.status()={}", response.status());
    //
    //         String responseBodyString = "";
    //
    //         try {
    //             String requestBody = new String(response.request().requestTemplate().body(), StandardCharsets.UTF_8);
    //             log.info("Mallowlink requestBody:{}", requestBody);
    //
    //             responseBodyString = IOUtils.toString(response.body().asReader(StandardCharsets.UTF_8));
    //             log.info("Mallowlink responseBody: {}", responseBodyString);
    //             log.warn("Mallowlink ErrorDecoder error:: method: {}, code: {}, body: {}", methodKey, response.status(), responseBodyString);
    //
    //             // MallowlinkErrorResponse mallowlinkErrorResponse = objectMapper.readValue(responseBodyString, MallowlinkErrorResponse.class);
    //             //
    //             // MallowlinkExceptionType mallowlinkExceptionType = MallowlinkExceptionType.byCode(mallowlinkErrorResponse.getCode());
    //             // return switch (mallowlinkExceptionType) {
    //             //     case USER_NOT_FOUND -> new MallowlinkNotFoundEnduserException();
    //             //     default -> MallowlinkException.of(mallowlinkExceptionType);
    //             // };
    //
    //         } catch (Exception e) {
    //             log.error("ErrorDecoder UnexpectedPartnerException", e);
    //             log.error("method: {}, code: {}, body: {}", methodKey, response.status(), responseBodyString);
    //         }
    //
    //         String errorMessage = String.format("UnexpectedPartnerException method: %s, code: %d, body: %s", methodKey, response.status(), responseBodyString);
    //         log.error(errorMessage);
    //
    //         alarmService.i("AuthMe UnexpectedException", errorMessage);
    //         return new MallowlinkException(MallowlinkExceptionType.INTERNAL_SERVER_ERROR);
    //     };
    // }
}
