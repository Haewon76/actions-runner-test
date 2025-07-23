package com.cashmallow.api.domain.model.traveler;

public record TravelerVerificationHistory(
        String travelerId,
        String travelerJson,
        String userJson,
        String worker
) {
}
