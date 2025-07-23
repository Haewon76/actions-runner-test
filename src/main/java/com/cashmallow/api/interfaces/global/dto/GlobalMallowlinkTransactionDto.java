package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

public record GlobalMallowlinkTransactionDto(TransactionRecord.RelatedTxnType txnType,
                                             Long cmTransactionId,
                                             String mallowlinkTransactionId) {
}
