package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

public record GlobalUnpaidTransactionDto(TransactionRecord.RelatedTxnType transactionType,
                                         Long cmTransactionId
) {
}
