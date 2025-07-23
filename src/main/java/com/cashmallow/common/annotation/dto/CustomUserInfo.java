package com.cashmallow.common.annotation.dto;

import java.util.Locale;

public record CustomUserInfo(
        long userId,
        String token,
        Locale locale
) {

    public boolean isInvalidUser() {
        return userId < 1;
    }


}
