package com.cashmallow.api.interfaces.authme.dto;

import java.util.List;

public record AuthmeEvent(
        AuthMeCustomerEventResponse customerEvent,
        List<TravelerImage> travelerImages
) {
}
