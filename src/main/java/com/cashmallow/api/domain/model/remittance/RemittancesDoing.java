package com.cashmallow.api.domain.model.remittance;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 진행 중(DP, RR, RC)인 송금 내역 합계
 */
@Data
public class RemittancesDoing {
    private String fromCd;
    private String toCd;
    private BigDecimal fromAmt;
    private BigDecimal toAmt;
}
