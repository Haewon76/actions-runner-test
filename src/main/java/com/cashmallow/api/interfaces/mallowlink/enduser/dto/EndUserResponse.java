package com.cashmallow.api.interfaces.mallowlink.enduser.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;

import java.time.ZonedDateTime;

public record EndUserResponse(
        String userId,
        String firstName,
        String lastName,
        String dateOfBirth,
        CountryCode countryCode,
        String phoneNumber,
        String callingCode,
        String email,
        ZonedDateTime requestTime
) {

}
