package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class CashmallowFxQuotationResponse {

    private Long quotationId;

    @NonNull
    private String fromCurrency;
    @NonNull
    private BigDecimal fromAmount;
    @NonNull
    private String toCurrency;
    @NonNull
    private BigDecimal toAmount;
    @NonNull
    private String expireDate;
    @NonNull
    private BigDecimal rate;
    @NonNull
    private String uid;
    @NonNull
    private String endUserId;

    private String currencyPair;

}
