package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.domain.model.traveler.enums.CertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class GlobalTravelerCertificationStep {
    private Long id;
    private Long userId;
    private CertificationType certificationType;
    private CertificationStep certificationStep;
    private String photoFileName;
    private boolean isActive;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public GlobalTravelerCertificationStep(Long userId, CertificationType certificationType,
                                           CertificationStep certificationStep, String photoFileName) {
        this.userId = userId;
        this.certificationType = certificationType;
        this.certificationStep = certificationStep;
        this.photoFileName = photoFileName;
        this.isActive = true;
    }
}
