package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.traveler.enums.ApprovalType;

public record GlobalTravelerCertificationRequest(
        String managerName,
        String rejectReason,
        ApprovalType approvalType,
        Long travelerCertificationStepId,
        boolean needAccount
) {

}
