package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.traveler.GlobalTravelerCertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;

public record GlobalTravelerCertificationStepDto(
        Long id,
        Long userId,
        CertificationType certificationType,
        CertificationStep certificationStep,
        String photoFileName,
        boolean isActive
) {
    public GlobalTravelerCertificationStepDto(GlobalTravelerCertificationStep globalTravelerCertificationStep) {
        this(
                globalTravelerCertificationStep.getId(),
                globalTravelerCertificationStep.getUserId(),
                globalTravelerCertificationStep.getCertificationType(),
                globalTravelerCertificationStep.getCertificationStep(),
                globalTravelerCertificationStep.getPhotoFileName(),
                globalTravelerCertificationStep.isActive()
        );
    }
}
