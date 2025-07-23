package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceBankBranchesRequest {
    private final CountryCode countryCode;
    @NotBlank
    private final String bankId;

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

}
