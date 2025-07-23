package com.cashmallow.api.domain.model.statistics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class DailyTransferStatisticsForDateRangeAsk {
    private String fromCountry;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<CountryAsk> toCountries;

    @Builder
    public DailyTransferStatisticsForDateRangeAsk(String fromCountry, LocalDate startDate,
                                                  LocalDate endDate, List<CountryAsk> toCountries) {
        this.fromCountry = fromCountry;
        this.startDate = startDate;
        this.endDate = endDate;
        this.toCountries = toCountries;
    }
}
