package com.cashmallow.api.interfaces.mallowlink.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.common.RsaUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

@ConfigurationProperties("mallowlink.api")
@ConstructorBinding
@RequiredArgsConstructor
public class MallowlinkProperties {

    @Getter
    private final String url;

    @Getter
    private final String clientId;
    @Getter
    private final String clientIdJp;

    private final String cmPrivateKey;
    private final String cmPublicKey;
    private final String mlPublicKey;

    @Getter
    private final Set<CountryCode> remittanceCountries;

    @Getter
    private final Set<CountryCode> withdrawalCountries;

    // private final String xApiKey;
    // private final String xApiKeySecret;
    // private final String partnerId;

    public RSAPrivateKey getPrivateKey() {
        return RsaUtil.readPrivateKey(cmPrivateKey);
    }

    public RSAPublicKey getPublicKey() {
        return RsaUtil.readPublicKey(cmPublicKey);
    }

    public RSAPublicKey getMlPublicKey() {
        return RsaUtil.readPublicKey(mlPublicKey);
    }
}
