package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

@Data
public class DbsBalanceRequest {
    private String accountNo;
    private String accountCcy;
    private String endUserId;
}
