package com.cashmallow.api.interfaces.mallowlink.common.dto;

import lombok.Data;

@Data
public class MallowlinkErrorResponse {

    private final String code;
    private final String message;

}
