package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryFee;
import com.cashmallow.api.domain.model.country.CurrencyLimit;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.system.JobPlan;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.DatatablesRequest;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import com.cashmallow.api.interfaces.traveler.dto.CountryExtVO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
//testsetestsetestsetestseteste
public interface CountryService {

    /**
     * Get country
     *
     * @param code
     * @return
     */
    Country getCountry(String code);

    /**
     * Get country list
     *
     * @param params ( service : null/Y/N, code : null, 001, 002, ... )
     * @return
     */
    List<CountryExtVO> getCountryExtVoList(String code, String service, String canSignup);

    List<Country> getCountryList(Map<String, Object> params);

    /**
     * Get exchange fee rate
     *
     * @param fromCd
     * @param toCd
     * @return
     */
    ExchangeConfig getExchangeConfig(String fromCd, String toCd);

    /**
     * Get exchange fee rate. can_exchange = 'Y'.
     *
     * @return
     */
    List<ExchangeConfig> getCanExchanageFeeRateList();

    List<ExchangeConfig> getCanRemittanceFeeRateList();

    /**
     * update rates table
     *
     * @param exchangeConfig
     * @return
     */
    int updateFeeRate(ExchangeConfig exchangeConfig);

    /**
     * Set country's service status
     *
     * @param fromCd
     * @param toCd
     * @param enabled
     * @return
     * @throws CashmallowException
     */
    int setExchangeServiceStatus(String fromCd, String toCd, String enabled) throws CashmallowException;

    int setRemittanceServiceStatus(String fromCd, String toCd, String enabled) throws CashmallowException;

    public List<Country> getServiceCountryList();

    int registerCountry(Country country, Long userId, String ip);

    int updateCountry(Country updateCountry, Long userId, String ip);

    List<CountryFee> getCountryFees();

    List<CountryFee> getCountryFeesByCd(String fromCd, String toCd, String useYn);

    int registerCountryFee(CountryFee countryFee, Long userId, String ip);

    int updateCountryFee(CountryFee countryFee, Long userId, String ip);

    BigDecimal calculateFee(String fromCd, String toCd, BigDecimal toMoney);

    List<ExchangeConfig> getExchangeConfigByCode(String fromCd, String toCd);

    int insertExchangeConfig(ExchangeConfig exchangeConfig, Long userId, String ip);

    int updateExchangeConfig(ExchangeConfig exchangeConfig, Long userId, String ip);

    String getCountryInfoJson();

    DatatablesResponse<CurrencyLimit> getCurrencyLimits(DatatablesRequest datatablesRequest);

    /**
     * 한도 제한금액 설정 수정
     *
     * @param ip            ip
     * @param currencyLimit 입력값
     * @return ID
     */
    Long saveCurrencyLimit(CurrencyLimit currencyLimit);

    List<JobPlan> getJobPlanListByFromCountry(String fromCountryCode);
}