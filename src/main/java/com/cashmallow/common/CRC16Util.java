package com.cashmallow.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class CRC16Util {

    public static boolean isInvalidScbQR(String qrString) {
        return !isValidScbQR(qrString);
    }
    public static boolean isValidScbQR(String qrString) {
        try {
            if (StringUtils.isNotEmpty(qrString)) {
                String crc16 = qrString.substring(qrString.length() - 4);
                String qrStringWithoutCRC16 = qrString.substring(0, qrString.length() - 4);
                String crc16Calculated = getCRC16CCITT(qrStringWithoutCRC16, 0x8005, 0x0000, false);
                return crc16.equals(crc16Calculated);
            }
        } catch (Exception e) {
            log.error("isValidScbQR error", e);
        }
        return false;
    }

    private static String getCRC16CCITT(String inputStr, int polynomial, int crc, boolean isHex) {
        int strLen = inputStr.length();
        int[] intArray;

        if (isHex) {
            if (strLen % 2 != 0) {
                inputStr = inputStr.substring(0, strLen - 1) + "0"
                        + inputStr.substring(strLen - 1, strLen);
                strLen++;
            }

            intArray = new int[strLen / 2];
            int ctr = 0;
            for (int n = 0; n < strLen; n += 2) {
                intArray[ctr] = Integer.valueOf(inputStr.substring(n, n + 2), 16);
                ctr++;
            }
        } else {
            intArray = new int[inputStr.getBytes().length];
            int ctr = 0;
            for (byte b : inputStr.getBytes()) {
                intArray[ctr] = b;
                ctr++;
            }
        }

        // main code for computing the 16-bit CRC-CCITT
        for (int b : intArray) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b >> (7 - i) & 1) == 1);
                boolean c15 = ((crc >> 15 & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) {
                    crc ^= polynomial;
                }
            }
        }

        crc &= 0xFFFF;
        return String.format("%04X", crc).toUpperCase();
    }
}


