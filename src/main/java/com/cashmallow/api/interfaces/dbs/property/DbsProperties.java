package com.cashmallow.api.interfaces.dbs.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "dbs")
public record DbsProperties(String url,
                            String clientKey,
                            String cmPrivateKey,
                            String mlPublicKey,
                            Long accountId,
                            String accountNo,
                            String accountName) {
}
