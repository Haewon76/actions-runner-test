package com.cashmallow.api.config;

import com.cashmallow.common.resolver.UserArgumentResolver;
import com.cashmallow.interceptor.*;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;
    private final GlobalInterceptor globalInterceptor;
    private final DeprecatedAlarmInterceptor deprecatedAlarmInterceptor;
    private final AuthInterceptor authInterceptor;
    private final HeaderInterceptor headerInterceptor;
    private final MallowlinkWebhookInterceptor mallowlinkWebhookInterceptor;
    private final DbsServiceApiInterceptor dbsServiceApiInterceptor;
    private final UserArgumentResolver userArgumentResolver;

    private final List<String> excludePaths = List.of(
            "/json/command/slackCommand",
            "/json/traveler/get",
            "/json/traveler/getBankInfo",
            "/json/mobile/exchanges/my-exchanges",
            "/auth/email/verify",
            "/traveler/customer-center/information",
            "/traveler/terms-of-service",
            "/traveler/privacy-policy",
            "/traveler/ad",
            "/traveler/coatm-manual",
            "/traveler/storekeepers/*/guide-test",
            "/traveler/remittance/exchange-configs",
            "/traveler/users/confirm-device-reset",
            "/traveler/travelers/confirm-bank-account",
            "/traveler/v2/remittances/**",
            "/global/**",
            "/auth/admin/login",
            "/auth/admin/login/otp",
            "/auth/password-reset",
            "/auth/admin/password-reset",
            "/global/**");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/images/**")
                .excludePathPatterns("/css/**")
                .order(1);

        registry.addInterceptor(headerInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/images/**")
                .excludePathPatterns("/css/**")
                .excludePathPatterns("/homepage/**")
                .excludePathPatterns("/global/**")
                .order(2);

        registry.addInterceptor(deprecatedAlarmInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/health")
                .excludePathPatterns("/js/**")
                .excludePathPatterns("/images/**")
                .excludePathPatterns("/css/**")
                .order(3);

        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/json/**")
                .addPathPatterns("/auth/**")
                .addPathPatterns("/easyLogin/**")
                .addPathPatterns("/traveler/**")
                .addPathPatterns("/openbank/**")
                .excludePathPatterns(excludePaths)
                .order(4);

        registry.addInterceptor(globalInterceptor)
                .addPathPatterns("/global/**")
                .order(5);

        registry.addInterceptor(mallowlinkWebhookInterceptor)
                .addPathPatterns("/mallowlink/v1/webhook");

        registry.addInterceptor(dbsServiceApiInterceptor)
                .addPathPatterns("/dbs/bank-accounts/records");

    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/homepage/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "https://cashmallow.ggcdemo.com",
                        "https://cashmallow.com",
                        "https://www.test-cors.org",
                        "https://www.cashmallow.com")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArgumentResolver);
    }
}