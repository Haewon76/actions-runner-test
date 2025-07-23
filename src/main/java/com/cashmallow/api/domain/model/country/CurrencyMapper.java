package com.cashmallow.api.domain.model.country;

import org.apache.ibatis.annotations.MapKey;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface CurrencyMapper {

    /**
     * source, target ISO-4217 코드로 환율 조회
     *
     * @param params (source, target) The ISO-4217 code of the currency
     * @return
     */
    CurrencyRate getCurrencyRate(Map<String, Object> params);

    /**
     * source ISO-4217 코드로 모든 target 환율 조회
     *
     * @param source : The ISO-4217 code of the currency
     * @return
     */
    List<CurrencyRate> getCurrencyRates(String source);

    /**
     * 환율 마스터 정보 입력
     *
     * @param currency
     * @return
     */
    int insertCurrency(CMCurrency currency);

    /**
     * 환율 정보 입력.
     *
     * @param currencyRate (currencyId, target, rate, baseRate, adjustedRate)
     * @return int updated row count
     */
    int insertCurrencyRate(CurrencyRate currencyRate);

    @MapKey("target")
    Map<String, CurrencyRate> getCurrencyRateByKrwAndUsd(String source, Timestamp createdDate);
}
