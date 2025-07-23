package com.cashmallow.api.interfaces;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public class Convert {

    private static final Logger logger = LoggerFactory.getLogger(Convert.class);

    private final static char[] digits = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };
    private final static int radix = digits.length;


    // 기능: int 값을 byte[] 형태로 변환한다.
    public static byte[] toByteArray(int value) {
        byte[] byteArray = new byte[4];
        byteArray[0] = (byte) (value >> 24);
        byteArray[1] = (byte) (value >> 16);
        byteArray[2] = (byte) (value >> 8);
        byteArray[3] = (byte) value;
        return byteArray;
    }

    // 기능: byte[] 값을 int 형태로 변환한다.
    public static int toInt(byte bytes[]) {
        return ((((int) bytes[0] & 0xff) << 24)
                | (((int) bytes[1] & 0xff) << 16)
                | (((int) bytes[2] & 0xff) << 8)
                | (((int) bytes[3] & 0xff)));
    }

    // 기능: 문자열 숫자를 Integer로 변환한다. 변환 중 오류가 발생되면 defValue을 응답한다.
    public static Integer objToIntDef(Object obj, Integer defValue) {
        return strToIntDef(obj != null ? obj.toString() : null, defValue);
    }

    public static Integer strToIntDef(String str, Integer defValue) {
        Integer result = defValue;

        if (StringUtils.isNotBlank(str)) {
            try {
                result = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                logger.warn(e.getMessage());
                result = Double.valueOf(str).intValue();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

    // 기능: 문자열 숫자를 Long으로 변환한다. 변환 중 오류가 발생되면 defValue을 응답한다.
    public static Long objToLongDef(Object obj, Long defValue) {
        return strToLongDef(obj != null ? obj.toString() : null, defValue);
    }

    public static Long strToLongDef(String str, Long defValue) {
        Long result = defValue;

        if (StringUtils.isNotBlank(str)) {
            try {
                result = Long.valueOf(str);
            } catch (NumberFormatException e) {
                logger.warn(e.getMessage());
                result = Double.valueOf(str).longValue();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

    // 기능: 문자열 숫자를 Double으로 변환한다. 변환 중 오류가 발생되면 defValue을 응답한다.
    public static BigDecimal objToBigDecimalDef(Object obj, BigDecimal defValue) {
        return strToBigDecimalDef(obj != null ? obj.toString() : null, defValue);
    }

    public static BigDecimal strToBigDecimalDef(String str, BigDecimal defValue) {
        BigDecimal result = defValue;

        if (str != null && !str.isEmpty()) {
            try {
                result = new BigDecimal(str);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

}
