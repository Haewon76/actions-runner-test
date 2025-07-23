package com.cashmallow.api.domain.model.company;

import lombok.Data;

@Data
public class DbsDto {

    private String firstName;
    private String lastName;
    private String amount;
    private String currency;
    private String id;

    public String getMessage() {
        return id + ", " + firstName + " " + lastName + ", " + amount + " " + currency;
    }
}
