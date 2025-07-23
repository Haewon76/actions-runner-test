package com.cashmallow.api.infrastructure.aml.dto;

import lombok.Data;

@Data
public class WLFResponse {

    private final int code;
    private final String message;

    public WLFResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public boolean isNotMatched() {
        return code == 200;
    }

    public boolean isMatched() {
        return !isNotMatched();
    }

}
