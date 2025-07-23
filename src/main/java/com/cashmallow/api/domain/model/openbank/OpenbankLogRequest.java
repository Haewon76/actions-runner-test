package com.cashmallow.api.domain.model.openbank;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OpenbankLogRequest {
    private long id;
    private final OpenbankRequestType requestType;
    private final String requestBankTranId;
    private final String requestData;
}
