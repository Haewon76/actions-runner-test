package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryMapper;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.common.HttpApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@SpringBootTest
        // @Transactional
class CurrencyServiceImplTest {

    @Value("${ttrate.url}")
    private String ttRateUrl;

    @Value("${ttrate.companyId}")
    private String ttRateCompanyId;

    @Value("${ttrate.companyKey}")
    private String ttRateCompanyKey;

    @Autowired
    private CurrencyServiceImpl currencyService;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private ExchangeCalculateServiceImpl exchangeCalculateService;


    @Test
    @Disabled
    void collectCurrencyRate() throws CashmallowException {

        List<String> currencies = countryMapper.getServiceCountriesIso4217();

        // "USD"가 없는 경우에만 처리
        if (!currencies.contains("USD")) {
            currencies.add("USD");
        }

        currencies.forEach(currency -> {
            try {
                currencyService.collectCurrencyRate(currency);
            } catch (CashmallowException e) {
                log.error(e.getMessage(), e);
            }
        });

    }

    @Test
    void updateTtrateCurrency() {
        // service가 활성화된 국가 목록 가져오기
        HashMap<String, Object> params = new HashMap<>();
        params.put("service", "Y");
        List<Country> countryList = countryService.getCountryList(params);

        // 중복되지 않는 iso4217 코드 목록 생성
        Set<String> toCurrencies = countryList.stream()
                .map(Country::getIso4217)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        String hkCode = "001";
        HashMap<String, Object> ttRateParams = new HashMap<>();
        List<Map<String, String>> currencyRates = new ArrayList<>();
        String honkongTime = LocalDateTime.now(ZoneId.of("Asia/Hong_Kong")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 환율 정보 가져오기
        toCurrencies.forEach(sourceCurrency -> {
            CountryCode sourceCountryCode = CountryCode.fromCurrency(sourceCurrency);
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(hkCode, sourceCountryCode.getCode());

            if (exchangeConfig != null) {
                Map<String, String> rateData = new HashMap<>();

                Country country = countryList.stream()
                        .filter(c -> c.getIso4217().equals(sourceCurrency))
                        .findFirst()
                        .orElse(null);

                if (country != null && ("Y".equals(country.getTtrateTtRate()) || "Y".equals(country.getTtrateNotesRate()))) {
                    BigDecimal currentBaseExchangeRate = currencyService.getCurrencyTarget(hkCode, sourceCountryCode.getCode());
                    BigDecimal exchangeSpreadRate = exchangeCalculateService.getSpreadRate(currentBaseExchangeRate, exchangeConfig.getFeeRateExchange());
                    BigDecimal refundExchangeSpreadRate = exchangeCalculateService.getRefundSpreadRate(currentBaseExchangeRate, exchangeConfig.getFeeRateExchange());
                    BigDecimal remitSpreadRate = exchangeCalculateService.getSpreadRate(currentBaseExchangeRate, exchangeConfig.getFeeRateRemittance());
                    BigDecimal refundRemitSpreadRate = exchangeCalculateService.getRefundSpreadRate(currentBaseExchangeRate, exchangeConfig.getFeeRateRemittance());

                    rateData.put("currency", sourceCurrency);

                    if ("Y".equals(country.getTtrateTtRate())) {
                        rateData.put("tt_buy", refundRemitSpreadRate.toString());
                        rateData.put("tt_sell", remitSpreadRate.toString());
                    } else {
                        rateData.put("tt_buy", "");
                        rateData.put("tt_sell", "");
                    }

                    if ("Y".equals(country.getTtrateNotesRate())) {
                        rateData.put("note_buy", refundExchangeSpreadRate.toString());
                        rateData.put("note_sell", exchangeSpreadRate.toString());
                    } else {
                        rateData.put("note_buy", "");
                        rateData.put("note_sell", "");
                    }

                    rateData.put("time", honkongTime);
                    currencyRates.add(rateData);

                    log.debug("Currency: {}, Rate Details: {}", sourceCurrency, rateData);
                }
            } else {
                log.warn("exchangeConfig is null. sourceCountryCode: {}", sourceCountryCode);
            }
        });

        // currencyRates가 비어있는 경우에는 요청하지 않음
        if (currencyRates.isEmpty()) {
            log.info("currencyRates is empty.");
            return;
        }

        ttRateParams.put("dryrun", 1); // 테스트시에만 사용. 실제 업데이트 되지 않음
        ttRateParams.put("api", "rate_update");
        ttRateParams.put("company_id", ttRateCompanyId);
        ttRateParams.put("company_key", ttRateCompanyKey);
        ttRateParams.put("currency_rates", currencyRates);

        try {
            // 전달하고자 하는 Parameter를 JSON 문자열로 변환한다.
            ObjectMapper objMapper = new ObjectMapper();
            String jsonParam = objMapper.writeValueAsString(ttRateParams);
            String ttRateResponse = HttpApi.httpPostWithJson(ttRateUrl, jsonParam);
            log.info("TTRate.com response : " + ttRateResponse);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}