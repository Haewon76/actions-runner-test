package com.cashmallow.api.interfaces.authme.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AuthMeTokenResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("expires_in")
        Long expiresIn, // 3600 초

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("scope")
        String scope
) {
}
