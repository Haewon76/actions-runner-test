package com.cashmallow.api.interfaces.hyphen.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class BankAccountInfoVo {
    private final String bankCode;
    private final String bankName;
    private final String accountNumber;
    private final String accountName;
}
