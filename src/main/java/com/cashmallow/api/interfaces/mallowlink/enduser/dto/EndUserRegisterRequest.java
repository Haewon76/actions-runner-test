package com.cashmallow.api.interfaces.mallowlink.enduser.dto;

import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public record EndUserRegisterRequest(
        @NotNull
        @Pattern(regexp = "^[a-zA-Z0-9]*$", message = "must be alphanumeric")
        String userId,
        @NotNull
        @Pattern(regexp = "^[a-zA-Z]*$", message = "must be alphabetic")
        String firstName,
        @NotNull
        @Pattern(regexp = "^[a-zA-Z]*$", message = "must be alphabetic")
        String lastName,
        @Pattern(regexp = "^\\d{8}$", message = "must be numeric")
        String dateOfBirth,
        CountryCode countryCode,
        @Pattern(regexp = "^\\d{7,15}$", message = "must be numeric")
        String phoneNumber,
        @Pattern(regexp = "^\\d{1,6}$", message = "Must be a numeric value with 1 to 6 digits")
        String callingCode,
        @Email
        String email,
        ZonedDateTime requestTime
) {

    public static EndUserRegisterRequest of(User user, Traveler traveler) throws CashmallowException {
        String calling = "";
        String userPhoneNumber = "";
        try {
            calling = Country3.valueOf(user.getPhoneCountry()).getCalling();
            userPhoneNumber = user.getPhoneNumber();
            if (userPhoneNumber.startsWith(calling)) {
                userPhoneNumber = userPhoneNumber.replace(calling, "");
            }
        } catch (Exception e) {
            // 예전 가입자중 핸드폰 번호를 입력 안한 사람일 경우를 위해 임시 처리
        }

        String firstName = traveler.getEnFirstName().replaceAll("[^a-zA-Z ]", "");
        String lastName = traveler.getEnLastName().replaceAll("[^a-zA-Z ]", "");
        if(StringUtils.isBlank(firstName) || StringUtils.isBlank(lastName)){
            throw new CashmallowException("영문 이름이 없습니다.");
        }

        return new EndUserRegisterRequest(
                traveler.getUserId().toString(),
                firstName,
                lastName,
                user.getBirthDate(),
                user.getCountryCode(),
                userPhoneNumber,
                calling.replace("+", ""),
                user.getEmail(),
                ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
        );
    }
}
