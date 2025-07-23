package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

@Data
public class PaygateRecordDeleteRequest {
    private String exchangeId;
    private String description;
}
