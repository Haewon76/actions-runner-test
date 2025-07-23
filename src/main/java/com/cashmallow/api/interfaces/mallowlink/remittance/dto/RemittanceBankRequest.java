package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceBankRequest {
    private final CountryCode countryCode;
    private final String currency;

    @NotBlank
    private final BigDecimal amount;

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
