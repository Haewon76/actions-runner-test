package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.traveler.GlobalTravelerCertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TravelerCertificationJpResponse {
    private Long id;
    private Long userId;
    CertificationType certificationType;
    CertificationStep certificationStep;

    public static TravelerCertificationJpResponse of(GlobalTravelerCertificationStep certificationStep) {
        return new TravelerCertificationJpResponse(
                certificationStep.getId(),
                certificationStep.getUserId(),
                certificationStep.getCertificationType(),
                certificationStep.getCertificationStep()
        );
    }
}
