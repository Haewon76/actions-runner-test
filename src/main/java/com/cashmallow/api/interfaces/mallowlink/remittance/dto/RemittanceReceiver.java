package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.remittance.Remittance;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceReceiver {
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final CountryCode countryCode;
    private final String phoneNumber;
    private final String callingCode;
    private final String email;
    private final String address;
    private final String bankCode;
    private final String bankAccount;

    private final Remittance.AccountType accountType;  // for EUR, USD
    private final String ibanCode;          // for EUR
    private final String swiftCode;         // for EUR
    private final String routingNumber;     // for USD, BDT
    private final String ifscCode;          // for INR
    private final String cardNumber;        // for CNY UnionPay

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

    public static RemittanceReceiver of(Remittance remittance) {
        return new RemittanceReceiver(
                remittance.getReceiverFirstName(),
                remittance.getReceiverLastName(),
                remittance.getReceiverBirthDate(),
                CountryCode.valueOf(remittance.getReceiverCountry()),
                remittance.getReceiverPhoneNo(),
                remittance.getReceiverPhoneCountry(),
                remittance.getReceiverEmail(),
                remittance.getReceiverAddressCountry() + ", " + remittance.getReceiverAddress(),
                remittance.getReceiverBankCode(),
                remittance.getReceiverBankAccountNo(),
                remittance.getReceiverAccountType(),
                remittance.getReceiverIbanCode(),
                remittance.getReceiverSwiftCode(),
                remittance.getReceiverRoutingNumber(),
                remittance.getReceiverIfscCode(),
                remittance.getReceiverCardNumber()
        );
    }

}
