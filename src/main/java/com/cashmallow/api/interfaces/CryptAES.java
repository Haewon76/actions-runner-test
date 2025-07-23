package com.cashmallow.api.interfaces;

import com.cashmallow.common.CommNet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.Checksum;

public class CryptAES {
    private static final Logger logger = LoggerFactory.getLogger(CryptAES.class);

    private static final String AesCbcPkcsPadding = "AES/CBC/PKCS5Padding";
    private static final String initVector = "#!cashmallow!is!best!for!you!#";

    // 출처: http://www.enjoydev.com/memo/405
    static final int AES_KEY_SIZE_128 = 128;

    //    private static byte[] aesEncryptEcb(String sKey, String sText) {
    //        byte[] key = null;
    //        byte[] text = null;
    //        byte[] encrypted = null;
    //
    //        try {
    //            // UTF-8
    //            key = sKey.getBytes("UTF-8");
    //
    //            // Key size 맞춤 (128bit, 16byte)
    //            key = Arrays.copyOf(key, AES_KEY_SIZE_128 / 8);
    //
    //            // UTF-8
    //            text = sText.getBytes("UTF-8");
    //
    //            // AES/EBC/PKCS5Padding
    //            Cipher cipher = Cipher.getInstance(Const.AesEcbPkcsPadding); // "AES/ECB/PKCS5Padding");
    //            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
    //            encrypted = cipher.doFinal(text);
    //        } catch (Exception e) {
    //            encrypted = null;
    //        }
    //
    //        return encrypted;
    //    }

    //    private static byte[] aesDecryptEcb(String sKey, byte[] encrypted) {
    //        byte[] key = null;
    //        byte[] decrypted = null;
    //        final int AES_KEY_SIZE_128 = 128;
    //
    //        try {
    //            // UTF-8
    //            key = sKey.getBytes("UTF-8");
    //
    //            // Key size 맞춤 (128bit, 16byte)
    //            key = Arrays.copyOf(key, AES_KEY_SIZE_128 / 8);
    //
    //            // AES/EBC/PKCS5Padding
    //            Cipher cipher = Cipher.getInstance(Const.AesEcbPkcsPadding);   // "AES/ECB/PKCS5Padding");
    //            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
    //            decrypted = cipher.doFinal(encrypted);
    //        } catch (Exception e) {
    //            decrypted = null;
    //        }
    //
    //        return decrypted;
    //    }
    //
    //    private static byte[] aesEncryptCbc(String sKey, String sText) {
    //        return aesEncryptCbc(sKey, sText, "");
    //    }
    //
    //    private static byte[] aesDecryptCbc(String sKey, byte[] encrypted) {
    //        return aesDecryptCbc(sKey, encrypted, "");
    //    }

    private static byte[] aesEncryptCbc(String sKey, String sText, String sInitVector) {

        byte[] key = null;
        byte[] text = null;
        byte[] iv = null;
        byte[] encrypted = null;

        try {
            // UTF-8
            key = sKey.getBytes(StandardCharsets.UTF_8);

            // Key size 맞춤 (128bit, 16byte)
            key = Arrays.copyOf(key, AES_KEY_SIZE_128 / 8);

            // UTF-8
            text = sText.getBytes(StandardCharsets.UTF_8);

            if (sInitVector != null) {
                // UTF-8
                iv = sInitVector.getBytes(StandardCharsets.UTF_8);

                // Key size 맞춤 (128bit, 16byte)
                iv = Arrays.copyOf(iv, AES_KEY_SIZE_128 / 8);

                // AES/EBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance(AesCbcPkcsPadding);
                IvParameterSpec ips = new IvParameterSpec(iv);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), ips);
                encrypted = cipher.doFinal(text);
            } else {
                // AES/EBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance(AesCbcPkcsPadding);
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
                encrypted = cipher.doFinal(text);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            encrypted = null;
        }

