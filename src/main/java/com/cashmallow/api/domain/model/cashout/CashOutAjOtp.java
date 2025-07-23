package com.cashmallow.api.domain.model.cashout;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CashOutAjOtp {
    private Long cashOutId;
    private String bankCodeVaNumber;
    private String withdrawalCode;
    private Timestamp expiredDate;

    public CashOutAjOtp(Long cashOutId, String bankCodeVaNumber, String withdrawalCode, Timestamp expiredDate) {
        this.cashOutId = cashOutId;
        this.bankCodeVaNumber = bankCodeVaNumber;
        this.withdrawalCode = withdrawalCode;
        this.expiredDate = expiredDate;
    }
}
