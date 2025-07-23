package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyLimit {

    /**
     * 생성자 // TODO : 처리 방안 논의 필요
     */
    @Setter
    private Long creator = -1L;

    /**
     * ID
     */
    private Long id;

    /**
     * FROM 국가코드
     */
    private String fromCd;

    /**
     * TO 국가코드
     */
    private String toCd;

    /**
     * from화폐기준 user edd 금액 제한
     */
    private BigDecimal fromEddAmountLimit;

    /**
     * from화폐기준 user edd 횟수 제한
     */
    private Long fromEddCountLimit;

    /**
     * from화폐기준 최소 송금 금액
     */
    private BigDecimal fromMinRemittance;

    /**
     * from화폐기준 최대 송금 금액
     */
    private BigDecimal fromMaxRemittance;

    /**
     * from화폐기준 최소 환전 금액
     */
    private BigDecimal fromMinExchange;

    /**
     * from화폐기준 최대 환전 금액
     */
    private BigDecimal fromMaxExchange;

    /**
     * from화폐기준 최소 인출 금액
     */
    private BigDecimal fromMinWithdrawal;

    /**
     * from화폐기준 최대 인출 금액
     */
    private BigDecimal fromMaxWithdrawal;

    /**
     * from화폐기준 최대 환불 금액
     */
    private BigDecimal fromMaxRefund;

    /**
     * from화폐기준 일간 최대 환전 금액
     */
    private BigDecimal fromDayMaxExchange;

    /**
     * from화폐기준 월간 최대 환전 금액
     */
    private BigDecimal fromMonthMaxExchange;

    /**
     * from화폐기준 연간 최대 환전 금액
     */
    private BigDecimal fromAnnualMaxExchange;

    /**
     * from화폐기준 최대 환전 금액(전체 통합)
     */
    private BigDecimal fromTotalMaxExchange;

    /**
     * to화폐기준 최소 송금 금액
     */
    private BigDecimal toMinRemittance;

    /**
     * to화폐기준 최대 송금 금액
     */
    private BigDecimal toMaxRemittance;

    /**
     * to화폐기준 최소 환전 금액
     */
    private BigDecimal toMinExchange;

    /**
     * to화폐기준 최대 환전 금액
     */
    private BigDecimal toMaxExchange;

    /**
     * to화폐기준 최소 인출 금액
     */
    private BigDecimal toMinWithdrawal;

    /**
     * to화폐기준 최대 인출 금액
     */
    private BigDecimal toMaxWithdrawal;

    /**
     * to화폐기준 일간 최대 환전 금액
     */
    private BigDecimal toDayMaxExchange;

    /**
     * to화폐기준 월간 최대 환전 금액
     */
    private BigDecimal toMonthMaxExchange;

    /**
     * to화폐기준 연간 최대 환전 금액
     */
    private BigDecimal toAnnualMaxExchange;

    /**
     * to화폐기준 일간 최대 인출 금액
     */
    private BigDecimal toDayMaxWithdrawal;

    /**
     * to화폐기준 월간 최대 인출 금액
     */
    private BigDecimal toMonthMaxWithdrawal;

    /**
     * E-티켓 만료일
     */
    private Integer walletExpiredDay;

    /**
     * E-티켓 만료시간(만료일 자정 - 분)
     */
    private Integer walletExpiredMinute;
}
