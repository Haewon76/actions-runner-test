package com.cashmallow.api.domain.model.fx;

import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.CashmallowFxQuotationResponse;
import com.cashmallow.common.JsonStr;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class FxQuotationEntity {
    private Long id;

    private String transactionId;
    private String exchangeTarget;
    private Long endUserId;
    private String fromCurrency;
    private String toCurrency;
    private String currencyPair;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private String fromAccount;
    private String toAccount;
    private String status;
    private String approveId;
    private String responseJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FxQuotationEntity(CashmallowFxQuotationRequest request, CashmallowFxQuotationResponse dbsFxQuotation) {
        this.transactionId = request.getTransactionId();
        this.exchangeTarget = request.getExchangeTarget();
        this.endUserId = Long.parseLong(request.getEndUserId());
        this.fromCurrency = dbsFxQuotation.getFromCurrency();
        this.toCurrency = dbsFxQuotation.getToCurrency();
        this.currencyPair = dbsFxQuotation.getCurrencyPair();
        this.fromAmount = dbsFxQuotation.getFromAmount();
        this.toAmount = request.getTargetAmount();
        this.fromAccount = request.getFromAccount();
        this.toAccount = request.getToAccount();
        this.status = "RESERVED";
        this.approveId = dbsFxQuotation.getUid();
        this.responseJson = JsonStr.toJson(dbsFxQuotation);
    }
}
