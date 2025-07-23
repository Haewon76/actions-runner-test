package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.country.enums.Country3;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.time.ZonedDateTime;

@Data
public class VerifyBankAccountRequest {

    private final Long bankInfoId;
    @NotBlank
    private final String bankName;

    @NotBlank
    @Pattern(regexp = "^[\\d]*$")
    private final String accountNo;

    private final String accountName;

    // Address
    private final Country3 addressCountry;

    @NotBlank
    private final String addressCity;

    @NotBlank
    private final String address;

    private final String addressSecondary;

    private final ZonedDateTime requestTime;

}
