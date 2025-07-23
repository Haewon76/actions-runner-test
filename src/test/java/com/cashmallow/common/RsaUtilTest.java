package com.cashmallow.common;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
class RsaUtilTest {

    RSAPublicKey publicKey;
    RSAPrivateKey privateKey;

    @BeforeEach
    void init() {
        try {
            KeyPairGenerator rsa = KeyPairGenerator.getInstance("RSA");
            rsa.initialize(2048);
            KeyPair keyPair = rsa.generateKeyPair();
            publicKey = (RSAPublicKey) keyPair.getPublic();
            privateKey = (RSAPrivateKey) keyPair.getPrivate();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void byte_개인키_공개키() {
        // given
        String plainText = "{298_ㄴ루2ㅐ.ㅋㅇ02S2nho#$!^.-6=?5s0a}";

        // when
        byte[] bytes = RsaUtil.encryptRsa2048(privateKey, plainText.getBytes());
        byte[] bytes1 = RsaUtil.decryptRsa2048(publicKey, bytes);

        // then
        String s = new String(bytes1);
        System.out.println("s = " + s);
        Assertions.assertThat(plainText).isEqualTo(s);

    }

    @Test
    void byte_공개키_개인키() {
        // given
        String plainText = "{298_ㄴ루2ㅐ.ㅋㅇ02S2nho#$!^.-6=?5s0a}";

        // when
        byte[] bytes = RsaUtil.encryptRsa2048(publicKey, plainText.getBytes());
        byte[] bytes1 = RsaUtil.decryptRsa2048(privateKey, bytes);

        // then
        String s = new String(bytes1);
        System.out.println("s = " + s);
        Assertions.assertThat(plainText).isEqualTo(s);

    }

    @Test
    void string_개인키_공개키() {
        // given
        String plainText = "{298_ㄴ루2ㅐ.ㅋㅇ02S2nho#$!^.-6=?5s0a}";

        // when
        String encrypted = RsaUtil.encryptRsa2048(privateKey, plainText);
        System.out.println("encrypted = " + encrypted);
        String decrypted = RsaUtil.decryptRsa2048(publicKey, encrypted);

        // then
        System.out.println("decrypted = " + decrypted);
        Assertions.assertThat(plainText).isEqualTo(decrypted);

    }

    @Test
    void string_공개키_개인키() {
        // given
        String plainText = "{298_ㄴ루2ㅐ.ㅋㅇ02S2nho#$!^.-6=?5s0a}";

        // when
        String encrypted = RsaUtil.encryptRsa2048(publicKey, plainText);
        System.out.println("encrypted = " + encrypted);
        String decrypted = RsaUtil.decryptRsa2048(privateKey, encrypted);

        // then
        System.out.println("decrypted = " + decrypted);
        Assertions.assertThat(plainText).isEqualTo(decrypted);

    }

}