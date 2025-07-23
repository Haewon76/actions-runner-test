package com.cashmallow.api.domain.model.authme;

public record Authme(
        String customerId,
        String status,
        String documentType,
        String json
) {
}
