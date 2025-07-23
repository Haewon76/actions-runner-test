package com.cashmallow.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "캐시멜로 API",
                description = "캐시멜로 서비스 API 명세서",
                version = "v2"))
@Configuration
public class SwaggerConfig {

    @Bean
    public GroupedOpenApi travelerApi() {
        return GroupedOpenApi.builder()
                .group("1. Traveler")
                .pathsToMatch("/**")
                .pathsToExclude("/admin/**")
                .build();
    }


    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("2. Admin")
                .pathsToMatch("/admin/**")
                .build();
    }

    @Bean
    public GroupedOpenApi OthersApi() {
        return GroupedOpenApi.builder()
                .group("3. Others")
                .pathsToMatch("/**")
                .pathsToExclude("/traveler/**", "/admin/**", "/json/**")
                .build();
    }

    @Bean
    public GroupedOpenApi LegacyApi() {
        return GroupedOpenApi.builder()
                .group("4. Legacy-Json")
                .pathsToMatch("/json/**")
                .build();
    }

}
