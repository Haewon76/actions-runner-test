package com.cashmallow.api.interfaces.admin.property;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(value = "jppost")
public record JpPostProperties(String bankName,
                               String branchName,
                               String accountNo) {
}
