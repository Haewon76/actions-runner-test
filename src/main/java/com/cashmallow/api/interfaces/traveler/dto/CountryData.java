package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.country.Country;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CountryData {

    private String code;
    private String engName;
    private String korName;
    private String iso4217;

    public CountryData(Country m) {
        this.code = m.getCode();
        this.engName = m.getEngName();
        this.korName = m.getKorName();
        this.iso4217 = m.getIso4217();
    }
}
