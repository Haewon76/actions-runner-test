package com.cashmallow.api.infrastructure.fileserver;

import com.cashmallow.api.domain.shared.CashmallowException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

/**
 * 파일 업로드 유효성 검사
 * - 파일 확장자 및 MIME 타입 검사
 * - 파일 확장자를 임의로 변경하여 업로드 되는 경우도 방지
 */
@Slf4j
@Service
public class FileUploadValidator {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private final Tika tika;

    public FileUploadValidator() {
        this.tika = new Tika();
    }

    /**
     * 이미지 파일(jpeg, png) 및 PDF 파일만 허용
     *
     * @param file 파일
     * @throws IllegalArgumentException 파일이 없는 경우
     * @throws IOException              파일을 읽을 수 없는 경우
     * @throws CashmallowException      허용되지 않는 파일 형식인 경우
     */
    public void validateFile(MultipartFile file) throws IllegalArgumentException, IOException, CashmallowException {
        verifyFileExtensionAndMime(file, ALLOWED_MIME_TYPES);
    }

    /**
     * 허용된 파일 형식만 허용
     *
     * @param file             파일
     * @param allowedMimeTypes 허용된 MIME 타입 - Set.of("image/jpeg", "image/png", "application/pdf")
     * @throws IllegalArgumentException 파일이 없는 경우
     * @throws IOException              파일을 읽을 수 없는 경우
     * @throws CashmallowException      허용되지 않는 파일 형식인 경우
     */
    public void validateFile(MultipartFile file, Set<String> allowedMimeTypes) throws IllegalArgumentException, IOException, CashmallowException {
        verifyFileExtensionAndMime(file, allowedMimeTypes);
    }

    /**
     * 파일 확장자 및 MIME 타입 검사
     * @param file 파일
     * @param allowedMimeTypes 허용된 MIME 타입
     * @throws IOException 파일을 읽을 수 없는 경우
     * @throws CashmallowException 허용되지 않는 파일 형식인 경우
     */
    private void verifyFileExtensionAndMime(MultipartFile file, Set<String> allowedMimeTypes) throws IOException, CashmallowException {
        String mimeType = tika.detect(file.getInputStream());
        if (!allowedMimeTypes.contains(mimeType)) {
            String message = "허용된 파일 형식이 아닙니다. 파일 형식: " + mimeType;
            // log.error(message);
            throw new CashmallowException(message);
        }

        log.debug("파일 확장자 및 MIME 타입 검사 완료. 파일 형식: {}", mimeType);
    }
}