package com.cashmallow.api.interfaces.aml.complyadvantage.client;

import com.cashmallow.api.interfaces.aml.complyadvantage.ComplyAdvantageAuthService;
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
public class ComplyadvantageBizFeignConfig {

    private final ComplyAdvantageAuthService complyAdvantageAuthService;

    @Bean
    public RequestInterceptor requestComplyAdvantageInterceptor() {
        return requestTemplate -> requestTemplate.header("Authorization", "Bearer " + complyAdvantageAuthService.getAccessToken());
    }

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

}
