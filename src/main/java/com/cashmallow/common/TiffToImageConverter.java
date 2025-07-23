package com.cashmallow.common;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class TiffToImageConverter {

    // TIFF 시그니처를 확인하는 메서드
    private static boolean isTiff(String base64String) {
        byte[] decodedBytes = Base64.getMimeDecoder().decode(base64String);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
            byte[] signature = new byte[2];
            if (inputStream.read(signature) != 2) {
                return false;
            }
            String signatureStr = new String(signature);
            return "II".equals(signatureStr) || "MM".equals(signatureStr);
        } catch (IOException e) {
            return false;
        }
    }

    // TIFF 파일을 JPEG 또는 PNG로 변환하는 메서드
    public static String convertToJpg(String base64String) throws IOException {
        if (!isTiff(base64String)) {
            return base64String;
        }

        byte[] decodedBytes = Base64.getMimeDecoder().decode(base64String);
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(decodedBytes)) {
            BufferedImage tiffImage = ImageIO.read(inputStream);
            if (tiffImage == null) {
                throw new IOException("Failed to decode TIFF image.");
            }
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(tiffImage, "JPG", outputStream);
                byte[] imageBytes = outputStream.toByteArray();
                return Base64.getMimeEncoder().encodeToString(imageBytes);
            }
        }
    }
}