package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.shared.CashmallowException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

public interface CurrencyService {

    /**
     * 현재 환율 조회
     *
     * @param fromCd
     * @param toCd
     * @return
     */
    BigDecimal getCurrencyTarget(String fromCd, String toCd);

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
     * Collect currency exchange rates of service countries.
     *
     * @throws CashmallowException
     */
    void collectCurrencyRate(String currencyType) throws CashmallowException;

    /**
     * Retrieves the CurrencyRate for a given ISO4217 code. The ISO4217 code represents the target currency.
     *
     * @param source the ISO4217 code of the target currency
     * @return the CurrencyRate object containing the currency rate information
     */
    Map<String, CurrencyRate> getCurrencyRateByKrwAndUsd(String source, Timestamp createdDate);
}