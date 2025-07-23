package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.FileService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import javax.crypto.Cipher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class FileServerController {

    private final Logger logger = LoggerFactory.getLogger(FileServerController.class);

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    // 이미지 파일 조회
    @Deprecated
    @GetMapping(value = "/FILE_SERVER/{kind}/{fileName}")
    public void download(@PathVariable("kind") String kind, @PathVariable("fileName") String fileName, HttpServletResponse response) {
        String method = "download()";

        logger.info("{}: kind={}", method, kind);
        logger.info("{}: file_name={}", method, fileName);

        if (StringUtils.isEmpty(fileName) || "null".equals(fileName) || "(null)".equals(fileName)) {
            return;
        }

        File file = null;

        try {
            file = fileService.download(kind, fileName);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
        }


        InputStream tempIs = null;
        try (InputStream is = new FileInputStream(file)) {
            if (Const.FILE_KIND_CERTIFICATION.equals(kind) || Const.FILE_KIND_PASSPORT.equals(kind) || Const.FILE_KIND_AUTHME.equals(kind)) {

                String prefix = kind.replace(Const.FILE_SEPARATOR, "");
                String tempFileDir = Files.createTempDirectory(prefix).toString();
                Path tempFilePath = fileService.makeRandomFileName(tempFileDir.toString());

                File tempFile = tempFilePath.toFile();

                securityService.cryptFileAES256(Cipher.DECRYPT_MODE, file, tempFile);

                logger.info("{} tempFilePath={}", method, tempFile.getPath());

                tempIs = new FileInputStream(tempFile.getPath());

                IOUtils.copy(tempIs, response.getOutputStream());

                tempIs.close();
                Files.delete(tempFile.toPath());
            } else {
                IOUtils.copy(is, response.getOutputStream());
            }

            response.flushBuffer();
        } catch (Exception e) {
            logger.error("Error writing file to output stream. kind={}, fileName={}", kind, fileName, e);
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            try {
                if (tempIs != null) {
                    tempIs.close();
                }
                // Download 를 위해 생성한 임시 파일 삭제
                if (file != null && file.exists() && "tmp".equals(FilenameUtils.getExtension(file.getName()))) {
                    Files.delete(file.toPath());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // 이미지 파일 조회
    @GetMapping(value = {"/FILE/{kind}/{file_name}", "/FILE/{kind}/{travelerId}/{file_name}"})
    public void fileDownload(@RequestHeader("Authorization") String token,
                             @PathVariable("kind") String kind,
                             @PathVariable("file_name") String fileName,
                             @PathVariable(value = "travelerId", required = false) Long travelerId,
                             HttpServletRequest request, HttpServletResponse response) throws IOException {

        String method = "fileDownload()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return;
        }

        // If the kind is not the 'SHOP' or 'PROFILE', do check token.
        if (Const.FILE_KIND_SHOP.equals(kind) || Const.FILE_KIND_PROFILE.equals(kind)) {
            // do not check token 
            logger.info("{}: shop or profile photo. kind={}, fileName={}", method, kind, fileName);
        } else if (authService.getUserId(token) == Const.NO_USER_ID) {
            logger.info("{}: check failed. kind={}, fileName={}", method, kind, fileName);
            return;
        }

        logger.info("FileServerController.fileDownload() : {}/{}", kind, fileName);

        if (StringUtils.isEmpty(fileName) || "null".equals(fileName) || "(null)".equals(fileName)) {
            return;
        }

        if(travelerId != null) {
            kind = kind + "/" + travelerId;
        }

        File file = null;

        try {
            file = fileService.download(kind, fileName);
        } catch (CashmallowException e) {
            logger.warn(e.getMessage());
        }

        InputStream tempIs = null;

        if (file == null) {
            // 파일이 없을 경우 대체 이미지를 보여준다.
            ClassPathResource resourceLogo = new ClassPathResource("images/cm-loading-failed.png");
            file = resourceLogo.getFile();
            kind = "/tmp";
        }

        try (InputStream is = new FileInputStream(file)) {
            if (Const.FILE_KIND_CERTIFICATION.equals(kind) || Const.FILE_KIND_PASSPORT.equals(kind) || kind.contains(Const.FILE_KIND_AUTHME)) {

                //                String tempFileDir = hostFilePathHome + File.separator + Const.FILE_SERVER_PASSPORT;
                String prefix = kind.replace(Const.FILE_SEPARATOR, "");
                String tempFileDir = Files.createTempDirectory(prefix).toString();
                Path tempFilePath = fileService.makeRandomFileName(tempFileDir.toString());

                File tempFile = tempFilePath.toFile();

                securityService.cryptFileAES256(Cipher.DECRYPT_MODE, file, tempFile);

                logger.info("{} tempFilePath={}", method, tempFile.getPath());

                tempIs = new FileInputStream(tempFile.getPath());

                IOUtils.copy(tempIs, response.getOutputStream());

                tempIs.close();
                Files.delete(tempFile.toPath());

            } else {
                // copy it to response's OutputStream
                IOUtils.copy(is, response.getOutputStream());
            }

            response.flushBuffer();

        } catch (Exception e) {
            logger.error("Error writing file to output stream. kind={}, fileName={}", kind, fileName, e);
            throw new RuntimeException("IOError writing file to output stream");
        } finally {
            try {
                if (tempIs != null) {
                    tempIs.close();
                }
                // Download 를 위해 생성한 임시 파일 삭제 
                if (file != null && file.exists() && "tmp".equals(FilenameUtils.getExtension(file.getName()))) {
                    Files.delete(file.toPath());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}
