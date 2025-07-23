package com.cashmallow.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Scanner;

public class CommNet {

    private static final Logger logger = LoggerFactory.getLogger(CommNet.class);

    private CommNet() {
        throw new IllegalStateException("Utility Class");
    }


    // 기능: byte[]를 Hexa 문자열로 응답한다.
    public static String toHexString(byte[] byteArray) {
        StringBuffer sb = new StringBuffer();

        if (byteArray != null) {
            for (int idx = 0; idx < byteArray.length; idx++) {
                sb.append(String.format("%02X", byteArray[idx]));
                // if ((idx + 1) % 16 == 0 && ((idx + 1) != byteArray.length)) {
                // sb.append(" ");
                // }
            }
        }

        return sb.toString();
    }

    // edited by kgy 20170511 request 에서 body 를 꺼내오도록 한다. 기존 코드가 아파치 -> 톰캣 을 연동하는 경우
    // body 를 가져오지 못하는 문제가 있어서 대응하도록 하는 코드이다.
    public static String extractPostRequestBody(HttpServletRequest request) {
        ServletInputStream is = null;
        try {
            is = request.getInputStream();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            is = null;
        }
        String ss = "";

        if (is != null) {
            Scanner scanner = new Scanner(is, "utf-8");
            scanner.useDelimiter("\\A");
            ss = scanner.hasNext() ? scanner.next() : "";
            scanner.close();
        }
        return ss;
    }

}
