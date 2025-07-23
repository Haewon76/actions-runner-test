package com.cashmallow.api.interfaces.authme.dto;

public record AuthMeWebhookDto(
        String passportIssueDate,
        String passportExpDate,
        String passportCountry,
        String certificationType,
        String identificationNumber,
        String certificationOkDate
) {

}
