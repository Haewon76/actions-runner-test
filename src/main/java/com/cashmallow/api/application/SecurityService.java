package com.cashmallow.api.application;

import com.cashmallow.api.domain.shared.CashmallowException;

import java.io.File;

public interface SecurityService {

    /**
     * Encrypt AES256
     *
     * @param decoded decoded string
     * @return
     */
    String encryptAES256(String decoded);

    /**
     * Decrypt AES256
     *
     * @param encoded encoded string
     * @return
     */
    String decryptAES256(String encoded);

    /**
     * 원본 파일을 암/복호화해서 대상 파일을 만든다.
     *
     * @param mode   암/복호화 모드
     * @param source 원본 파일
     * @param dest   대상 파일
     * @throws Exception
     */
    void cryptFileAES256(int mode, File source, File dest) throws CashmallowException;
    byte[] cryptFileAES256(int mode, byte[] source) throws CashmallowException;

    /**
     * Encrypt AES256
     *
     * @param decoded decoded string
     * @return 74 bytes
     */
    String encryptAES256GCM(String decoded);

    /**
     * Decrypt AES256
     *
     * @param encoded encoded string (74 bytes)
     * @return
     */
    String decryptAES256GCM(String encoded);

    /**
     * Encrypt KISA SEED ECB
     *
     * @param pbszUserKey
     * @param decoded
     * @return
     */
    String encryptSeedEcb(String pbszUserKey, String decoded);

    /**
     * Decrypt KISA SEED ECB
     *
     * @param pbszUserKey
     * @param encoded
     * @return
     */
    String decryptSeedEcb(String pbszUserKey, String encoded);

    /**
     * Encrypt string with SHA2
     *
     * @param decoded string
     * @return
     */
    String encryptSHA2(String decoded);

}