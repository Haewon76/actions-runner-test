package com.cashmallow.common;

import java.security.SecureRandom;
import java.util.Random;

public class RandomUtil {
    public static final String LOWER_ALPHA = "abcdefghijklmnopqrstuvwxyz";
    public static final String UPPER_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String NUMERIC = "0123456789";
    public static final String ALPHA_NUMERIC = NUMERIC + UPPER_ALPHA + LOWER_ALPHA;
    public static final String CAPITAL_ALPHA_NUMERIC = NUMERIC + UPPER_ALPHA;

    private static final Random random = new SecureRandom();

    /**
     * 주어진 문자로 지정한 길이의 랜덤 문자열를 만드는 함수.
     *
     * @param characters 문자 범위
     * @param length     랜덤 문자열의 길이
     * @return characters로 이우러진 length길이의 문자열을 반환.
     */
    public static String generateRandomString(String characters, int length) {
        StringBuilder builder = new StringBuilder();

        char[] charArray = characters.toCharArray();
        for (int i = 0; i < length; ++i) {
            int character = random.nextInt(characters.length());
            builder.append(charArray[character]);
        }
        return builder.toString();
    }
}
