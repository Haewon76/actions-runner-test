package com.cashmallow.api.interfaces.dbs.client;

import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("deprecation")
public class DbsFeignConfig {

    private String ML_CLIENT_KEY = "ML-Client-Key";
    private String ML_SIGNATURE = "ML-Signature";

    private final DbsProperties dbsProperties;

    private final ObjectMapper mapper;

    @Bean
    public Feign.Builder feignBuilder(List<RequestInterceptor> requestInterceptors) {
        return Feign.builder().logLevel(Logger.Level.FULL).client(new OkHttpClient()).requestInterceptors(requestInterceptors);
    }

    @Bean
    public Encoder feignEncoder() {
        MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter();
        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new SpringEncoder(objectFactory);
    }

    @Bean
    public RequestInterceptor requestInterceptor(ObjectMapper objectMapper) {
        return requestTemplate -> {
            // ML-Client-Key 추가
            requestTemplate.header(ML_CLIENT_KEY, dbsProperties.clientKey());

            if (!requestTemplate.method().equals("POST") || requestTemplate.requestBody() == null) {
                return;
            }

            try {
                log.debug("[DBS-BodyInterceptor] jsonBody: {}", requestTemplate.requestBody().asString());
            } catch (Exception e) {
                log.error("[DBS-BodyInterceptor] Error. " + e.getMessage(), e);
            }
        };
    }
}
