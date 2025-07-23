package com.cashmallow.api.interfaces.mallowlink.common.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class MallowlinkBaseResponse<T> {

    private final String code;
    private final String status;
    private final String message;
    private final ZonedDateTime responseTime;

    private final T data;

}
