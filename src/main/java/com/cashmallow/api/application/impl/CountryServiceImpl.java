package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.domain.model.country.*;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.system.JobPlan;
import com.cashmallow.api.domain.model.system.JobPlanHistory;
import com.cashmallow.api.domain.model.system.JobType;
import com.cashmallow.api.domain.model.system.SystemMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.DatatablesRequest;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import com.cashmallow.api.interfaces.batch.client.BatchClient;
import com.cashmallow.api.interfaces.traveler.dto.CountryExtVO;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.EnvUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CountryServiceImpl implements CountryService {
    private static final Logger logger = LoggerFactory.getLogger(CountryServiceImpl.class);
    private final String WALLET_EXPIRED_JOB_KEY = "%s_%s_WALLET_EXPIRED";

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private CountryHistoryMapper countryHistoryMapper;

    @Autowired
    @Qualifier("gsonSnakeCase")
    private Gson gsonSnakeCase;

    @Autowired
    private ObjectMapper objectMapper;

    private String CountryInfoJson;

    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private SystemMapper systemMapper;
    @Autowired
    private BatchClient batchClient;

    @PostConstruct
    public void init() {
        try {
            this.CountryInfoJson = objectMapper.writeValueAsString(CountryInfo.values());
        } catch (JsonProcessingException e) {
            logger.error("CountryInfoJson 초기화 실패, " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Country getCountry(String code) {
        return countryMapper.getCountry(code);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<CountryExtVO> getCountryExtVoList(String code, String service, String canSignup) {
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("service", service);
        if (canSignup != null && !canSignup.equals("")) {
            params.put("canSignup", canSignup);
        }

        return countryMapper.getCountryList(params).stream().map(c -> {
            CountryExtVO countryExtVO = gsonSnakeCase.fromJson(gsonSnakeCase.toJson(c), CountryExtVO.class);
            // ObjectMapper SNAKE_CASE exception properties.
            countryExtVO.setIso_3166(c.getIso3166());
            countryExtVO.setIso_4217(c.getIso4217());
            countryExtVO.setDefault_lat(c.getDefaultLat());
            countryExtVO.setDefault_lng(c.getDefaultLng());

            String flagImageFormat = "/images/country_flags/flag_country_%s.png";
            countryExtVO.setFlag_image_url(envUtil.getStaticUrl() + flagImageFormat.formatted(c.getIso3166())); // 국기 cdn url // FIXME 개선필요

            return countryExtVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Country> getCountryList(Map<String, Object> params) {
        return countryMapper.getCountryList(params);
    }

    /* (non-Javadoc)
     * @see com.cashmallow.api.application.impl.CountryService#getExchangeFeeRates(java.lang.String, java.lang.String)
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ExchangeConfig getExchangeConfig(String fromCd, String toCd) {

        logger.debug("CountryServiceImpl.getExchangeConfig() : fromCd={}, toCd={}", fromCd, toCd);

        HashMap<String, Object> params = new HashMap<>();
        params.put("fromCd", fromCd);
        params.put("toCd", toCd);

        return countryMapper.getExchangeConfigByFromCdToCd(params);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ExchangeConfig> getCanExchanageFeeRateList() {
        return countryMapper.getCanExchanageFeeRateList();
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ExchangeConfig> getCanRemittanceFeeRateList() {
        return countryMapper.getCanRemittanceFeeRateList();
    }

    @Override
    @Transactional
    public int updateFeeRate(ExchangeConfig exchangeConfig) {
        return countryMapper.updateFeeRate(exchangeConfig);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public int setExchangeServiceStatus(String fromCd, String toCd, String enabled) throws CashmallowException {

        String method = "setExchangeServiceStatus()";

        if (enabled.equals("Y") || enabled.equals("N")) {
            ExchangeConfig exchangeConfig = getExchangeConfig(fromCd, toCd);
            exchangeConfig.setEnabledExchange(enabled);
            int affectedRow = updateFeeRate(exchangeConfig);

            if (affectedRow != 1) {
                logger.error("{}: Failed to update. fromCd={}, toCd={}, enabledExchange={}", method, fromCd, toCd, enabled);
                throw new CashmallowException("Check fromCd and toCd. fromCd=" + fromCd + ", toCd=" + toCd);
            }

            return affectedRow;

        } else {
            logger.error("{}: Invalid parameter. enabled={}", method, enabled);
            throw new CashmallowException("enabled value must to be 'Y' or 'N'. enabled=" + enabled);
        }
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public int setRemittanceServiceStatus(String fromCd, String toCd, String enabled) throws CashmallowException {

        String method = "setRemittanceServiceStatus()";

        if (enabled.equals("Y") || enabled.equals("N")) {
            ExchangeConfig exchangeConfig = getExchangeConfig(fromCd, toCd);
            exchangeConfig.setEnabledRemittance(enabled);

            int affectedRow = updateFeeRate(exchangeConfig);

            if (affectedRow != 1) {
                logger.error("{}: Failed to update. fromCd={}, toCd={}, enabledRemittance={}", method, fromCd, toCd, enabled);
                throw new CashmallowException("Check fromCd and toCd. fromCd=" + fromCd + ", toCd=" + toCd);
            }

            return affectedRow;

        } else {
            logger.error("{}: Invalid parameter. enabled={}", method, enabled);
            throw new CashmallowException("enabled value must to be 'Y' or 'N'. enabled=" + enabled);
        }
    }

    @Transactional
    @Override
    public int registerCountry(Country country, Long userId, String ip) {
        String maxCountryCode = countryMapper.getCountryMaxCode();
        int updateCode = Integer.parseInt(maxCountryCode) + 1;
        country.setCode(String.format("%03d", updateCode));

        int result = countryMapper.registerCountry(country);

        CountryHistory countryHistory = new CountryHistory();
        BeanUtils.copyProperties(country, countryHistory);
        countryHistory.setUserId(userId);
        countryHistory.setIp(ip);
        countryHistoryMapper.registerCountryHistory(countryHistory);

        return 1;
    }

    @Transactional
    @Override
    public int updateCountry(Country updateCountry, Long userId, String ip) {
        int result = countryMapper.updateCountry(updateCountry);

        CountryHistory countryHistory = new CountryHistory();
        BeanUtils.copyProperties(updateCountry, countryHistory);
        countryHistory.setUserId(userId);
        countryHistory.setIp(ip);
        countryHistoryMapper.registerCountryHistory(countryHistory);

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public List<CountryFee> getCountryFees() {
        return countryMapper.getCountryFees();
    }

    @Transactional(readOnly = true)
    @Override
    public List<CountryFee> getCountryFeesByCd(String fromCd, String toCd, String useYn) {
        List<CountryFee> countryFeeList = countryMapper.getCountryFeesByCd(fromCd, toCd, useYn);
        return getCountryFees(countryFeeList);
    }

    @NotNull
    private List<CountryFee> getCountryFees(List<CountryFee> countryFeeList) {
        countryFeeList.forEach(countryFee -> {
            if (countryFee.getCreatedAt() != null) {
                countryFee.setCreatedAtString(CommDateTime.toString(countryFee.getCreatedAt()));
            }
            if (countryFee.getUpdatedAt() != null) {
                countryFee.setUpdatedAtString(CommDateTime.toString(countryFee.getUpdatedAt()));
            }
        });

        return countryFeeList;
    }

    @Transactional
    @Override
    public int registerCountryFee(CountryFee countryFee, Long userId, String ip) {

        int result = countryMapper.registerCountryFee(countryFee);

        CountryFeeHistory countryFeeHistory = new CountryFeeHistory();
        BeanUtils.copyProperties(countryFee, countryFeeHistory);
        countryFeeHistory.setCountryFeeId(countryFee.getId());
        countryFeeHistory.setUserId(userId);
        countryFeeHistory.setIp(ip);
        countryHistoryMapper.registerCountryFeeHistory(countryFeeHistory);

        return result;
    }

    @Transactional
    @Override
    public int updateCountryFee(CountryFee countryFee, Long userId, String ip) {

        int result = countryMapper.updateCountryFee(countryFee);

        CountryFeeHistory countryFeeHistory = new CountryFeeHistory();
        BeanUtils.copyProperties(countryFee, countryFeeHistory);
        countryFeeHistory.setCountryFeeId(countryFee.getId());
        countryFeeHistory.setUserId(userId);
        countryFeeHistory.setIp(ip);
        countryHistoryMapper.registerCountryFeeHistory(countryFeeHistory);

        return result;
    }

    @Transactional(readOnly = true)
    @Override
    public BigDecimal calculateFee(String fromCd, String toCd, BigDecimal toMoney) {
        return countryMapper.calculateFee(fromCd, toCd, toMoney);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ExchangeConfig> getExchangeConfigByCode(String fromCd, String toCd) {
        return countryMapper.getExchangeConfigByCode(fromCd, toCd);
    }

    @Transactional
    @Override
    public int insertExchangeConfig(ExchangeConfig exchangeConfig, Long userId, String ip) {
        int result = countryMapper.insertExchangeConfig(exchangeConfig);

        ExchangeConfigHistory exchangeConfigHistory = new ExchangeConfigHistory();
        BeanUtils.copyProperties(exchangeConfig, exchangeConfigHistory);
        exchangeConfigHistory.setExchangeConfigId(exchangeConfig.getId());
        exchangeConfigHistory.setUserId(userId);
        exchangeConfigHistory.setIp(ip);
        countryHistoryMapper.insertExchangeConfigHistory(exchangeConfigHistory);

        return result;
    }

    @Transactional
    @Override
    public int updateExchangeConfig(ExchangeConfig exchangeConfig, Long userId, String ip) {
        int result = countryMapper.updateExchangeConfig(exchangeConfig);

        ExchangeConfigHistory exchangeConfigHistory = new ExchangeConfigHistory();
        BeanUtils.copyProperties(exchangeConfig, exchangeConfigHistory);
        exchangeConfigHistory.setExchangeConfigId(exchangeConfig.getId());
        exchangeConfigHistory.setUserId(userId);
        exchangeConfigHistory.setIp(ip);
        countryHistoryMapper.insertExchangeConfigHistory(exchangeConfigHistory);

        return result;
    }

    public void makeJobPlan(CurrencyLimit currencyLimit) {
        CountryCode fromCountry = CountryCode.of(currencyLimit.getFromCd());
        CountryCode toCountry = CountryCode.of(currencyLimit.getToCd());
        String jobKey = WALLET_EXPIRED_JOB_KEY.formatted(fromCountry.name(), toCountry.name());

        JobPlan existJobPlan = systemMapper.getJobPlanByJobKey(jobKey);

        boolean canCreateJobPlan = ObjectUtils.isNotEmpty(currencyLimit.getWalletExpiredDay()) && ObjectUtils.isNotEmpty(currencyLimit.getWalletExpiredMinute());

        if(canCreateJobPlan) {
            // JobPlan이 생성 가능한 경우 job 생성 혹은 업데이트
            String cronExpress = getCronExpression(currencyLimit.getWalletExpiredMinute());
            long walletExpiredCalendarDay = currencyLimit.getWalletExpiredDay();
            JobPlan jobPlan = new JobPlan(fromCountry.getCode(), toCountry.getCode(), jobKey, cronExpress, walletExpiredCalendarDay, JobType.WALLET_EXPIRE);
            systemMapper.insertJobPlan(jobPlan);
            insertJobPlanHistory(jobPlan);
            batchClient.updateJobSchedule();
        } else if (ObjectUtils.isNotEmpty(existJobPlan)) {
            // 기존에 만든 jobPlan이 있지만, JobPlan을 지워야 하는 경우
            batchClient.deleteJobSchedule(existJobPlan.getId());
        }
    }

    private void insertJobPlanHistory(JobPlan jobPlan) {
        JobPlanHistory jobPlanHistory = new JobPlanHistory();
        BeanUtils.copyProperties(jobPlan, jobPlanHistory);
        jobPlanHistory.setJobPlanId(jobPlan.getId());
        systemMapper.insertJobPlanHistory(jobPlanHistory);
    }

    public List<JobPlan> getJobPlanListByFromCountry(String fromCountryCode) {
        return systemMapper.getJobPlanListByFromCountryCode(fromCountryCode);
    }

    private String getCronExpression(Integer minute) {
        LocalTime scheduleTime = LocalTime.MIDNIGHT.minusMinutes(minute);

        return String.format("0 %d %d * * ?", scheduleTime.getMinute(), scheduleTime.getHour());
    }

    @Override
    public String getCountryInfoJson() {
        return CountryInfoJson;
    }

    public List<Country> getServiceCountryList() {
        return countryMapper.getServiceCountryList();
    }

    /**
     * 한도 제한금액 설정 조회
     *
     * @param datatablesRequest 입력값
     * @return 결과
     */
    @Override
    public DatatablesResponse<CurrencyLimit> getCurrencyLimits(DatatablesRequest datatablesRequest) {
        Map<String, Long> recordCounts = countryMapper.getCurrencyLimitCount(datatablesRequest);
        long totalRecords = recordCounts.get("totalRecords");
        long filteredRecords = recordCounts.get("filteredRecords");
        List<CurrencyLimit> currencyLimits = countryMapper.getCurrencyLimits(datatablesRequest);

        // DataTables 응답 생성
        return new DatatablesResponse<>(totalRecords, filteredRecords, currencyLimits);
    }

    @Override
    public Long saveCurrencyLimit(CurrencyLimit currencyLimit) {
        countryMapper.saveCurrencyLimit(currencyLimit);
        makeJobPlan(currencyLimit);

        ExchangeConfig exchangeConfig = getExchangeConfig(currencyLimit.getFromCd(), currencyLimit.getToCd());
        ExchangeConfigHistory exchangeConfigHistory = new ExchangeConfigHistory();
        BeanUtils.copyProperties(exchangeConfig, exchangeConfigHistory);
        exchangeConfigHistory.setExchangeConfigId(exchangeConfig.getId());
        exchangeConfigHistory.setUserId(currencyLimit.getCreator());
        exchangeConfigHistory.setIp("0.0.0.0");
        countryHistoryMapper.insertExchangeConfigHistory(exchangeConfigHistory);

        return currencyLimit.getId();
    }
}
