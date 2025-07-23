package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import lombok.Data;
import org.springframework.transaction.TransactionStatus;

import java.math.BigDecimal;

@Data
public final class MallowlinkInquiryResponse {
    private final String transactionId;
    private final String userId;
    private final CountryCode countryCode;
    private final String currency;
    private final BigDecimal amount;
    private final TransactionStatus status;
}
