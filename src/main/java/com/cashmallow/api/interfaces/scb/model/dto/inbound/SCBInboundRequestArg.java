package com.cashmallow.api.interfaces.scb.model.dto.inbound;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/*
    "receptionId" : "CASM0000012345",
    “TransactionId : "2022112500001",
    "result" : "done",
    "datetime" : "2022-06-28T17:38:00+07.00"
*/
@Data
public class SCBInboundRequestArg {
    private String receptionId;

    @JsonProperty("transactionId")
    @JsonAlias("TransactionId")
    private String transactionId;

    private String result; // done
    private String datetime;
}
