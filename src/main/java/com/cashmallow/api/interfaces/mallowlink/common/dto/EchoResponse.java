package com.cashmallow.api.interfaces.mallowlink.common.dto;

import lombok.Data;

@Data
public class EchoResponse<T> {

    private final String clientName;
    private final T body;


}
