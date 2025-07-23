package com.cashmallow.api.interfaces.mallowlink.enduser.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public final class EndUserInquiryRequest {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
    private final String userId;

    public static EndUserInquiryRequest of(String userId) {
        return new EndUserInquiryRequest(userId);
    }

}
