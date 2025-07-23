package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

@Data
public class ChangeBankAccountIdRequest {
    public enum TxnType {
        EXCHANGE, // 환전
        REMITTANCE // 송금 매핑 완료
    }

    private final TxnType txnType;
    private final Long id;
    private final Long bankAccountId;

}
