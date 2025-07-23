package com.cashmallow.api.application;

import com.cashmallow.api.domain.shared.CashmallowException;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public interface FileService {

    /**
     * File edit actions
     *
     * @author swshin
     */
    public enum Action {
        // rotate action
        ROTATE
    }

    /**
     * Download a file
     *
     * @param kind
     * @param fileName
     * @return
     * @throws CashmallowException
     */
    File download(String kind, String fileName) throws CashmallowException;

    /**
     * upload a file and delete the old file from storage
     *
     * @param mf
     * @param fileServerDir
     * @return
     * @throws CashmallowException
     */
    String upload(MultipartFile mf, String fileServerDir) throws CashmallowException;

    /**
     * Upload file to storage and delete old file
     *
     * @param file
     * @param fileServerDir
     * @return
     * @throws CashmallowException
     */
    String upload(File file, String fileServerDir) throws CashmallowException;

    /**
     * 원본 파일을 저장
     *
     * @param file
     * @param fileServerDir
     * @return
     */
    String uploadNoEncrypt(File file, String fileServerDir);

    /**
     * Rotate the image in storage. The file name does not be changed.
     *
     * @param isAdmin
     * @param object
     * @param fileServerDir
     * @param photo
     * @param direction
     * @throws CashmallowException
     */
    void rotagePhoto(boolean isAdmin, Object object, String fileServerDir, String photo, String direction) throws CashmallowException;

    /**
     * Make random file name for the path
     *
     * @param path
     * @return
     * @throws NoSuchAlgorithmException
     */
    public Path makeRandomFileName(String path) throws NoSuchAlgorithmException;

    /**
     * get a file from KT ucloud storage
     *
     * @param container
     * @param path
     * @param fileName
     */
    File getObject(String container, String path, String fileName);

    byte[] getImageByte(String path, String fileName);
    String getImageBase64(String path, String fileName);

    void deleteFile(String path, String fileName) throws CashmallowException;
}