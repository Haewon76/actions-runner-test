package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryMapper;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeMapper;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class LimitCheckService {

    private final WalletRepositoryService walletRepositoryService;
    private final CountryServiceImpl countryService;

    private final ExchangeMapper exchangeMapper;
    private final CountryMapper countryMapper;

    private final MessageSource messageSource;
    private final AlarmService alarmService;

    public Map<String, Object> validateLimitForToCountry(Traveler traveler, BigDecimal toAmt, Country toCountry,
                                                         Locale locale, boolean isRemittance, ExchangeConfig exchangeConfig) {
        Map<String, Object> result = new HashMap<>();

        Map<String, Object> annualLimit = validateAnnualLimitForToCountry(traveler, toAmt, toCountry, locale, exchangeConfig);
        Map<String, Object> monthLimit = validateMonthLimitForToCountry(traveler, toAmt, toCountry, locale, isRemittance, exchangeConfig);
        Map<String, Object> dayLimit = validateDayLimitForToCountry(traveler, toAmt, toCountry, locale, isRemittance, exchangeConfig);

        if (StringUtils.equals((String) annualLimit.get("status"), Const.STATUS_FAILURE)) {
            BigDecimal toAmtResult = (BigDecimal) result.get("toAmt");
            if (Objects.equals(toAmtResult, BigDecimal.ZERO)) {
                return annualLimit;
            }
            result = annualLimit;
        }

        if (StringUtils.equals((String) monthLimit.get("status"), Const.STATUS_FAILURE)) {

            BigDecimal annualToAmt = result.get("toAmt") == null ? BigDecimal.ZERO : (BigDecimal) result.get("toAmt");
            BigDecimal monthToAmt = (BigDecimal) monthLimit.get("toAmt");

            if (monthToAmt.compareTo(BigDecimal.ZERO) == 0) {
                return monthLimit;
            } else if (annualToAmt.compareTo(BigDecimal.ZERO) == 0 || monthToAmt.compareTo(annualToAmt) < 0) {
                result = monthLimit;
            }
        }

        if (StringUtils.equals((String) dayLimit.get("status"), Const.STATUS_FAILURE)) {

            BigDecimal agoToAmt = result.get("toAmt") == null ? BigDecimal.ZERO : (BigDecimal) result.get("toAmt");
            BigDecimal dayToAmt = (BigDecimal) dayLimit.get("toAmt");

            if (dayToAmt.compareTo(BigDecimal.ZERO) == 0) {
                return dayLimit;
            } else if (agoToAmt.compareTo(BigDecimal.ZERO) == 0 || dayToAmt.compareTo(agoToAmt) < 0) {
                result = dayLimit;
            }
        }

        return result;
    }


    public Map<String, Object> validateWalletLimitForFromCountry(Traveler traveler, BigDecimal fromAmt,
                                                                 Country fromCountry, String toCd, Locale locale, ExchangeConfig exchangeConfig) {
        String method = "validateWalletLimitForFromCountry()";

        HashMap<String, Object> results = new HashMap<>();

        String fromCd = fromCountry.getCode();

        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletListByTravelerId(traveler.getId());

        BigDecimal totalWalletFromMoney = BigDecimal.ZERO;

        // 현재 지갑에 소지하고 있는 금액들의 fromAmt를 가져와서 from금액의 합계를 구한다.
        for (TravelerWallet w : wallets) {
            if (w.getExchangeIds() == null) {
                continue;
            }

            if (w.getRootCd().equals(fromCd) && w.getCountry().equals(toCd)) {
                JSONObject exchangeIds = new JSONObject(w.getExchangeIds());
                JSONArray exchangeArray = exchangeIds.getJSONArray(Const.EXCHANGE_IDS);

                for (Object exchangeId : exchangeArray) {
                    Exchange exchange = exchangeMapper.getExchangeByExchangeId(Long.valueOf(exchangeId.toString()));
                    totalWalletFromMoney = totalWalletFromMoney.add(exchange.getFromAmt());
                }
            }
        }

        BigDecimal fromMaxExchangeAmt = exchangeConfig.getFromMaxExchange();

        // 한도 금액 설정이 없으면 무조건 패스
        if (fromMaxExchangeAmt == null || fromMaxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("fromAmt", fromAmt);
            return results;
        }

        /* Check max exchange amount per case based on from_cd */
        if (totalWalletFromMoney.add(fromAmt).compareTo(fromMaxExchangeAmt) > 0) {
            results.put("status", Const.STATUS_FAILURE);

            if (fromMaxExchangeAmt.subtract(totalWalletFromMoney).compareTo(exchangeConfig.getFromMinExchange()) >= 0) {
                results.put("fromAmt", fromMaxExchangeAmt.subtract(totalWalletFromMoney));

                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromMaxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));
            } else {
                results.put("fromAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(totalWalletFromMoney));
                array.add(NumberFormat.getNumberInstance(locale).format(fromMaxExchangeAmt));

                // 환전 한도 금액 세팅
                log.info("{}: 지갑에 보유한 금액이 한도를 넘었습니다. totalWalletFromMoney={}, fromMaxExchange={}",
                        method, totalWalletFromMoney, fromMaxExchangeAmt);

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_WALLET_LIMIT", array.toArray(), "Exchange limit exceeded.", locale));
            }
            return results;
        }
        results.put("status", Const.STATUS_SUCCESS);
        results.put("fromAmt", fromAmt);

        return results;

    }

    public Map<String, Object> validateWalletLimitForToCountry(Traveler traveler, BigDecimal toAmt,
                                                               String toCd, Locale locale, ExchangeConfig exchangeConfig) {
        String method = "validateWalletLimitForToCountry()";

        HashMap<String, Object> results = new HashMap<>();

        Country toCountry = countryMapper.getCountry(toCd);

        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletListByTravelerId(traveler.getId());

        BigDecimal totalWalletAmt = BigDecimal.ZERO;

        // 현재 지갑에 소지하고 있는 금액들의 fromAmt를 가져와서 from금액의 합계를 구한다.
        for (TravelerWallet w : wallets) {
            if (w.getCountry().equals(toCd)) {
                totalWalletAmt = totalWalletAmt.add(w.geteMoney());
                totalWalletAmt = totalWalletAmt.add(w.getcMoney());
                totalWalletAmt = totalWalletAmt.add(w.getrMoney());
            }
        }

        BigDecimal toMaxExchangeAmt = exchangeConfig.getToMaxExchange();

        // 한도 금액 설정이 없으면 무조건 패스
        if (toMaxExchangeAmt == null || toMaxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("toAmt", toAmt);
            return results;
        }

        /* Check max exchange amount per case based on from_cd */
        if (totalWalletAmt.add(toAmt).compareTo(toMaxExchangeAmt) > 0) {
            results.put("status", Const.STATUS_FAILURE);

            if (toMaxExchangeAmt.subtract(totalWalletAmt).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", toMaxExchangeAmt.subtract(totalWalletAmt));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toMaxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));
            } else {
                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(totalWalletAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(toMaxExchangeAmt));

                // 환전 한도 금액 세팅
                log.info("{}: 지갑에 보유한 금액이 한도를 넘었습니다. totalWalletToMoney={}, toMaxExchange={}",
                        method, totalWalletAmt, toMaxExchangeAmt);

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_WALLET_LIMIT", array.toArray(), "Exchange limit exceeded.", locale));
            }
            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);

        return results;
    }

    private Map<String, Object> validateDayLimitForToCountry(Traveler traveler, BigDecimal toAmt,
                                                             Country toCountry, Locale locale, boolean isRemittance, ExchangeConfig exchangeConfig) {

        final boolean isStatic = !StringUtils.equals(toCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] daily = DateUtil.getDaily(CountryCode.of(toCountry.getCode()).getZoneId(), isStatic);

        String toCd = toCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("toCd", toCd);
        params.put("fromDate", daily[0]);
        params.put("toDate", daily[1]);

        Map<String, Object> sum = exchangeMapper.getToAmtSumByPeriod(params);
        // 일 환전 가능 금액
        BigDecimal toDayMaxExchangeAmt = exchangeConfig.getToDayMaxExchange();

        // to일본의 경우 인출 파트너(퀸비), 송금 파트너(월드패밀리)가 달라서 송금금액 체크를 별도로 한다.
        if(isRemittance && "004".equalsIgnoreCase(toCd)) {
            // 일본 송금인 경우 예외 처리
            sum = exchangeMapper.getToAmtRemittanceSumByPeriod(params);
        }

        // 기간내 총 환전 및 송금 금액
        BigDecimal toAmtSum = (BigDecimal) sum.get("toAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = toAmtSum.add(toAmt);

        HashMap<String, Object> results = new HashMap<>();

        // 일 한도 금액은 필수값
        // 총 금액이 일한도 금액 보다 작으면, 실패
        if (toDayMaxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (toDayMaxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", toDayMaxExchangeAmt.subtract(toAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toDayMaxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                // 환전 가능 금액이 최소금액보다 작으면 출금 불가로 정의
            } else if (toDayMaxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) < 0) {
                alarmService.i("환전한도초과",
                        """
                                [일] 한도를 초과하였습니다.
                                - To Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                toCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                toDayMaxExchangeAmt.toString(),
                                toDayMaxExchangeAmt.subtract(toAmtSum)
                        )
                );

                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                // array.add(NumberFormat.getNumberInstance(locale).format(toAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(toDayMaxExchangeAmt));
                array.add(toCountry.getEngName());
                array.add(CommDateTime.getTimeFormat(CountryCode.of(toCountry.getCode()).getZoneId(), locale));

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_DAY_LIMIT_NEW", array.toArray(), "Yearly exchange limit exceeded.", locale));
            }

            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);
        return results;
    }

    private Map<String, Object> validateMonthLimitForToCountry(Traveler traveler, BigDecimal toAmt, Country toCountry,
                                                               Locale locale, boolean isRemittance, ExchangeConfig exchangeConfig) {

        final boolean isStatic = !StringUtils.equals(toCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] monthly = DateUtil.getMonthly(CountryCode.of(toCountry.getCode()).getZoneId(), isStatic);

        String toCd = toCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("toCd", toCd);
        params.put("fromDate", monthly[0]);
        params.put("toDate", monthly[1]);

        Map<String, Object> sum = exchangeMapper.getToAmtSumByPeriod(params);
        // 월 환전 가능 금액
        BigDecimal toMonthMaxExchangeAmt = exchangeConfig.getToMonthMaxExchange();

        // to일본의 경우 인출 파트너(퀸비), 송금 파트너(월드패밀리)가 달라서 송금금액 체크를 별도로 한다.
        if(isRemittance && CountryCode.JP.getCode().equalsIgnoreCase(toCd)) {
            // 일본 송금인 경우 예외 처리
            sum = exchangeMapper.getToAmtRemittanceSumByPeriod(params);
        }

        // 기간내 총 환전 및 송금 금액
        BigDecimal toAmtSum = (BigDecimal) sum.get("toAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = toAmtSum.add(toAmt);

        HashMap<String, Object> results = new HashMap<>();

        // 월 한도 금액 설정이 없으면 무조건 패스
        if (toMonthMaxExchangeAmt == null || toMonthMaxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("toAmt", toAmt);
            return results;
        }

        // 총 금액이 일한도 금액 보다 작으면, 실패
        if (toMonthMaxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (toMonthMaxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", toMonthMaxExchangeAmt.subtract(toAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toMonthMaxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_MONTH_AUTO_LIMIT", array.toArray(), "Monthly exchange limit exceeded.", locale));

                // 환전 가능 금액이 최소금액보다 작으면 출금 불가로 정의
            } else if (toMonthMaxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) < 0) {
                alarmService.i("환전한도초과",
                        """
                                [월] 한도를 초과하였습니다.
                                - To Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                toCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                toMonthMaxExchangeAmt.toString(),
                                toMonthMaxExchangeAmt.subtract(toAmtSum)
                        )
                );

                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(toMonthMaxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_MONTH_LIMIT", array.toArray(), "Monthly exchange limit exceeded.", locale));
            }

            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);
        return results;
    }

    private Map<String, Object> validateAnnualLimitForToCountry(Traveler traveler, BigDecimal toAmt,
                                                                Country toCountry, Locale locale, ExchangeConfig exchangeConfig) {
        final boolean isStatic = !StringUtils.equals(toCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] yearly = DateUtil.getYearly(CountryCode.of(toCountry.getCode()).getZoneId(), isStatic);

        String toCd = toCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("toCd", toCd);
        params.put("fromDate", yearly[0]);
        params.put("toDate", yearly[1]);

        Map<String, Object> sum = exchangeMapper.getToAmtSumByPeriod(params);
        // 기간내 총 환전 및 송금 금액
        BigDecimal toAmtSum = (BigDecimal) sum.get("toAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = toAmtSum.add(toAmt);
        // 연간 환전 가능 금액
        BigDecimal maxExchangeAmt = exchangeConfig.getToAnnualMaxExchange();

        HashMap<String, Object> results = new HashMap<>();

        // 연 한도 금액 설정이 없으면 무조건 패스
        if (maxExchangeAmt == null || maxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("toAmt", toAmt);
            return results;
        }

        // 총 금액이 연한도 금액 보다 작으면, 실패
        if (maxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (maxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", maxExchangeAmt.subtract(toAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

            } else if (maxExchangeAmt.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) < 0) {
                alarmService.i("환전한도초과",
                        """
                                [연] 한도를 초과하였습니다.
                                - To Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                toCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                maxExchangeAmt.toString(),
                                maxExchangeAmt.subtract(toAmtSum)
                        )
                );

                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_ANNUAL_LIMIT", array.toArray(), "Yearly exchange limit exceeded.", locale));
            }

            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);

        return results;
    }

    /**
     * 일간 from country limit 체크
     *
     * @param traveler
     * @param fromAmt
     * @param fromCountry
     * @param locale
     * @param exchangeConfig
     * @return
     */
    public Map<String, Object> validateDayLimitForFromCountry(Traveler traveler, BigDecimal fromAmt,
                                                              Country fromCountry, Locale locale, ExchangeConfig exchangeConfig) {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] daily = DateUtil.getDaily(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", daily[0]);
        params.put("toDate", daily[1]);

        Map<String, Object> sum = exchangeMapper.getFromAmtSumByPeriod(params);
        // 기간내 총 환전 및 송금 금액
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = fromAmtSum.add(fromAmt);
        // 일 환전 가능 금액
        BigDecimal fromDayMaxExchangeAmt = exchangeConfig.getFromDayMaxExchange();

        HashMap<String, Object> results = new HashMap<>();

        // 일 한도 금액은 필수값
        // 총 금액이 일한도 금액 보다 작으면, 실패
        if (fromDayMaxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (fromDayMaxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) >= 0) {
                results.put("fromAmt", fromDayMaxExchangeAmt.subtract(fromAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromDayMaxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                // 환전 가능 금액이 최소금액보다 작으면 출금 불가로 정의
            } else if (fromDayMaxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) < 0) {
                alarmService.i("환전한도초과",
                        """
                                [일] 한도를 초과하였습니다.
                                - From Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                fromCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                fromDayMaxExchangeAmt.toString(),
                                fromDayMaxExchangeAmt.subtract(fromAmtSum)
                        )
                );
                results.put("fromAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(fromDayMaxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_DAY_LIMIT", array.toArray(), "Yearly exchange limit exceeded.", locale));
            }
            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("fromAmt", fromAmt);
        return results;
    }

    /**
     * 월간 from country limit 체크
     *
     * @param traveler
     * @param fromAmt
     * @param fromCountry
     * @param locale
     * @param exchangeConfig
     * @return
     */
    public Map<String, Object> validateMonthLimitForFromCountry(Traveler traveler, BigDecimal fromAmt,
                                                                Country fromCountry, Locale locale, ExchangeConfig exchangeConfig) {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] monthly = DateUtil.getMonthly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", monthly[0]);
        params.put("toDate", monthly[1]);

        Map<String, Object> sum = exchangeMapper.getFromAmtSumByPeriod(params);
        // 기간내 총 환전 및 송금 금액
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = fromAmtSum.add(fromAmt);
        // 월 환전 가능 금액
        BigDecimal maxExchangeAmt = exchangeConfig.getFromMonthMaxExchange();

        HashMap<String, Object> results = new HashMap<>();

        // 월 한도 금액 설정이 없으면 무조건 패스
        if (maxExchangeAmt == null || maxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("fromAmt", fromAmt);
            return results;
        }

        // 총 금액이 월한도 금액 보다 작으면, 실패
        if (maxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (maxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) >= 0) {
                results.put("fromAmt", maxExchangeAmt.subtract(fromAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_MONTH_AUTO_LIMIT", array.toArray(), "Yearly exchange limit exceeded.", locale));

                // 환전 가능 금액이 최소금액보다 작으면 출금 불가로 정의
            } else if (maxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) < 0) {

                alarmService.i("환전한도초과",
                        """
                                [월] 한도를 초과하였습니다.
                                - From Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                fromCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                maxExchangeAmt.toString(),
                                maxExchangeAmt.subtract(fromAmtSum)
                        )
                );
                results.put("fromAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_MONTH_LIMIT", array.toArray(), "Yearly exchange limit exceeded.", locale));
            }
            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("fromAmt", fromAmt);
        return results;
    }

    /**
     * 연간 from country limit 계산
     *
     * @param traveler
     * @param fromAmt
     * @param fromCountry
     * @param locale
     * @param exchangeConfig
     * @return
     */
    public Map<String, Object> validateAnnualLimitForFromCountry(Traveler traveler, BigDecimal fromAmt,
                                                                 Country fromCountry, Locale locale, ExchangeConfig exchangeConfig) {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] yearly = DateUtil.getYearly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", yearly[0]);
        params.put("toDate", yearly[1]);

        Map<String, Object> sum = exchangeMapper.getFromAmtSumByPeriod(params);
        // 기간내 총 환전 및 송금 금액
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = fromAmtSum.add(fromAmt);
        // 연간 환전, 송금 가능 금액
        BigDecimal fromAnnualMaxExchangeAmt = exchangeConfig.getFromAnnualMaxExchange();

        HashMap<String, Object> results = new HashMap<>();

        // 연 한도 금액 설정이 없으면 무조건 패스
        if (fromAnnualMaxExchangeAmt == null || fromAnnualMaxExchangeAmt.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("fromAmt", fromAmt);
            return results;
        }

        // 총 금액이 연한도 금액 보다 작으면, 실패
        if (fromAnnualMaxExchangeAmt.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 환전 가능 금액이 최소 환전 금액을 넘었을 경우
            if (fromAnnualMaxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) >= 0) {
                results.put("fromAmt", fromAnnualMaxExchangeAmt.subtract(fromAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromAnnualMaxExchangeAmt));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                // 환전 가능 금액이 최소금액보다 작으면 출금 불가로 정의
            } else if (fromAnnualMaxExchangeAmt.subtract(fromAmtSum).compareTo(exchangeConfig.getFromMinExchange()) < 0) {
                alarmService.i("환전한도초과",
                        """
                                [연] 한도를 초과하였습니다.
                                - From Currency : %s
                                - UserId : %s
                                - 최대 환전 가능 금액 : %s
                                - 남아있는 환전 가능 금액 : %s
                                """.formatted(
                                fromCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                fromAnnualMaxExchangeAmt.toString(),
                                fromAnnualMaxExchangeAmt.subtract(fromAmtSum)
                        )
                );

                results.put("fromAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(fromCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(fromAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(fromAnnualMaxExchangeAmt));

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_ANNUAL_LIMIT", array.toArray(), "Yearly exchange limit exceeded.", locale));
            }
            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("fromAmt", fromAmt);

        return results;
    }

    public Map<String, Object> validateWithdrawalMonthLimitForToCountry(Traveler traveler, BigDecimal toAmt,
                                                                        Country toCountry, Locale locale, ExchangeConfig exchangeConfig) {

        final boolean isStatic = !StringUtils.equals(toCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] monthly = DateUtil.getMonthly(CountryCode.of(toCountry.getCode()).getZoneId(), isStatic);

        String toCd = toCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("toCd", toCd);
        params.put("fromDate", monthly[0]);
        params.put("toDate", monthly[1]);

        Map<String, Object> sum = exchangeMapper.getWithdrawalToAmtSumByPeriod(params);
        // 월 인출 가능 금액
        BigDecimal toMonthMaxWithdrawal = exchangeConfig.getToMonthMaxWithdrawal();

        // 기간내 총 환전 및 송금 금액
        BigDecimal toAmtSum = (BigDecimal) sum.get("toAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = toAmtSum.add(toAmt);

        HashMap<String, Object> results = new HashMap<>();

        // 월 한도 금액 설정이 없으면 무조건 패스
        if (toMonthMaxWithdrawal == null || toMonthMaxWithdrawal.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("toAmt", toAmt);
            return results;
        }

        // 총 금액이 월 인출 한도 금액 보다 작으면, 실패
        if (toMonthMaxWithdrawal.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 인출 가능 금액이 최소 환전 금액을 넘었을 경우
            if (toMonthMaxWithdrawal.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", toMonthMaxWithdrawal.subtract(toAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toMonthMaxWithdrawal));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Monthly exchange limit exceeded.", locale));

            } else if (toMonthMaxWithdrawal.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) < 0) {
                results.put("status", Const.STATUS_FAILURE);

                alarmService.i("인출한도초과",
                        """
                                [월] 인출 한도를 초과하였습니다.
                                - To Currency : %s
                                - UserId : %s
                                - 최대 월간 인출 가능 금액 : %s
                                - 남아있는 인출 가능 금액 : %s
                                """.formatted(
                                toCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                toMonthMaxWithdrawal.toString(),
                                toMonthMaxWithdrawal.subtract(toAmtSum)
                        )
                );

                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toAmt));
                array.add(NumberFormat.getNumberInstance(locale).format(toMonthMaxWithdrawal));

                results.put("message", messageSource.getMessage("EXCHANGE_MONTH_LIMIT", array.toArray(), "Monthly exchange limit exceeded.", locale));
            }

            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);
        return results;
    }

    public Map<String, Object> validateWithdrawalDailyLimitForToCountry(Traveler traveler, BigDecimal toAmt,
                                                                        Country toCountry, Locale locale, ExchangeConfig exchangeConfig) {

        final boolean isStatic = !StringUtils.equals(toCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] daily = DateUtil.getDaily(CountryCode.of(toCountry.getCode()).getZoneId(), isStatic);

        String toCd = toCountry.getCode();

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("toCd", toCd);
        params.put("fromDate", daily[0]);
        params.put("toDate", daily[1]);

        Map<String, Object> sum = exchangeMapper.getWithdrawalToAmtSumByPeriod(params);
        // 월 인출 가능 금액
        BigDecimal toDayMaxWithdrawal = exchangeConfig.getToDayMaxWithdrawal();

        // 기간내 총 환전 및 송금 금액
        BigDecimal toAmtSum = (BigDecimal) sum.get("toAmtSum");
        // 총 환전 금액 + 요청 금액
        BigDecimal totalAmt = toAmtSum.add(toAmt);

        HashMap<String, Object> results = new HashMap<>();

        // 월 한도 금액 설정이 없으면 무조건 패스
        if (toDayMaxWithdrawal == null || toDayMaxWithdrawal.intValue() == 0) {
            results.put("status", Const.STATUS_SUCCESS);
            results.put("toAmt", toAmt);
            return results;
        }

        // 총 금액이 일 인출 한도 금액 보다 작으면, 실패
        if (toDayMaxWithdrawal.compareTo(totalAmt) < 0) {
            results.put("status", Const.STATUS_FAILURE);

            // 남아 있는 인출 가능 금액이 최소 환전 금액을 넘었을 경우
            if (toDayMaxWithdrawal.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) >= 0) {
                results.put("toAmt", toDayMaxWithdrawal.subtract(toAmtSum));

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toDayMaxWithdrawal));

                results.put("message", messageSource.getMessage("WITHDRAWAL_ATUO_EXCHANGE_MESSAGE", array.toArray(), "Daily exchange limit exceeded.", locale));

            } else if (toDayMaxWithdrawal.subtract(toAmtSum).compareTo(exchangeConfig.getToMinExchange()) < 0) {
                results.put("status", Const.STATUS_FAILURE);

                alarmService.i("인출한도초과",
                        """
                                [일] 인출 한도를 초과하였습니다.
                                - To Currency : %s
                                - UserId : %s
                                - 최대 일간 인출 가능 금액 : %s
                                - 남아있는 인출 가능 금액 : %s
                                """.formatted(
                                toCountry.getIso4217(),
                                traveler.getUserId().toString(),
                                toDayMaxWithdrawal.toString(),
                                toDayMaxWithdrawal.subtract(toAmtSum)
                        )
                );

                results.put("toAmt", BigDecimal.ZERO);
                ArrayList<String> array = new ArrayList<>();

                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(toDayMaxWithdrawal));
                array.add(toCountry.getEngName());
                array.add(CommDateTime.getTimeFormat(CountryCode.of(toCountry.getCode()).getZoneId(), locale));

                results.put("message", messageSource.getMessage("EXCHANGE_EXCEEDED_DAY_LIMIT_NEW", array.toArray(), "Daily exchange limit exceeded.", locale));
            }

            return results;
        }

        results.put("status", Const.STATUS_SUCCESS);
        results.put("toAmt", toAmt);
        return results;
    }
}
