package com.cashmallow.api.interfaces.aml.complyadvantage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ComplyAdvantageTokenResponse(@JsonProperty("access_token") String accessToken,
                                           @JsonProperty("scope") String scope,
                                           @JsonProperty("expires_in") Long expiresIn,
                                           @JsonProperty("token_type") String tokenType) {
}
