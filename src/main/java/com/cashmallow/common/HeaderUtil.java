package com.cashmallow.common;

import org.springframework.http.HttpHeaders;

public class HeaderUtil {

    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE = "Content-Type";

    public static HttpHeaders setHeader(String token) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CONTENT_TYPE, "application/json");
        httpHeaders.set(AUTHORIZATION, token);
        return httpHeaders;
    }
}
