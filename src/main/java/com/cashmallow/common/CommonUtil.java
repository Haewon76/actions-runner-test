package com.cashmallow.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.InvalidPasswordException;
import com.cashmallow.api.interfaces.authme.AuthMeProperties;
import com.cashmallow.api.interfaces.traveler.OsType;
import com.cashmallow.filter.CacheReadHttpServletRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.mngsk.devicedetector.DeviceDetector;
import io.github.mngsk.devicedetector.device.Device;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.cashmallow.common.HashUtil.getMd5Hash;

@Slf4j
public class CommonUtil {

    private CommonUtil() {
        throw new IllegalStateException("Utility Class");
    }

    /**
     * This method takes a text as input and returns the normalized form of the text.
     * The normalization is performed using the NFKC (Normalization Form KC) normalization form.
     * Leading and trailing whitespaces are also removed from the normalized text.
     *
     * @param text The text to be normalized.
     * @return The normalized form of the input text.
     */
    public static String textToNormalize(String text) {
        if (StringUtils.isBlank(text)) {
            return text;
        }

        final String normalize = Normalizer.normalize(text, Normalizer.Form.NFKC).trim();
        if (!StringUtils.equals(text, normalize)) {
            log.info("Normalizer:{}, text:{}", normalize, text);
        }
        return normalize;
    }

    public static String convertSmallKanaToLarge(String text) {
        return text.replace("ャ", "ヤ")
                .replace("ュ", "ユ")
                .replace("ョ", "ヨ")
                .replace("ァ", "ア")
                .replace("ィ", "イ")
                .replace("ゥ", "ウ")
                .replace("ェ", "エ")
                .replace("ォ", "オ");
    }

