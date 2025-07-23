package com.cashmallow.api.interfaces.mallowlink.currency.dto;

import lombok.Data;

@Data
public class MallowlinkBoBaseResponse<T> {

    private final String status;
    private final String message;

    private final T result;

}
