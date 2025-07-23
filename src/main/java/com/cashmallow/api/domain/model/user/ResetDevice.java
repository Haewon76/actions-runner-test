package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.application.SecurityService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;


@Getter
@AllArgsConstructor
public class ResetDevice {

    private Long tokenExpTime;

    // emailToken.getUserId() + "_" + emailToken.getAccountToken()
    //userId_token (유저ID_디바이스토큰) 암호화
    private String deviceResetToken;

    @JsonIgnore
    private String code;


    public boolean isValidTokenIdAndCode(SecurityService securityService, String emailTokenId, String codeNumber) {
        final boolean isCodeMatch = codeNumber.equals(code);
        final String accountToken = securityService.decryptAES256(deviceResetToken).split("_")[1];

        return StringUtils.equals(accountToken, emailTokenId) && isCodeMatch;
    }


    @JsonIgnore
    public String getUserId(SecurityService securityService) {
        return securityService.decryptAES256(deviceResetToken).split("_")[0];
    }

    public boolean isTokenExpired() {
        java.time.LocalDateTime expirationDateTime = java.time.LocalDateTime.ofInstant(
                Instant.ofEpochMilli(tokenExpTime),
                ZoneId.systemDefault()
        );
        return LocalDateTime.now(Clock.systemDefaultZone()).isBefore(expirationDateTime);
    }
}
