package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

@Data
public class DbsRemittanceResponse {
    private String dbsTransactionId;
    private String dbsTransactionStatus;
    private String dbsTransactionDescription;
    private String endUserId;
}
