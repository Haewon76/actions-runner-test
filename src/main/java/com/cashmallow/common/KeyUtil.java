package com.cashmallow.common;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Slf4j
public class KeyUtil {

    private static final String START_PK = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String END_PK = "-----END RSA PRIVATE KEY-----";
    private static final String LINE_BREAK = "\n";


    // RSA 형식으로 변환한다
    public static String getRSAKey(String base64RSAKey) {
        String decodeString = getDecode(base64RSAKey);
        String tmpKey = decodeString.replace(START_PK, "").replace(END_PK, "").trim();
        return START_PK + LINE_BREAK + tmpKey.replaceAll(" ", LINE_BREAK) + LINE_BREAK + END_PK;
    }

    // 임시 파일을 생성하고, 해당 경로를 리턴한다
    public static String getTempFileLocation(String text) throws IOException {
        Path tempFile = Files.createTempFile("paygate", ".pem");
        Files.write(tempFile, text.getBytes(StandardCharsets.UTF_8));
        return tempFile.toString();
    }

    // JSON으로 된 키값을 읽어온다. 구글 FCM에서 사용
    public static String getJSONKey(String base64Json) {
        return getDecode(base64Json);
    }

    // DECODE된 String 값을 읽어온다.
    private static String getDecode(String encodedString) {
        Base64.Decoder decode = Base64.getDecoder();
        byte[] decodeByte = decode.decode(encodedString);
        return new String(decodeByte);
    }


}
