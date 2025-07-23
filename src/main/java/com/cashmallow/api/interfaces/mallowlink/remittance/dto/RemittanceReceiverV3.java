package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.remittance.Remittance;
import lombok.Data;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Data
public final class RemittanceReceiverV3 {
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final CountryCode countryCode;
    private final String phoneNumber;
    private final String callingCode;
    private final String email;
    private final String address;
    private final String region; // AUD, USD만 필수
    private final Remittance.RemittanceType type;
    private final String typeCode;
    private final String typeNumber;

    private final Remittance.AccountType accountType;   // EUR, USD
    private final String ibanCode;                      // EUR
    private final String swiftCode;                     // EUR
    private final String routingNumber;                 // USD, BDT
    private final String ifscCode;                      // INR
    private final String cardNumber;                    // CNY UnionPay

    private final ZonedDateTime requestTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

    public static RemittanceReceiverV3 of(Remittance remittance) {
        return new RemittanceReceiverV3(
                remittance.getReceiverFirstName(),
                remittance.getReceiverLastName(),
                remittance.getReceiverBirthDate(),
                CountryCode.valueOf(remittance.getReceiverCountry()),
                remittance.getReceiverPhoneNo(),
                remittance.getReceiverPhoneCountry(),
                remittance.getReceiverEmail(),
                remittance.getReceiverAddress(),
                remittance.getReceiverAddressStateProvince(), // State/Province/Region (AUD, USD만 필수)
                remittance.getRemittanceType(),
                remittance.getReceiverBankCode(), // 앱 receiverTypeCode, 멜로링크 typeCode
                remittance.getReceiverBankAccountNo(), // 앱 receiverTypeNumber, 멜로링크 typeNumber
                remittance.getReceiverAccountType(),
                remittance.getReceiverIbanCode(),
                remittance.getReceiverSwiftCode(),
                remittance.getReceiverRoutingNumber(),
                remittance.getReceiverIfscCode(),
                remittance.getReceiverCardNumber()
        );
    }

}
