package com.cashmallow.api.interfaces.user.dto;

import com.cashmallow.api.domain.model.user.UserSearch;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserSearchRequest {

    private List<Long> ids;

    private String country;

    private String birthMonthDay;

    public UserSearch toEntity() {
        return UserSearch.builder()
                .ids(ids)
                .country(country)
                .birthMonthDay(birthMonthDay)
                .build();
    }
}
