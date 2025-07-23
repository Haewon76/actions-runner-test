package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.CurrencyService;
import com.cashmallow.api.domain.model.country.*;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.currency.MallowlinkBoCurrency;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkBoBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkCurrencyRateRequest;
import com.cashmallow.api.interfaces.mallowlink.currency.dto.MallowlinkCurrencyRateResponse;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.HttpApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CurrencyServiceImpl implements CurrencyService {

    Logger logger = LoggerFactory.getLogger(CurrencyServiceImpl.class);

    @Value("${apilayer.url}")
    private String apilayerUrl;

    @Value("${apilayer.accessKey}")
    private String apilayerAccessKey;

    @Value("${mallowlink.bank.url}")
    private String mallowlinkBankUrl;

    @Autowired
    private MallowlinkBoCurrency mallowlinkBoCurrency;

    @Value("${mallowlink.bank.currencyRateAPI}")
    private String mallowlinkCurrencyRateAPI;

    @Value("${ttrate.url}")
    private String ttRateUrl;

    @Value("${ttrate.companyId}")
    private String ttRateCompanyId;

    @Value("${ttrate.companyKey}")
    private String ttRateCompanyKey;

    @Autowired
    private Gson gson;


    @Autowired
    private CurrencyMapper currencyMapper;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private ExchangeCalculateServiceImpl exchangeCalculateService;

    @Autowired
    private EnvUtil envUtil;

    /* (non-Javadoc)
     * @see com.cashmallow.api.application.impl.CurrencyService#getCurrencyTarget(java.lang.String, java.lang.String)
     */
    @Override
    public BigDecimal getCurrencyTarget(String fromCd, String toCd) {

        Country fromCountry = countryMapper.getCountry(fromCd);
        Country toCountry = countryMapper.getCountry(toCd);

        HashMap<String, Object> params = new HashMap<>();
        params.put("source", toCountry.getIso4217());
        params.put("target", fromCountry.getIso4217());

        CurrencyRate currencyRate = currencyMapper.getCurrencyRate(params);

        return currencyRate.getRate();
    }

    /**
     * source, target ISO-4217 코드로 환율 조회
     *
     * @param params (source, target) The ISO-4217 code of the currency
     * @return
     */
    @Override
    public CurrencyRate getCurrencyRate(Map<String, Object> params) {
        return currencyMapper.getCurrencyRate(params);
    }

    /**
     * source ISO-4217 코드로 모든 target 환율 조회
     *
     * @param source : The ISO-4217 code of the currency
     * @return
     */
    @Override
    public List<CurrencyRate> getCurrencyRates(String source) {
        return currencyMapper.getCurrencyRates(source);
    }

    /**
     * Collect Currency Rate and Save Database
     *
     * @param sourceCurrency
     * @return
     * @throws Exception
     */
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public void collectCurrencyRate(String sourceCurrency) throws CashmallowException {
        String error = "";

        MallowlinkCurrencyRateRequest request = new MallowlinkCurrencyRateRequest(sourceCurrency);
        MallowlinkBoBaseResponse<List<MallowlinkCurrencyRateResponse>> response = mallowlinkBoCurrency.collectCurrency(request);

        if (response.getResult().isEmpty()) {
            error += "\n환율 수집 중 오류가 발생했습니다.\n" + response.getMessage();
            logger.info(error);
            throw new CashmallowException(error);
        }

        List<MallowlinkCurrencyRateResponse> currencyRateList = response.getResult();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

        Timestamp timestamp = Timestamp.valueOf(currencyRateList.get(0).getUpdatedAt());
        long ts = timestamp.getTime() / 1000;

        CMCurrency currency = new CMCurrency(sourceCurrency, ts, sdf.format(timestamp));
        int affectedRow = currencyMapper.insertCurrency(currency);

        if (affectedRow != 1) {
            error = "환율 마스터 정보를 저장할 수 없습니다.";
            throw new CashmallowException(error);
        }

        long currencyId = currency.getId();

        for (MallowlinkCurrencyRateResponse singleRate : currencyRateList) {
            String target = singleRate.getTarget();
            BigDecimal baseRate = singleRate.getRate();

            CurrencyRate currencyRate = new CurrencyRate(currencyId, sourceCurrency, target, baseRate, baseRate, BigDecimal.ZERO);

            affectedRow = currencyMapper.insertCurrencyRate(currencyRate);

            if (affectedRow != 1) {
                error = "환율 정보를 저장할 수 없습니다.";
                throw new CashmallowException(error);
            }
        }

        // TTRate.com에 환율 업데이트
        if (envUtil.isPrd()) {
            updateTtRate();
        }
    }

    /**
     * Retrieves the CurrencyRate for a given ISO4217 code. The ISO4217 code represents the target currency.
     *
     * @param source the ISO4217 code of the target currency
     * @return the CurrencyRate object containing the currency rate information
     */
    @Override
    public Map<String, CurrencyRate> getCurrencyRateByKrwAndUsd(String source, Timestamp createdDate) {
        Map<String, CurrencyRate> currencyRateByKrwAndUsd = currencyMapper.getCurrencyRateByKrwAndUsd(source, createdDate);
        // target과 source가 같은 경우, 환율 정보가 없음, KRW와 USD 환율 정보(1:1)를 추가
        if (!currencyRateByKrwAndUsd.containsKey("KRW") && "KRW".equals(source)) {
            currencyRateByKrwAndUsd.put("KRW", new CurrencyRate("KRW", source, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO));
        }
        if (!currencyRateByKrwAndUsd.containsKey("USD") && "USD".equals(source)) {
            currencyRateByKrwAndUsd.put("USD", new CurrencyRate("USD", source, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO));
        }
        return currencyRateByKrwAndUsd;
    }

    // ttrate.com 환율 업데이트
    private void updateTtRate() {
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
                    BigDecimal currentBaseExchangeRate = getCurrencyTarget(hkCode, sourceCountryCode.getCode());
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

                    logger.debug("Currency: {}, Rate Details: {}", sourceCurrency, rateData);
                }
            } else {
                logger.warn("exchangeConfig is null. sourceCountryCode: {}", sourceCountryCode);
            }
        });

        // currencyRates가 비어있는 경우에는 요청하지 않음
        if (currencyRates.isEmpty()) {
            logger.info("currencyRates is empty.");
            return;
        }

        ttRateParams.put("api", "rate_update");
        ttRateParams.put("company_id", ttRateCompanyId);
        ttRateParams.put("company_key", ttRateCompanyKey);
        ttRateParams.put("currency_rates", currencyRates);

        try {
            ObjectMapper objMapper = new ObjectMapper();
            String jsonParam = objMapper.writeValueAsString(ttRateParams);
            String ttRateResponse = HttpApi.httpPostWithJson(ttRateUrl, jsonParam);
            logger.info("TTRate.com response : " + ttRateResponse);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
