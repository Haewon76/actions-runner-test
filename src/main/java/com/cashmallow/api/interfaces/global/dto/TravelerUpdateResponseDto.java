package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;

public record TravelerUpdateResponseDto(
        String addressCity,
        String addressLocal,
        String addressFull,
        String zipCode
) {
    public TravelerUpdateResponseDto(String addressLocal,
                                     GoogleAddressResultResponse address) {
        this(
                address.getCityName(),
                addressLocal,
                address.getFullAddress(),
                address.getZipCode()
        );
    }
}