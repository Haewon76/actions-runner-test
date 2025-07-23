package com.cashmallow.api.interfaces.global.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record Deposit(
        @NotBlank
        String depositId,

        @Positive
        BigDecimal amount,

        String description,

        @NotBlank
        String senderName,
        String senderBank,
        String senderAccountNo,

        ZonedDateTime depositTime
) {
}
