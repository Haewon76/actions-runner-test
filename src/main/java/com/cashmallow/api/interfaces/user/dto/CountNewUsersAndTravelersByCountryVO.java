package com.cashmallow.api.interfaces.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountNewUsersAndTravelersByCountryVO {
    private String countryCode; // 국가 코드
    private int travelerCount;  // 여행자 수
    private int userCount;      // 사용자 수
}
