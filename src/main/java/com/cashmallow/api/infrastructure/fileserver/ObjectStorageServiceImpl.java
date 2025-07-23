package com.cashmallow.api.infrastructure.fileserver;

import com.cashmallow.api.application.FileService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.remittance.RemittanceDepositReceipt;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.common.EnvUtil;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.crypto.Cipher;
import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Random;

@Service
public class ObjectStorageServiceImpl implements FileService {
    private static final Logger logger = LoggerFactory.getLogger(ObjectStorageServiceImpl.class);

    // ErrorMessage
    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";

    // Random
    private static final Random random = new SecureRandom();

    @Autowired
    private EnvUtil envUtil;

    // to make random file name
    private static final String ALPHA_NUMERIC_STRING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    @Value("${host.file.path.home}")
    private String hostFilePathHome;

    @Autowired
    private SecurityService securityService;

    private final MinioClient minioClient;
    private final String minioBucketName;

    public ObjectStorageServiceImpl(
            @Value("${minio.endpoint}") String minioEndpoint,
            @Value("${minio.accessKey}") String minioAccessKey,
            @Value("${minio.secretKey}") String minioSecretKey,
            @Value("${minio.bucketName}") String minioBucketName,
            @Value("${minio.regionName}") String minioRegionName
    ) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioEndpoint)
                .credentials(minioAccessKey, minioSecretKey)
                .region(minioRegionName)
                .build();

        this.minioBucketName = minioBucketName;
    }

    /**
     * Download a file
     *
     * @param kind
     * @param fileName
     * @return
     */
    @Override
    @SneakyThrows
    public File download(String kind, String fileName) throws CashmallowException {
        // get object given the bucket and object name
        String pathName = kind + File.separator + fileName;
        logger.debug("download() pathName: {}", pathName);

        if (Const.FILE_SERVER_PASSPORT.equals(kind)) {
            pathName = Const.FILE_SERVER_CERTIFICATION + fileName;
        }
        return getObjectMinio(pathName);
    }

    @SneakyThrows
    private File getObjectMinio(String pathName) {
        logger.debug("getObjectMinio() pathName: {}", pathName);
        File targetFile = new File(hostFilePathHome + File.separator + pathName);
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(pathName)
                        .build())) {
            Files.createDirectories(targetFile.toPath().getParent());
            Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (MinioException e) {
            logger.error("getObjectMinio() error: " + e.getMessage());
            throw new CashmallowException(INTERNAL_SERVER_ERROR, e);
        }
        return targetFile;
    }

    /**
     * upload a file and delete the old file from storage
     *
     * @param mf
     * @param fileServerDir
     * @return
     * @throws CashmallowException
     */
    @SneakyThrows
    @Override
    public String upload(MultipartFile mf, String fileServerDir) throws CashmallowException {
        logger.debug("upload() fileServerDir: {}", fileServerDir);
        try {
            // Upload known sized input stream.
            Path saveFilePath = makeRandomFileName(fileServerDir);
            String result = uploadStream(mf.getInputStream(), fileServerDir, saveFilePath);
            if (StringUtils.isNotEmpty(result)) {
                return saveFilePath.getFileName().toString();
            }
        } catch (MinioException e) {
            logger.error("upload() error: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Upload file to storage and delete old file
     *
     * @param file
     * @param fileServerDir
     * @return
     * @throws CashmallowException
     */
    @SneakyThrows
    @Override
    public String upload(File file, String fileServerDir) throws CashmallowException {
        String contentType = Files.probeContentType(file.toPath());
        FileItem fileItem = new DiskFileItem("file", contentType, false, file.getName(), (int) file.length(), file.getParentFile());

        try {
            InputStream input = new FileInputStream(file);
            OutputStream os = fileItem.getOutputStream();
            IOUtils.copy(input, os);
        } catch (IOException e) {
            logger.error("upload() convert input stream error: " + e.getMessage(), e);
        }

        MultipartFile mf = new CommonsMultipartFile(fileItem);
        Path saveFilePath = Paths.get(fileServerDir, file.getName());
        return uploadStream(mf.getInputStream(), fileServerDir, saveFilePath);
    }

    @SneakyThrows
    public String uploadNoEncrypt(File file, String fileServerDir) {
        String contentType = Files.probeContentType(file.toPath());
        FileItem fileItem = new DiskFileItem("file", contentType, false, file.getName(), (int) file.length(), file.getParentFile());

        try {
            InputStream input = new FileInputStream(file);
            OutputStream os = fileItem.getOutputStream();
            IOUtils.copy(input, os);
        } catch (IOException e) {
            logger.error("upload() convert input stream error: " + e.getMessage(), e);
        }

        MultipartFile mf = new CommonsMultipartFile(fileItem);
        Path saveFilePath = Paths.get(fileServerDir, file.getName());

        InputStream stream = mf.getInputStream();
        int length = stream.available();
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                PutObjectArgs.builder().bucket(minioBucketName).object(saveFilePath.toString()).stream(
                                stream, length, -1)
                        // .contentType("video/mp4")
                        .build());
        return objectWriteResponse.object();
    }

    /**
     * Uploads a stream of data to storage and deletes the old file.
     *
     * @param stream        The input stream of data to be uploaded.
     * @param fileServerDir The directory in the file server where the file is to be saved.
     * @param saveFilePath  The path of the file to be saved in the storage system.
     * @return The name of the object after it has been uploaded.
     * @throws MinioException If there is an error during the upload process.
     */
    @SneakyThrows
    private String uploadStream(InputStream stream, String fileServerDir, Path saveFilePath) throws MinioException {
        logger.debug("upload() filePath={}", saveFilePath.toString());

        // 신분증 암호화 처리
        if (fileServerDir.equals(Const.FILE_SERVER_CERTIFICATION) || fileServerDir.contains(Const.FILE_SERVER_AUTHME)) {
            File decryptedFile = new File(hostFilePathHome + File.separator + saveFilePath + "-decrypted");
            String encryptedFileDir = hostFilePathHome + fileServerDir;
            File encryptedFile = Paths.get(encryptedFileDir + File.separator + saveFilePath.getFileName()).toFile();

            Files.copy(stream, decryptedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            securityService.cryptFileAES256(Cipher.ENCRYPT_MODE, decryptedFile, encryptedFile);
            saveFilePath = Paths.get(fileServerDir + File.separator + encryptedFile.getName());
            logger.debug("upload() oss_save_path={}", saveFilePath);
            stream = Files.newInputStream(encryptedFile.toPath());


            // 서버 파일 삭제 - 암호화 파일
            if (encryptedFile.exists()) {
                Files.delete(encryptedFile.toPath());
            }

            // 서버 파일 삭제 - 복호화 파일
            if (decryptedFile.exists()) {
                Files.delete(decryptedFile.toPath());
            }
        }

        int length = stream.available();
        ObjectWriteResponse objectWriteResponse = minioClient.putObject(
                PutObjectArgs.builder().bucket(minioBucketName).object(saveFilePath.toString()).stream(
                                stream, length, -1)
                        // .contentType("video/mp4")
                        .build());
        return objectWriteResponse.object();
    }

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
    @Override
    @SneakyThrows
    public void rotagePhoto(boolean isAdmin, Object object, String fileServerDir, String photo, String direction) throws CashmallowException {
        String method = "editPhoto()";

        logger.info("{}: fileServerDir={}, photo={}, direction={}", method, fileServerDir, photo, direction);

        String fileServerDirPath = hostFilePathHome + fileServerDir;

        boolean isSameFile = false;

        // If PASSPORT container, change the fileServerDir to CERTIFICATION container
        if (fileServerDir.equals(Const.FILE_SERVER_PASSPORT)) {
            fileServerDir = Const.FILE_SERVER_CERTIFICATION;
        }

        // Admin 이 아닌 경우 아래 조건 체크
        if (!isAdmin) {
            if (fileServerDir.equals(Const.FILE_SERVER_PROFILE)) {
                User user = (User) object;
                isSameFile = photo.equals(user.getProfilePhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_CERTIFICATION)) {
                Traveler traveler = (Traveler) object;
                isSameFile = photo.equals(traveler.getCertificationPhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_RECEIPT)) {
                Exchange exchange = (Exchange) object;
                isSameFile = photo.equals(exchange.getTrReceiptPhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_RECEIPT_REMIT)) {
                RemittanceDepositReceipt receipt = (RemittanceDepositReceipt) object;
                isSameFile = photo.equals(receipt.getReceiptPhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_BANKBOOK)) {
                // Refund refund = (Refund)object;
                // isSameFile = photo.equals(refund.getTrBankbookPhoto());
                Traveler traveler = (Traveler) object;
                isSameFile = photo.equals(traveler.getAccountBankbookPhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_SHOP)) {
                WithdrawalPartner withdrawalPartner = (WithdrawalPartner) object;
                isSameFile = photo.equals(withdrawalPartner.getShopPhoto());

            } else if (fileServerDir.equals(Const.FILE_SERVER_BIZ)) {
                WithdrawalPartner withdrawalPartner = (WithdrawalPartner) object;
                isSameFile = photo.equals(withdrawalPartner.getBusinessPhoto());
            } else if (fileServerDir.equals(Const.FILE_SERVER_ADDRESS)) {
                Traveler traveler = (Traveler) object;
                isSameFile = photo.equals(traveler.getAddressPhoto());
            }
        }

        if (!isAdmin && !isSameFile) {
            logger.error("{}: isAdmin is false and isSameFile is false. isAdmin={}, isSameFile={}", method, isAdmin, isSameFile);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // Set KT ucloud storage information
        // Map<String, String> containerMap = getContainerMap(fileServerDir);

        // Get file from KT ucloud
        File saveFile = getObject(minioBucketName, fileServerDir, photo);

        // If the file does not exist in KT ucloud, get the file in Host server.
        if (!saveFile.exists()) {
            // try to get the image from PASSPORT container for old version.
            if (fileServerDir.equals(Const.FILE_SERVER_CERTIFICATION)) {
                // Map<String, String> passportContainerMap = getContainerMap(Const.FILE_SERVER_PASSPORT);
                saveFile = getObject(minioBucketName, Const.FILE_SERVER_PASSPORT, photo);
            }

            if (!saveFile.exists()) {
                String filePathString = fileServerDirPath + File.separator + photo;
                saveFile = new File(filePathString);
                if (!saveFile.exists()) {
                    logger.error("{}: File Not Found : filePathString={}", method, filePathString);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }
        }

        logger.info("{}: file_path={}", method, saveFile.getPath());

        File decryptedFile = null;

        try {

            if (fileServerDir.equals(Const.FILE_SERVER_CERTIFICATION) || fileServerDir.contains(Const.FILE_SERVER_AUTHME)) {

                String decryptedFileDir = hostFilePathHome + File.separator + fileServerDir;
                Path decryptedFilePath = makeRandomFileName(decryptedFileDir);

                File decryptedFileParent = decryptedFilePath.getParent().toFile();
                if (decryptedFileParent.exists() || decryptedFileParent.mkdirs()) {
                    decryptedFile = decryptedFilePath.toFile();
                } else {
                    logger.error("{}: mkdir failure", method);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }

                securityService.cryptFileAES256(Cipher.DECRYPT_MODE, saveFile, decryptedFile);

                if (decryptedFile.exists()) {
                    // saveFile = decryptedFile;
                    Files.copy(decryptedFile.toPath(), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } else {
                    logger.error("{}: decryptedFile generation failure", method);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }

            BufferedImage image = ImageIO.read(saveFile);

            AffineTransform tx = new AffineTransform();
            int oWidth = image.getWidth();
            int oHeight = image.getHeight();

            int tWidth = oHeight;
            int tHeight = oWidth;

            // default : right rotation
            int centerX = oHeight / 2;
            int centerY = centerX;
            int numquadrants = 1;

            logger.info("{}: centerX={}, centerY={}", method, centerX, centerY);

            if ("left".equals(direction)) {
                centerX = oWidth / 2;
                centerY = centerX;
                numquadrants = 3;
            }

            tx.setToQuadrantRotation(numquadrants, centerX, centerY);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage transformedImage = new BufferedImage(tWidth, tHeight, image.getType());
            image = op.filter(image, transformedImage);

            ImageIO.write(image, "JPEG", saveFile);
            // ImageIO.write(image, "PNG", saveFile);

            upload(saveFile, fileServerDir);


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();

        } finally {
            if (decryptedFile != null) {
                try {
                    Files.delete(decryptedFile.toPath());
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    /**
     * Make random file name for the path
     *
     * @param path
     * @return
     * @throws NoSuchAlgorithmException
     */
    @Override
    public Path makeRandomFileName(String path) {
        int fileNameLength = 32;
        Long d = new Date().getTime();
        String postfix = Long.toHexString(d);
        String randomString = generateRandomAlphaNumeric(fileNameLength - postfix.length());
        String fileName = randomString + postfix;

        logger.info("makeRandomFileName() : id={}", fileName);

        Path filePath = Paths.get(path + File.separator + fileName);
        while (filePath.toFile().exists()) {
            randomString = generateRandomAlphaNumeric(fileNameLength - postfix.length());
            fileName = randomString + postfix;
            filePath = Paths.get(path + File.separator + fileName);
        }

        return filePath;
    }

    /**
     * get a file from KT ucloud storage
     *
     * @param container
     * @param path
     * @param fileName
     */
    @SneakyThrows
    @Override
    public File getObject(String container, String path, String fileName) {
        String pathName = path + File.separator + fileName;
        return getObjectMinio(pathName);
    }

    @Override
    @SneakyThrows
    public byte[] getImageByte(String path, String fileName) {
        String pathName = path + File.separator + fileName;

        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(minioBucketName)
                        .object(pathName)
                        .build())) {

            return stream.readAllBytes();
        } catch (MinioException e) {
            logger.error("getObjectMinio() error: " + e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @SneakyThrows
    public String getImageBase64(String path, String fileName) {
        byte[] imageByte = getImageByte(path, fileName);
        // 암호화된 이미지 복호화
        byte[] decryptedImageByte = securityService.cryptFileAES256(Cipher.DECRYPT_MODE, imageByte);
        return Base64.getEncoder().encodeToString(decryptedImageByte);
    }

    private String generateRandomAlphaNumeric(int count) {
        logger.info("generateRandomAlphaNumeric() : count={}", count);

        StringBuilder builder = new StringBuilder();

        while (count-- != 0) {
            int character = random.nextInt(ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }

        return builder.toString();
    }

    @Override
    @SneakyThrows
    public void deleteFile(String fileServerDir, String fileName) throws CashmallowException {
        String pathName = fileServerDir + File.separator + fileName;
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioBucketName)
                            .object(pathName)
                            .build()
            );
            logger.info("File deleted successfully: {}", pathName);
        } catch (MinioException e) {
            logger.error("Error deleting file: {}", pathName, e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }
}
