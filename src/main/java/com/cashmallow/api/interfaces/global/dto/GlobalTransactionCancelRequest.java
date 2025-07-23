package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public record GlobalTransactionCancelRequest(
        @NotNull
        TransactionRecord.RelatedTxnType txnType,
        @Min(1)
        long relatedTxnId
) {
}
