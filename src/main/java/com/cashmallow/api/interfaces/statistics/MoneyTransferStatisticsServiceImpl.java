package com.cashmallow.api.interfaces.statistics;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.statistics.MoneyTransferStatistics;
import com.cashmallow.api.domain.model.statistics.MoneyTransferStatisticsMapper;
import com.cashmallow.api.domain.model.statistics.dto.CountryAsk;
import com.cashmallow.api.domain.model.statistics.dto.DailyTransferStatisticsForDateRangeAsk;
import com.cashmallow.api.domain.model.statistics.dto.MonthlyTransferStatisticsForDateRangeAsk;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.cashmallow.api.interfaces.admin.dto.DailyMoneyTransferStatisticsResponse;
import com.cashmallow.api.interfaces.admin.dto.MonthlyMoneyTransferDashBoardResponse;
import com.cashmallow.api.interfaces.statistics.dto.DailyMoneyTransferStatisticsVO;
import com.cashmallow.api.interfaces.statistics.dto.MonthlyMoneyTransferDashBoardVO;
import com.cashmallow.common.JsonStr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.model.statistics.MoneyTransferStatistics.Type.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class MoneyTransferStatisticsServiceImpl implements MoneyTransferStatisticsService {

    private final MoneyTransferStatisticsMapper moneyTransferStatisticsMapper;

    private final AlarmService alarmService;

    private static final String SUCCESS = "정산 완료";

    private static final int RECENTLY_MONTH_COUNT = 3;

    @Override
    @Transactional
    public void addMoneyTransferStatistics(CountryCode fromCd, LocalDate date) {
        LocalDateTime startTime = LocalDateTime.of(date, LocalTime.MIN);
        log.info("fromCd={}, localDateTime={}", fromCd.getName(), startTime);
        ZonedDateTime zonedDateTime = startTime.atZone(fromCd.getZoneId());
        log.info("fromCd={}, zonedDateTime={}", fromCd.getName(), zonedDateTime);

        List<MoneyTransferStatistics> moneyTransferStatisticsList = moneyTransferStatisticsMapper.getMoneyTransferStatistics(fromCd.getCode(), zonedDateTime);
        moneyTransferStatisticsList.forEach(m -> m.setCreatedDate(date));

        moneyTransferStatisticsMapper.deleteDailyTransferStatistics(fromCd.getCode(), date);
        if (!moneyTransferStatisticsList.isEmpty()) {
            moneyTransferStatisticsMapper.addMoneyTransferStatistics(moneyTransferStatisticsList);
            alarmService.i(SUCCESS, JsonStr.toJson(moneyTransferStatisticsList));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DailyMoneyTransferStatisticsResponse getMoneyTransferStatisticsList(String fromCd, LocalDate startDate, LocalDate endDate) {
        List<String> toCountries = moneyTransferStatisticsMapper.getDistinctToCountryTransferStatistics(fromCd, startDate, endDate);

        List<CountryAsk> countryAskList = new ArrayList<>();
        for (String countryCode : toCountries) {
            CountryCode code = CountryCode.of(countryCode);
            String currency = CountryInfo.valueOf(code.name()).getCurrency();
            countryAskList.add(CountryAsk.builder()
                    .toCountry(countryCode)
                    .currency(currency)
                    .build());
        }

        DailyTransferStatisticsForDateRangeAsk requestDto = DailyTransferStatisticsForDateRangeAsk.builder()
                .fromCountry(fromCd)
                .startDate(startDate)
                .endDate(endDate)
                .toCountries(countryAskList)
                .build();

        List<Map<String, Object>> dailyTransferStatisticsList = moneyTransferStatisticsMapper.getDailyTransferStatisticsForDateRange(requestDto);
        List<Map<String, Object>> dailyAllTransferStatisticsList = moneyTransferStatisticsMapper.getDailyAllTransferStatisticsForDateRange(requestDto);
        List<Map<String, Object>> dailyExchangeRemittanceTransferStatisticsList = moneyTransferStatisticsMapper.getDailyExchangeAndRemittanceTransferStatisticsForDateRange(requestDto);

        DailyMoneyTransferStatisticsVO all = getAllDailyMoneyTransferStatisticsVO(countryAskList);

        return getDailyMoneyTransferStatisticsVO(dailyTransferStatisticsList,
                dailyAllTransferStatisticsList, dailyExchangeRemittanceTransferStatisticsList, all, countryAskList);
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyMoneyTransferDashBoardResponse getMoneyTransferStatisticsDashBoard(LocalDate date) {
        LocalDate startDate = LocalDate.of(date.getYear(), 1, 1);
        LocalDate endDate = LocalDate.of(date.getYear(), 12, 31);

        MonthlyTransferStatisticsForDateRangeAsk requestDto = MonthlyTransferStatisticsForDateRangeAsk.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();

        List<MoneyTransferStatistics> moneyTransferStatisticsDashBoard = moneyTransferStatisticsMapper.getMoneyTransferStatisticsDashBoard(requestDto);
        List<MoneyTransferStatistics> moneyTransferStatisticsDashBoardForYear = moneyTransferStatisticsMapper.getMoneyTransferStatisticsDashBoardForYear(requestDto);

        // endDate = LocalDate.of(date.getYear(), LocalDate.now().getMonth(), LocalDate.now().getDayOfMonth());
        // startDate = LocalDate.of(endDate.minusMonths(RECENTLY_MONTH_COUNT).getYear(),
        //         endDate.minusMonths(RECENTLY_MONTH_COUNT).getMonth(), 1);
        //
        // MonthlyTransferStatisticsForDateRangeAsk recentlyRequestDto = MonthlyTransferStatisticsForDateRangeAsk.builder()
        //         .startDate(startDate)
        //         .endDate(endDate)
        //         .build();
        //
        // List<MoneyTransferStatistics> recentlyMoneyTransferStatisticsDashBoard = moneyTransferStatisticsMapper.getMoneyTransferStatisticsDashBoard(recentlyRequestDto);

        return getMonthlyMoneyTransferDashBoardResponse(moneyTransferStatisticsDashBoard, moneyTransferStatisticsDashBoardForYear,
                null);
    }

    private MonthlyMoneyTransferDashBoardResponse getMonthlyMoneyTransferDashBoardResponse(List<MoneyTransferStatistics> moneyTransferStatisticsDashBoard,
                                                                                           List<MoneyTransferStatistics> moneyTransferStatisticsDashBoardForYear,
                                                                                           List<MoneyTransferStatistics> recentlyMoneyTransferStatisticsDashBoard) {
        Map<String, MonthlyMoneyTransferDashBoardVO> resultMap = new LinkedHashMap<>();
        moneyTransferStatisticsDashBoard.stream()
                .map(MoneyTransferStatistics::getFromCd)
                .distinct()
                .forEach(s -> {
                    CountryCode code = CountryCode.of(s);
                    String currency = CountryInfo.valueOf(code.name()).getCurrency();

                    MonthlyMoneyTransferDashBoardVO dashBoardVO = new MonthlyMoneyTransferDashBoardVO();
                    dashBoardVO.setCurrency(currency);
                    ZonedDateTime today = ZonedDateTime.now(code.getZoneId()).truncatedTo(ChronoUnit.DAYS);
                    List<MoneyTransferStatistics> daily = moneyTransferStatisticsMapper.getMoneyTransferStatistics(s, today);
                    dashBoardVO.setDailyMoneyTransferStatistics(getDailyMoneyTransferStatistics(daily));
                    resultMap.put(s, dashBoardVO);
                });

        moneyTransferStatisticsDashBoard.forEach(m -> resultMap.get(m.getFromCd())
                .getMoneyTransferStatisticsList().add(m));

        moneyTransferStatisticsDashBoardForYear.forEach(m -> resultMap.get(m.getFromCd())
                .setAllMoneyTransferStatistics(m));

        moneyTransferStatisticsDashBoard.forEach(m -> {
            MonthlyMoneyTransferDashBoardVO dashBoardVO = resultMap.get(m.getFromCd());
            dashBoardVO.getLabelList().add(m.getCreatedDateString());
            dashBoardVO.getFromAmtList().add(m.getFromAmt());
            dashBoardVO.getCntList().add(m.getTotCnt());
        });

        // recentlyMoneyTransferStatisticsDashBoard.forEach(m -> {
        //     MonthlyMoneyTransferDashBoardVO dashBoardVO = resultMap.get(m.getFromCd());
        //     dashBoardVO.getLabelList().add(m.getCreatedDateString());
        //     dashBoardVO.getFromAmtList().add(m.getFromAmt());
        //     dashBoardVO.getFromOriAmtList().add(m.getFromOriAmt());
        //     dashBoardVO.getFeeList().add(m.getFee());
        // });

        MonthlyMoneyTransferDashBoardResponse response = new MonthlyMoneyTransferDashBoardResponse();
        response.setResult(new ArrayList<>(resultMap.values()));
        return response;
    }

    private DailyMoneyTransferStatisticsResponse getDailyMoneyTransferStatisticsVO(List<Map<String, Object>> dailyTransferStatisticsList,
                                                                                   List<Map<String, Object>> dailyAllTransferStatisticsList,
                                                                                   List<Map<String, Object>> dailyExchangeRemittanceTransferStatisticsList,
                                                                                   DailyMoneyTransferStatisticsVO all,
                                                                                   List<CountryAsk> countryAskList) {
        Map<String, DailyMoneyTransferStatisticsVO> yearMap = new TreeMap<>();

        for (Map<String, Object> map : dailyAllTransferStatisticsList) {
            String createDate = map.get("createdDate").toString();
            if (!yearMap.containsKey(createDate)) {
                yearMap.put(createDate, new DailyMoneyTransferStatisticsVO());
            }
            yearMap.get(createDate).setAll(map);
            sumMap(all.getAll(), map);

            yearMap.get(createDate).setExchangeAndRemittance(new HashMap<>(all.getExchangeAndRemittance()));
            yearMap.get(createDate).setExchange(new HashMap<>(all.getExchange()));
            yearMap.get(createDate).setRemittance(new HashMap<>(all.getRemittance()));
            yearMap.get(createDate).setRefund(new HashMap<>(all.getRefund()));
        }

        for (Map<String, Object> map : dailyExchangeRemittanceTransferStatisticsList) {
            String createDate = map.get("createdDate").toString();
            yearMap.get(createDate).setExchangeAndRemittance(map);
            sumCurrencyMap(all.getExchangeAndRemittance(), map, countryAskList);
        }

        for (Map<String, Object> map : dailyTransferStatisticsList) {
            String createDate = map.get("createdDate").toString();

            if (EXCHANGE.name().equals(map.get("type"))) {
                sumMap(yearMap.get(createDate).getExchange(), map);
                sumMap(all.getExchange(), map);
                sumCurrencyMap(all.getExchange(), map, countryAskList);
                sumCurrencyMap(yearMap.get(createDate).getExchange(), map, countryAskList);
            } else if (REMITTANCE.name().equals(map.get("type"))) {
                sumMap(yearMap.get(createDate).getRemittance(), map);
                sumMap(all.getRemittance(), map);
                sumCurrencyMap(all.getRemittance(), map, countryAskList);
                sumCurrencyMap(yearMap.get(createDate).getRemittance(), map, countryAskList);
            } else if (REFUND_EX.name().equals(map.get("type")) ||
                    REFUND_RM.name().equals(map.get("type"))) {
                sumMap(yearMap.get(createDate).getRefund(), map);
                sumMap(all.getRefund(), map);
            }
        }

        List<String> currencyList = countryAskList.stream()
                .map(CountryAsk::getCurrency)
                .collect(Collectors.toList());
        return new DailyMoneyTransferStatisticsResponse(new ArrayList<>(yearMap.values()), all, currencyList);
    }

    private void sumMap(Map<String, Object> currentMap, Map<String, Object> updateMap) {
        currentMap.put("fromAmt", new BigDecimal(currentMap.get("fromAmt").toString()).add(new BigDecimal(updateMap.get("fromAmt").toString())));
        currentMap.put("fromOriAmt", new BigDecimal(currentMap.get("fromOriAmt").toString()).add(new BigDecimal(updateMap.get("fromOriAmt").toString())));
        currentMap.put("fee", new BigDecimal(currentMap.get("fee").toString()).add(new BigDecimal(updateMap.get("fee").toString())));
        currentMap.put("feePerAmt", new BigDecimal(currentMap.get("feePerAmt").toString()).add(new BigDecimal(updateMap.get("feePerAmt").toString())));
        currentMap.put("feeRateAmt", new BigDecimal(currentMap.get("feeRateAmt").toString()).add(new BigDecimal(updateMap.get("feeRateAmt").toString())));
        currentMap.put("totCnt", new BigDecimal(currentMap.get("totCnt").toString()).add(new BigDecimal(updateMap.get("totCnt").toString())));
    }

    private void sumCurrencyMap(Map<String, Object> currentMap, Map<String, Object> updateMap, List<CountryAsk> countryAskList) {
        for (CountryAsk countryAsk : countryAskList) {
            currentMap.put(countryAsk.getCurrency(),
                    new BigDecimal(currentMap.get(countryAsk.getCurrency()).toString())
                            .add(new BigDecimal(updateMap.get(countryAsk.getCurrency()).toString())));
        }
    }

    private DailyMoneyTransferStatisticsVO getAllDailyMoneyTransferStatisticsVO(List<CountryAsk> countryAskList) {
        DailyMoneyTransferStatisticsVO result = new DailyMoneyTransferStatisticsVO();

        result.setExchange(new HashMap<>());
        initMap(result.getExchange());
        for (CountryAsk countryAsk : countryAskList) {
            result.getExchange().put(countryAsk.getCurrency(), BigDecimal.ZERO);
        }

        result.setRemittance(new HashMap<>());
        initMap(result.getRemittance());
        for (CountryAsk countryAsk : countryAskList) {
            result.getRemittance().put(countryAsk.getCurrency(), BigDecimal.ZERO);
        }

        result.setRefund(new HashMap<>());
        initMap(result.getRefund());

        result.setAll(new HashMap<>());
        initMap(result.getAll());

        result.setExchangeAndRemittance(new HashMap<>());
        for (CountryAsk countryAsk : countryAskList) {
            result.getExchangeAndRemittance().put(countryAsk.getCurrency(), BigDecimal.ZERO);
        }

        return result;
    }

    private void initMap(Map<String, Object> map) {
        map.put("fromAmt", BigDecimal.ZERO);
        map.put("fromOriAmt", BigDecimal.ZERO);
        map.put("fee", BigDecimal.ZERO);
        map.put("feePerAmt", BigDecimal.ZERO);
        map.put("feeRateAmt", BigDecimal.ZERO);
        map.put("totCnt", BigDecimal.ZERO);
    }

    private MoneyTransferStatistics getDailyMoneyTransferStatistics(List<MoneyTransferStatistics> daily) {
        List<MoneyTransferStatistics> filteredDaily = daily.stream()
                .filter(m -> !m.getType().equals(REMITTANCE_CF))
                .filter(m -> !m.getType().equals(CASH_OUT))
                .collect(Collectors.toList());

        MoneyTransferStatistics result = new MoneyTransferStatistics();
        result.setFromAmt(filteredDaily.stream().map(MoneyTransferStatistics::getFromAmt).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.setFromOriAmt(filteredDaily.stream().map(MoneyTransferStatistics::getFromOriAmt).reduce(BigDecimal.ZERO, BigDecimal::add));
        result.setFee(filteredDaily.stream().map(MoneyTransferStatistics::getFee).reduce(BigDecimal.ZERO, BigDecimal::add));
        return result;
    }
}
