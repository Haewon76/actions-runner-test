package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class DbsFxTransactionResponse {
    List<String> clientTransactionIdList;
}
