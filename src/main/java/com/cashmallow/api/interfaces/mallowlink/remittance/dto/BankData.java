package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BankData {
    private final String bankId;
    private final String bankCode;
    private final String bankName;
    private final String bankNameEng;

    public static BankData of(RemittanceBank bank) {
        return new BankData(bank.getBankId(), bank.getBankCode(), bank.getBankName(), bank.getBankNameEng());
    }
}
