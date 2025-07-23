package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.domain.model.country.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@Transactional
public class CountryResponseServiceImplTest {
    @Autowired
    CountryService countryService;

    @Autowired
    CountryMapper countryMapper;
    @Autowired
    CountryHistoryMapper countryHistoryMapper;

    @Test
    void registerCountry_호출_저장_성공() {
        // given
        Country country = getCountry();
        Long userId = 9999L;
        String ip = "testIp";

        countryMapper.registerCountry(country);
        country.setIso4217("889");

        String maxCountryCode = countryMapper.getCountryMaxCode();
        int updateCode = Integer.parseInt(maxCountryCode) + 1;

        // when
        countryService.registerCountry(country, userId, ip);

        // then
        Country resultCountry = countryMapper.getCountry(country.getCode());
        List<CountryHistory> resultCountryHistory = countryHistoryMapper.getCountryHistory(resultCountry.getCode());

        assertEquals(String.format("%03d", updateCode), resultCountry.getCode());
        assertEquals(country.getEngName(), resultCountry.getEngName());
        assertEquals(country.getKorName(), resultCountry.getKorName());
        assertEquals(country.getIso4217(), resultCountry.getIso4217());
        assertEquals(country.getService(), resultCountry.getService());
        assertEquals(country.getLastRefValue(), resultCountry.getLastRefValue());
        assertEquals(country.getIsFamilyNameAfterFirstName(), resultCountry.getIsFamilyNameAfterFirstName());
        assertEquals(country.getTypeOfRefValue(), resultCountry.getTypeOfRefValue());
        assertEquals(country.getIso3166(), resultCountry.getIso3166());
        assertEquals(country.getCanSignup(), resultCountry.getCanSignup());

        assertEquals(country.getEngName(), resultCountryHistory.get(0).getEngName());
        assertEquals(country.getKorName(), resultCountryHistory.get(0).getKorName());
        assertEquals(country.getIso4217(), resultCountryHistory.get(0).getIso4217());
        assertEquals(country.getService(), resultCountryHistory.get(0).getService());
        assertEquals(country.getLastRefValue(), resultCountryHistory.get(0).getLastRefValue());
        assertEquals(country.getIsFamilyNameAfterFirstName(), resultCountryHistory.get(0).getIsFamilyNameAfterFirstName());
        assertEquals(country.getTypeOfRefValue(), resultCountryHistory.get(0).getTypeOfRefValue());
        assertEquals(country.getIso3166(), resultCountryHistory.get(0).getIso3166());
        assertEquals(country.getCanSignup(), resultCountryHistory.get(0).getCanSignup());
        assertEquals(userId, resultCountryHistory.get(0).getUserId());
        assertEquals(ip, resultCountryHistory.get(0).getIp());
    }

    @Test
    void updateCountry_호출_업데이트_성공() {
        // given
        Country country = getCountry();
        Long userId = 9999L;
        String ip = "testIp";
        countryMapper.registerCountry(country);

        country.setEngName("updateEng");
        country.setKorName("updateKor");
        country.setIso4217("777");
        country.setService("N");
        country.setLastRefValue(1);
        country.setIsFamilyNameAfterFirstName("N");
        country.setTypeOfRefValue("Y");
        country.setIso3166("YY");
        country.setCanSignup("N");
        country.setTtrateTtRate("N");
        country.setTtrateNotesRate("N");

        // when
        countryService.updateCountry(country, userId, ip);

        // then
        Country resultCountry = countryMapper.getCountry(country.getCode());
        List<CountryHistory> resultCountryHistory = countryHistoryMapper.getCountryHistory(country.getCode());

        assertEquals(country.getCode(), resultCountry.getCode());
        assertEquals(country.getEngName(), resultCountry.getEngName());
        assertEquals(country.getKorName(), resultCountry.getKorName());
        assertEquals(country.getIso4217(), resultCountry.getIso4217());
        assertEquals(country.getService(), resultCountry.getService());
        assertEquals(country.getLastRefValue(), resultCountry.getLastRefValue());
        assertEquals(country.getIsFamilyNameAfterFirstName(), resultCountry.getIsFamilyNameAfterFirstName());
        assertEquals(country.getTypeOfRefValue(), resultCountry.getTypeOfRefValue());
        assertEquals(country.getIso3166(), resultCountry.getIso3166());
        assertEquals(country.getCanSignup(), resultCountry.getCanSignup());

        assertEquals(country.getEngName(), resultCountryHistory.get(0).getEngName());
        assertEquals(country.getKorName(), resultCountryHistory.get(0).getKorName());
        assertEquals(country.getIso4217(), resultCountryHistory.get(0).getIso4217());
        assertEquals(country.getService(), resultCountryHistory.get(0).getService());
        assertEquals(country.getLastRefValue(), resultCountryHistory.get(0).getLastRefValue());
        assertEquals(country.getIsFamilyNameAfterFirstName(), resultCountryHistory.get(0).getIsFamilyNameAfterFirstName());
        assertEquals(country.getTypeOfRefValue(), resultCountryHistory.get(0).getTypeOfRefValue());
        assertEquals(country.getIso3166(), resultCountryHistory.get(0).getIso3166());
        assertEquals(country.getCanSignup(), resultCountryHistory.get(0).getCanSignup());
        assertEquals(userId, resultCountryHistory.get(0).getUserId());
        assertEquals(ip, resultCountryHistory.get(0).getIp());
    }

