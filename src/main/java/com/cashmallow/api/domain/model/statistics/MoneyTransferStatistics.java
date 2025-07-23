package com.cashmallow.api.domain.model.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;


/**
 * Domain model for MoneyTransferStatistics
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class MoneyTransferStatistics {

    public enum Type {
        EXCHANGE, // 환전
        REMITTANCE, // 송금 매핑 완료
        REMITTANCE_CF, // 송금 완료
        REFUND_RM, // 송금 환불
        REFUND_EX, // 환전 환불
        CASH_OUT // 인출
    }

    private Long id;
    private String fromCd;
    private BigDecimal fromAmt;
    private BigDecimal fromOriAmt;
    private String toCd;
    private BigDecimal toAmt;
    private BigDecimal fee;
    private BigDecimal feePerAmt;
    private BigDecimal feeRateAmt;
    private int totCnt;
    private Type type;
    private BigDecimal mappedAmt;
    private LocalDate createdDate;
    private String createdDateString;
}
