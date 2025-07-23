package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.interfaces.mallowlink.remittance.enums.IdCardType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record IdData(
        // ID_CARD_TYPE
        @NotNull
        IdCardType type,

        // ID_CARD_NUMBER
        @NotBlank
        String number
) {

    public static IdData of(Traveler traveler) {
        return new IdData(IdCardType.of(traveler.getCertificationType()), traveler.getIdentificationNumber());
    }
}
