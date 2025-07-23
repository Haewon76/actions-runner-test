package com.cashmallow.api.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Component
public class CashmallowEncrypt {

    public static String alg = "AES/CBC/PKCS5Padding";

    private static String KEY;

    @Value("${encrypt.key256}")
    public void setKey(String key) {
        KEY = key;
    }

    private static String IV;

    @Value("${encrypt.key128}")
    public void setIV(String iv) {
        IV = iv;
    }

    // 암호화
    public static byte[] encryptAES256(byte[] text) {
        try {
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
            return cipher.doFinal(text);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    // 복호화
    public static byte[] decryptAES256(byte[] text) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance(alg);
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes(), "AES");
            IvParameterSpec ivParamSpec = new IvParameterSpec(IV.getBytes());
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
            return cipher.doFinal(text);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
