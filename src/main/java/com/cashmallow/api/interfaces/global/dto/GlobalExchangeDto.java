package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record GlobalExchangeDto(Long cmTransactionId,
                                Long travelerId,
                                String bankName,
                                String fromCurrency, //iso4217 코드
                                BigDecimal fromAmount,
                                CountryCode toCountry,
                                String toCurrency, //iso4217 코드
                                BigDecimal toAmount,
                                BigDecimal totalFee,
                                BigDecimal feeRate,
                                BigDecimal feeRateAmount,
                                BigDecimal feePerAmount,
                                BigDecimal currencyRate,
                                BigDecimal currencyValue,
                                BigDecimal couponDiscountAmount,
                                Long couponUserId,
                                Long couponIssueSyncId
) {
    public GlobalExchangeDto(Exchange exchange, CountryCode fromCountry, CountryCode toCountry, String bankName, Long couponIssueSyncId) {
        this(
                exchange.getId(),
                exchange.getTravelerId(),
                bankName,
                fromCountry.getCurrency(),
                exchange.getFromAmt(),
                toCountry,
                toCountry.getCurrency(),
                exchange.getToAmt(),
                exchange.getFee(),
                exchange.getFeeRate(),
                exchange.getFeeRateAmt(),
                exchange.getFeePerAmt(),
                exchange.getExchangeRate(),
                exchange.getExchangeRate().multiply(exchange.getToAmt()).setScale(0, RoundingMode.HALF_UP), // 일본은 소수점이 없다
                exchange.getCouponDiscountAmt(),
                exchange.getCouponUserId(),
                couponIssueSyncId
        );
    }
}
