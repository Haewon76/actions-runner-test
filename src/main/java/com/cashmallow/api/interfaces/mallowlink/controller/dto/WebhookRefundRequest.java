package com.cashmallow.api.interfaces.mallowlink.controller.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigInteger;
import java.time.ZonedDateTime;

public record WebhookRefundRequest(
        Type type,
        RefundType refundType,  // REVERT(1), REFUND(2)
        String clientTransactionId, // 원 transactionId
        Status status, // 상태코드(1:SUCCESS, 2:FAIL)
        ZonedDateTime statusUpdateAt, // BO 요청 시간 (현지시간으로 변환)
        Currency currency, // 인출 화폐
        CountryCode country, // 인출 국가
        BigInteger refundAmount, // 환불금액
        BigInteger reCompleteAmount, // 재처리 금액,
        String reCompleteTransactionId, // 재처리 TransactionId
        ZonedDateTime requestTime // ML 발송 시간
) {

    public enum Type {
        WITHDRAWAL,
        REMITTANCE
    }

    @AllArgsConstructor
    @Getter
    public enum RefundType {
        REVERT("1"),
        REFUND("2");

        private final String code;
    }

    @RequiredArgsConstructor
    @Getter
    public enum Status {
        SUCCESS("1"),
        FAIL("2"),
        ;

        private final String code;
    }

}

