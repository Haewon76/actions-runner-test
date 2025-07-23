package com.cashmallow.api.interfaces.scb.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SCBLog {
    private final String transactionId;
    private final String requestJson;
    private final String responseJson;
    private final LogType requestType;
    private final int code;
}
