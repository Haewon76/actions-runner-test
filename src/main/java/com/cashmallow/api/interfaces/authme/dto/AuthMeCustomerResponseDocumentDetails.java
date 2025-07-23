package com.cashmallow.api.interfaces.authme.dto;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.model.traveler.Traveler.TravelerSex.MALE;
import static com.cashmallow.common.CommonUtil.textToNormalize;

public record AuthMeCustomerResponseDocumentDetails(
        String otherLicenceDate,
        String motorcycleLicenceDate,
        String semiMediumVehicleLicenceDate,
        String heavyMotorcycleLicenceDate,
        String country, // HK-ID_CARD, HK-PASSPORT
        String address,
        String dateOfBirth, // HK-ID_CARD, HK-PASSPORT
        String dateOfIssue, // HK-ID_CARD
        String documentNumber, // HK-PASSPORT
        String documentType, // HK-ID_CARD, HK-PASSPORT
        String expiryDate, // HK-PASSPORT
        String gender,  // HK-ID_CARD, HK-PASSPORT
        String name,    // HK-ID_CARD
        @JsonProperty("name_en")
        @JsonAlias("nameEn")
        String nameEn,    // HK-ID_CARD
        String surname, // HK-PASSPORT
        String givenName, // HK-PASSPORT
        String nationality, // HK-PASSPORT
        String periodOfStay,
        String restrictionEmployment,
        String visaStatus,
        @JsonProperty("personal_number")
        String personalNumber,
        String idNumber // HK-ID_CARD, HK-PASSPORT, my number
) {

    public String getDateOfBirth() {
        if (StringUtils.isNotBlank(dateOfBirth)) {
            return dateOfBirth.replaceAll("-", "");
        }
        return null;
    }

    // 성
    public String getFirstName() {
        return getName().firstName();
    }

    // 이름
    public String getLastName() {
        return getName().lastName();
    }

    public AuthmeName getName() {
        String englishName = name;
        // 영문 이름 필드가 있는 경우 name_en을 사용
        if(StringUtils.isNotBlank(nameEn)) {
            englishName = nameEn;
        }

        if (StringUtils.isBlank(englishName)) {
            return new AuthmeName("", "");
        }

        try {
            List<String> list = Arrays.asList(textToNormalize(englishName).split(" |,"));
            String firstName = list.stream().skip(1).map(String::trim).map(String::toUpperCase).collect(Collectors.joining(" ")).trim();
            String lastName = list.get(0).toUpperCase().trim();
            return new AuthmeName(firstName, lastName);
        } catch (Exception ignore) {
        }
        return new AuthmeName("", "");
    }


    public Traveler.TravelerSex getGender() {
        if (StringUtils.isBlank(gender)) {
            return null;
        }

        if ("Male".equalsIgnoreCase(gender) || "M".equalsIgnoreCase(gender) || gender.contains("男")) {
            return MALE;
        } else if ("Female".equalsIgnoreCase(gender) || "F".equalsIgnoreCase(gender) || gender.contains("女")) {
            return Traveler.TravelerSex.FEMALE;
        }

        return null;
    }

    public String getAddress() {
        return textToNormalize(address);
    }

    public String getIdNumber() {
        return textToNormalize(idNumber);
    }

    public String getLicensedDate() {
        if (StringUtils.isNotBlank(heavyMotorcycleLicenceDate)) {
            return heavyMotorcycleLicenceDate;
        }

        if (StringUtils.isNotBlank(semiMediumVehicleLicenceDate)) {
            return semiMediumVehicleLicenceDate;
        }

        if (StringUtils.isNotBlank(motorcycleLicenceDate)) {
            return motorcycleLicenceDate;
        }

        if (StringUtils.isNotBlank(otherLicenceDate)) {
            return otherLicenceDate;
        }

        return null;
    }

    @Override
    public String dateOfBirth() {
        if(StringUtils.isNotBlank(dateOfBirth)) {
            return dateOfBirth.replaceAll("-", "");
        }
        return null;
    }
}