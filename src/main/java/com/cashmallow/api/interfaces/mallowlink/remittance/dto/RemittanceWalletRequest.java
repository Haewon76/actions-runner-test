package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceWalletRequest {
    private final CountryCode countryCode;
    private final String currency;

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
