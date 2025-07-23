package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public record JpRefundAccountInfoRequest(
        @NotEmpty
        String localLastName,
        @NotEmpty
        String localFirstName,
        @NotNull
        @Min(1)
        Long bankId,
        @NotEmpty
        String bankCode,
        @NotEmpty
        String bankName,
        String branchCode,
        String branchName,
        @NotNull
        JpRefundAccountType accountType,
        @NotEmpty
        String accountNo
) {
}
