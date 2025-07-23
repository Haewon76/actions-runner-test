package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

@Data
public class MatchPaygateRecordRequest {
    private final String cmTempTid;
    private final String realTid;
}
