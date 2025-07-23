package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

public record GlobalTransactionCancelDto(TransactionRecord.RelatedTxnType txnType,
                                         Long cmTransactionId,
                                         String cancelStatus,
                                         Long couponIssueSyncId
                                         ) {
}
