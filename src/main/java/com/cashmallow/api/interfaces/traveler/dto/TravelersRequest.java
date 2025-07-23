package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.text.MessageFormat;
import java.util.Optional;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "certificationType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TravelersHkIdCardDto.class, name = "ID_CARD"),
        @JsonSubTypes.Type(value = TravelersHkPassportDto.class, name = "PASSPORT")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TravelersRequest {

    protected final String enLastName;
    protected final String enFirstName;

    protected final String identificationNumber;

    protected final String dateOfBirth; //yyyyMMdd

    // @NonNull
    protected final Job job;
    protected final String jobDetail;

    protected final Traveler.TravelerSex sex;
    // @NonNull
    protected final String fundPurpose; // LIVINGEXPENSES, DONATE, REMITTANCE_PURPOSE_STUDY
    // @NonNull
    protected final String fundSource; // INTERESTINCOME, GIFTINCOME

    public Traveler.TravelerSex getSex() {
        return Optional.ofNullable(sex).orElse(Traveler.TravelerSex.MALE);
    }

    public String getFundPurpose() {
        return Optional.ofNullable(fundPurpose).orElse(null);
    }

    public String getFundSource() {
        return Optional.ofNullable(fundSource).orElse(null);
    }
}
