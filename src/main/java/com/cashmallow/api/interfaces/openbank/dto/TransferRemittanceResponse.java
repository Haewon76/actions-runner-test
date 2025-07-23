package com.cashmallow.api.interfaces.openbank.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TransferRemittanceResponse {

    private final Long remittanceId;
    private final BigDecimal fromAmt;

    private final String rspCode;
    private final String rspMessage;
}
