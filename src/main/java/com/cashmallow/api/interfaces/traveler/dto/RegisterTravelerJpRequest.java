package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource;
import com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static com.cashmallow.common.CommonUtil.textToNormalize;

/**
 * @param identificationNumber 주민등록번호, 외국인 등록번호
 * @param issueDate            발급일자
 * @param expirationDate       신분증 만료일
 * @param address              주소 API 영문 도로명
 * @param addressSecondary     상세 주소
 * @param passportCountry      외국인등록증 국적
 * @param fundPurpose          LIVINGEXPENSES, DONATE
 * @param fundSource           INTERESTINCOME, GIFTINCOME
 */
public record RegisterTravelerJpRequest(
        @NotNull
        CertificationType certificationType,

        String kanjiFirstName, // mynumber, driver license
        String kanjiLastName, // mynumber, driver license

        @NotBlank
        String localFirstName,
        @NotBlank
        String localLastName,
        // @NotBlank
        String enFirstName,
        // @NotBlank
        String enLastName,
        String accountLastName,
        String accountFirstName,

        // @NotBlank
        // String photoBitmapBase64, // 이미지 파일 base64

        @NotBlank
        String identificationNumber,

        // @NotBlank
        String dateOfBirth,

        String issueDate, // null

        // @NotBlank
        String expirationDate,

        @NotNull
        Job job,

        String zipCode, // null
        String addressCity, // null
        // @NotBlank
        String address,
        String addressSecondary, // null
        String passportCountry, // residence card = 외국인 등록증인 경우 'KOR' alpha3로 입력 받는다

        // @NotNull
        Traveler.TravelerSex sex,

        @NotNull
        RemittancePurpose fundPurpose,
        @NotNull
        RemittanceFundSource fundSource
) {

    public Country3 getPassportCountry() {
        if (StringUtils.isBlank(passportCountry)) {
            return null;
        }
        return Country3.valueOf(passportCountry);
    }

    @Override
    public String address() {
        return textToNormalize(address);
    }

    @Override
    public String kanjiFirstName() {
        return textToNormalize(kanjiFirstName);
    }

    @Override
    public String kanjiLastName() {
        return textToNormalize(kanjiLastName);
    }

    @Override
    public String localFirstName() {
        return textToNormalize(localFirstName);
    }

    @Override
    public String localLastName() {
        return textToNormalize(localLastName);
    }

    @Override
    public String enFirstName() {
        return textToNormalize(enFirstName);
    }

    @Override
    public String enLastName() {
        return textToNormalize(enLastName);
    }

}
