package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.country.enums.Country3;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.text.MessageFormat;

@Data
public class EditTravelerRequest {

    @NotNull
    public final Country3 phoneCountry;
    @NotNull
    private final String phoneNumber;

    private final Job job;
    private final String jobDetail;

    private final String address;
    private final String addressSecondary;

    private final String otp;
}
