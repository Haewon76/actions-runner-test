package com.cashmallow.api.domain.model.country;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
public class Country {

    public enum CashOutType {
        /* Full withdrawal */
        F,
        /* Partial withdrawal */
        P
    }

    private String code;

    private String iso3166;
    private String iso4217;

    private String engName;
    private String korName;

    private String callingCode;
    private String service;
    private String canSignup;
    private Integer timezoneInterval;
    private Integer lastRefValue;
    private String isFamilyNameAfterFirstName;
    private String typeOfRefValue;
    private BigDecimal mappingUpperRange;
    private BigDecimal mappingLowerRange;
    private BigDecimal mappingInc;

    private BigDecimal unitScale;
    private BigDecimal defaultLat;
    private BigDecimal defaultLng;

    private String cashOutType; // F:Full withdrawal, P:Partial withdrawal

    private String dateCalculationStandard;

    private String ttrateTtRate;
    private String ttrateNotesRate;
}
