package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class PaygateRecordRequestManual {
    @NotBlank
    private final String exchangeId;
    @NotBlank
    private final BigDecimal amount;
    private final Long bankAccountId;
}
