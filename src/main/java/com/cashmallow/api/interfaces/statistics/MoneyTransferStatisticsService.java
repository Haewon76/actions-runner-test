package com.cashmallow.api.interfaces.statistics;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.interfaces.admin.dto.DailyMoneyTransferStatisticsResponse;
import com.cashmallow.api.interfaces.admin.dto.MonthlyMoneyTransferDashBoardResponse;

import java.time.LocalDate;

public interface MoneyTransferStatisticsService {

    void addMoneyTransferStatistics(CountryCode fromCd, LocalDate date);

    DailyMoneyTransferStatisticsResponse getMoneyTransferStatisticsList(String fromCd, LocalDate startDate, LocalDate endDate);

    MonthlyMoneyTransferDashBoardResponse getMoneyTransferStatisticsDashBoard(LocalDate date);
}
