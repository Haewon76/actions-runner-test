package com.cashmallow.api.infrastructure.fileserver;

import org.apache.commons.io.FilenameUtils;

import java.text.Normalizer;
import java.util.Set;
import java.util.function.Predicate;

@FunctionalInterface
interface FileExtensionValidator extends Predicate<String> {
    boolean test(String filename);

    static FileExtensionValidator forImageType() {
        final Set<String> fileTypes = Set.of("jpeg", "jpg", "png", "pdf");

        return filename -> {
            // MAC 한글 파일명 깨짐 이슈로 인한 정규화
            String fileName = Normalizer.normalize(filename, Normalizer.Form.NFC);
            String extension = FilenameUtils.getExtension(fileName).toLowerCase();
            return fileTypes.contains(extension);
        };
    }
}