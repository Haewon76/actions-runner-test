package com.cashmallow.api.interfaces.authme;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("authme")
public record AuthMeProperties(
        String url,
        String signatureKey,
        String clientId,
        String clientSecret,
        String clientIdJp,
        String clientSecretJp,
        String grantType,
        String scope
) {
}
