package com.cashmallow.api.interfaces.traveler.dto;

import lombok.Data;

@Data
public class MatchPasswordRequest {

    private final String loginId;
    private final String password;

}
