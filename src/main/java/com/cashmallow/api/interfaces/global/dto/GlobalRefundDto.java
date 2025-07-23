package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.refund.JpRefundAccountInfo;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record GlobalRefundDto(Long cmTransactionId,
                              Long travelerId,
                              CountryCode fromCountry,
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

                              Long cmRefundAccountId,
                              String localLastName,
                              String localFirstName,
                              Long mlBankId,
                              String bankCode,
                              String bankName,
                              String branchCode,
                              String branchName,
                              JpRefundAccountType accountType,
                              String accountNo,

                              TransactionRecord.RelatedTxnType originalTransactionType,
                              Long originalCmTransactionId // 환불과 연관된 Exchange 또는 Remittance ID
) {
    public GlobalRefundDto(NewRefund newRefund, CountryCode fromCountry, CountryCode toCountry, JpRefundAccountInfo jpRefundAccountInfo,
                           TransactionRecord.RelatedTxnType originalTransactionType, Long originalCmTransactionId) {
        this(
                newRefund.getId(),
                newRefund.getTravelerId(),
                fromCountry,
                fromCountry.getCurrency(),
                newRefund.getFromAmt(),
                toCountry,
                toCountry.getCurrency(),
                newRefund.getToAmt(),
                newRefund.getFee(),
                newRefund.getFeeRate(),
                newRefund.getFeeRateAmt(),
                newRefund.getFeePerAmt(),
                newRefund.getExchangeRate(),
                newRefund.getFromAmt().divide(newRefund.getExchangeRate(), 0, RoundingMode.HALF_UP),
                newRefund.getCouponDiscountAmount(),
                newRefund.getCouponUserId(),
                jpRefundAccountInfo.getId(),
                jpRefundAccountInfo.getLocalLastName(),
                jpRefundAccountInfo.getLocalFirstName(),
                jpRefundAccountInfo.getMlBankId(),
                jpRefundAccountInfo.getBankCode(),
                jpRefundAccountInfo.getBankName(),
                jpRefundAccountInfo.getBranchCode(),
                jpRefundAccountInfo.getBranchName(),
                jpRefundAccountInfo.getAccountType(),
                jpRefundAccountInfo.getAccountNo(),
                originalTransactionType,
                originalCmTransactionId
        );
    }
}

