package com.cashmallow.api.domain.model.openbank;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OpenbankLog {
    private long id;
    private OpenbankRequestType requestType;
    private String requestBankTranId;
    private String requestData;
    private ZonedDateTime requestTime;
    private String responseCode;
    private String responseTranBankId;
    private String responseData;
    private String responseRspCode;
    private String responseRspMessage;
    private ZonedDateTime responseTime;
}
