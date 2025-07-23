package com.cashmallow.api.model;

public record ApiResponse(
        String code,
        String status,
        String message,
        Object obj
) {
}
