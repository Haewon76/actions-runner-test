package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.remittance.Remittance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record GlobalRemittanceDto(Long cmTransactionId,
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
                                  String purpose,
                                  String fundSource,
                                  String relationship,
                                  String addressStateProvince,
                                  String addressStateProvinceEn,
                                  String receiverFirstName,
                                  String receiverLastName,
                                  String receiverDateOfBirth,
                                  CountryCode receiverCountryCode,
                                  String receiverPhoneNumber,
                                  String receiverCallingCode,
                                  String receiverEmail,
                                  String receiverAddress,
                                  String receiverAddressStateProvince,
                                  String receiverAddressCity,
                                  String receiverAddressCountry,
                                  String receiverAddressLine,
                                  String receiverZipCode,
                                  String receiverBankName,
                                  String bankCode,
                                  String bankAccount,
                                  BigDecimal couponDiscountAmount,
                                  Long couponUserId,
                                  Long couponIssueSyncId

) {
    public GlobalRemittanceDto(Remittance remittance, String remitRelationship,
                               CountryCode fromCountry, CountryCode toCountry, String bankName, String addressStateProvince, String addressStateProvinceEn, Long couponIssueSyncId) {
        this(
                remittance.getId(),
                remittance.getTravelerId(),
                bankName,
                fromCountry.getCurrency(),
                remittance.getFromAmt(),
                toCountry,
                toCountry.getCurrency(),
                remittance.getToAmt(),
                remittance.getFee(),
                remittance.getFeeRate(),
                remittance.getFeeRateAmt(),
                remittance.getFeePerAmt(),
                remittance.getExchangeRate(),
                remittance.getExchangeRate().multiply(remittance.getToAmt()).setScale(0, RoundingMode.HALF_UP),
                remittance.getRemitPurpose().name(),
                remittance.getRemitFundSource().name(),
                remitRelationship,
                addressStateProvince,
                addressStateProvinceEn,
                remittance.getReceiverFirstName(),
                remittance.getReceiverLastName(),
                remittance.getReceiverBirthDate(),
                CountryCode.valueOf(remittance.getReceiverCountry()),
                remittance.getReceiverPhoneNo(),
                CountryInfo.callingPrefix.get(remittance.getReceiverPhoneCountry()),
                remittance.getReceiverEmail(),
                remittance.getReceiverAddress(),
                remittance.getReceiverAddressStateProvince(),
                remittance.getReceiverAddressCity(),
                remittance.getReceiverAddressCountry(),
                remittance.getReceiverAddressSecondary(),
                remittance.getReceiverZipCode(),
                remittance.getReceiverBankName(),
                remittance.getReceiverBankCode(),
                remittance.getReceiverBankAccountNo(),
                remittance.getCouponDiscountAmt(),
                remittance.getCouponUserId(),
                couponIssueSyncId
        );
    }
}
