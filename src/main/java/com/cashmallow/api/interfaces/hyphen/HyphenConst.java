package com.cashmallow.api.interfaces.hyphen;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties(prefix = "hyphen")
@ConstructorBinding
@RequiredArgsConstructor
@Getter
public class HyphenConst {
    private final String URL;
    private final String userId;
    private final String hKey;
    private final String companyName;
}
