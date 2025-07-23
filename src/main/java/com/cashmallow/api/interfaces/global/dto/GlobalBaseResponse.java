package com.cashmallow.api.interfaces.global.dto;


public record GlobalBaseResponse(
        String code,
        String message,
        Object data
) {
    public static GlobalBaseResponse ok() {
        return new GlobalBaseResponse("200", "SUCCESS", null);
    }

    public static GlobalBaseResponse ok(Object o) {
        return new GlobalBaseResponse("200", "SUCCESS", o);
    }

    public static GlobalBaseResponse error(Exception e) {
        return new GlobalBaseResponse("400", "ERROR", e.getMessage());
    }

    public static GlobalBaseResponse serverError(Object o) {
        return new GlobalBaseResponse("500", "SERVER ERROR", o.toString());
    }
}
