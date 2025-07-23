package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CashmallowFxQuotationApproveResponse {

    private String fromCurrency;
    private BigDecimal fromAmount;
    private String toCurrency;
    private BigDecimal toAmount;
    private String expireDate;
    private BigDecimal rate;
    private String uid;
    private String endUserId;

}
