package com.cashmallow.common;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class RegularExpressionUtil {

    public static boolean sequenceNumbers(String number) {
        String pattern_num = "(012)|(123)|(234)|(345)|(456)|(567)|(678)|(789)|(890)|(210)|(321)|(432)|(543)|(654)|(765)|(876)|(987)";
        return Pattern.compile(pattern_num).matcher(number).find();
    }

    public static boolean sameNumbers(String number) {
        String pattern_num = "(000)|(111)|(222)|(333)|(444)|(555)|(666)|(777)|(888)|(999)";
        return Pattern.compile(pattern_num).matcher(number).find();
    }

    public static boolean frequencyNumber(String number, int frequency) {
        List<String> list = new ArrayList<>(Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9"));
        List<String> numList = new ArrayList<>(Arrays.asList(number.split("")));

        for (String num : list) {
            if (Collections.frequency(numList, num) >= frequency) {
                return true;
            }
        }
        return false;
    }


    public static boolean isPinCodeContainsBirthDate(String pinCode, String birthDate) {
        if (StringUtils.isEmpty(birthDate)) {
            return false;
        }

        return CommonUtil.matchString(pinCode, birthDate) >= 4;
    }

    public static boolean isPinCodeContainsPhoneNumber(String pinCode, String phoneNumber) {

        if (StringUtils.isEmpty(phoneNumber)) {
            return false;
        }

        phoneNumber = phoneNumber.replaceAll("[^\\d]", "");

        boolean result = false;
        String mid = "";
        String last = "";

        if (phoneNumber.length() <= 8) {
            String phoneNumberTemp = StringUtils.substring(phoneNumber, phoneNumber.length() - 4, phoneNumber.length());

            if (pinCode.contains(phoneNumberTemp)) {
                return true;
            }
            return false;
        }

        String phoneNumberTemp = StringUtils.substring(phoneNumber, phoneNumber.length() - 8, phoneNumber.length());
        mid = phoneNumberTemp.substring(0, 4);
        last = phoneNumberTemp.substring(4, 8);

        if (pinCode.contains(mid) || pinCode.contains(last)) {
            return true;
        }

        phoneNumberTemp = StringUtils.substring(phoneNumber, phoneNumber.length() - 7, phoneNumber.length());
        // 폰번호가 7자리 일 경우
        mid = phoneNumberTemp.substring(0, 4);
        last = phoneNumberTemp.substring(3, 7);

        return pinCode.contains(mid) || pinCode.contains(last);
    }
}
