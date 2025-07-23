package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public record GlobalRollbackMappingRequest(
        @NotNull
        TransactionRecord.RelatedTxnType txnType,
        @Min(1)
        Long relatedTxnId,
        @NotEmpty
        List<String> depositIdList,
        String bankCode,
        String accountNo
) {
}
