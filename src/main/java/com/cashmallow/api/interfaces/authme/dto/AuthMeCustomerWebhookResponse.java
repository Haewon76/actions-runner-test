package com.cashmallow.api.interfaces.authme.dto;

public record AuthMeCustomerWebhookResponse(
        AuthMeCustomerWebhookVerificationResponse data,
        String customerId,
        String type,
        String event
) {
    public boolean isChangeStateApprovedOrRejected() {
        boolean isFinalDataState = isApproved() || isRejected();
        boolean isFinalChangeState = "IdentityVerification.ChangeState".equalsIgnoreCase(type) || "IdentityVerification.ChangeStateEventV2".equalsIgnoreCase(type);
        return isFinalChangeState && isFinalDataState;
    }

    public boolean isManualApproved() {
        return "IdentityVerification.ChangeState".equalsIgnoreCase(type) && data.isManualApproved();
    }

    public boolean isApproved() {
        return data.isApproved();
    }

    public boolean isRejected() {
        return data.isRejected();
    }

    public Long getUserId() {
        return Long.parseLong(customerId.substring(2));
    }

    public String getCountry() {
        return customerId.substring(0, 1);
    }
}
