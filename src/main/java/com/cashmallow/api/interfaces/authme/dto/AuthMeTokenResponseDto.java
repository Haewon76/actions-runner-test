package com.cashmallow.api.interfaces.authme.dto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public record AuthMeTokenResponseDto(
        String clientId,
        String accessToken,
        String expiredAt // 2024-04-08T08:26:39.889954Z
) {

    public static AuthMeTokenResponseDto toAdminResponse(String clientId,
                                                         AuthMeTokenResponse response) {
        final ZonedDateTime expiredAt = LocalDateTime.now()
                .plusSeconds(response.expiresIn())
                .toInstant(ZoneOffset.UTC)
                .atZone(ZoneOffset.UTC);
        return new AuthMeTokenResponseDto(clientId, "Bearer " + response.accessToken(), expiredAt.toString());
    }

    public static AuthMeTokenResponseDto toApiResponse(String clientId,
                                                       AuthMeTokenResponse response) {
        final ZonedDateTime expiredAt = LocalDateTime.now()
                .plusSeconds(response.expiresIn())
                .toInstant(ZoneOffset.UTC)
                .atZone(ZoneOffset.UTC);
        return new AuthMeTokenResponseDto(clientId, response.accessToken(), expiredAt.toString());
    }

}
