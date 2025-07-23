package com.cashmallow.api.interfaces.mallowlink.controller.dto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record DepositEmailRequest(
        BigDecimal amount,
        String currency,
        ZonedDateTime receivedTime
) {
}
