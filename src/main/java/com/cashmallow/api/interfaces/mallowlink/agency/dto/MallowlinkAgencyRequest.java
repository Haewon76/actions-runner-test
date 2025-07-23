package com.cashmallow.api.interfaces.mallowlink.agency.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @param currency USD, HKD, KRW ...
 */
public record MallowlinkAgencyRequest(
        @NotNull
        CountryCode countryCode,
        @NotNull
        String currency,
        @NotBlank
        String latitude,
        @NotBlank
        String longitude
) {

    public MallowlinkAgencyRequest(CountryCode countryCode,
                                   Double latitude,
                                   Double longitude) {
        this(countryCode,
                countryCode.getCurrency(),
                String.valueOf(latitude),
                String.valueOf(longitude));
    }
}
