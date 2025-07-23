package com.cashmallow.api.interfaces.authme.dto;

public record AuthMeCustomerWebhookVerificationResponse(
        String status, // Pending, Approved, Rejected
        String source
) {
    public boolean isApproved() {
        return !"Agent".equalsIgnoreCase(source) && "Approved".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }

    public boolean isManualApproved() {
        return "Agent".equalsIgnoreCase(source) && "Approved".equalsIgnoreCase(status);
    }

}
