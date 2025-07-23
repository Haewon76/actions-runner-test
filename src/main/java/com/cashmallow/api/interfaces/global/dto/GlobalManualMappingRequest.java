package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public record GlobalManualMappingRequest(
        @NotNull
        TransactionRecord.RelatedTxnType txnType,
        @Min(1)
        long relatedTxnId,
        BigDecimal amount,
        String currency,
        @Min(1)
        long travelerId,
        String senderName,
        @NotEmpty
        List<String> depositIdList,
        CountryCode countryCode,
        String bankCode,
        String accountNo
) {
}
