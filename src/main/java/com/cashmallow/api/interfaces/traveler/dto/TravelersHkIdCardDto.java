package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.Traveler;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class TravelersHkIdCardDto extends TravelersRequest {

    public TravelersHkIdCardDto(String enLastName,
                                String enFirstName,
                                String identificationNumber,
                                String dateOfBirth,
                                Job job,
                                String jobDetail,
                                Traveler.TravelerSex sex,
                                String fundPurpose,
                                String fundSource) {
        super(enLastName,
                enFirstName,
                identificationNumber,
                dateOfBirth,
                job,
                jobDetail,
                sex,
                fundPurpose,
                fundSource);
    }

}
