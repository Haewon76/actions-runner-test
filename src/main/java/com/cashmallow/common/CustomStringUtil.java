package com.cashmallow.common;

import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.CryptAES;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
public class CustomStringUtil {
    private static final Logger logger = LoggerFactory.getLogger(CustomStringUtil.class);

    private static int tokenCount = 0;
    private static int tokenWithBearer = 0;
    private static int tokenNoBearer = 0;
    private static int tokenFail = 0;

    private CustomStringUtil() {
        throw new IllegalStateException("Utility Class");
    }

    // 기능 : 입력 문자열의 전.후 공백을 제거한다. 만약 입력 문자열이 null인 경우 ""를 응답한다.
    public static String trim(String str) {
        if (str != null) {
            return str.trim();
        } else {
            return "";
        }
    }

    // 기능: 문자열의 부분문자열을 구한다.
    public static String subStr(String str, int beginIdx, int len) {
        String result = "";

        if (str != null && beginIdx >= 0 && len >= 0) {
            int strLen = str.length();

            if (beginIdx < strLen) {
                len = (strLen - beginIdx) < len ? (strLen - beginIdx) : len;
                result = str.substring(beginIdx, beginIdx + len);
            }
        }

        return result;
    }

    // 기능: 사용자 권한에 대한 소유 권한 문자열 배열을 응답한다.
    public static String[] getAuthStr(String auth) {
        String[] result = new String[0];

        if (auth != null) {
            if (auth.equals(ROLE_ADMIN)) {
                result = ROLE_STRS_ADMIN;
            } else if (auth.equals(ROLE_SYSTEM)) {
                result = ROLE_STRS_SYSTEM;
            } else if (auth.equals(ROLE_SUPERMAN)) {
                result = ROLE_STRS_SUPERMAN;
            } else if (auth.equals(ROLE_MANAGER)) {
                result = ROLE_STRS_MANAGER;
            } else if (auth.equals(ROLE_ASSIMAN)) {
                result = ROLE_STRS_ASSIMAN;
            } else if (auth.equals(ROLE_USER)) {
                result = ROLE_STRS_USER;
            } else if (auth.equals(ROLE_ANONYMOUS)) {
                result = ROLE_STRS_ANONYMOUS;
            }
        }

        // return lstAuths.toArray(new String[lstAuths.size()]);
        return result;
    }


    // 기능: 입력 문자열이 'Y' 또는 'y'인 경우 0을, 'N' 또는 'n'인 경우 1, 'R' 또는 'r'인 경우 2을 응답하며, 그렇지 않을 경우 Const.NO_DATA를 응답한다.
    public static int ynrToNo(String ynr) {
        int result = NO_DATA;

        if (ynr != null) {
            ynr = ynr.toUpperCase();

            if (ynr.equals("Y")) {
                result = 0;
            } else if (ynr.equals("N")) {
                result = 1;
            } else if (ynr.equals("R")) {
                result = 2;
            }
        }

        return result;
    }

    // 기능: UUID를 생성한다.
    public static String randomUuidStr() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    // 기능: 인출용 QR-CODE를 생성한다.
    public static String generateQrCode() {
        String uuid1 = randomUuidStr();
        String uuid2 = randomUuidStr();
        return uuid1 + makeCode(uuid1, uuid2) + uuid2;
    }

    // 기능: 2개의 uuid의 xor 결과을 문자열로 음답한다.
    private static String makeCode(String uuid1, String uuid2) {
        String result = "";

        if ((uuid1 != null) & (uuid2 != null)) {
            if (uuid1.length() == SIZE_OF_UUID && uuid2.length() == SIZE_OF_UUID) {
                long l11 = Long.parseUnsignedLong(uuid1.substring(0, 16), 16);
                long l12 = Long.parseUnsignedLong(uuid1.substring(16, 16 + 16), 16);
                long l21 = Long.parseUnsignedLong(uuid2.substring(0, 16), 16);
                long l22 = Long.parseUnsignedLong(uuid2.substring(16, 16 + 16), 16);
                long l31 = l11 ^ l21;
                long l32 = l12 ^ l22;
                String s31 = Long.toString(l31, 16);
                String s32 = Long.toString(l32, 16);
                result = lpad(16, "0", s31.substring(0, 1).equals("-") ? s31.substring(1) : s31)
                        + lpad(16, "0", s32.substring(0, 1).equals("-") ? s32.substring(1) : s32);
            }
        }

        return result;
    }

