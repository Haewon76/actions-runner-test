package com.cashmallow.api.domain.model.user;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserAgreeTerms {
    private Long userId;
    private String login;
    private String passwordHash;
    private String iso3166;
    private String agreeTerms;
    private String agreePrivacy;
    private Boolean activated;
    private String countryCode;
}
