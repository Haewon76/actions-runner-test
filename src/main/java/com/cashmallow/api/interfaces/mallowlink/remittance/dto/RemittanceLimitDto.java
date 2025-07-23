package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import java.math.BigDecimal;

public record RemittanceLimitDto(
        String currency,
        String messageKey,
        String formattedLimit,
        BigDecimal limit
) { }
