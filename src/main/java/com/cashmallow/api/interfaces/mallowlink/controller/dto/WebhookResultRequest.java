package com.cashmallow.api.interfaces.mallowlink.controller.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public record WebhookResultRequest(
        Type type,
        Status status,
        @JsonProperty("statusUpdateAt")
        @JsonAlias("statusUpdateTime")
        ZonedDateTime statusUpdateAt,
        @JsonProperty("clientTransactionId")
        @JsonAlias("transactionId")
        String clientTransactionId,
        String errorMessage,
        @JsonProperty("requestTime")
        @JsonAlias("dateTime")
        ZonedDateTime requestTime
) {

    public enum Type {
        WITHDRAWAL,
        REMITTANCE
    }

    public enum Status {
        SUCCESS,
        FAIL,
        REVERT,
        INVALID_RECEIVER
    }

}
