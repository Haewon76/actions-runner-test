package com.cashmallow.api.interfaces.openbank.dto;

import lombok.Data;

@Data
public class TransferRemittanceRequest {
    // private BigDecimal from_amt;
    private Long remittanceId;
    private String otp;
}
