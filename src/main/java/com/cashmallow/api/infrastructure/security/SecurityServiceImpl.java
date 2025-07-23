package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.shared.CashmallowException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class SecurityServiceImpl implements SecurityService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityServiceImpl.class);

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION_CBC = ALGORITHM + "/CBC/PKCS5Padding";
    private static final String TRANSFORMATION_GCM = ALGORITHM + "/GCM/NoPadding";

    // Create Key Site https://passwordsgenerator.net/
    // 4 USA XBOX golf XBOX egg PARK GOLF 4 queen DRIP apple $ MUSIC rope apple
    @Value("${encrypt.key128}")
    private String STR_KEY_128;
    // nut ZIP bestbuy & drip rope YELP @ KOREAN walmart yelp korean 9 9 coffee 8 tokyo SKYPE USA & 9 YELP 5 ZIP xbox # 3 fruit = KOREAN QUEEN 5
    @Value("${encrypt.key256}")
    private String STR_KEY_256;
    @Value("${encrypt.file.key128}")
    private String FILE_KEY_128;
    // nut ZIP bestbuy & drip rope YELP @ KOREAN walmart yelp korean 9 9 coffee 8 tokyo SKYPE USA & 9 YELP 5 ZIP xbox # 3 fruit = KOREAN QUEEN 5
    @Value("${encrypt.file.key256}")
    private String FILE_KEY_256;

    private static Random random = new SecureRandom();

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#encryptAES256(java.lang.String)
     */
    @Override
    public String encryptAES256(String decoded) {

        if (StringUtils.isEmpty(decoded)) {
            return null;
        }

        try {
            byte[] key256 = STR_KEY_256.getBytes(StandardCharsets.UTF_8);
            byte[] key128 = STR_KEY_128.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key256, ALGORITHM), new IvParameterSpec(key128));

            byte[] cleartext = decoded.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = cipher.doFinal(cleartext);
            char[] encodeHex = Hex.encodeHex(ciphertextBytes);

            return new String(encodeHex);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#decryptAES256(java.lang.String)
     */
    @Override
    public String decryptAES256(String encoded) {

        if (StringUtils.isEmpty(encoded)) {
            return null;
        }

        try {
            byte[] key256 = STR_KEY_256.getBytes(StandardCharsets.UTF_8);
            byte[] key128 = STR_KEY_128.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);

            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key256, ALGORITHM), new IvParameterSpec(key128));

            char[] cleartext = encoded.toCharArray();
            byte[] decodeHex = Hex.decodeHex(cleartext);
            byte[] plaintextBytes = cipher.doFinal(decodeHex);

            return new String(plaintextBytes);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#cryptFileAES256(int, java.io.File, java.io.File)
     */
    @Override
    public void cryptFileAES256(int mode, File source, File dest) throws CashmallowException {

        OutputStream output = null;

        try (InputStream input = new BufferedInputStream(new FileInputStream(source))) {
            byte[] key256 = FILE_KEY_256.getBytes(StandardCharsets.UTF_8);
            byte[] key128 = FILE_KEY_128.getBytes(StandardCharsets.UTF_8);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);

            cipher.init(mode, new SecretKeySpec(key256, ALGORITHM), new IvParameterSpec(key128));

            output = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[1024];
            int read = -1;
            while ((read = input.read(buffer)) != -1) {
                output.write(cipher.update(buffer, 0, read));
            }
            output.write(cipher.doFinal());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

@Override
public byte[] cryptFileAES256(int mode, byte[] source) throws CashmallowException {
    try {
        byte[] key256 = FILE_KEY_256.getBytes(StandardCharsets.UTF_8);
        byte[] key128 = FILE_KEY_128.getBytes(StandardCharsets.UTF_8);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION_CBC);

        cipher.init(mode, new SecretKeySpec(key256, ALGORITHM), new IvParameterSpec(key128));

        return cipher.doFinal(source);
    } catch (Exception e) {
        logger.error(e.getMessage(), e);
        throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
    }
}

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#encryptAES256(java.lang.String)
     */
    @Override
    public String encryptAES256GCM(String decoded) {

        if (StringUtils.isEmpty(decoded)) {
            return null;
        }

        try {
            byte[] key = STR_KEY_256.getBytes(StandardCharsets.UTF_8);

            byte[] iv = new byte[12]; // NEVER REUSE THIS IV WITH SAME KEY
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_GCM);

            GCMParameterSpec paramSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALGORITHM), paramSpec);

            byte[] cleartext = decoded.getBytes(StandardCharsets.UTF_8);
            byte[] cipherText = cipher.doFinal(cleartext);

            ByteBuffer byteBuffer = ByteBuffer.allocate(4 + iv.length + cipherText.length);
            byteBuffer.putInt(iv.length);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            byte[] cipherMessage = byteBuffer.array();

            char[] encodeHex = Hex.encodeHex(cipherMessage);

            return new String(encodeHex).toUpperCase();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#decryptAES256(java.lang.String)
     */
    @Override
    public String decryptAES256GCM(String encoded) {

        if (StringUtils.isEmpty(encoded)) {
            return null;
        }

        try {

            byte[] key = STR_KEY_256.getBytes(StandardCharsets.UTF_8);

            char[] cleartext = encoded.toCharArray();
            byte[] decodeHex = Hex.decodeHex(cleartext);

            ByteBuffer byteBuffer = ByteBuffer.wrap(decodeHex);
            int ivLength = byteBuffer.getInt();
            if (ivLength < 12 || ivLength >= 16) { // check input parameter
                throw new IllegalArgumentException("invalid iv length");
            }
            byte[] iv = new byte[ivLength];
            byteBuffer.get(iv);
            byte[] cipherText = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherText);

            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, iv));
            byte[] plainText = cipher.doFinal(cipherText);

            return new String(plainText);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.cashmallow.api.infrastructure.security.SecurityService#encryptSHA2(String)
     */
    @Override
    public String encryptSHA2(String decoded) {
        StringBuilder encoded = new StringBuilder();
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            String trDecoded = "~" + decoded + "1";
            byte[] hash = digest.digest(trDecoded.getBytes(StandardCharsets.UTF_8));
            for (byte b : hash) {
                encoded.append(String.format("%02X", b));
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return encoded.toString();
    }


    public String encryptSeedEcb(String pbszUserKey, String decoded) {
        try {
            byte[] cleartext = decoded.getBytes(StandardCharsets.UTF_8);
            byte[] ciphertextBytes = KISA_SEED_ECB.SEED_ECB_Encrypt(pbszUserKey.getBytes(), cleartext, 0, cleartext.length);
            char[] encodeHex = Hex.encodeHex(ciphertextBytes);

            return new String(encodeHex);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

    public String decryptSeedEcb(String pbszUserKey, String encoded) {
        try {
            char[] cleartext = encoded.toCharArray();
            byte[] decodeHex = Hex.decodeHex(cleartext);
            byte[] plaintextBytes = KISA_SEED_ECB.SEED_ECB_Decrypt(pbszUserKey.getBytes(), decodeHex, 0, decodeHex.length);

            return new String(plaintextBytes);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }

}
