package com.cashmallow.api.application.impl;


import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class AmountLimitServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(AmountLimitServiceImpl.class);

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CurrencyServiceImpl currencyService;

    @Autowired
    private AlarmService alarmService;

    public String checkRemittanceEnabled(Long userId, String fromCd, BigDecimal fromAmt, String toCd, BigDecimal toAmt,
                                         BigDecimal exchangeRate) throws CashmallowException {
        final String method = "checkRemittanceEnabled(): ";
        logger.info("{} userId={}, fromCd={}, toCd={}, exchangeRate={}", method, userId, fromCd, toCd,
                exchangeRate);

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);
        if (!"Y".equals(exchangeConfig.getEnabledRemittance())) {
            throw new CashmallowException(exchangeConfig.getExchangeNotice());
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        travelerService.validateVerification(traveler);

        List<NewRefund> refundList = refundRepositoryService.getNewRefundListInProgressByTravelerId(traveler.getId());

        for (NewRefund rf : refundList) {
            if (toCd.equals(rf.getFromCd())) {
                logger.error("{}: There is a refund in progress. userId={}", method, userId);
                throw new CashmallowException("EXCHANGE_NOT_PROCESS_REFUND");
            }
        }

        Remittance remittance = remittanceRepositoryService.getRemittanceInprogress(traveler.getId());
        if (remittance != null) {
            logger.error("{} : 이미 진행중인 송금이 있습니다. process Remittance Id={}", method, remittance.getId());
            throw new CashmallowException(MsgCode.PREVIOUS_REMITTANCE_IN_PROGRESS);
        }

        /* Check the currency rate changed */
        // BigDecimal currentExchangeRate = currencyService.getCurrencyTarget(fromCd, toCd);
        // if (exchangeRate.compareTo(currentExchangeRate) != 0) {
        //     throw new CashmallowException(MsgCode.EXCHANGE_CHANGED_EXCHANGE_RATE);
        // }

        HashMap<String, Object> params = new HashMap<>();
        params.put("code", fromCd);
        Country fromCountry = countryService.getCountryList(params).get(0);

        // Check max exchange amount per day based on from_cd
        validateDayLimit(traveler, fromAmt, fromCountry, toCd);

        // Check max exchange amount and count per month based on from_cd
        validateMonthLimit(traveler, fromAmt, fromCountry, toCd);

        // Check annual max exchange amount based on from_cd
        validateAnnualLimit(traveler, fromAmt, fromCountry, toCd);

        return "Y";
    }


    private void validateDayLimit(Traveler traveler, BigDecimal fromAmt, Country fromCountry, String toCd)
            throws CashmallowException {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] daily = DateUtil.getDaily(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", daily[0]);
        params.put("toDate", daily[1]);

        Map<String, Object> exchangeSum = exchangeRepositoryService.getExchangeFromAmtSumByPeriod(params);
        BigDecimal exchangeFromAmtSum = (BigDecimal) exchangeSum.get("fromAmtSum");

        Map<String, Object> remittanceSum = remittanceRepositoryService.getRemittanceFromAmtSumByPeriod(params);
        BigDecimal remittanceFromAmtSum = (BigDecimal) remittanceSum.get("fromAmtSum");

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        BigDecimal totalAmt = exchangeFromAmtSum.add(remittanceFromAmtSum).add(fromAmt);
        if (totalAmt.compareTo(exchangeConfig.getFromDayMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    totalAmt, exchangeConfig.getFromDayMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [일] 한도를 초과하였습니다.
                            - To Currency : %s
                            - UserId : %s
                            - 최대 환전 가능 금액 : %s
                            - %s
                            """.formatted(
                            fromCountry.getIso4217(),
                            traveler.getUserId().toString(),
                            exchangeConfig.getFromDayMaxExchange().toString(),
                            option
                    )
            );

            throw new CashmallowException("EXCHANGE_EXCEEDED_DAY_LIMIT", option);
        }
    }

    private void validateMonthLimit(Traveler traveler, BigDecimal fromAmt, Country fromCountry, String toCd)
            throws CashmallowException {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] monthly = DateUtil.getMonthly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", monthly[0]);
        params.put("toDate", monthly[1]);

        Map<String, Object> exchangeSum = exchangeRepositoryService.getExchangeFromAmtSumByPeriod(params);
        BigDecimal exchangeFromAmtSum = (BigDecimal) exchangeSum.get("fromAmtSum");

        Map<String, Object> remittanceSum = remittanceRepositoryService.getRemittanceFromAmtSumByPeriod(params);
        BigDecimal remittanceFromAmtSum = (BigDecimal) remittanceSum.get("fromAmtSum");

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        BigDecimal totalAmt = exchangeFromAmtSum.add(remittanceFromAmtSum).add(fromAmt);

        /* Check max exchange amount per month based on from_cd */
        if ( exchangeConfig.getFromMonthMaxExchange() != null &&
                totalAmt.compareTo(exchangeConfig.getFromMonthMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    totalAmt, exchangeConfig.getFromMonthMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [월] 한도를 초과하였습니다.
                            - To Currency : %s
                            - UserId : %s
                            - 최대 환전 가능 금액 : %s
                            - %s
                            """.formatted(
                            fromCountry.getIso4217(),
                            traveler.getUserId().toString(),
                            exchangeConfig.getFromMonthMaxExchange().toString(),
                            option
                    )
            );

            throw new CashmallowException("EXCHANGE_EXCEEDED_MONTH_LIMIT", option);
        }
    }

    private void validateAnnualLimit(Traveler traveler, BigDecimal fromAmt, Country fromCountry, String toCd)
            throws CashmallowException {

        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] yearly = DateUtil.getYearly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);
        final String fromCd = fromCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", yearly[0]);
        params.put("toDate", yearly[1]);

        Map<String, Object> exchangeSum = exchangeRepositoryService.getExchangeFromAmtSumByPeriod(params);
        BigDecimal exchangeFromAmtSum = (BigDecimal) exchangeSum.get("fromAmtSum");

        Map<String, Object> remittanceSum = remittanceRepositoryService.getRemittanceFromAmtSumByPeriod(params);
        BigDecimal remittanceFromAmtSum = (BigDecimal) remittanceSum.get("fromAmtSum");

        BigDecimal totalAmt = exchangeFromAmtSum.add(remittanceFromAmtSum).add(fromAmt);
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        if (exchangeConfig.getFromAnnualMaxExchange() != null &&
                totalAmt.compareTo(exchangeConfig.getFromAnnualMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    totalAmt, exchangeConfig.getFromAnnualMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [연] 한도를 초과하였습니다.
                            - To Currency : %s
                            - UserId : %s
                            - 최대 환전 가능 금액 : %s
                            - %s
                            """.formatted(
                            fromCountry.getIso4217(),
                            traveler.getUserId().toString(),
                            exchangeConfig.getFromAnnualMaxExchange().toString(),
                            option
                    )
            );

            throw new CashmallowException("EXCHANGE_EXCEEDED_ANNUAL_LIMIT", option);
        }
    }

}