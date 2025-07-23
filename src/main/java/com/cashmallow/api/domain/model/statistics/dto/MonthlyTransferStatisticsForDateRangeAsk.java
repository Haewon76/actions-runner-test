package com.cashmallow.api.domain.model.statistics.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class MonthlyTransferStatisticsForDateRangeAsk {

    private LocalDate startDate;
    private LocalDate endDate;

    @Builder
    public MonthlyTransferStatisticsForDateRangeAsk(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
