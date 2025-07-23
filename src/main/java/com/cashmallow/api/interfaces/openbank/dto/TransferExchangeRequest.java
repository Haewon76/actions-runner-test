package com.cashmallow.api.interfaces.openbank.dto;

import lombok.Data;

@Data
public class TransferExchangeRequest {
    // private BigDecimal from_amt;
    private Long exchange_id;
    private String otp;
}