    @Test
    void getCountryFees_호출_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFees();

        // then
        assertTrue(result.size() > 0);
    }

    @Test
    void getCountryFeesByCd_호출_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(countryFee.getFromCd(),
                countryFee.getToCd(), countryFee.getUseYn());

        // then
        assertTrue(result.size() > 0);
        assertEquals(countryFee.getFee(), result.get(0).getFee());
        assertEquals(countryFee.getMin(), result.get(0).getMin());
        assertEquals(countryFee.getMax(), result.get(0).getMax());
        assertEquals(countryFee.getSort(), result.get(0).getSort());
        assertEquals(countryFee.getUseYn(), result.get(0).getUseYn());
    }

    @Test
    void getCountryFeesByCd_호출_fromCd_null_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(null,
                countryFee.getToCd(), countryFee.getUseYn());

        // then
        assertTrue(result.size() > 0);
        assertEquals(countryFee.getFee(), result.get(0).getFee());
        assertEquals(countryFee.getMin(), result.get(0).getMin());
        assertEquals(countryFee.getMax(), result.get(0).getMax());
        assertEquals(countryFee.getSort(), result.get(0).getSort());
        assertEquals(countryFee.getUseYn(), result.get(0).getUseYn());
    }

    @Test
    void getCountryFeesByCd_호출_toCd_null_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(countryFee.getFromCd(),
                null, countryFee.getUseYn());

        // then
        assertTrue(result.size() > 0);
        assertEquals(countryFee.getFee(), result.get(0).getFee());
        assertEquals(countryFee.getMin(), result.get(0).getMin());
        assertEquals(countryFee.getMax(), result.get(0).getMax());
        assertEquals(countryFee.getSort(), result.get(0).getSort());
        assertEquals(countryFee.getUseYn(), result.get(0).getUseYn());
    }

    @Test
    void getCountryFeesByCd_호출_useYn_null_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(countryFee.getFromCd(),
                countryFee.getToCd(), null);

        // then
        assertTrue(result.size() > 0);
        assertEquals(countryFee.getFee(), result.get(0).getFee());
        assertEquals(countryFee.getMin(), result.get(0).getMin());
        assertEquals(countryFee.getMax(), result.get(0).getMax());
        assertEquals(countryFee.getSort(), result.get(0).getSort());
        assertEquals(countryFee.getUseYn(), result.get(0).getUseYn());
    }

    @Test
    void getCountryFeesByCd_호출_fromCd_null_toCd_null_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(null, null,
                countryFee.getUseYn());

        // then
        assertTrue(result.size() > 0);
    }

    @Test
    void getCountryFeesByCd_호출_fromCd_null_toCd_null_useYn_null_조회_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        countryMapper.registerCountryFee(countryFee);

        // when
        List<CountryFee> result = countryService.getCountryFeesByCd(null, null, null);

        // then
        assertTrue(result.size() > 0);
    }

    @Test
    void registerCountryFee_호출_저장_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        Long userId = 9999L;
        String ip = "testIp";

        // when
        countryService.registerCountryFee(countryFee, userId, ip);

        // then
        List<CountryFee> resultCountryFee = countryMapper
                .getCountryFeesByCd(countryFee.getFromCd(), countryFee.getToCd(), null);
        List<CountryFeeHistory> resultCountryFeeHistory = countryHistoryMapper
                .getCountryFeeHistory(resultCountryFee.get(0).getId());

        assertTrue(resultCountryFee.size() > 0);
        assertEquals(countryFee.getFee(), resultCountryFee.get(0).getFee());
        assertEquals(countryFee.getMin(), resultCountryFee.get(0).getMin());
        assertEquals(countryFee.getMax(), resultCountryFee.get(0).getMax());
        assertEquals(countryFee.getSort(), resultCountryFee.get(0).getSort());
        assertEquals(countryFee.getUseYn(), resultCountryFee.get(0).getUseYn());

        assertEquals(countryFee.getFee(), resultCountryFeeHistory.get(0).getFee());
        assertEquals(countryFee.getMin(), resultCountryFeeHistory.get(0).getMin());
        assertEquals(countryFee.getMax(), resultCountryFeeHistory.get(0).getMax());
        assertEquals(countryFee.getSort(), resultCountryFeeHistory.get(0).getSort());
        assertEquals(countryFee.getUseYn(), resultCountryFeeHistory.get(0).getUseYn());
        assertEquals(userId, resultCountryFeeHistory.get(0).getUserId());
        assertEquals(ip, resultCountryFeeHistory.get(0).getIp());
    }

    @Test
    void updateCountryFee_호출_업데이트_성공() {
        // given
        CountryFee countryFee = getCountryFee();
        Long userId = 9999L;
        String ip = "testIp";
        countryMapper.registerCountryFee(countryFee);
        Long countryFeeId = countryMapper.getCountryFeesByCd(countryFee.getFromCd(),
                countryFee.getToCd(), null).get(0).getId();
        countryFee.setId(countryFeeId);
        countryFee.setFee(new BigDecimal("12345.00"));
        countryFee.setMin(new BigDecimal("123.00"));
        countryFee.setMax(new BigDecimal("456.00"));
        countryFee.setSort(987);
        countryFee.setUseYn("N");

        // when
        countryService.updateCountryFee(countryFee, userId, ip);

        // then
        CountryFee resultCountryFee = countryMapper.getCountryFeeById(countryFeeId);
        List<CountryFeeHistory> resultCountryFeeHistory = countryHistoryMapper
                .getCountryFeeHistory(resultCountryFee.getId());

        assertEquals(countryFee.getFee(), resultCountryFee.getFee());
        assertEquals(countryFee.getMin(), resultCountryFee.getMin());
        assertEquals(countryFee.getMax(), resultCountryFee.getMax());
        assertEquals(countryFee.getSort(), resultCountryFee.getSort());
        assertEquals(countryFee.getUseYn(), resultCountryFee.getUseYn());

        assertEquals(countryFee.getFee(), resultCountryFeeHistory.get(0).getFee());
        assertEquals(countryFee.getMin(), resultCountryFeeHistory.get(0).getMin());
        assertEquals(countryFee.getMax(), resultCountryFeeHistory.get(0).getMax());
        assertEquals(countryFee.getSort(), resultCountryFeeHistory.get(0).getSort());
        assertEquals(countryFee.getUseYn(), resultCountryFeeHistory.get(0).getUseYn());
        assertEquals(userId, resultCountryFeeHistory.get(0).getUserId());
        assertEquals(ip, resultCountryFeeHistory.get(0).getIp());
    }

    @Test
    void calculateFee_호출_조회_성공() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(50000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateFee_호출_minFee_min_값_조회_성공() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(10000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateFee_호출_minFee_max_값_조회_성공() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(1000000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertEquals(new BigDecimal("10.00"), result);
    }

    @Test
    void calculateFee_호출_secondFee_min_값_조회_성공() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(1010000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertEquals(new BigDecimal("20.00"), result);
    }

    @Test
    void calculateFee_호출_minFee_min_under_값_조회_결과_null() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(9000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertNull(result);
    }

    @Test
    void calculateFee_호출_maxFee_max_over_값_조회_결과_null() {
        // given
        String fromCd = "888";
        String toCd = "999";
        BigDecimal toMoney = BigDecimal.valueOf(3001000);
        initCountryFees();

        // when
        BigDecimal result = countryService.calculateFee(fromCd, toCd, toMoney);

        // then
        assertNull(result);
    }

    @Test
    void getExchangeConfigByCode_호출_조회_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();
        String fromCd = "888";
        String toCd = "999";

        countryMapper.insertExchangeConfig(exchangeConfig);

        // when
        List<ExchangeConfig> result = countryService.getExchangeConfigByCode(fromCd, toCd);

        // then
        assertTrue(result.size() > 0);
        assertEquals(exchangeConfig.getFromCd(), result.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), result.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(result.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(result.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), result.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), result.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), result.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), result.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(result.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(result.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), result.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), result.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(result.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(result.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), result.get(0).getRemittanceNotice());
    }

    @Test
    void getExchangeConfigByCode_호출_fromCd_null_조회_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();
        String toCd = "999";

        countryMapper.insertExchangeConfig(exchangeConfig);

        // when
        List<ExchangeConfig> result = countryService.getExchangeConfigByCode(null, toCd);

        // then
        assertTrue(result.size() > 0);
        assertEquals(exchangeConfig.getFromCd(), result.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), result.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(result.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(result.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), result.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), result.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), result.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), result.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(result.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(result.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), result.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), result.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(result.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(result.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), result.get(0).getRemittanceNotice());
    }

    @Test
    void getExchangeConfigByCode_호출_toCd_null_조회_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();
        String fromCd = "888";

        countryMapper.insertExchangeConfig(exchangeConfig);

        // when
        List<ExchangeConfig> result = countryService.getExchangeConfigByCode(fromCd, null);

        // then
        assertTrue(result.size() > 0);
        assertEquals(exchangeConfig.getFromCd(), result.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), result.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(result.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(result.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), result.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), result.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), result.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), result.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(result.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(result.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), result.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), result.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(result.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(result.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), result.get(0).getRemittanceNotice());
    }

    @Test
    void getExchangeConfigByCode_호출_fromCd_toCd_null_조회_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();

        countryMapper.insertExchangeConfig(exchangeConfig);

        // when
        List<ExchangeConfig> result = countryService.getExchangeConfigByCode(null, null);

        // then
        assertTrue(result.size() > 0);
    }

    @Test
    void insertExchangeConfig_호출_삽입_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();
        Long userId = 9999L;
        String ip = "testIp";

        // when
        countryService.insertExchangeConfig(exchangeConfig, userId, ip);

        // then
        List<ExchangeConfig> result = countryMapper
                .getExchangeConfigByCode(exchangeConfig.getFromCd(), exchangeConfig.getToCd());
        List<ExchangeConfigHistory> resultExchangeConfigHistory = countryHistoryMapper
                .getExchangeConfigHistory(exchangeConfig.getId());

        assertEquals(exchangeConfig.getFromCd(), result.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), result.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(result.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(result.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), result.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), result.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), result.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), result.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(result.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(result.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), result.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), result.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(result.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(result.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), result.get(0).getRemittanceNotice());

        assertEquals(exchangeConfig.getId(), resultExchangeConfigHistory.get(0).getExchangeConfigId());
        assertEquals(exchangeConfig.getFromCd(), resultExchangeConfigHistory.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), resultExchangeConfigHistory.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(resultExchangeConfigHistory.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(resultExchangeConfigHistory.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), resultExchangeConfigHistory.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), resultExchangeConfigHistory.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), resultExchangeConfigHistory.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), resultExchangeConfigHistory.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(resultExchangeConfigHistory.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(resultExchangeConfigHistory.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), resultExchangeConfigHistory.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), resultExchangeConfigHistory.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(resultExchangeConfigHistory.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(resultExchangeConfigHistory.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), resultExchangeConfigHistory.get(0).getRemittanceNotice());
    }

    @Test
    void updateExchangeConfig_호출_업데이트_성공() {
        // given
        ExchangeConfig exchangeConfig = getExchangeConfig();
        Long userId = 9999L;
        String ip = "testIp";

        countryMapper.insertExchangeConfig(exchangeConfig);

        exchangeConfig.setFeePerExchange(new BigDecimal("2"));
        exchangeConfig.setFeePerRemittance(new BigDecimal("2"));
        exchangeConfig.setExchangeNotice("testNotice");
        exchangeConfig.setRemittanceNotice("remitTest");

        // when
        countryService.updateExchangeConfig(exchangeConfig, userId, ip);

        // then
        ExchangeConfig result = countryMapper.getExchangeConfigById(exchangeConfig.getId());
        List<ExchangeConfigHistory> resultExchangeConfigHistory = countryHistoryMapper
                .getExchangeConfigHistory(exchangeConfig.getId());

        assertEquals(exchangeConfig.getFromCd(), result.getFromCd());
        assertEquals(exchangeConfig.getToCd(), result.getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(result.getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(result.getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), result.getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), result.getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), result.getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), result.getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(result.getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(result.getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), result.getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), result.getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(result.getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(result.getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), result.getRemittanceNotice());

        assertEquals(exchangeConfig.getId(), resultExchangeConfigHistory.get(0).getExchangeConfigId());
        assertEquals(exchangeConfig.getFromCd(), resultExchangeConfigHistory.get(0).getFromCd());
        assertEquals(exchangeConfig.getToCd(), resultExchangeConfigHistory.get(0).getToCd());
        assertEquals(0, exchangeConfig.getFeeRateExchange().compareTo(resultExchangeConfigHistory.get(0).getFeeRateExchange()));
        assertEquals(0, exchangeConfig.getMinFee().compareTo(resultExchangeConfigHistory.get(0).getMinFee()));
        assertEquals(exchangeConfig.getCanExchange(), resultExchangeConfigHistory.get(0).getCanExchange());
        assertEquals(exchangeConfig.getEnabledExchange(), resultExchangeConfigHistory.get(0).getEnabledExchange());
        assertEquals(exchangeConfig.getCreator(), resultExchangeConfigHistory.get(0).getCreator());
        assertEquals(exchangeConfig.getExchangeNotice(), resultExchangeConfigHistory.get(0).getExchangeNotice());
        assertEquals(0, exchangeConfig.getRefundFeePer().compareTo(resultExchangeConfigHistory.get(0).getRefundFeePer()));
        assertEquals(0, exchangeConfig.getFeePerExchange().compareTo(resultExchangeConfigHistory.get(0).getFeePerExchange()));
        assertEquals(exchangeConfig.getCanRemittance(), resultExchangeConfigHistory.get(0).getCanRemittance());
        assertEquals(exchangeConfig.getEnabledRemittance(), resultExchangeConfigHistory.get(0).getEnabledRemittance());
        assertEquals(0, exchangeConfig.getFeePerRemittance().compareTo(resultExchangeConfigHistory.get(0).getFeePerRemittance()));
        assertEquals(0, exchangeConfig.getFeeRateRemittance().compareTo(resultExchangeConfigHistory.get(0).getFeeRateRemittance()));
        assertEquals(exchangeConfig.getRemittanceNotice(), resultExchangeConfigHistory.get(0).getRemittanceNotice());
    }

    Country getCountry() {
        Country country = new Country();
        country.setCode("777");
        country.setEngName("testEng");
        country.setKorName("testKor");
        country.setIso4217("888");
        country.setService("Y");
        country.setLastRefValue(0);
        country.setIsFamilyNameAfterFirstName("Y");
        country.setTypeOfRefValue("N");
        country.setIso3166("ZZ");
        country.setCanSignup("Y");
        country.setTtrateTtRate("N");
        country.setTtrateNotesRate("N");
        return country;
    }

    CountryFee getCountryFee() {
        return CountryFee.builder()
                .fromCd("888")
                .toCd("999")
                .fee(new BigDecimal("1.00"))
                .min(new BigDecimal("1.00"))
                .max(new BigDecimal("1.00"))
                .sort(1)
                .useYn("Y")
                .build();
    }

    void initCountryFees() {
        CountryFee countryFee1 = getCountryFee();
        CountryFee countryFee2 = getCountryFee();
        CountryFee countryFee3 = getCountryFee();

        countryFee1.setFee(new BigDecimal("10.00"));
        countryFee2.setFee(new BigDecimal("20.00"));
        countryFee3.setFee(new BigDecimal("30.00"));

        countryFee1.setMin(new BigDecimal("10000.00"));
        countryFee2.setMin(new BigDecimal("1010000.00"));
        countryFee3.setMin(new BigDecimal("2010000.00"));

        countryFee1.setMax(new BigDecimal("1000000.00"));
        countryFee2.setMax(new BigDecimal("2000000.00"));
        countryFee3.setMax(new BigDecimal("3000000.00"));

        countryMapper.registerCountryFee(countryFee1);
        countryMapper.registerCountryFee(countryFee2);
        countryMapper.registerCountryFee(countryFee3);
    }

    ExchangeConfig getExchangeConfig() {
        return new ExchangeConfig("888",
                "999",
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                "N",
                "N",
                "N",
                "N",
                "exchangeNotice",
                "remitNotice",
                51L,
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("100000"),
                Integer.valueOf(10),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                BigDecimal.ONE,
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                new BigDecimal("100000"),
                30L,
                15L
                );
    }
}
