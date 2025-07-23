package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.traveler.Traveler;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class TravelersHkPassportDto extends TravelersRequest {

    // private final TravelersRequest.CertificationType certificationType = TravelersRequest.CertificationType.PASSPORT;

    @NonNull
    private final String localLastName;
    @NonNull
    private final String localFirstName;

    @NonNull
    private final String passportIssueDate; //yyyyMMdd
    private final String passportExpDate;   //yyyyMMdd
    private final Country3 passportCountry;

    public TravelersHkPassportDto(String enLastName,
                                  String enFirstName,
                                  String identificationNumber,
                                  String dateOfBirth,
                                  Job job,
                                  String jobDetail,
                                  String localLastName,
                                  String localFirstName,
                                  String passportIssueDate,
                                  String passportExpDate,
                                  Country3 passportCountry,
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
        this.localLastName = localLastName;
        this.localFirstName = localFirstName;
        this.passportIssueDate = passportIssueDate;
        this.passportExpDate = passportExpDate;
        this.passportCountry = passportCountry;
    }

}
