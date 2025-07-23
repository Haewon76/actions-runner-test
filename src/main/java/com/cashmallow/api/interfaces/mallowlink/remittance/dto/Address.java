package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceTravelerSnapshot;
import com.cashmallow.api.domain.model.traveler.Traveler;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record Address(
        @NotNull
        CountryCode country,
        @NotBlank
        String postalCode,
        @NotBlank
        String city,
        @NotBlank
        String addressLine1,
        String region // State/Province/Region
) {

    public static Address ofEnduser(Traveler traveler, RemittanceTravelerSnapshot snapshot, boolean isFromJpRemittance) {
        // NATION
        CountryCode countryCode = CountryCode.valueOf(Country3.valueOf(traveler.getAddressCountry()).getAlpha2());

        // ADDRESS_ZIPCODE
        String zipCode = traveler.getZipCodeDigit();
        // from JP송금일 경우 traveler 주소가 일본어라 영문으로 변경후 사용
        // ADDRESS_CITY
        String addressCity = traveler.getAddressCity();
        // ADDRESS_LINE1
        String fullAddress = isFromJpRemittance ? traveler.getAddressEn() : traveler.getAddress();
        // EUR, USD 통화에서만 송금인 region 필수값. 영어로만 보내야 함.
        String region = null;
        if (snapshot != null) {
            region = snapshot.getAddressStateProvinceEn();
        }

        return new Address(countryCode,
                zipCode,
                addressCity,
                fullAddress,
                region);
    }

    public static Address ofReceiver(Remittance remittance) {
        CountryCode countryCode = CountryCode.valueOf(remittance.getReceiverCountry());

        String zipCode = remittance.getReceiverZipCode();

        return new Address(
                countryCode,
                zipCode, // ADDRESS_ZIPCODE
                remittance.getReceiverAddressCity(), // ADDRESS_CITY
                remittance.getReceiverAddress(), // ADDRESS_LINE1
                remittance.getReceiverAddressStateProvince() // State/Province/Region(주이름)
        );
    }
}
