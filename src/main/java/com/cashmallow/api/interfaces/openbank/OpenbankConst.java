package com.cashmallow.api.interfaces.openbank;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("openbank")
@ConstructorBinding
@AllArgsConstructor
@Getter
public class OpenbankConst {
    private final String URL;
    private final String clientId;
    private final String secret;
    private final String redirectUri;
    private final String scope;
    private final String cashmallowCd;
    private final String cntrBankCode;
    private final String cntrAccountNum;
    private final String cntrAccountName;
}
