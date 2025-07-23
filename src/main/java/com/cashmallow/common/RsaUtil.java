package com.cashmallow.common;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;

import javax.crypto.Cipher;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class RsaUtil {
    public static final String RSA_ALGORITHM = "RSA";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static RSAPublicKey readPublicKey(String publicKey) {
        try (StringReader stringReader = new StringReader(publicKey);
             PemReader pemReader = new PemReader(stringReader)) {

            KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(content);
            return (RSAPublicKey) factory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static RSAPrivateKey readPrivateKey(String privateKey) {
        try (StringReader stringReader = new StringReader(privateKey);
             PemReader pemReader = new PemReader(stringReader)) {

            KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);

            PemObject pemObject = pemReader.readPemObject();
            byte[] content = pemObject.getContent();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(content);
            return (RSAPrivateKey) factory.generatePrivate(privateKeySpec);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static byte[] encryptRsa2048(RSAPrivateKey privateKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] encryptRsa2048(RSAPublicKey publicKey, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptRsa2048(RSAPrivateKey privateKey, byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encrypted);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static byte[] decryptRsa2048(RSAPublicKey publicKey, byte[] encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(encrypted);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 평문을 암호화 후 BASE64URL로 인코딩
     *
     * @param privateKey
     * @param text
     * @return
     */
    public static String encryptRsa2048(RSAPrivateKey privateKey, String text) {
        byte[] bytes = encryptRsa2048(privateKey, text.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    /**
     * 평문을 암호화 후 BASE64URL로 인코딩
     *
     * @param publicKey
     * @param text
     * @return
     */
    public static String encryptRsa2048(RSAPublicKey publicKey, String text) {
        byte[] bytes = encryptRsa2048(publicKey, text.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().encodeToString(bytes);
    }

    /**
     * BASE64URL로 인코딩된 암호문을 평문으로 복호화
     *
     * @param privateKey
     * @param encryptedText
     * @return
     */
    public static String decryptRsa2048(RSAPrivateKey privateKey, String encryptedText) {
        byte[] decode = Base64.getUrlDecoder().decode(encryptedText);
        byte[] bytes = decryptRsa2048(privateKey, decode);
        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

    /**
     * BASE64URL로 인코딩된 암호문을 평문으로 복호화
     *
     * @param publicKey
     * @param encryptedText
     * @return
     */
    public static String decryptRsa2048(RSAPublicKey publicKey, String encryptedText) {
        byte[] decode = Base64.getUrlDecoder().decode(encryptedText);
        byte[] bytes = decryptRsa2048(publicKey, decode);

        return new String(bytes, StandardCharsets.UTF_8).trim();
    }

}
