package com.cashmallow.api.interfaces.authme;

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
public class AuthMeApiClientConfig {

    @Bean
    Feign.Builder clientConfig(ObjectFactory<HttpMessageConverters> converters) {
        // FETCH를 위해 OkHttpClient를 사용
        return Feign.builder()
                .client(new OkHttpClient())
                .logLevel(Logger.Level.FULL);
    }

}
