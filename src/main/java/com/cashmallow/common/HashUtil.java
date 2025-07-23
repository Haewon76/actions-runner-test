package com.cashmallow.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
public class HashUtil {

    public static String getMd5Hash(String str) {
        return getHash(HashAlgo.MD5, str);
    }

    public static String getSha512(String text) {
        return getHash(HashAlgo.SHA512, text);
    }

    public static String getSha256(String text) {
        return getHash(HashAlgo.SHA256, text);
    }

    public static String getHash(HashAlgo hex, String text) {
        String result = "";
        try {
            MessageDigest digest = MessageDigest.getInstance(hex.getAlgorithm());
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            result = Hex.encodeHexString(hash, false);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        return result;
    }

    @Getter
    @AllArgsConstructor
    public enum HashAlgo {
        MD5("MD5"),
        SHA256("SHA-256"),
        SHA512("SHA-512");

        private final String algorithm;

    }
}
