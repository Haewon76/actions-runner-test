package com.cashmallow.api.interfaces.openbank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferExchangeResponse {

    private final Long exchangeId;
    private final BigDecimal fromAmt;

    private final String rspCode;
    private final String rspMessage;
}
