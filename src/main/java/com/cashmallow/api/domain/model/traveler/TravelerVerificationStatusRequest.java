package com.cashmallow.api.domain.model.traveler;

public record TravelerVerificationStatusRequest(
        Long travelerId,
        String verificationType,
        String verificationValue,
        String verificationHoldReason,
        String fileName,
        String creater
) {
}
