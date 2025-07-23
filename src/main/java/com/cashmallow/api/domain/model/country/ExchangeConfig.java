package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
public class ExchangeConfig {

    private Long id;
    private String fromCd;
    private String toCd;
    private BigDecimal feeRateExchange;
    private BigDecimal feeRateRemittance;
    private BigDecimal minFee;
    private BigDecimal refundFeePer;
    private String canExchange;
    private String canRemittance;
    private String enabledExchange;
    private String enabledRemittance;
    private String exchangeNotice;
    private String remittanceNotice;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;
    private BigDecimal feePerExchange;
    private BigDecimal feePerRemittance;

    private BigDecimal fromEddAmountLimit;
    private Integer fromEddCountLimit;
    private BigDecimal fromMinRemittance;
    private BigDecimal fromMaxRemittance;
    private BigDecimal fromMinExchange;
    private BigDecimal fromMaxExchange;
    private BigDecimal fromMinWithdrawal;
    private BigDecimal fromMaxWithdrawal;
    private BigDecimal fromMaxRefund;
    private BigDecimal fromDayMaxExchange;
    private BigDecimal fromMonthMaxExchange;
    private BigDecimal fromAnnualMaxExchange;

    // from기준 wallet에 가질수있는 최대 환전금액
    private BigDecimal fromTotalMaxExchange;

    private BigDecimal toMinRemittance;
    private BigDecimal toMaxRemittance;
    private BigDecimal toMinExchange;
    private BigDecimal toMaxExchange;
    private BigDecimal toMinWithdrawal;
    private BigDecimal toMaxWithdrawal;
    private BigDecimal toDayMaxExchange;
    private BigDecimal toMonthMaxExchange;
    private BigDecimal toAnnualMaxExchange;
    private BigDecimal toDayMaxWithdrawal;
    private BigDecimal toMonthMaxWithdrawal;

    private Long walletExpiredDay;
    private Long walletExpiredMinute;

    public ExchangeConfig(String fromCd, String toCd, BigDecimal feeRateExchange, BigDecimal feeRateRemittance, BigDecimal minFee, BigDecimal refundFeePer,
                          String canExchange, String canRemittance, String enabledExchange, String enabledRemittance, String exchangeNotice, String remittanceNotice,
                          Long creator, BigDecimal feePerExchange, BigDecimal feePerRemittance, BigDecimal fromEddAmountLimit, Integer fromEddCountLimit,
                          BigDecimal fromMinRemittance, BigDecimal fromMaxRemittance, BigDecimal fromMinExchange, BigDecimal fromMaxExchange,
                          BigDecimal fromMinWithdrawal, BigDecimal fromMaxWithdrawal, BigDecimal fromMaxRefund, BigDecimal fromDayMaxExchange,
                          BigDecimal fromMonthMaxExchange, BigDecimal fromAnnualMaxExchange, BigDecimal toMinRemittance, BigDecimal toMaxRemittance, BigDecimal toMinExchange,
                          BigDecimal toMaxExchange, BigDecimal toMinWithdrawal, BigDecimal toMaxWithdrawal,
                          BigDecimal toDayMaxExchange, BigDecimal toMonthMaxExchange, BigDecimal toAnnualMaxExchange, BigDecimal toDayMaxWithdrawal,
                          BigDecimal toMonthMaxWithdrawal, Long walletExpiredDay, Long walletExpiredMinute) {
        this.fromCd = fromCd;
        this.toCd = toCd;
        this.feeRateExchange = feeRateExchange;
        this.feeRateRemittance = feeRateRemittance;
        this.minFee = minFee;
        this.refundFeePer = refundFeePer;
        this.canExchange = canExchange;
        this.canRemittance = canRemittance;
        this.enabledExchange = enabledExchange;
        this.enabledRemittance = enabledRemittance;
        this.exchangeNotice = exchangeNotice;
        this.remittanceNotice = remittanceNotice;
        this.creator = creator;
        this.feePerExchange = feePerExchange;
        this.feePerRemittance = feePerRemittance;
        this.fromEddAmountLimit = fromEddAmountLimit;
        this.fromEddCountLimit = fromEddCountLimit;
        this.fromMinRemittance = fromMinRemittance;
        this.fromMaxRemittance = fromMaxRemittance;
        this.fromMinExchange = fromMinExchange;
        this.fromMaxExchange = fromMaxExchange;
        this.fromMinWithdrawal = fromMinWithdrawal;
        this.fromMaxWithdrawal = fromMaxWithdrawal;
        this.fromMaxRefund = fromMaxRefund;
        this.fromDayMaxExchange = fromDayMaxExchange;
        this.fromMonthMaxExchange = fromMonthMaxExchange;
        this.fromAnnualMaxExchange = fromAnnualMaxExchange;
        this.toMinRemittance = toMinRemittance;
        this.toMaxRemittance = toMaxRemittance;
        this.toMinExchange = toMinExchange;
        this.toMaxExchange = toMaxExchange;
        this.toMinWithdrawal = toMinWithdrawal;
        this.toMaxWithdrawal = toMaxWithdrawal;
        this.toDayMaxExchange = toDayMaxExchange;
        this.toMonthMaxExchange = toMonthMaxExchange;
        this.toAnnualMaxExchange = toAnnualMaxExchange;
        this.toDayMaxWithdrawal = toDayMaxWithdrawal;
        this.toMonthMaxWithdrawal = toMonthMaxWithdrawal;
        this.walletExpiredDay = walletExpiredDay;
        this.walletExpiredMinute = walletExpiredMinute;
    }
}
