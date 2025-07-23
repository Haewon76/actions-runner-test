package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

@Data
public class DbsRemittanceNotificationRequest {
    private String remittanceId;
    private String remittanceResultCode;
    private String remittanceRejectReason;
}
