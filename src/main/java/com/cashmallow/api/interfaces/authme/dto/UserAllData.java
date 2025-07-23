package com.cashmallow.api.interfaces.authme.dto;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerVerificationStatusResponse;
import com.cashmallow.api.domain.model.user.User;

import java.util.List;

public record UserAllData(
        User user,
        Traveler traveler,
        List<TravelerImage> travelerImages,
        List<TravelerVerificationStatusResponse> verificationStatuses
) {
}
