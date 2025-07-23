package com.cashmallow.api.interfaces.global.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ExchangeConfigRequest {
    private Long id;
    private Long syncId;
    private String fromCd;
    private String toCd;
    private BigDecimal feeRateExchange;
    private BigDecimal minFee;
    private boolean canExchange;
    private boolean enabledExchange;
    private String exchangeNotice;
    private BigDecimal refundFeePer;
    private BigDecimal feePerExchange;
    private boolean canRemittance;
    private boolean enabledRemittance;
    private BigDecimal feePerRemittance;
    private BigDecimal feeRateRemittance;
    private String remittanceNotice;
}
