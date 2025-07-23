package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.remittance.Remittance;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record GlobalReRegisterRemittanceDto(Long cmTransactionId,
                                            String receiverFirstName,
                                            String receiverLastName,
                                            String receiverDateOfBirth,
                                            CountryCode receiverCountryCode,
                                            String receiverPhoneNumber,
                                            String receiverCallingCode,
                                            String receiverEmail,
                                            String receiverAddress,
                                            String receiverAddressCity,
                                            String receiverAddressStateProvince,
                                            String receiverAddressCountry,
                                            String receiverAddressLine,
                                            String receiverBankName,
                                            String bankCode,
                                            String bankAccount,
                                            Remittance.RemittanceStatusCode status

) {
    public GlobalReRegisterRemittanceDto(Remittance remittance) {
        this(
                remittance.getId(),
                remittance.getReceiverFirstName(),
                remittance.getReceiverLastName(),
                remittance.getReceiverBirthDate(),
                CountryCode.valueOf(remittance.getReceiverCountry()),
                remittance.getReceiverPhoneNo(),
                CountryInfo.callingPrefix.get(remittance.getReceiverPhoneCountry()),
                remittance.getReceiverEmail(),
                remittance.getReceiverAddress(),
                remittance.getReceiverAddressCity(),
                remittance.getReceiverAddressStateProvince(),
                remittance.getReceiverAddressCountry(),
                remittance.getReceiverAddressSecondary(),
                remittance.getReceiverBankName(),
                remittance.getReceiverBankCode(),
                remittance.getReceiverBankAccountNo(),
                remittance.getRemitStatus()
        );
    }
}
