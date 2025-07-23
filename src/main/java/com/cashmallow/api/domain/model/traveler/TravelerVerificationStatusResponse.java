package com.cashmallow.api.domain.model.traveler;

public record TravelerVerificationStatusResponse(
        String verificationType,
        String verificationValue,
        String verificationHoldReason,
        String fileName,
        String creater,
        String createdDate
) {
}
