package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record GlobalChangeTransactionBankRequest(
        @NotNull
        TransactionRecord.RelatedTxnType txnType,
        @Min(1)
        long relatedTxnId,
        @NotNull
        String bankCode,
        @NotNull
        String bankAccountNo
) {
}
