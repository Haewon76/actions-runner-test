package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.CurrencyService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.application.impl.ExchangeServiceImpl;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeDepositReceipt;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.Convert;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.admin.property.JpPostProperties;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeReqResultVO;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeReqVO;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.INVALID_COUPON;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * Handles requests for the application home page.
 */
@Controller
public class TravelerExchangeController {

    private final Logger logger = LoggerFactory.getLogger(TravelerExchangeController.class);

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private PartnerServiceImpl withdrawalPartnerService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private AuthService authService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private Gson gsonPretty;

    @Autowired
    private JpPostProperties jpPostProperties;

    @Autowired
    private CouponUserService couponUserService;

    // -------------------------------------------------------------------------------
    // 9. 여행자 환전
    // -------------------------------------------------------------------------------

    /**
     * Get exchange configuration. A travelers can exchange or the exchange service is enable.
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/exchanges/exchange-configs", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeConfig(@RequestHeader("Authorization") String token,
                                    HttpServletRequest request, HttpServletResponse response) {

        String method = "getExchangeConfig()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: Invalid token.", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.debug("{}: API has been called.", method);

        List<ExchangeConfig> exchangeConfigs = countryService.getCanExchanageFeeRateList();

        ArrayList<ExchangeConfig> feeRatesWithMessage = new ArrayList<>();
        Locale locale = localeResolver.resolveLocale(request);
        for (ExchangeConfig f : exchangeConfigs) {
            String notice = messageSource.getMessage(f.getExchangeNotice(), null, f.getExchangeNotice(), locale);
            f.setExchangeNotice(notice);
            feeRatesWithMessage.add(f);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<Map<String, String>> obj = mapper.convertValue(feeRatesWithMessage, new TypeReference<List<Map<String, String>>>() {
        });

        voResult.setSuccessInfo(obj);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Get Exchange limit(min, max) with toCountry currency by fromCd and toCd
     *
     * @param token
     * @param country
     * @param toCd
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/countries/{country}/exchange-limit", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeLimitByFromCd(@RequestHeader("Authorization") String token,
                                           @PathVariable String country,
                                           @RequestParam String toCd,
                                           HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getCurrencyTarget(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // If login user then update token expired date
        authService.getUserAuthInfo(token);

        logger.info("getExchangeLimit(): country={}, toCd={}", country, toCd);

        try {
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(country, toCd);
            Country fromCountry = countryService.getCountry(country);
            Country toCountry = countryService.getCountry(toCd);
            Map<String, String> result = exchangeService.getExchangeLimit(fromCountry, toCountry, exchangeConfig);
            voResult.setSuccessInfo(result);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Get Currency with toCd's ISO4217. It is for calculating the exchange amount to convert USD.
     *
     * @param token
     * @param iso4217
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/currencies/sources/{iso4217}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeRateByISO4217(@RequestHeader("Authorization") String token,
                                           @PathVariable String iso4217,
                                           HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getExchangeRateByISO4217(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("getExchangeRateByISO4217()");

        try {
            List<CurrencyRate> currencyRates = currencyService.getCurrencyRates(iso4217);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            List<HashMap<String, Object>> object = mapper.convertValue(currencyRates, new TypeReference<List<HashMap<String, Object>>>() {
            });

            voResult.setSuccessInfo(object);

            Locale locale = localeResolver.resolveLocale(request);
            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 9.3.2 환전 금액 계산
    @GetMapping(value = "/traveler/v2/exchange/in-progress", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getLastestExchangeInProgress(@RequestHeader("Authorization") String token,
                                               HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("calcExchangeV2(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        String method = "getLastestExchangeInProgress()";

        logger.info(method);

        ExchangeReqResultVO requestExchangeResultVo = null;
        Exchange exchange = exchangeRepositoryService.getLatestExchangeInProgress(userId);
        if (exchange != null) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            requestExchangeResultVo = mapper.convertValue(exchange, ExchangeReqResultVO.class);

            BankAccount bankAccount = companyService.getBankAccountByBankAccountId(exchange.getBankAccountId());
            requestExchangeResultVo.setCountry(bankAccount.getCountry());
            requestExchangeResultVo.setBank_code(bankAccount.getBankCode());
            requestExchangeResultVo.setBank_name(bankAccount.getBankName());
            requestExchangeResultVo.setBranch_name(bankAccount.getBranchName());
            requestExchangeResultVo.setBank_account_no(bankAccount.getBankAccountNo());
            requestExchangeResultVo.setAccount_type(bankAccount.getAccountType());
            requestExchangeResultVo.setFirst_name(bankAccount.getFirstName());
            requestExchangeResultVo.setLast_name(bankAccount.getLastName());

            if (CountryCode.JP.getCode().equals(bankAccount.getCountry())) {
                if (jpPostProperties.bankName().equalsIgnoreCase(bankAccount.getBankName())) {
                    // jp Post일경우 계좌번호, 지점명 추가
                    requestExchangeResultVo.setJp_post_branch_name(jpPostProperties.branchName());
                    requestExchangeResultVo.setJp_post_account_no(jpPostProperties.accountNo());
                }
                requestExchangeResultVo.setJp_account_type("普通");
            }
        }

        voResult.setSuccessInfo(requestExchangeResultVo);

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    // http://localhost:58080/api/mobile/exchanges/my-exchanges?token=6.6.yskim
    // 기능: 9.2. travelerId에 대한 환전 정보들 읽기
    @PostMapping(value = "/json/mobile/exchanges/my-exchanges", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeByTravelerId(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("getExchangeByTravelerId(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);

        logger.info("getExchangeByTravelerId(): jsonStr={}", jsonStr);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        Integer page = Convert.objToIntDef(map.get("page"), Const.DEF_PAGE_NO);
        Integer size = Convert.objToIntDef(map.get("size"), Const.DEF_PAGE_SIZE);
        String sort = (String) map.get("sort");

        try {
            SearchResultVO obj = exchangeRepositoryService.getExchangeByTravelerId(userId, page, size);
            voResult.setSuccessInfo(obj);
            logger.debug("exchangeHistory={}", gsonPretty.toJson(obj));
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Check exchange enabled
     *
     * @param token
     * @param country
     * @param toCd
     * @param fromAmt
     * @param toAmt
     * @param exchangeRate
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/countries/{country}/services", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String checkExchangeServiceEnabled(@RequestHeader("Authorization") String token,
                                              @PathVariable String country,
                                              @RequestParam String toCd, @RequestParam BigDecimal fromAmt,
                                              @RequestParam BigDecimal toAmt, @RequestParam BigDecimal exchangeRate,
                                              HttpServletRequest request, HttpServletResponse response) {

        String method = "checkExchangeServiceEnabled()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        logger.info("{}: userId={}, country={}, toCd={}, fromAmt={}, toAmt={}, exchangeRate={}",
                method, userId, country, toCd, fromAmt, toAmt, exchangeRate);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: check token in access_token table : CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        Locale locale = localeResolver.resolveLocale(request);
        ArrayList<String> array = new ArrayList<>();

        try {
            String result = exchangeService.checkExchangeEnabled(userId, country, fromAmt, toCd, toAmt, exchangeRate);
            voResult.setSuccessInfo(result);
        } catch (CashmallowException e) {
            logger.debug(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            if (e.getOption() != null) {
                JSONObject jo = new JSONObject(e.getOption());
                array.add(jo.getString("iso4217"));
                String amtString = NumberFormat.getNumberInstance(locale).format(jo.get("amt"));
                array.add(amtString);
                String maxString = NumberFormat.getNumberInstance(locale).format(jo.get("max"));
                array.add(maxString);
            }
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), array.toArray(), voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Check exchange enabled, 쿠폰 적용 추가
     *
     * @param token
     * @param country
     * @param toCd
     * @param fromAmt
     * @param toAmt
     * @param exchangeRate
     * @param couponIssueId
     * @param couponUseYn
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/v2/countries/{country}/services", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String checkExchangeServiceEnabledV2(@RequestHeader("Authorization") String token,
                                                @PathVariable String country,
                                                @RequestParam String toCd, @RequestParam BigDecimal fromAmt,
                                                @RequestParam BigDecimal toAmt, @RequestParam BigDecimal exchangeRate,
                                                @RequestParam(required = false) Long couponIssueId,
                                                @RequestParam String couponUseYn,
                                                HttpServletRequest request, HttpServletResponse response) {

        String method = "checkExchangeServiceEnabledV2()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        logger.info("{}: userId={}, country={}, toCd={}, fromAmt={}, toAmt={}, exchangeRate={}, couponIssueId={}, couponUseYn={}",
                method, userId, country, toCd, fromAmt, toAmt, exchangeRate, couponIssueId, couponUseYn);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: check token in access_token table : CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        Locale locale = localeResolver.resolveLocale(request);
        ArrayList<String> array = new ArrayList<>();

        try {
            String result = exchangeService.checkExchangeEnabled(userId, country, fromAmt, toCd, toAmt, exchangeRate);
            voResult.setSuccessInfo(result);
        } catch (CashmallowException e) {
            logger.debug(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            if (e.getOption() != null) {
                JSONObject jo = new JSONObject(e.getOption());
                array.add(jo.getString("iso4217"));
                String amtString = NumberFormat.getNumberInstance(locale).format(jo.get("amt"));
                array.add(amtString);
                String maxString = NumberFormat.getNumberInstance(locale).format(jo.get("max"));
                array.add(maxString);
            }
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), array.toArray(), voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * V3 환전 요청
     * 쿠폰 개선 적용 2025.03.06
     **/
    @PostMapping(value = {"/traveler/exchange", "/traveler/v3/exchange"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestExchangeV3(@RequestHeader("Authorization") String token,
                                  @RequestBody String requestBody,
                                  HttpServletRequest request, HttpServletResponse response) throws CashmallowException {
        String method = "requestExchangeV1,V3()";

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);

        logger.info("{}: userId={}, requestBody={}", method, userId, requestBody);

        ExchangeReqVO exchangeReqVO = (ExchangeReqVO) JsonStr.toObject(ExchangeReqVO.class.getName(), requestBody);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Exchange exchangeParam = mapper.convertValue(exchangeReqVO, Exchange.class);

        // 쿠폰 사용가능 여부를 다시 한 번 체크
        if (exchangeParam.getCouponUserId() != null && exchangeParam.getCouponUserId() != -1L) {
            CouponIssueUser couponIssueUser = couponUserService.getCouponUserById(exchangeParam.getCouponUserId());
            if (!AvailableStatus.AVAILABLE.name().equals(couponIssueUser.getAvailableStatus())) {
                throw new CashmallowException(INVALID_COUPON);
            }
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        try {
            BigDecimal fromMoney = exchangeReqVO.getFrom_money();
            BigDecimal feePerAmt = exchangeReqVO.getFee_per_amt();
            BigDecimal feeRateAmt = exchangeReqVO.getFee_rate_amt();

            Long couponUserId = exchangeReqVO.getCoupon_user_id();

            BigDecimal fromAmt = fromMoney.add(feeRateAmt).add(feePerAmt);

            if (exchangeReqVO.getFrom_amt().compareTo(fromAmt) != 0) {
                logger.error("{}: 환전 신청된 금액이 잘못 되었습니다. fromAmt={}, fromMoney={}, feePerAmt={}, feeRateAmt={}",
                        method, exchangeReqVO.getFrom_amt(), fromMoney, feePerAmt, feeRateAmt);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            final BigDecimal originExchangeRate = exchangeParam.getExchangeRate();
            final BigDecimal originExchangeFromAmt = exchangeParam.getFromAmt();
            final BigDecimal originFee = exchangeParam.getFee();
            Exchange exchange = exchangeService.requestExchangeV3(userId, exchangeParam, couponUserId);

            ExchangeReqResultVO resultVO = mapper.convertValue(exchange, ExchangeReqResultVO.class);
            BankAccount bankAccount = companyService.getBankAccountByBankAccountId(exchange.getBankAccountId());
            resultVO.setCountry(bankAccount.getCountry());
            resultVO.setBank_code(bankAccount.getBankCode());
            resultVO.setBank_name(bankAccount.getBankName());
            resultVO.setBranch_name(bankAccount.getBranchName());
            resultVO.setBank_account_no(bankAccount.getBankAccountNo());
            resultVO.setAccount_type(bankAccount.getAccountType());
            resultVO.setFirst_name(bankAccount.getFirstName());
            resultVO.setLast_name(bankAccount.getLastName());

            if (CountryCode.JP.getCode().equals(bankAccount.getCountry())) {
                if (jpPostProperties.bankName().equalsIgnoreCase(bankAccount.getBankName())) {
                    // jp Post일경우 계좌번호, 지점명 추가
                    resultVO.setJp_post_branch_name(jpPostProperties.branchName());
                    resultVO.setJp_post_account_no(jpPostProperties.accountNo());
                }
                resultVO.setJp_account_type("普通");
            }

            voResult.setSuccessInfo(resultVO);
            if (!originExchangeRate.equals(exchange.getExchangeRate()) || originExchangeFromAmt.compareTo(exchange.getFromAmt()) != 0
                    || originFee.compareTo(exchange.getFee()) != 0) {
                voResult.setStatus(Const.STATUS_CHANGED_EXCHANGE_RATE);
                voResult.setMessage(messageSource.getMessage(MsgCode.EXCHANGE_CHANGED_EXCHANGE_RATE, null, voResult.getMessage(), locale));
            }

            // 일본환전 데이터 전송
            if (CountryCode.JP.getCode().equals(exchange.getFromCd())) {
                String bankName = companyService.getBankAccountByBankAccountId(Math.toIntExact(exchange.getBankAccountId())).getBankName();

                Long couponIssueSyncId = null;
                if (exchange.getCouponUserId() != null && exchange.getCouponUserId() != -1L) {
                    CouponIssueUser couponIssueUser = couponUserService.getCouponUserById(exchange.getCouponUserId());
                    exchange.setCouponUserId(couponIssueUser.getId());
                    exchange.setCouponDiscountAmt(couponIssueUser.getCouponUsedAmount());
                    couponIssueSyncId = couponIssueUser.getCouponIssueId();
                }
                globalQueueService.sendExchange(exchange, couponIssueSyncId, bankName);
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            ArrayList<String> array = new ArrayList<>();
            if (!StringUtils.isEmpty(e.getOption())) {
                JSONObject jo = new JSONObject(e.getOption());
                array.add(jo.getString("iso4217"));
                String amtString = NumberFormat.getNumberInstance(locale).format(jo.get("amt"));
                array.add(amtString);
                String maxString = NumberFormat.getNumberInstance(locale).format(jo.get("max"));
                array.add(maxString);
            }

            if (e.getMessage().equals(MsgCode.PREVIOUS_EXCHANGE_IN_PROGRESS)) {
                voResult.setStatus(e.getMessage());
            }

            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), array.toArray(), voResult.getMessage(), locale));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    // 기능: 9.3. 여행자 환전 신청
    @PostMapping(value = "/kr/traveler/exchange", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestExchangeKr(@RequestHeader("Authorization") String token,
                                    @RequestBody String requestBody,
                                    HttpServletRequest request, HttpServletResponse response) {
        String method = "requestExchange()";

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);

        logger.info("{}: userId={}, requestBody={}", method, userId, requestBody);

        ExchangeReqVO exchangeReqVO = (ExchangeReqVO) JsonStr.toObject(ExchangeReqVO.class.getName(), requestBody);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        Exchange exchangeParam = mapper.convertValue(exchangeReqVO, Exchange.class);

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        try {
            BigDecimal fromMoney = exchangeReqVO.getFrom_money();
            BigDecimal feePerAmt = exchangeReqVO.getFee_per_amt();
            BigDecimal feeRateAmt = exchangeReqVO.getFee_rate_amt();

            BigDecimal fromAmt = fromMoney.add(feeRateAmt).add(feePerAmt);

            if (exchangeReqVO.getFrom_amt().compareTo(fromAmt) != 0) {
                logger.error("{}: 환전 신청된 금액이 잘못 되었습니다. fromAmt={}, fromMoney={}, feePerAmt={}, feeRateAmt={}",
                        method, exchangeReqVO.getFrom_amt(), fromMoney, feePerAmt, feeRateAmt);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            Exchange exchange = exchangeService.requestExchangeKr(userId, exchangeParam);
            ExchangeReqResultVO resultVO = mapper.convertValue(exchange, ExchangeReqResultVO.class);

            voResult.setSuccessInfo(resultVO);

        } catch (CashmallowException e) {

            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            ArrayList<String> array = new ArrayList<>();
            if (!StringUtils.isEmpty(e.getOption())) {
                JSONObject jo = new JSONObject(e.getOption());
                array.add(jo.getString("iso4217"));
                String amtString = NumberFormat.getNumberInstance(locale).format(jo.get("amt"));
                array.add(amtString);
                String maxString = NumberFormat.getNumberInstance(locale).format(jo.get("max"));
                array.add(maxString);
            }

            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), array.toArray(), voResult.getMessage(), locale));
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * V6 환전 계산
     * 쿠폰 개선 적용 2025.03.06
     * 환전 계산 로직 추가, 일, 월, 연간 제한 추가. 사이드 이팩트를 피하기 위해서 controller 생성
     *
     * @param token
     * @param fromCd
     * @param toCd
     * @param fromMoney
     * @param toMoney
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = {"/traveler/v5/exchange/calculate", "/traveler/v6/exchange/calculate"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String calcExchangeV6(@RequestHeader("Authorization") String token,
                                 @RequestParam String fromCd, @RequestParam String toCd,
                                 @RequestParam BigDecimal fromMoney, @RequestParam BigDecimal toMoney,
                                 @RequestParam(required = false) Long couponUserId,
                                 @RequestParam String couponUseYn,
                                 HttpServletRequest request, HttpServletResponse response) {

        Locale locale = localeResolver.resolveLocale(request);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("calcExchangeV6(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("calcExchangeV6() : fromCd={}, toCd={}, fromMoney={}, toMoney={}", fromCd, toCd, fromMoney, toMoney);

        ApiResultVO voResult = new ApiResultVO();
        try {

            ExchangeCalcVO exchangeCalcVO = null;

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            if (userId == Const.NO_USER_ID || traveler == null) {
                exchangeCalcVO = exchangeService.calcExchangeAnonymous(fromCd, toCd, fromMoney, toMoney);
                voResult.setSuccessInfo(exchangeCalcVO);
            } else {
                exchangeCalcVO = exchangeService.calcExchangeV6(fromCd, toCd, fromMoney, toMoney, traveler, couponUserId, couponUseYn);
                voResult.setSuccessInfo(exchangeCalcVO);
                logger.info("calcExchangeV6() : exchangeCalcVO={}", exchangeCalcVO);
            }

            if (exchangeCalcVO != null && !StringUtils.isEmpty(exchangeCalcVO.getStatus())) {
                voResult.setStatus(exchangeCalcVO.getStatus());
                voResult.setMessage(exchangeCalcVO.getMessage());
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * V2 마지막 환전 신청 취소
     * 쿠폰 개선 적용 2025.03.07
     **/
    // 기능: 9.4. 마지막 환전 신청을 취소한다.
    @PostMapping(value ={ "/json/mobile/exchanges/traveler-cancel", "/exchanges/v2/traveler-cancel"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelLastExchangeReqByTravelerIdV2(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        Long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            logger.info("cancelLastExchangeReqByTravelerId(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("cancelLastExchangeReqByTravelerId(): userId={}", userId);
        try {
            exchangeService.cancelLastExchangeReqByTravelerIdV2(userId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 입금 정보 등록 시 영수증 사진 업로드
    @PostMapping(value = "/traveler/exchanges/{exchangeId}/deposit-receipts", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String uploadExchangeReceiptsPhoto(@RequestHeader("Authorization") String token,
                                              @PathVariable long exchangeId,
                                              @RequestPart("picture") List<MultipartFile> pictureLists,
                                              HttpServletRequest request, HttpServletResponse response) {

        String method = "uploadExchangeReceiptsPhoto";

        for (MultipartFile picture : pictureLists) {
            String filename = (picture != null ? picture.getOriginalFilename() : null);
            logger.info("{}: picture={}", method, filename);
        }

        Long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            logger.info("updateReceiptPhoto(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        try {
            for (MultipartFile picture : pictureLists) {
                exchangeService.uploadExchangeReceiptPhoto(exchangeId, picture);
            }

            List<ExchangeDepositReceipt> receipts = exchangeService.getExchangeDepositReceiptList(exchangeId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
            });

            voResult.setSuccessInfo(obj);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/traveler/exchanges/{exchangeId}/deposit-receipts", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeReceiptPhotos(@RequestHeader("Authorization") String token,
                                           @PathVariable long exchangeId,
                                           HttpServletRequest request, HttpServletResponse response) {

        String method = "getExchangeReceiptPhotos()";

        logger.info("{}: exchangeId={}", method, exchangeId);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        List<ExchangeDepositReceipt> receipts = exchangeService.getExchangeDepositReceiptList(exchangeId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
        });

        voResult.setSuccessInfo(obj);

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/traveler/v2/exchanges/setExchangeTravelerBankInfo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String setExchangeTravelerBankInfoV2(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        Long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            logger.info("setExchangeTravelerBankInfo(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("setExchangeTravelerBankInfo(): jsonStr={}", jsonStr);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String trBankName = (String) map.get("tr_bank_name");
        String trAccountName = (String) map.get("tr_account_name");
        String trAccountNo = (String) map.get("tr_account_no");
        BigDecimal trFromAmt = new BigDecimal(map.get("tr_from_amt").toString());

        Timestamp trDepositDate = null;
        if (Convert.objToLongDef(map.get("tr_deposit_date"), (long) 0) != 0) {
            trDepositDate = CommDateTime.objToTimestamp(map.get("tr_deposit_date"));
        }

        try {
            Exchange exchange = exchangeService.setExchangeTravelerBankInfo(userId, trBankName, trAccountName,
                    trAccountNo, trFromAmt, trDepositDate);

            // from JP는 매핑을 Global-JP에서 시행, 그외는 직접 진행
            if (!CountryCode.JP.getCode().equals(exchange.getFromCd())) {
                companyService.tryAutoMappingForExchange(exchange.getId());
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> obj = mapper.convertValue(exchange, new TypeReference<Map<String, Object>>() {
            });

            BankAccount bankAccount = companyService.getBankAccountByBankAccountId(exchange.getBankAccountId());

            obj.put("account_type", bankAccount.getAccountType());
            obj.put("bank_code", bankAccount.getBankCode());
            obj.put("bank_name", bankAccount.getBankName());
            obj.put("branch_name", bankAccount.getBranchName());
            obj.put("first_name", bankAccount.getFirstName());
            obj.put("last_name", bankAccount.getLastName());
            obj.put("bank_account_no", bankAccount.getBankAccountNo());

            if (CountryCode.JP.getCode().equals(bankAccount.getCountry())) {
                if (jpPostProperties.bankName().equalsIgnoreCase(bankAccount.getBankName())) {
                    // jp Post일경우 계좌번호, 지점명 추가
                    obj.put("jp_post_account_no", jpPostProperties.accountNo());
                    obj.put("jp_post_branch_name", jpPostProperties.branchName());
                }
                obj.put("jp_account_type", "普通");
            }

            voResult.setSuccessInfo(obj);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/traveler/v2/exchanges/{exchange_id}/record", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTravelerExchangeRecord(@RequestHeader("Authorization") String token,
                                            @PathVariable("exchange_id") Long exchangeId,
                                            HttpServletRequest request, HttpServletResponse response) {

        String method = "getTravelerExchangeRecord()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        Map<String, Object> reponseMap = new HashMap<>();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        long travelerId = traveler.getId();

        List<CashOut> cashouts = cashoutRepositoryService.getCashOutByExchangeId(exchangeId.toString(), traveler.getId());

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        reponseMap.put("exchange_id", exchangeId);

        ArrayList<Map<String, Object>> cashoutList = new ArrayList<>();
        for (CashOut cashout : cashouts) {
            if (!cashout.getTravelerId().equals(travelerId)) {
                logger.error("{}: travelerId does not match. travelerId={}, cashout.getTravelerId()={}",
                        method, travelerId, cashout.getTravelerId());
                return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
            }

            Map<String, Object> cashoutMap = mapper.convertValue(cashout, new TypeReference<Map<String, Object>>() {
            });
            cashoutMap.put("cash_out_id", cashout.getId());

            WithdrawalPartner withdrawalPartner = withdrawalPartnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashout.getWithdrawalPartnerId());
            Map<String, Object> withdrawalPartnerMap = mapper.convertValue(withdrawalPartner, new TypeReference<Map<String, Object>>() {
            });
            cashoutMap.put("withdrawal_partner", withdrawalPartnerMap);

            cashoutList.add(cashoutMap);
        }

        if (!cashoutList.isEmpty()) {
            reponseMap.put("cash_out", cashoutList);

        }

        List<NewRefund> refunds = refundRepositoryService.getNewRefundNotCancelByExchangeId(exchangeId.toString(), traveler.getId());
        ArrayList<Map<String, Object>> refundList = new ArrayList<>();
        for (NewRefund refund : refunds) {
            if (!refund.getTravelerId().equals(travelerId)) {
                logger.error("{}: travelerId does not match. travelerId={}, refund.getTravelerId()={}",
                        method, travelerId, refund.getTravelerId());
                return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
            }

            Map<String, Object> refundMap = mapper.convertValue(refund, new TypeReference<Map<String, Object>>() {
            });
            refundList.add(refundMap);
        }

        if (!refundList.isEmpty()) {
            reponseMap.put("refund", refundList);
        }

        ApiResultVO resultVO = new ApiResultVO();
        resultVO.setSuccessInfo(reponseMap);

        return JsonStr.toJsonString(resultVO, response);
    }
}
