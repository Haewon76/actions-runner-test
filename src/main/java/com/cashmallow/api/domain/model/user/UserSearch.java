package com.cashmallow.api.domain.model.user;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserSearch {

    private List<Long> ids;

    private String country;

    private String birthMonthDay;

    @Builder
    public UserSearch(List<Long> ids, String country, String birthMonthDay) {
        this.ids = ids;
        this.country = country;
        this.birthMonthDay = birthMonthDay;
    }
}
