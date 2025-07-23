package com.cashmallow.api.domain.model.statistics;

import com.cashmallow.api.domain.model.statistics.dto.DailyTransferStatisticsForDateRangeAsk;
import com.cashmallow.api.domain.model.statistics.dto.MonthlyTransferStatisticsForDateRangeAsk;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface MoneyTransferStatisticsMapper {

    /**
     * 일일 환전, 송금, 환불, 인출 정보 가져오기
     *
     * @param params
     * @return
     */
    List<MoneyTransferStatistics> getMoneyTransferStatistics(String fromCd, ZonedDateTime date);

    /**
     * 일일 환전, 송금, 환불 정보 저장하기
     *
     * @param moneyTransferStatistics
     * @return
     */
    int addMoneyTransferStatistics(List<MoneyTransferStatistics> moneyTransferStatistics);

    /**
     * 일일 환전, 송금, 환불 정보 가져오기
     *
     * @param fromCounty
     * @param date
     * @return
     */
    List<MoneyTransferStatistics> getDailyTransferStatistics(String fromCounty, LocalDate date);

    /**
     * 일일 환전, 송금, 환불 정보 가져오기
     *
     * @param fromCounty
     * @param startDate
     * @param endDate
     * @return
     */
    List<MoneyTransferStatistics> getRangeTransferStatistics(String fromCounty, LocalDate startDate, LocalDate endDate);

    void deleteDailyTransferStatistics(String fromCountry, LocalDate date);

    /**
     * 일일 환전, 송금, 환불 정보, 화폐별 통계 가져오기
     *
     * @param dailyTransferStatisticsForDateRangeAsk
     * @return
     */
    List<Map<String, Object>> getDailyTransferStatisticsForDateRange(DailyTransferStatisticsForDateRangeAsk dailyTransferStatisticsForDateRangeAsk);

    /**
     * 일일 환전, 송금, 환불 정보 다 합친 통계 가져오기
     *
     * @param dailyTransferStatisticsForDateRangeAsk
     * @return
     */
    List<Map<String, Object>> getDailyAllTransferStatisticsForDateRange(DailyTransferStatisticsForDateRangeAsk dailyTransferStatisticsForDateRangeAsk);

    /***
     * 일일 환전, 송금, 정보 다 합친 통계 가져오기
     *
     * @param dailyTransferStatisticsForDateRangeAsk
     * @return
     */
    List<Map<String, Object>> getDailyExchangeAndRemittanceTransferStatisticsForDateRange(DailyTransferStatisticsForDateRangeAsk dailyTransferStatisticsForDateRangeAsk);

    /***
     * 월별 환전, 송금, 환불 정보 통계 가져오기
     *
     * @param monthlyTransferStatisticsForDateRangeAsk
     * @return
     */
    List<MoneyTransferStatistics> getMoneyTransferStatisticsDashBoard(MonthlyTransferStatisticsForDateRangeAsk monthlyTransferStatisticsForDateRangeAsk);

    /***
     * 연도별 환전, 송금, 환불 정보 통계 가져오기
     *
     * @param monthlyTransferStatisticsForDateRangeAsk
     * @return
     */
    List<MoneyTransferStatistics> getMoneyTransferStatisticsDashBoardForYear(MonthlyTransferStatisticsForDateRangeAsk monthlyTransferStatisticsForDateRangeAsk);

    /***
     * 화폐 distinct 가져오기
     *
     * @param fromCountry
     * @param startDate
     * @param endDate
     * @return
     */
    List<String> getDistinctToCountryTransferStatistics(String fromCountry, LocalDate startDate, LocalDate endDate);

}
