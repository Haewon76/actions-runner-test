package com.cashmallow.api.interfaces.homepage.web;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.impl.ExchangeServiceImpl;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.interfaces.traveler.dto.CountryResponse;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalculateResponse;
import com.cashmallow.common.geoutil.GeoUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;

import static com.cashmallow.common.CommonUtil.getRequestIp;

@RestController
@Slf4j
@RequestMapping("/homepage")
public class HomepageController {
    public static final String EXCHANGE_API_KEY = "cabd899a-c010-4aca-b6f0-966ddb8ff2c9";

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private GeoUtil geoUtil;

    private ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    private void postConstruct() {
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    }

    @GetMapping(value = "/exchange/calculate")
    public ResponseEntity<ExchangeCalculateResponse> exchangeCalculate(
            @RequestParam(value = "key") String key,
            @RequestParam(value = "from_cd") String fromCd,
            @RequestParam(value = "to_cd") String toCd,
            @RequestParam(value = "from_money") BigDecimal fromMoney) {

        ExchangeCalculateResponse response = new ExchangeCalculateResponse();

        if (EXCHANGE_API_KEY.equalsIgnoreCase(key)) {
            final CurrencyRate exchangeRate = exchangeService.getExchangeRate(fromCd, toCd);
            final ExchangeCalculateResponse res = response.toResponse(exchangeRate.getRate().multiply(fromMoney));
            return ResponseEntity.status(res.getCode()).body(res);
        }


        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping(value = "/exchanges")
    public ResponseEntity<CountryResponse> exchanges(
            @RequestParam(value = "key") String key) {
        CountryResponse country = new CountryResponse();

        if (EXCHANGE_API_KEY.equalsIgnoreCase(key)) {
            country.setData(countryService.getServiceCountryList());
            return ResponseEntity.status(country.getCode()).body(country);
        }

        return ResponseEntity.status(country.getCode()).body(country);
    }

}
