package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;


@Data
public final class RemittanceBank {
    private final String bankId;
    private final String bankName;
    private final String bankCode;
    private final String bankNameEng;
}
