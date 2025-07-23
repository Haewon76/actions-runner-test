package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record InsertDepositRequest(
        @NotNull
        CountryCode country,

        @NotEmpty
        String bankCode,

        String bankAccountNumber,

        @NotEmpty
        String currency,

        @Valid
        @NotNull
        Deposit deposit
) {
}
