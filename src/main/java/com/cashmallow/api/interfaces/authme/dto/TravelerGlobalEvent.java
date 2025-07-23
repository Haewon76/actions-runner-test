package com.cashmallow.api.interfaces.authme.dto;

import java.util.List;

public record TravelerGlobalEvent(
        List<TravelerImage> images
) {
}
