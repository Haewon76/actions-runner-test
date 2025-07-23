package com.cashmallow.api.interfaces.scb.model.dto.request.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {
    private int addressId;
    private String detail;
    private String city;
    private String province;
    private String postalCode;
    private String countryCode;
    private String countryName;
}
