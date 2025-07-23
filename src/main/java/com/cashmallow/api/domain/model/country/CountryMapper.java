package com.cashmallow.api.domain.model.country;

import com.cashmallow.api.interfaces.admin.dto.DatatablesRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CountryMapper {

    /**
     * Country list
     *
     * @param params ( service : null/Y/N, code : null, 001, 002, ... )
     * @return
     */
    List<Country> getCountryList(Map<String, Object> params);

    /**
     * Country
     *
     * @param params (@NotNull code : 001, 002, ... )
     * @return
     */
    Country getCountry(String code);

    /**
     * Rates
     *
     * @param params
     * @return
     */
    ExchangeConfig getExchangeConfigByFromCdToCd(Map<String, Object> params);

    /**
     * Rates. can_exchange = 'Y'
     *
     * @return
     */
    List<ExchangeConfig> getCanExchanageFeeRateList();

    List<ExchangeConfig> getCanRemittanceFeeRateList();

    /**
     * update rates table
     *
     * @return
     */
    int updateFeeRate(ExchangeConfig exchangeConfig);

    /**
     * 서비스에 필요한 국가 Iso4217 목록 가져오기.
     *
     * @return
     */
    public List<String> getServiceCountriesIso4217();

    /**
     * 서비스 중인 국가 목록
     */
    public List<Country> getServiceCountryList();

    int registerCountry(Country country);

    int updateCountry(Country updateCountry);

    List<CountryFee> getCountryFees();

    CountryFee getCountryFeeById(Long id);

    List<CountryFee> getCountryFeesByCd(String fromCd, String toCd, String useYn);

    int registerCountryFee(CountryFee countryFee);

    String getCountryMaxCode();

    int updateCountryFee(CountryFee countryFee);

    BigDecimal calculateFee(String fromCd, String toCd, BigDecimal toMoney);

    ExchangeConfig getExchangeConfigById(Long id);

    int insertExchangeConfig(ExchangeConfig exchangeConfig);

    int updateExchangeConfig(ExchangeConfig exchangeConfig);

    List<ExchangeConfig> getExchangeConfigByCode(String fromCd, String toCd);

    Long saveCurrencyLimit(CurrencyLimit currencyLimit);

    List<CurrencyLimit> getCurrencyLimits(DatatablesRequest datatablesRequest);

    Map<String, Long> getCurrencyLimitCount(DatatablesRequest datatablesRequest);
}
