package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.country.enums.CountryCode;

import java.math.BigDecimal;

public record GlobalWithdrawalDto(Long cmTransactionId,
                                  Long travelerId,
                                  Long withdrawalParentId,
                                  CountryCode toCountry,
                                  String toCurrency, //iso4217 코드
                                  BigDecimal toAmount,
                                  BigDecimal totalFee,
                                  BigDecimal feePerAmount,
                                  Long originalCmExchangeId,
                                  String mallowlinkTxnId
) {
    public GlobalWithdrawalDto(CashOut cashOut, CountryCode toCountry) {
        this(
                cashOut.getId(),
                cashOut.getTravelerId(),
                cashOut.getWithdrawalPartnerId(),
                toCountry,
                toCountry.getCurrency(),
                cashOut.getTravelerCashOutAmt(),
                cashOut.getTravelerCashOutFee(),
                cashOut.getTravelerCashOutFee(),
                cashOut.getExchangeId(),
                cashOut.getCasmTxnId()
        );
    }
}
