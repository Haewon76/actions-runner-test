package com.cashmallow.api.interfaces.authme.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record AuthMeCustomerEventResponse(
        String status, // Pending, Approved, Rejected
        String code,
        String message,
        AuthMeCustomerResponseDocument document,
        List<AuthMeCustomerResponseDocument> documents,
        AuthMeCustomerResponseLiveness liveness,
        String id,
        ZonedDateTime createTime,
        ZonedDateTime updateTime
) {

    public static AuthMeCustomerEventResponse rejected() {
        return new AuthMeCustomerEventResponse("Rejected", null, null, null, null, null, null, null, null);
    }

    public boolean isApproved() {
        return "Approved".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "Rejected".equalsIgnoreCase(status);
    }

    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }

    public boolean isInComplete() {
        return "Incomplete".equalsIgnoreCase(status);
    }

    public record AuthMeCustomerResponseLiveness(
            double score,
            ZonedDateTime createTime,
            ZonedDateTime updateTime
    ) {
    }

    public AuthMeCustomerResponseDocument getDocument() {
        return document();
    }
}
