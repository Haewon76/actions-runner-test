package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.country.Country;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

import static com.cashmallow.api.interfaces.traveler.dto.ExchangeCalculateResponse.CODE_ERROR;
import static com.cashmallow.api.interfaces.traveler.dto.ExchangeCalculateResponse.CODE_SUCCESS;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class CountryResponse {

    private int code;
    private String message;
    private List<CountryData> data;

    public CountryResponse() {
        this.code = CODE_ERROR;
        this.message = "FAIL";
    }

    public void setData(List<Country> data) {
        this.code = CODE_SUCCESS;
        this.message = "SUCCESS";
        this.data = data.stream().map(CountryData::new).toList();
    }
}