    public static boolean isSameCertification(String oldId, String newId) {
        try {
            String oldCertId = textToNormalize(oldId).replaceAll("\\(", "").replaceAll("\\)", "");
            String newCertId = textToNormalize(newId).replaceAll("\\(", "").replaceAll("\\)", "");
            return StringUtils.equals(oldCertId, newCertId);
        } catch (Exception ignore) {
        }
        return false;
    }

    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            return obj == null || "".equals(obj.toString().trim());
        } else if (obj instanceof List) {
            return obj == null || ((List<?>) obj).isEmpty();
        } else if (obj instanceof Map) {
            return obj == null || ((Map<?, ?>) obj).isEmpty();
        } else if (obj instanceof Object[]) {
            return obj == null || Array.getLength(obj) == 0;
        } else {
            return obj == null;
        }
    }

    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    public static String getFullUrl(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURL() + (StringUtils.isNotEmpty(queryString) ? "?" + queryString : "");
    }

    public static String getUri(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURI() + (StringUtils.isNotEmpty(queryString) ? "?" + queryString : "");
    }

    public static String getRequestIp() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            return null;
        }

        String remoteAddr = getRemoteAddr(requestAttributes.getRequest());
        if ("unknown".equals(remoteAddr)) {
            return null;
        }

        return remoteAddr;
    }

    public static String getRemoteAddr(HttpServletRequest request) {

        final String unknown = "unknown";
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isEmpty(ip) || unknown.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        return ip;
    }

    public static String getAuthorization(HttpServletRequest request) {
        return request.getHeader("Authorization");
    }

    public static String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }

    public static boolean isAndroid(HttpServletRequest request) {
        return StringUtils.equals("ANDROID", getOsType(request));
    }

    public static String getOsType(HttpServletRequest request) {
        final String unknownDevice = "unknown";
        try {
            // 앱의 헤더값을 먼저 체크하여, IOS인지 AOS 인치 확인
            final String osType = request.getHeader("cm-os-type");
            if (osType != null) {
                switch (OsType.valueOf(osType)) {
                    case iOS:
                    case IOS:
                        return "IOS";
                    case AOS:
                        return "ANDROID";
                }
            }

            final Device device = new DeviceDetector.DeviceDetectorBuilder().build().detect(getUserAgent(request)).getDevice()
                    .orElse(new Device(unknownDevice, unknownDevice, unknownDevice));
            final String deviceType = device.getType();
            if ("smartphone".equalsIgnoreCase(deviceType)) {
                if ("apple".equalsIgnoreCase(device.getBrand().orElse(unknownDevice))) {
                    return "IOS";
                }
                return "ANDROID";
            }

            return deviceType.toUpperCase();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return unknownDevice;
    }

    public static long getAppBuildVersion(HttpServletRequest request) {
        String versionCode = request.getHeader("cm-version-code");
        if (StringUtils.isBlank(versionCode)) {
            return -1;
        }
        return Long.parseLong(versionCode);
    }

    public static String appDebugInfo() {
        final Map<String, String> headers = getHeaders();

        final String osType = StringUtils.left(headers.get("cm-os-type"), 1);

        if (StringUtils.isEmpty(osType)) {
            return "";
        }

        return "\ninfo: " +
                osType.toUpperCase() + "_" +
                headers.get("cm-version-code") + "_" +
                headers.get("cm-os-version") + "(" + headers.get("cm-version-name") + ")_" +
                headers.get("cm-bundle-version") + "_" +
                headers.get("cm-device-model");
    }

    public static HttpServletRequest getHttpServletRequest() {
        try {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes == null) {
                return null;
            }

            return servletRequestAttributes.getRequest();
        } catch (Exception ignore) {
        }
        return null;
    }

    public static Map<String, String> getHeaders() {
        Map<String, String> map = new HashMap<>();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            return map;
        }

        final HttpServletRequest request = servletRequestAttributes.getRequest();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String key = headerNames.nextElement().toLowerCase(Locale.ROOT);
                String value = request.getHeader(key);
                map.put(key, value);
            }
        }
        return map;
    }

    public static boolean isValidURL(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    public static String getRandomToken() {
        return getMd5Hash(UUID.randomUUID() + String.valueOf(System.currentTimeMillis()));
    }


    /**
     * 임시 비밀번호를 생성하여 리턴한다.
     *
     * @return
     */
    public static String generateResetRandomPassword() {
        final String s = temporaryPassword(1, 3, 4);
        List<Character> chars = IntStream.range(0, s.length()).mapToObj(s::charAt).collect(Collectors.toList());
        Collections.shuffle(chars);
        return chars.stream().map(a -> a.toString()).collect(Collectors.joining());
    }


    private static String temporaryPassword(int specialCharCount, int charCount, int numberCount) {
        StringBuilder sb = new StringBuilder();

        String specialChars = "!@#$%";
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz";
        String numbers = "0123456789";

        sb.append(getRandomString(specialChars, specialCharCount));
        sb.append(getRandomString(chars, charCount));
        sb.append(getRandomString(numbers, numberCount));

        return sb.toString();
    }

    private static String getRandomString(String arrays, int size) {
        Random random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        int length = arrays.length();
        for (int i = 0; i < size; i++) {
            sb.append(arrays.charAt(random.nextInt(length)));
        }
        return sb.toString();
    }

    /**
     * +82로 시작 하는 번호 정제
     * eg. +821012341234 -> 01012341234
     *
     * @param user
     * @return
     * @throws CashmallowException
     */
    public static String getKorPhoneNumber(User user) throws CashmallowException {
        if (!StringUtils.equals("KOR", user.getPhoneCountry())) {
            log.info("한국 전화번호가 아닙니다. 유저ID:{} user PhoneCountry:{}", user.getId(), user.getPhoneCountry());
            throw new CashmallowException("INTERNAL_SERVER_ERROR");
        }
        String callPrefix = CountryInfo.callingPrefix.get(user.getPhoneCountry()); // +82
        String korPhoneNumber = StringUtils.removeStart(user.getPhoneNumber(), callPrefix);
        if (!korPhoneNumber.startsWith("0") && korPhoneNumber.length() < 12) {
            korPhoneNumber = "0" + korPhoneNumber;
        }
        return korPhoneNumber;
    }

    public static String getTemplateByCountry(CountryCode countryCode, String templateName) {
        // 한국과 대만인 경우에 대한 처리
        if (CountryCode.TW == countryCode ||
                CountryCode.KR == countryCode) {
            return "mail-templates/ko/" + templateName;
        } else if (CountryCode.JP.equals(countryCode)) {
            return "mail-templates/ja/" + templateName;
        }

        // 기본적으로 홍콩(HK) 영어로 처리
        return "mail-templates/en/" + templateName;
    }

    public static String getMessageByCountry(MessageSource messageSource, CountryCode countryCode, String messageName) {
        // 한국과 대만인 경우에 대한 처리
        // 한국어로 메일이 발송되는 경우가 너무 많아서, KR인 경우만 한국어로 발송
        if (CountryCode.KR == countryCode) {
            return messageSource.getMessage(messageName, null, Locale.KOREA);
        } else if (CountryCode.JP.equals(countryCode)) {
            return messageSource.getMessage(messageName, null, Locale.JAPAN);
        }

        // 기본적으로 홍콩(HK) 영어로 처리
        return messageSource.getMessage(messageName, null, Locale.ENGLISH);
    }

    /**
     * JSON Body를 읽어온다, request가 CacheReadHttpServletRequest거나 MultipartHttpServletRequest 일때 가능.
     *
     * @param request
     * @return
     */
    public static String getRequestBodyString(HttpServletRequest request) {
        try {
            // Cache된 request면 Body를 읽도록 필터링
            if (request instanceof MultipartHttpServletRequest) {
                // 파라미터가 1개인 것을 가정함.
                if (request.getParameterMap().size() != 1) {
                    return null;
                }

                Enumeration<String> parameterNames = request.getParameterNames();
                if (parameterNames.hasMoreElements()) {
                    return request.getParameter(parameterNames.nextElement());
                }
            } else if (request instanceof CacheReadHttpServletRequest) {
                return IOUtils.readLines(request.getInputStream(), StandardCharsets.UTF_8).stream().map(String::trim).collect(Collectors.joining());
            }

        } catch (Exception e) {
            log.warn(e.getMessage());
        }

        return null;
    }

    public static void isValidPassword(User user, String password) throws InvalidPasswordException {
        // 허용되지 않은 문자
        if (hasInvalidCharacter(password)) {
            throw new InvalidPasswordException("USER_PASSWORD_INVALID_CHARACTER");
        }

        // 8자 이상 3종류
        if (!isComplex(password, 8)) {
            throw new InvalidPasswordException("SETTING_ERROR_MIN_PWD_LEN");
        }

        // 4 같은 글자
        if (hasSameCharacters(password, 4)) {
            throw new InvalidPasswordException("USER_PASSWORD_SAME_NUMBERS");
        }

        // 4 연속된 글자
        if (hasPasswordSequence(password, 4)) {
            throw new InvalidPasswordException("USER_PASSWORD_SEQUENCE_NUMBERS");
        }

        // 키보드 패턴
        if (hasKeyboardPattern(password)) {
            throw new InvalidPasswordException("USER_PASSWORD_KEYBOARD_PATTERNS");
        }

        // 계정
        String emailToken = user.getEmail().split("@")[0];
        if (StringUtils.contains(password.toUpperCase(), emailToken.toUpperCase())) {
            throw new InvalidPasswordException("USER_PASSWORD_SAME_ID");
        }

        // 전화번호
        String phoneNumber = user.getPhoneNumber();
        if (phoneNumber != null) {
            String part2 = null;
            String part1 = null;

            if (phoneNumber.length() >= 8) {
                part2 = phoneNumber.substring(phoneNumber.length() - 4);
                part1 = phoneNumber.substring(phoneNumber.length() - 8, phoneNumber.length() - 4);
            } else if (phoneNumber.length() >= 4) {
                part2 = phoneNumber.substring(phoneNumber.length() - 4);
            }

            if (StringUtils.containsAny(password, part2, part1)) {
                throw new InvalidPasswordException("USER_PASSWORD_SAME_PHONE_NUMBER");
            }
        }

        // 생년월일
        String birthDate = user.getBirthDate();
        if (birthDate != null && birthDate.length() >= 4) {
            if (matchString(password, birthDate) >= 4) {
                throw new InvalidPasswordException("USER_PASSWORD_SAME_BIRTH_DATE");
            }
        }
    }

    /**
     * 비밀번호에 허용하지 않는 문자열.
     *
     * @param password
     * @return
     */
    private static boolean hasInvalidCharacter(String password) {
        return Pattern.compile("[^\\da-zA-Z~`!@#$%^&*()_\\-+=\\[\\]\\{\\}\\|\\\\;:'\"<>,.?/]").matcher(password).find();
    }

    /**
     * Password의 복잡성이 충분한지, 최소 길이와 섞여야 할 패턴 종류(대문자, 소문자, 숫자, 특수문자)
     *
     * @param password
     * @param minLength
     */
    private static boolean isComplex(String password, int minLength) {
        if (password.length() < minLength) {
            return false;
        }

        boolean hasDigit = Pattern.compile("\\d").matcher(password).find();
        boolean hasAlpha = Pattern.compile("[a-zA-Z]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[~`!@#$%^&*()_\\-+=\\[\\]\\{\\}\\|\\\\;:'\"<>,.?/]").matcher(password).find();

        return hasDigit && hasAlpha && hasSpecial;
    }

    /**
     * passward에 sameLength번 이상 같은 문자 반복되면 exception
     *
     * @param password
     * @param sameLength
     * @return
     * @throws InvalidPasswordException
     */
    private static boolean hasSameCharacters(String password, int sameLength) {
        if (sameLength < 2) {
            throw new InvalidParameterException("sameLength must be over 2");
        }

        String regex = "(.)\\1{" + (sameLength - 1) + "}";
        return Pattern.compile(regex).matcher(password).find();
    }

    /**
     * sequenceLength번 이상 문자가 1씩 증가 또는 감소하면
     *
     * @param password
     * @param sequenceLength
     * @throws InvalidPasswordException
     */
    private static boolean hasPasswordSequence(String password, int sequenceLength) {
        char[] charArray = password.toCharArray();
        int maxUp = Integer.MIN_VALUE;
        int maxDown = Integer.MIN_VALUE;

        int upCount = 1;
        int downCount = 1;
        for (int i = 0; i < charArray.length - 1; ++i) {
            if (charArray[i] + 1 == charArray[i + 1]) {
                upCount++;
                maxUp = Math.max(maxUp, upCount);
            } else {
                upCount = 1;
            }
            if (charArray[i] - 1 == charArray[i + 1]) {
                downCount++;
                maxDown = Math.max(maxDown, downCount);
            } else {
                downCount = 1;
            }
        }

        return maxUp >= sequenceLength || maxDown >= sequenceLength;
    }

    /**
     * passward에 키보드 패턴이 있는가?
     *
     * @param password
     * @throws InvalidPasswordException
     */
    private static boolean hasKeyboardPattern(String password) {
        List<String> keyboardPatterns = List.of("~!@#$%^&*()_+", "`1234567890-=", "qwertyuiop[]\\", "asdfghjkl;'", "zxcvbnm,./");
        for (String pattern : keyboardPatterns) {
            if (matchString(password, pattern) >= 4) {
                return true;
            }
        }
        return false;
    }

    /**
     * 두 문자열이 연속적으로 가장 많이 겹치는 길이
     *
     * @param s1
     * @param s2
     * @return
     */
    public static int matchString(String s1, String s2) {
        int[][] count = new int[s1.length() + 1][s2.length() + 1];

        char[] chars1 = s1.toCharArray();
        char[] chars2 = s2.toCharArray();

        int maxMatch = Integer.MIN_VALUE;
        for (int i = 0; i < s1.length(); ++i) {
            for (int j = 0; j < s2.length(); ++j) {
                if (chars1[i] == chars2[j]) {
                    count[i + 1][j + 1] = count[i][j] + 1;
                    maxMatch = Math.max(maxMatch, count[i + 1][j + 1]);
                }
            }
        }

        return maxMatch;
    }

    public static String getRandomDigits(int size) {
        Random rand = new Random();
        StringBuilder sb = new StringBuilder(size);

        for (int i = 0; i < 6; i++) {
            sb.append(rand.nextInt(10));
        }

        return sb.toString();
    }

    public static boolean isValidPinCodeDeviceReset() {
        try {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = sra.getRequest();

            String osType = CommonUtil.getOsType(request);
            long appBuildVersion = CommonUtil.getAppBuildVersion(request);
            final int androidVersion = 730;
            final int iosVersion = 650;

            log.debug("osType={}, appBuildVersion={}, targetAndroidVersion={}, targetIosVersion={}", osType, appBuildVersion, androidVersion, iosVersion);

            if (StringUtils.equals(osType, "ANDROID")) {
                return appBuildVersion >= androidVersion;
            } else if (StringUtils.equals(osType, "IOS")) {
                return appBuildVersion >= iosVersion;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    public static String getDeviceType() {
        try {
            ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = sra.getRequest();

            String osType = CommonUtil.getOsType(request);
            if (StringUtils.equals(osType, "ANDROID")) {
                return "A";
            } else if (StringUtils.equals(osType, "IOS")) {
                return "I";
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * 입금 확인 유효성 검사
     * 자동매핑시 사용
     *
     * @param fromAccountNo
     * @param toAccountNo
     * @param bankCode
     * @param fromAmt
     * @param toAmt
     * @return
     */
    public static boolean isValidateDeposit(String fromAccountNo,
                                            String toAccountNo,
                                            String bankCode,
                                            BigDecimal fromAmt,
                                            BigDecimal toAmt) {

        final boolean isValidAmount = toAmt.compareTo(fromAmt) == 0;
        if (!isValidAmount) {
            return false;
        }

        if (fromAccountNo.equals(toAccountNo)) {
            return true;
        }

        if (StringUtils.isNotEmpty(bankCode)) {
            return toAccountNo.equals(bankCode + fromAccountNo);
        } else {
            return toAccountNo.substring(3).equals(fromAccountNo);
        }
    }

    public static String abstractJsonString(String message) {

        String regex = "\\[\\{.*?\\}\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            sb.append(matcher.group());
        }

        return sb.toString();
    }

    public static String getStackTraceFormat(StackTraceElement element, String errorMessage) {
        return getStackTraceMessageFormat("Throw", element, errorMessage);
    }

    public static String getErrorLogFormat(StackTraceElement element, String errorMessage) {
        return getStackTraceMessageFormat("Log", element, errorMessage);
    }


    private static String getStackTraceMessageFormat(String prefix, StackTraceElement element, String errorMessage) {
        StringBuilder sb = new StringBuilder();
        if (element.getClassName().contains("mallow")) {
            sb.append("*").append(prefix).append(" Message:* ").append(errorMessage).append("\n");
            sb.append("    Class/Method: ").append(element.getClassName()).append(".").append(element.getMethodName()).append("\n");
            sb.append("    File: ").append(element.getFileName() == null ? "Unknown" : element.getFileName()).append(":").append(element.getLineNumber() == -1 ? "Unknown" : element.getLineNumber()).append("\n");
        }

        return sb.toString();
    }


    public static String getJsonStringToPretty(String json) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Object o = gson.fromJson(json, Object.class);
        return gson.toJson(o);
    }

    public static File getBase64ToJPGFile(String fileServerDir, String fileName, String base64String) {
        try {
            // 디렉토리 생성
            Path dirPath = Paths.get(fileServerDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 파일 경로 설정
            Path filePath = dirPath.resolve(fileName);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            // Base64 디코딩 및 파일 작성
            byte[] decodedBytes = Base64.getMimeDecoder().decode(base64String);
            Files.write(filePath, decodedBytes, StandardOpenOption.WRITE);

            return filePath.toFile();
        } catch (Exception e) {
            log.error("getBase64ToJPGFile: " + e.getMessage(), e);
        }
        return null;
    }

    public static String getAuthmeSignature(String json, String secretKey) {
        try {
            String message = json.replaceAll("\\s+", ""); // hmacSha256 = kmlgiCWMcrnOb/gR0O/rEKLi02TbdLkEbe5fJQB2oM8=
            // String message = json;
            // System.out.println("message = " + message);

            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            return Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("hmacSha256: " + e.getMessage(), e);
        }
        return null;
    }

    public static String getAuthmeEventName(CountryCode countryCode,
                                            CertificationType certificationType) {
        if (certificationType == null) {
            return switch (countryCode) {
                case JP -> "jp_default";
                case HK -> "hk_default";
                default -> "default";
            };
        }

        return countryCode.name() + "_" + certificationType.name();
    }

    public static String getClientId(CountryCode countryCode, AuthMeProperties authMeProperties) {
        return switch (countryCode) {
            case JP -> authMeProperties.clientIdJp();
            case HK -> authMeProperties.clientId();
            default -> throw new IllegalStateException("Unexpected value: " + countryCode);
        };
    }

    public static String getClientSecret(CountryCode countryCode, AuthMeProperties authMeProperties) {
        return switch (countryCode) {
            case JP -> authMeProperties.clientSecretJp();
            case HK -> authMeProperties.clientSecret();
            default -> throw new IllegalStateException("Unexpected value: " + countryCode);
        };
    }

    public static String getCountryByAuthmeId(String customerId) {
        return customerId.substring(0, 2);
    }

    public static String removeNonNumeric(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("[^\\d]", "");
    }
}

