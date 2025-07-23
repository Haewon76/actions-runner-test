package com.cashmallow.api.domain.model.terms;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class TermsHistory {

    private Long id;
    private Integer version;
    private TermsType type;
    private String countryCode;
    private Timestamp announcedAt;
    private Timestamp startedAt;
    private String path;
    private Boolean required;
    private Boolean requiredReAgreement;
    private Boolean showSignup;
}
