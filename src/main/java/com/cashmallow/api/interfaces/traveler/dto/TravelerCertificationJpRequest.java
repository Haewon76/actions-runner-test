package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.traveler.enums.CertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;

import javax.validation.constraints.NotNull;

public record TravelerCertificationJpRequest(
        @NotNull Long userId,
        @NotNull CertificationType certificationType,
        @NotNull CertificationStep certificationStep,
        String accountLastName,
        String accountFirstName
) {
}