        return encrypted;
    }

    //    private static byte[] aesDecryptCbc(String sKey, byte[] encrypted, String sInitVector) {
    //        return aesDecryptCbc(sKey, encrypted, 0, encrypted.length, sInitVector);
    //    }

    private static byte[] aesDecryptCbc(String sKey, byte[] encrypted, int start, int len, String sInitVector) {
        byte[] key = null;
        byte[] iv = null;
        byte[] decrypted = null;
        final int AES_KEY_SIZE_128 = 128;

        try {
            // UTF-8
            key = sKey.getBytes(StandardCharsets.UTF_8);

            // Key size 맞춤 (128bit, 16byte)
            key = Arrays.copyOf(key, AES_KEY_SIZE_128 / 8);

            if (sInitVector != null) {
                // UTF-8
                iv = sInitVector.getBytes(StandardCharsets.UTF_8);

                // Key size 맞춤 (128bit, 16byte)
                iv = Arrays.copyOf(iv, AES_KEY_SIZE_128 / 8);

                // AES/EBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance(AesCbcPkcsPadding);
                IvParameterSpec ips = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), ips);
                decrypted = cipher.doFinal(encrypted, start, len);
            } else {
                // AES/EBC/PKCS5Padding
                Cipher cipher = Cipher.getInstance(AesCbcPkcsPadding);
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
                decrypted = cipher.doFinal(encrypted);
            }
        } catch (Exception e) {
            decrypted = null;
            // logger.error(e.getMessage(), e);
        }

        return decrypted;
    }

    private static byte[] toByteArray(String hexaStr) {
        byte[] result = null;

        if (hexaStr != null) {
            int len1 = hexaStr.length();
            int len2 = len1 >> 1;
            int len3 = len2 << 1;

            if (len1 == len3 && len2 >= 1) {
                int idx = 0;
                char[] chs = hexaStr.toUpperCase().toCharArray();
                result = new byte[len2];

                for (int nI = 0; nI < len3; nI += 2) {
                    char ch1 = chs[nI + 0];
                    char ch2 = chs[nI + 1];
                    byte by1 = (byte) (('0' <= ch1 && ch1 <= '9') ? ch1 - '0' : (('A' <= ch1 && ch1 <= 'F') ? ch1 - 'A' + 10 : -1));
                    byte by2 = (byte) (('0' <= ch2 && ch2 <= '9') ? ch2 - '0' : (('A' <= ch2 && ch2 <= 'F') ? ch2 - 'A' + 10 : -1));

                    if (by1 != -1 && by2 != -1) {
                        result[idx++] = (byte) (by1 << 4 | by2);
                    } else {
                        result = null;
                        break;
                    }
                }
            }
        }

        return result;
    }


    // 출처: Checksum crc32 = (Checksum) newjava.util.zip.CRC32()
    private static long getCRC32(byte[] bytes) {
        return getCRC32(bytes, 0);
    }

    private static long getCRC32(byte[] bytes, int start) {
        Checksum crc32 = (Checksum) new java.util.zip.CRC32();
        crc32.update(bytes, start, bytes.length - start);
        long calc = (int) crc32.getValue();
        crc32.reset();
        return calc;
    }


    // 기능: 암호화 후 CRC32 + 암호환된 데이터를 응답한다. 
    private static byte[] toAesCbcWithCrc32(String key, String txt, String initVector) {
        byte[] enc = aesEncryptCbc(key, txt, initVector);
        long crc32 = getCRC32(enc);   // 1800533767
        // String crc32Str = Long.toHexString(crc32);  // 6b51f707
        byte[] crc = Convert.toByteArray((int) crc32);
        byte[] crc_enc = new byte[enc.length + crc.length];  // 107, 81, -9, 7 = 6b 51 f7 07
        // long crc33 = Convert.toInt(crc);   //
        System.arraycopy(crc, 0, crc_enc, 0, crc.length);
        System.arraycopy(enc, 0, crc_enc, crc.length, enc.length);
        return crc_enc;
    }

    // 기능: CRC32 + 암호화된 데이터를 복호화한다. 단, CRC가 맞지 않으면 null를 응답한다.. 
    private static byte[] aesCbcWithCrc32ToString(String sKey, byte[] encWithCrc32, String sInitVector) {
        int start = 4;

        if (encWithCrc32 != null && encWithCrc32.length >= start) {
            long crc = Convert.toInt(encWithCrc32);
            long crc32 = getCRC32(encWithCrc32, start);

            if (crc == crc32) {
                return aesDecryptCbc(sKey, encWithCrc32, start, encWithCrc32.length - start, sInitVector);
            }
        }

        return null;
    }

    /**
     * txt를 암호화한다.
     *
     * @param key
     * @param txt
     * @return
     */
    public static String encode(String key, String txt) {
        byte[] enc = null;

        try {
            enc = toAesCbcWithCrc32(key, txt, initVector);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return (enc != null) ? CommNet.toHexString(enc) : null;
    }

    /**
     * hexaStr를 복호화한다.
     *
     * @param key
     * @param hexaStr
     * @return
     */
    public static String decode(String key, String hexaStr) {
        try {
            if (hexaStr != null) {
                byte[] hexaArray = toByteArray(hexaStr);

                if (hexaArray != null) {
                    byte[] decrypted = aesCbcWithCrc32ToString(key, hexaArray, initVector);

                    if (decrypted != null) {
                        return new String(decrypted, StandardCharsets.UTF_8);
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("key: {}, hexaStr: {}", key, hexaStr);
            logger.debug(e.getMessage(), e);
        }

        return null;
    }
}
