package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.Data;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Data
public final class RemittanceDto {
    private final String transactionId;
    private final String userId;
    private final String partnerName;
    private final CountryCode countryCode;
    private final String currency;
    private final BigDecimal amount;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String ip;
    private final ZonedDateTime requestTime;
    private final ZonedDateTime expireTime;
    private final String status;

}