    // 기능: 문자열을 고정길이 형태로 만든다.
    private static String lpad(int fixedLen, String prefix, String value) {
        if (fixedLen < 0) {
            fixedLen = 0;
        }

        if (prefix == null || prefix.isEmpty()) {
            prefix = " ";
        } else if (prefix.length() >= 2) {
            prefix = prefix.substring(0, 1);
        }

        if (value == null) {
            value = "";
        }

        int valLen = value.length();
        int mkLen = fixedLen - valLen;

        if (mkLen >= 1) {
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < mkLen; i++) {
                sb.append(prefix);
            }

            value = sb.toString() + value;
        }

        return value;
    }

    /**
     * Decode request body string by token
     *
     * @param token
     * @param jsonStr
     * @return
     */
    public static String decode(String token, String jsonStr) {
        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(jsonStr)) {
            return "";
        }

        tokenCount++;

        String pureToken = token.replaceFirst("Bearer ", "");

        String result = CryptAES.decode(pureToken, jsonStr);

        if (StringUtils.isEmpty(result)) {
            result = CryptAES.decode(token, jsonStr);
            if (!StringUtils.isEmpty(result)) {
                tokenWithBearer++;
                String rate = String.format("%.2f", (double) 100 * tokenWithBearer / tokenCount);
                logger.debug("복호화 성공 With Bearer {}/{} {}% token={}", tokenWithBearer, tokenCount, rate, token);
                return result;
            }
        }

        if (!StringUtils.isEmpty(result)) {
            tokenNoBearer++;
            String rate = String.format("%.2f", (double) 100 * tokenNoBearer / tokenCount);
            logger.debug("복호화 성공 Not Bearer {}/{} {}% token={}", tokenNoBearer, tokenCount, rate, pureToken);
            return result;
        }

        if (StringUtils.isEmpty(result)) {
            tokenFail++;
            String rate = String.format("%.2f", (double) 100 * tokenFail / tokenCount);
            logger.info("복호화 실패 {}/{} {}%", tokenNoBearer, tokenCount, rate);
            logger.warn("복호화 실패, token={}, jsonStr={}", token, jsonStr);
        }
        return result;
    }

    public static String encryptJsonString(String token, ApiResultVO voResult, HttpServletResponse response) {
        String jsonStr = JsonStr.toJsonString(voResult, response);
        return CryptAES.encode(token, jsonStr);
    }

    public static String toResponse(ApiResultVO voResult, HttpServletResponse response) {
        return JsonStr.toJsonString(voResult, response);
    }

    public static String toJsonEncode(String token, String json) {
        return CryptAES.encode(token, json);
    }


    public static String localizeNumberFormat(BigDecimal mappingInc, BigDecimal amt) {
        String result = null;

        int t = (int) (-1 * Math.floor(Math.log10(mappingInc.doubleValue())));

        String pattern = "#,##0";
        if (t > 0) {
            pattern = pattern + ".%0" + t + "d";
            pattern = String.format(pattern, 0);
        }

        DecimalFormat df = new DecimalFormat(pattern);
        result = df.format(amt);

        return result;
    }

    public static String maskingName(String nonMaskedName) {
        if (nonMaskedName == null) {
            return "";
        }

        int length = nonMaskedName.length();
        int maskLength = length / 3;

        return nonMaskedName.substring(0, maskLength) +
                "*".repeat(Math.max(maskLength, length - 2 * maskLength)) +
                nonMaskedName.substring(length - maskLength);
    }

    public static boolean matchMaskedString(String maskedString, String originString) {
        if (StringUtils.isEmpty(maskedString) || StringUtils.isEmpty(originString)) {
            return false;
        }

        if (maskedString.length() != originString.length()) {
            return false;
        }

        for (int i = 0; i < maskedString.length(); ++i) {
            if (maskedString.charAt(i) == '*') {
                continue;
            }
            if (maskedString.charAt(i) != originString.charAt(i)) {
                return false;
            }
        }

        return true;
    }

    public static boolean equalOnlyNumbers(String str1, String str2) {
        String num1 = extractNumbers(str1);
        String num2 = extractNumbers(str2);

        return num1.equals(num2);
    }

    public static String extractNumbers(String str) {
        StringBuilder numbers = new StringBuilder();
        Pattern pattern = Pattern.compile("\\d+");
        Matcher matcher = pattern.matcher(str);

        while (matcher.find()) {
            numbers.append(matcher.group());
        }

        return numbers.toString();
    }
}
