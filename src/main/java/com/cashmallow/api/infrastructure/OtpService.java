package com.cashmallow.api.infrastructure;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final UserRepositoryService userService;
    private final SecurityService securityService;

    private boolean validOtp(String code, String encodedKey) {
        try {
            // 키, 코드, 시간으로 일회용 비밀번호가 맞는지 일치 여부 확인.
            return checkOtpCode(encodedKey, Integer.parseInt(code), new Date().getTime() / 30000);
        } catch (InvalidKeyException e) {
            log.error(e.getMessage(), e);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public String addOtpKey(String email) {
        User user = userService.getUserByLoginId(email.replaceAll("[^A-Za-z0-9]", ""));
        if (user != null) {
            user.setLogin(securityService.encryptAES256(generateOtpKey()));
            userService.insertOtp(user);
            return securityService.decryptAES256(userService.getUserOtp(user.getId()));
        }
        return null;
    }

    public boolean isValidOtp(String email, String code) {
        try {
            User user = userService.getUserByLoginId(email.replaceAll("[^A-Za-z0-9]", ""));
            if (user != null) {
                final String encodedKey = securityService.decryptAES256(userService.getUserOtp(user.getId()));
                return validOtp(code, encodedKey);
            }
        } catch (Exception e) {
        }
        return false;
    }

    private String generateOtpKey() {
        final int MAX_LENGTH = 10;
        // Allocating the buffer
        //      byte[] buffer = new byte[secretSize + numOfScratchCodes * scratchCodeSize];
        byte[] buffer = new byte[5 + 5 * 5];

        // Filling the buffer with random numbers.
        // Notice: you want to reuse the same random generator
        // while generating larger random number sequences.
        new Random().nextBytes(buffer);

        // Getting the key and converting it to Base32
        Base32 codec = new Base32();
        //      byte[] secretKey = Arrays.copyOf(buffer, secretSize);
        byte[] secretKey = Arrays.copyOf(buffer, 10);
        byte[] bEncodedKey = codec.encode(secretKey);

        // 생성된 Key!
        String encodedKey = new String(bEncodedKey);

        System.out.println("encodedKey : " + encodedKey);

        // String url = getQRBarcodeURL(email, encodedKey); // 생성된 바코드 주소!

        return encodedKey;
    }

    private String getQRBarcodeURL(String email, String secret) {
        String format = "https://www.google.com/chart?chs=300x300&chld=M%%7C0&cht=qr&chl=otpauth://totp/%s%%3Fsecret%%3D%s";
        return String.format(format, email, secret);
    }

    private boolean checkOtpCode(String secret, long code, long t) throws NoSuchAlgorithmException, InvalidKeyException {
        Base32 codec = new Base32();
        byte[] decodedKey = codec.decode(secret);

        // Window is used to check codes generated in the near past.
        // You can use this value to tune how far you're willing to go.
        int window = 3;
        for (int i = -window; i <= window; ++i) {
            long hash = verifyCode(decodedKey, t + i);

            if (hash == code) {
                return true;
            }
        }

        // The validation code is invalid.
        return false;
    }

    private int verifyCode(byte[] key, long t)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] data = new byte[8];
        long value = t;
        for (int i = 8; i-- > 0; value >>>= 8) {
            data[i] = (byte) value;
        }

        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[20 - 1] & 0xF;

        // We're using a long because Java hasn't got unsigned int.
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            // We are dealing with signed bytes:
            // we just keep the first byte.
            truncatedHash |= (hash[offset + i] & 0xFF);
        }

        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return (int) truncatedHash;
    }
}
