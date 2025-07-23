package com.cashmallow.api.interfaces.mallowlink.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@SpringBootTest
class MallowlinkPropertiesTest {

    @Autowired
    MallowlinkProperties properties;

    @Test
    void cashmallow키_로드_확인() {
        // given

        // when
        RSAPrivateKey privateKey = properties.getPrivateKey();
        RSAPublicKey publicKey = properties.getPublicKey();

        // then
        Assertions.assertThat(privateKey).isNotNull();
        Assertions.assertThat(publicKey).isNotNull();

    }

}