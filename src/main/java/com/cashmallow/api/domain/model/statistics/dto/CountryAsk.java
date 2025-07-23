package com.cashmallow.api.domain.model.statistics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CountryAsk {
    private String toCountry;
    private String currency;

    @Builder
    public CountryAsk(String toCountry, String currency) {
        this.toCountry = toCountry;
        this.currency = currency;
    }
}
