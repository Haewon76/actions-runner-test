package com.cashmallow.api.interfaces.batch.client;

import feign.Feign;
import feign.Logger;
import feign.RequestInterceptor;
import feign.codec.Encoder;
import feign.okhttp.OkHttpClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class BatchFeignConfig {

    private String CM_API_KEY = "CM-API-KEY";

    @Value("${batch.key}")
    private String batchKey;

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
    public RequestInterceptor requestBatchInterceptor() {
        return requestTemplate -> requestTemplate.header(CM_API_KEY, batchKey);
    }
}
