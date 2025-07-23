package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

public record InsertDepositBulkRequest(
        @NotNull
        CountryCode country,

        @NotEmpty
        String bankCode,

        String bankAccountNumber,

        @NotEmpty
        String currency,

        @Size(min = 1)
        List<Deposit> deposits
) {
}
