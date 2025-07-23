package com.cashmallow.api.interfaces.authme.dto;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

class AuthmeTravelerTest {

    public static void main(String[] args) {
        String localName = "明花子";
        System.out.println("firstName = " + getFirstName(localName));
        System.out.println("lastName = " + getLastName(localName));
    }

    // 성
    private static String getFirstName(String localName) {
        if(StringUtils.isBlank(localName)) {
            return "";
        }

        try {
            String[] s = localName.split(" ");
            return s[s.length - 1];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    // 이름
    private static String getLastName(String localName) {
        if(StringUtils.isBlank(localName)) {
            return "";
        }

        try {
            List<String> list = Arrays.asList(localName.split(" "));
            if(list.size() == 1) {
                return "";
            }
            list.remove(list.size() - 1);
            return String.join(" ", list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}