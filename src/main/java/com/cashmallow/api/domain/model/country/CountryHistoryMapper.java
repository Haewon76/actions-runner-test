package com.cashmallow.api.domain.model.country;

import java.util.List;

public interface CountryHistoryMapper {

    int registerCountryHistory(CountryHistory countryHistory);

    List<CountryHistory> getCountryHistory(String countryCode);

    int registerCountryFeeHistory(CountryFeeHistory countryFeeHistory);

    List<CountryFeeHistory> getCountryFeeHistory(Long countryFeeId);

    int insertExchangeConfigHistory(ExchangeConfigHistory exchangeConfigHistory);

    List<ExchangeConfigHistory> getExchangeConfigHistory(Long exchangeConfigId);
}
