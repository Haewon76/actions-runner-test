package com.cashmallow.api.domain.model.traveler;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AllWallet {
    private String fromCd;
    private String toCd;
    private BigDecimal totalPendingMoney; // 지급 대기 총액 eMoney + cMoney
    private BigDecimal eMoney;
    private BigDecimal rMoney;
    private BigDecimal cMoney;
    private int count;
}
