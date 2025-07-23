package com.cashmallow.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("global")
public record GlobalProperties(
        GlobalData jp,
        GlobalData kr
) {
    public record GlobalData(
            String url,
            String xAuthKey
    ) {
    }
}
