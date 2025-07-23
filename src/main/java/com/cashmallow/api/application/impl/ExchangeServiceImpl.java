package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.company.CompanyMapper;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryMapper;
import com.cashmallow.api.domain.model.country.CurrencyRate;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import com.cashmallow.api.domain.model.exchange.*;
import com.cashmallow.api.domain.model.exchange.Exchange.ExStatus;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.admin.dto.AdminExchangeAskVO;
import com.cashmallow.api.interfaces.admin.dto.BankAccountVO;
import com.cashmallow.api.interfaces.admin.dto.MappingPinRegVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponSystemManagementService;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.coupon.dto.CouponCalcResponse;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;
import com.cashmallow.api.interfaces.dbs.property.DbsProperties;
import com.cashmallow.api.interfaces.edd.UserEddService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.common.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class ExchangeServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ExchangeServiceImpl.class);

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private CurrencyService currencyService;

    @Autowired
    private FileService fileService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CountryMapper countryMapper;

    @Autowired
    private CompanyMapper companyMapper;

    @Autowired
    private MappingMapper mappingMapper;

    @Autowired
    private ExchangeMapper exchangeMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserEddService userEddService;

    @Autowired
    private DbsProperties dbsProperties;

    @Value("${openbank.bankAccountId}")
    private int openbankAccountId;

    @Autowired
    private CouponSystemManagementService couponSystemManagementService;
    @Autowired
    private CouponMobileServiceV2 couponMobileService;
    @Autowired
    private CouponUserService couponUserService;

    @Autowired
    private ExchangeCalculateServiceImpl exchangeCalculateService;

    @Autowired
    private LimitCheckService limitCheckService;

    @Autowired
    private GlobalQueueService globalQueueService;


    public void cancelExchangeByAdmin(Long exchangeId) throws CashmallowException {
        String method = "cancelExchangeByAdmin()";

        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);

        logger.info("{}: exchangeId={}, exStatus={}", method, exchange.getId().toString(), exchange.getExStatus());

        if (exchange.getExStatus().equals(ExStatus.OP.name()) || exchange.getExStatus().equals(ExStatus.DR.name())) {
            cancelExchangeByCashmallow(exchange, method);
        } else {
            String errorMsg = method + ": Exchange Status is not OP or DR. status=" + exchange.getExStatus();
            logger.error(errorMsg);
            throw new CashmallowException(errorMsg);
        }

    }

    public void reregisterExchangeReceiptPhoto(Exchange exchange) throws CashmallowException {
        String method = "reregisterExchangeReceiptPhoto()";

        logger.info("{}: exchangeId={}, exStatus={}", method, exchange.getId().toString(), exchange.getExStatus());

        if (!exchange.getExStatus().equals(ExStatus.OP.name())) {
            String errorMsg = method + ": Exchange Status is not OP." + exchange.getExStatus();
            logger.error(errorMsg);
            throw new CashmallowException(errorMsg);
        }

        exchange.setExStatus(exchange.getStatus());
        exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        exchange.setFcmYn("Y");
        exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        final int affectedRows = exchangeRepositoryService.updateExchange(exchange);

        if (affectedRows == 1) {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            // 메세지가 존재하는 경우 푸시 알람에 메세지를 포함하여 발송한다
            String slackMessage = null;
            if (StringUtils.isNotBlank(exchange.getMessage())) {
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.EX, FcmEventValue.valueOf(exchange.getStatus()), exchange.getId(), exchange.getMessage());
                slackMessage = ", 메세지: " + exchange.getMessage();
            } else {
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.EX, FcmEventValue.valueOf(exchange.getStatus()), exchange.getId());
            }

            notificationService.sendEmailReRegisterReceipt(user);

            String msg = "[ADMIN] 환전 거래번호:" + exchange.getId() +
                    "\n유저ID:" + user.getId() + ", 은행:" + exchange.getBankName() + ", 이름:" + exchange.getTrAccountName() + ", 코드:" + exchange.getTrAccountNo() + ", 금액:" + exchange.getFromAmt() +
                    "\n국가:" + exchange.getFromCd() + ", 금액:" + exchange.getFromAmt() + ", 수수료:" + exchange.getFee() + slackMessage;
            alarmService.aAlert("DR".equals(exchange.getStatus()) ? "(영수증) 재등록 요청(DR)" : "(송금실패) 재등록 요청(RR)", msg, user);

        } else {
            logger.error("{}: Failed to update exchange data. travelerId={}, exchange.getTravelerId()={}", method,
                    exchange.getTravelerId(), exchange.getTravelerId());
            throw new CashmallowException("Failed to update exchange data. travelerId=" + exchange.getTravelerId()
                    + ", exchange.getTravelerId()=" + exchange.getTravelerId());
        }
    }

    /**
     * travelerId의 마지막 환전 신청 정보를 읽는다.
     *
     * @param userId
     * @return
     * @throws CashmallowException
     */
    @Deprecated
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Exchange getLatestExchangeOpStatusInfo(Long userId) {
        Exchange exchange = null;
        String method = "getLatestExchangeOpStatusInfo()";
        logger.info("{}: userId={}", method, userId);
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler != null) {
            Long travelerId = traveler.getId();
            // 1. 여행자의 마지막 환전 환전 신청 정보를 읽는다.
            List<Exchange> exchanges = exchangeMapper.getExchangeOpListByTravelerId(travelerId);
            if (exchanges != null && !exchanges.isEmpty()) {
                exchange = exchanges.get(0);

                List<ExchangeDepositReceipt> receipts = exchangeMapper.getExchangeDepositReceiptList(exchange.getId());
                if (receipts != null && !receipts.isEmpty()) {
                    String trReceiptPhoto = receipts.get(receipts.size() - 1).getReceiptPhoto();
                    exchange.setTrReceiptPhoto(trReceiptPhoto);
                }
            }
        } else {
            logger.info("{}: traveler 정보가 등록되지 않았습니다. userId={}", method, userId);
        }

        return exchange;
    }

    /**
     * 관리자용 환전 리스트 조회
     *
     * @param pvo
     * @return
     */
    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO findAdminExchange(AdminExchangeAskVO pvo) {

        int size = pvo.getSize() != null ? pvo.getSize() : Const.DEF_PAGE_SIZE;
        int page = (pvo.getStart_row() + size) / size - 1;

        pvo.setPage(page);
        pvo.setSize(size);

        SearchResultVO searchResult = new SearchResultVO(page, size, pvo.getSort());

        int totalCount = exchangeMapper.countAdminExchange(pvo);
        List<Object> vos = exchangeMapper.findAdminExchange(pvo);
        List<Object> exchangeResult = new ArrayList<>();

        Map<String, Map<String, Object>> transformedMap = new HashMap<>();
        if ("CF".equals(pvo.getEx_status())) {
            List<Map<String, Object>> adminExchangeSubQuery1s = exchangeMapper.findAdminExchangeSubQuery1(pvo);
            // 정제
            for (Map<String, Object> row : adminExchangeSubQuery1s) {
                String key = String.valueOf(row.get("exchange_id"));
                transformedMap.put(key, row);
            }
        }

        for (Object obj : vos) {
            Map<String, String> exchange = (Map) obj;
            exchange.put("transaction_type", ServiceType.EXCHANGE.name());
            exchange.put("first_name", securityService.decryptAES256(exchange.get("first_name")));
            exchange.put("last_name", securityService.decryptAES256(exchange.get("last_name")));
            exchange.put("email", securityService.decryptAES256(exchange.get("email")));
            exchange.put("tr_account_no", securityService.decryptAES256(exchange.get("tr_account_no")));
            exchange.put("tr_account_name", securityService.decryptAES256(exchange.get("tr_account_name")));

            if ("CF".equals(pvo.getEx_status())) {
                String key = String.valueOf(exchange.get("exchange_id"));
                if (transformedMap.containsKey(key)) {
                    exchange.putAll((Map) transformedMap.get(key));
                }
            }
            exchangeResult.add(exchange);
        }


        searchResult.setResult(vos, totalCount, page);

        return searchResult;

    }

    /**
     * 환전 금액 계산
     *
     * @param fromCountry
     * @param toCountry
     * @return
     * @throws CashmallowException
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, String> getExchangeLimit(Country fromCountry, Country toCountry, ExchangeConfig exchangeConfig) throws CashmallowException {

        String method = "getExchangeLimit()";
        Map<String, String> result = new HashMap<>();

        logger.info("{}: fromCd={}, toCd={}", method, fromCountry.getCode(), toCountry.getCode());

        if (fromCountry.equals(toCountry)) {
            return result;
        }

        BigDecimal feeRate = exchangeConfig.getFeeRateExchange();

        Map<String, Object> params = new HashMap<>();
        params.put("source", toCountry.getIso4217());
        params.put("target", fromCountry.getIso4217());
        BigDecimal currencyRate = currencyService.getCurrencyRate(params).getRate();

        BigDecimal min = exchangeConfig.getToMinExchange();
        BigDecimal max = exchangeConfig.getToMaxExchange();

        if (min.compareTo(toCountry.getUnitScale()) < 0) {
            min = toCountry.getUnitScale();
        }

        // from 국가의 fromMax환전값을 환율로 계산해서 toMax를 구한뒤 to국가의 맥스와 비교해서 둘중 작은 것으로 선택함.
        BigDecimal fromMax = exchangeCalculateService.getFromMaxAmountToCurrency(feeRate, exchangeConfig.getFromMaxExchange(), toCountry.getUnitScale(), currencyRate);

        if (fromMax.compareTo(max) < 0) {
            max = fromMax;
        }

        result.put("min", min.toString());
        result.put("max", max.toString());

        return result;
    }

    /**
     * 환전 금액 계산, 미로그인시 최소, 최대값 메세지 추가
     *
     * @param fromCd
     * @param toCd
     * @param fromMoney (traveler's exchange fee included)
     * @param toMoney
     * @return
     * @throws CashmallowException
     */
    public ExchangeCalcVO calcExchangeAnonymous(String fromCd, String toCd, BigDecimal fromMoney, BigDecimal toMoney)
            throws CashmallowException {

        String method = "calcExchange(): ";

        Locale locale = LocaleContextHolder.getLocale();

        logger.info("{}: fromCd={}, toCd={}, fromMoney={}, toMoney={}", method, fromCd, toCd, fromMoney, toMoney);

        try {
            Map<String, Object> params = new HashMap<>();
            ExchangeCalcVO vo = new ExchangeCalcVO();

            params.put("code", toCd);
            Country toCountry = countryService.getCountryList(params).get(0);

            params.put("code", fromCd);
            Country fromCountry = countryService.getCountryList(params).get(0);

            // source 와 target 이 바뀐 것이 아님. to_money 기준으로 환전 금액을 계산하고 있어서 이렇게 함.
            // 즉, to_money 가 10000 원이면 from_money 는 10000 * 적용환율
            // 적용환율 : 기준환율에서 일정 비율로 조정된 환율.
            params.clear();
            params.put("source", toCountry.getIso4217());
            params.put("target", fromCountry.getIso4217());
            CurrencyRate currencyRate = currencyService.getCurrencyRate(params);

            // 수수료 정보 조회
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

            fromMoney = fromMoney == null ? BigDecimal.ZERO : fromMoney;

            toMoney = toMoney == null ? BigDecimal.ZERO : toMoney;

            BigDecimal mappingInc = fromCountry.getMappingInc();
            BigDecimal unitScale = toCountry.getUnitScale();
            BigDecimal baseRate = currencyRate.getRate();
            BigDecimal spreadRate= exchangeCalculateService.getSpreadRate(baseRate, exchangeConfig.getFeeRateExchange());

            Map<String, String> exchangeLimit = getExchangeLimit(fromCountry, toCountry, exchangeConfig);
            BigDecimal minExchange = BigDecimal.ZERO;
            BigDecimal maxExchange = BigDecimal.ZERO;
            if (exchangeLimit.get("max") != null && exchangeLimit.get("min") != null) {
                maxExchange = new BigDecimal(exchangeLimit.get("max"));
                minExchange = new BigDecimal(exchangeLimit.get("min"));
            }

            if (fromMoney.compareTo(BigDecimal.ZERO) > 0 && toMoney.equals(BigDecimal.ZERO)) {
                toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP);
            }

            if (toMoney.compareTo(minExchange) < 0) {

                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchange));

                if (!(fromMoney.compareTo(BigDecimal.ZERO) == 0 && toMoney.compareTo(BigDecimal.ZERO) == 0)) {
                    vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                    vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));
                }
                toMoney = minExchange;
            }

            if (toMoney.compareTo(maxExchange) > 0) {
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchange));

                vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                toMoney = maxExchange;
            }

            toMoney = toMoney.divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);

            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            BigDecimal feeRateAmt = exchangeCalculateService.getFeeRateAmt(toMoney, baseRate, spreadRate, mappingInc);
            BigDecimal feePerAmt = getFeePerAmt(fromCd, toCd, toMoney, exchangeConfig.getFeePerExchange());

            BigDecimal fee = feeRateAmt.add(feePerAmt);

            vo.setFrom_cd(fromCd);
            vo.setTo_cd(toCd);
            vo.setFrom_money(fromMoney);
            vo.setTo_money(toMoney);
            vo.setFee(fee);
            vo.setFee_rate(exchangeConfig.getFeeRateExchange());
            vo.setFee_per_amt(feePerAmt);
            vo.setFee_rate_amt(feeRateAmt);
            vo.setExchange_rate(spreadRate);
            vo.setBase_exchange_rate(baseRate);

            logger.info("{}: vo={}", method, vo);
            return vo;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * V6 쿠폰 기능 개선 추가
     * 환전 금액 계산, 일-월-연간 Limit 추가, User EDD Limit 추가, coupon 정보 계산 추가
     *
     * @param fromCd
     * @param toCd
     * @param fromMoney    (traveler's exchange fee included)
     * @param toMoney
     * @param traveler
     * @param couponUserId
     * @param couponUseYn  쿠폰 사용 여부, Y일때 couponUserId가 null 이면 자동으로 쿠폰 추천.
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public ExchangeCalcVO calcExchangeV6(String fromCd, String toCd, BigDecimal fromMoney, BigDecimal toMoney, Traveler traveler, Long couponUserId, String couponUseYn)
            throws CashmallowException {
        String method = "calcExchangeV5,V6(): ";

        logger.info("{}: fromCd={}, toCd={}, fromMoney={}, toMoney={}, couponUserId={}", method, fromCd, toCd, fromMoney, toMoney, couponUserId);

        Locale locale = LocaleContextHolder.getLocale();
        Long userId = traveler.getUserId();

        try {
            // Traveler traveler = travelerService.getTravelerByUserId(userId);

            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

            Map<String, Object> params = new HashMap<>();

            params.put("code", toCd);
            Country toCountry = countryService.getCountryList(params).get(0);

            params.put("code", fromCd);
            Country fromCountry = countryService.getCountryList(params).get(0);

            // source 와 target 이 바뀐 것이 아님. to_money 기준으로 환전 금액을 계산하고 있어서 이렇게 함.
            // 즉, to_money 가 10000 원이면 from_money 는 10000 * 적용환율
            // 적용환율 : 기준환율에서 일정 비율로 조정된 환율.
            params.clear();
            params.put("source", toCountry.getIso4217());
            params.put("target", fromCountry.getIso4217());
            CurrencyRate currencyRate = currencyService.getCurrencyRate(params);

            BigDecimal mappingInc = fromCountry.getMappingInc();
            BigDecimal unitScale = toCountry.getUnitScale();
            BigDecimal baseRate = currencyRate.getRate();
            BigDecimal spreadRate = exchangeCalculateService.getSpreadRate(baseRate, exchangeConfig.getFeeRateExchange());

            if (fromMoney.compareTo(BigDecimal.ZERO) > 0 && toMoney.equals(BigDecimal.ZERO)) {
                toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP);
            }

            // Validation 체크를 위해서 fromMoney를 셋팅해준다. 밑에서 최종 toMoney가 결정되면 다시 계산해준다.
            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            Map<String, String> exchangeLimit = getExchangeLimit(fromCountry, toCountry, exchangeConfig);
            BigDecimal maxExchange = new BigDecimal(exchangeLimit.get("max"));
            BigDecimal minExchange = new BigDecimal(exchangeLimit.get("min"));

            ExchangeCalcVO vo = new ExchangeCalcVO();

            // 금액 셋팅 후...최소 인출 금액을 넘지 않았을 경우 셋팅 해준다.
            if (toMoney.compareTo(minExchange) < 0) {
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchange));

                vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));

                toMoney = exchangeConfig.getToMinExchange();
                fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);
            }

            Map<String, Object> walletFromCountryLimit = limitCheckService.validateWalletLimitForFromCountry(traveler, fromMoney, fromCountry, toCd, locale, exchangeConfig);

            if (StringUtils.equals((String) walletFromCountryLimit.get("status"), Const.STATUS_FAILURE)) {
                if (walletFromCountryLimit.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.WALLET_LIMIT_STATUS);
                    vo.setMessage((String) walletFromCountryLimit.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) walletFromCountryLimit.get("message"));
                    fromMoney = (BigDecimal) walletFromCountryLimit.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            Map<String, Object> walletToCountryLimit = limitCheckService.validateWalletLimitForToCountry(traveler, toMoney, toCd, locale, exchangeConfig);

            if (StringUtils.equals((String) walletToCountryLimit.get("status"), Const.STATUS_FAILURE)) {
                if (walletToCountryLimit.get("toAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.WALLET_LIMIT_STATUS);
                    vo.setMessage((String) walletToCountryLimit.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) walletToCountryLimit.get("message"));
                    toMoney = (BigDecimal) walletToCountryLimit.get("toAmt");
                    fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);
                }
            }

            Map<String, Object> annualLimitForFC = limitCheckService.validateAnnualLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);
            Map<String, Object> monthLimitForFC = limitCheckService.validateMonthLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);
            Map<String, Object> dayLimitForFC = limitCheckService.validateDayLimitForFromCountry(traveler, fromMoney, fromCountry, locale, exchangeConfig);

            if (StringUtils.equals((String) annualLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (annualLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) annualLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) annualLimitForFC.get("message"));
                    fromMoney = (BigDecimal) annualLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            if (StringUtils.equals((String) monthLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (monthLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) monthLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) monthLimitForFC.get("message"));
                    fromMoney = (BigDecimal) monthLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            if (StringUtils.equals((String) dayLimitForFC.get("status"), Const.STATUS_FAILURE)) {
                if (dayLimitForFC.get("fromAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) dayLimitForFC.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) dayLimitForFC.get("message"));
                    fromMoney = (BigDecimal) dayLimitForFC.get("fromAmt");
                    toMoney = fromMoney.divide(baseRate, 9, RoundingMode.HALF_UP)
                            .divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);
                }
            }

            Map<String, Object> toCountryLimit = limitCheckService.validateLimitForToCountry(traveler, toMoney, toCountry, locale, false, exchangeConfig);

            if (StringUtils.equals((String) toCountryLimit.get("status"), Const.STATUS_FAILURE)) {
                if (toCountryLimit.get("toAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) toCountryLimit.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) toCountryLimit.get("message"));
                    toMoney = (BigDecimal) toCountryLimit.get("toAmt");
                }
            }

            Map<String, Object> toCountryMonthWithdrawalLimit = limitCheckService.validateWithdrawalMonthLimitForToCountry(traveler, toMoney, toCountry, locale, exchangeConfig);
            Map<String, Object> toCountryDailyWithdrawalLimit = limitCheckService.validateWithdrawalDailyLimitForToCountry(traveler, toMoney, toCountry, locale, exchangeConfig);

            if (StringUtils.equals((String) toCountryMonthWithdrawalLimit.get("status"), Const.STATUS_FAILURE)) {
                if (toCountryMonthWithdrawalLimit.get("toAmt") == BigDecimal.ZERO) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) toCountryMonthWithdrawalLimit.get("message"));
                    return vo;
                } else {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) toCountryMonthWithdrawalLimit.get("message"));
                    toMoney = (BigDecimal) toCountryMonthWithdrawalLimit.get("toAmt");
                }
            }

            if (StringUtils.equals((String) toCountryDailyWithdrawalLimit.get("status"), Const.STATUS_FAILURE)) {
                BigDecimal toCountryDailyWithdrawal = (BigDecimal) toCountryDailyWithdrawalLimit.get("toAmt");
                if (toCountryDailyWithdrawal.compareTo(BigDecimal.ZERO) == 0) {
                    vo.setStatus(Const.MONEY_WITHDRAWAL_LIMIT_STATUS);
                    vo.setMessage((String) toCountryDailyWithdrawalLimit.get("message"));
                    return vo;
                } else if(toCountryDailyWithdrawal.compareTo(toMoney) < 1) {
                    // toCountryDailyWithdrawal 금액이 더 toMoney보다 클 경우 금액을 변경하지 않는다.
                    vo.setStatus(Const.MONEY_WITHDRAWAL_EXCESS_STATUS);
                    vo.setMessage((String) toCountryDailyWithdrawalLimit.get("message"));
                    toMoney = (BigDecimal) toCountryDailyWithdrawalLimit.get("toAmt");
                }
            }

            if (toMoney.compareTo(maxExchange) > 0) {
                ArrayList<String> array = new ArrayList<>();
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(minExchange));
                array.add(toCountry.getIso4217());
                array.add(NumberFormat.getNumberInstance(locale).format(maxExchange));

                vo.setStatus(Const.MONEY_EXCHANGE_LIMIT_STATUS);
                vo.setMessage(messageSource.getMessage("WITHDRAWAL_EXCHANGE_MIN_MAX_MESSAGE", array.toArray(), "Yearly exchange limit exceeded.", locale));
                toMoney = maxExchange;
            }

            toMoney = toMoney.divide(unitScale, 0, RoundingMode.HALF_UP).multiply(unitScale);

            fromMoney = exchangeCalculateService.getFromMoney(toMoney, baseRate, mappingInc);

            BigDecimal feeRateAmt = exchangeCalculateService.getFeeRateAmt(toMoney, baseRate, spreadRate, mappingInc);
            BigDecimal feePerAmt = getFeePerAmt(fromCd, toCd, toMoney, exchangeConfig.getFeePerExchange());

            BigDecimal fee = feeRateAmt.add(feePerAmt);

            CouponCalcResponse calcResponse = new CouponCalcResponse();
            // 쿠폰 정보 계산
            if (Const.Y.equals(couponUseYn)) {
                calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, fromMoney.add(feeRateAmt), feePerAmt, couponUserId, userId, ServiceType.EXCHANGE, Const.FALSE);
            } else {
                calcResponse.setPaymentAmount(fromMoney.add(feePerAmt).add(feeRateAmt));
            }

            vo.setFrom_cd(fromCd);
            vo.setTo_cd(toCd);
            vo.setFrom_money(fromMoney);
            vo.setTo_money(toMoney);
            vo.setFee(fee);
            vo.setFee_rate(exchangeConfig.getFeeRateExchange());
            vo.setFee_per_amt(feePerAmt);
            vo.setFee_rate_amt(feeRateAmt);
            vo.setExchange_rate(spreadRate);
            vo.setBase_exchange_rate(baseRate);
            vo.setCouponUserId(calcResponse.getCouponUserId());
            vo.setDiscountAmount(calcResponse.getDiscountAmount());
            vo.setPaymentAmount(calcResponse.getPaymentAmount());

            // 쿠폰 적용 discount 금액이 있고 기존 메세지가 없을 경우 쿠폰 적용 메세지 전달
            if (couponUserId == null && vo.getDiscountAmount() != null && vo.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0
                    && StringUtils.isEmpty(vo.getMessage())) {
                vo.setStatus(Const.STATUS_SUCCESS);
                vo.setMessage(messageSource.getMessage("COUPON_USED_AUTOMATICALLY", null, "Coupon has been used automatically", locale));
            }

            logger.info("{}: vo={}", method, vo);
            return vo;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 환전 가능 여부 처크
     *
     * @param userId
     * @param fromCd
     * @param fromAmt
     * @param toCd
     * @param toAmt
     * @param exchangeRate
     * @return
     * @throws CashmallowException
     */
    public String checkExchangeEnabled(Long userId, String fromCd, BigDecimal fromAmt, String toCd, BigDecimal toAmt,
                                       BigDecimal exchangeRate) throws CashmallowException {
        final String method = "checkExchangeEnabled(): ";
        logger.info("{} userId={}, fromCd={}, toCd={}, exchangeRate={}", method, userId, fromCd, toCd,
                exchangeRate);

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);
        if (!"Y".equals(exchangeConfig.getEnabledExchange())) {
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

        /* Check the currency rate changed */
        // BigDecimal currentExchangeRate = currencyService.getCurrencyTarget(fromCd, toCd);
        // if (exchangeRate.compareTo(currentExchangeRate) != 0) {
        //     throw new CashmallowException("EXCHANGE_CHANGED_EXCHANGE_RATE");
        // }

        //		HashMap<String, Object> params = new HashMap<>();
        //		params.put("code", fromCd);
        //		Country fromCountry = countryMapper.getCountryList(params).get(0);
        //		params.put("code", toCd);
        //		Country toCountry = countryMapper.getCountryList(params).get(0);
        //
        //		/* Check max wallet exchange */
        //		// 환전할 금액
        //		BigDecimal totalAmt = BigDecimal.ZERO;

        //		List<TravelerWallet> wallets = travelerMapper.getTravelerWalletByTravelerId(traveler.getId());

        // 현재 잔액, 환불, 인출 예정 금액을 더한다.
        //		for (TravelerWallet w : wallets) {
        //			if (w.getCountry().equals(toCd)) {
        //				totalAmt = totalAmt.add(w.geteMoney());
        //				totalAmt = totalAmt.add(w.getrMoney());
        //				totalAmt = totalAmt.add(w.getcMoney());
        //			}
        //		}
        //
        //		Map<String, String> exchangeLimit = getExchangeLimit(fromCd, toCd);
        //		BigDecimal maxExchange = new BigDecimal(exchangeLimit.get("max"));
        //
        //		if (maxExchange.compareTo(totalAmt.add(toAmt)) < 0) {
        //			// 환전 한도 금액 세팅
        //			String option = String.format("{iso4217:%s, amt:%d}", toCountry.getIso4217(), maxExchange.intValue());
        //			// Exchange is disabled, return failure with exchange_notice.
        //			throw new CashmallowException("EXCHANGE_EXCEEDED_WALLET_LIMIT", option);
        //		}

        Country fromCountry = countryMapper.getCountry(fromCd);

        // validate wallet fromCountry list
        validateWalletFromCountryLimit(traveler, fromAmt, fromCountry, toCd);

        // validate wallet toCountry limit
        validateWalletToCountryLimit(traveler, toAmt, fromCd, toCd);

        // Check max exchange amount per day based on from_cd
        validateDayLimit(traveler, fromAmt, fromCountry, toCd);

        // Check max exchange amount and count per month based on from_cd
        validateMonthLimit(traveler, fromCountry, toCd);

        // Check annual max exchange amount based on from_cd
        validateAnnualLimit(traveler, fromAmt, fromCountry, toCd);

        return "Y";
    }

    private void validateWalletFromCountryLimit(Traveler traveler, BigDecimal fromAmt, Country fromCountry, String toCd)
            throws CashmallowException {
        String method = "validateWalletTotal()";

        String fromCd = fromCountry.getCode();

        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletListByTravelerId(traveler.getId());
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        BigDecimal totalWalletFromMoney = fromAmt;

        // 현재 지갑에 소지하고 있는 금액들의 fromAmt를 가져와서 from금액의 합계를 구한다.
        for (TravelerWallet w : wallets) {
            if (w.getExchangeIds() == null) {
                continue;
            }

            if (w.getRootCd().equals(fromCd) && w.getCountry().equals(toCd)) {

                JSONObject exchangeIds = new JSONObject(w.getExchangeIds());
                JSONArray exchangeArray = exchangeIds.getJSONArray(Const.EXCHANGE_IDS);

                for (Object exchangeId : exchangeArray) {
                    Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(Long.valueOf(exchangeId.toString()));
                    totalWalletFromMoney = totalWalletFromMoney.add(exchange.getFromAmt());
                }
            }
        }

        /* Check max exchange amount per case based on from_cd */
        if (!exchangeConfig.getFromTotalMaxExchange().equals(BigDecimal.ZERO) && totalWalletFromMoney.compareTo(exchangeConfig.getFromTotalMaxExchange()) > 0) {
            // 환전 한도 금액 세팅

            logger.info("{}: 지갑에 보유한 금액이 한도를 넘었습니다. totalWalletFromMoney={}, maxExchange={}", method, totalWalletFromMoney, exchangeConfig.getFromTotalMaxExchange());

            String option = String.format("{iso4217:%s, amt:%d, max:%d}", fromCountry.getIso4217(),
                    totalWalletFromMoney.intValue(), exchangeConfig.getFromTotalMaxExchange().intValue());

            alarmService.i("인출한도초과",
                    """
                            지갑에 보유한 금액이 한도를 넘었습니다
                            - From Currency : %s
                            - UserId : %s
                            - 최대 인출 가능 금액 : %s
                            - %s
                            """.formatted(
                            fromCountry.getIso4217(),
                            traveler.getUserId().toString(),
                            exchangeConfig.getFromTotalMaxExchange(),
                            option
                    )
            );

            throw new CashmallowException("EXCHANGE_EXCEEDED_WALLET_LIMIT", option);
        }
    }

    private void validateWalletToCountryLimit(Traveler traveler, BigDecimal toAmt, String fromCd, String toCd)
            throws CashmallowException {

        String method = "validateWalletTotal()";

        Country toCountry = countryMapper.getCountry(toCd);
        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletListByTravelerId(traveler.getId());

        BigDecimal totalWalletAmt = toAmt;

        // 현재 지갑에 소지하고 있는 금액들의 toAmt를 가져와서 to금액의 합계를 구한다.
        for (TravelerWallet w : wallets) {
            if (w.getCountry().equals(toCd)) {
                totalWalletAmt = totalWalletAmt.add(w.geteMoney());
                totalWalletAmt = totalWalletAmt.add(w.getcMoney());
                totalWalletAmt = totalWalletAmt.add(w.getrMoney());
            }
        }

        /* Check max exchange amount per case based on from_cd */
        if (totalWalletAmt.compareTo(exchangeConfig.getToMaxExchange()) > 0) {

            // 환전 한도 금액 세팅
            logger.info("{}: To머니 기준 지갑에 보유한 금액이 한도를 넘었습니다. totalWalletToMoney={}, toMaxExchange={}",
                    method, totalWalletAmt, exchangeConfig.getToMaxExchange());

            String option = String.format("{iso4217:%s, amt:%f, max:%d}", toCountry.getIso4217(),
                    totalWalletAmt, exchangeConfig.getToMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            지갑에 보유한 to금액이 한도를 넘었습니다.
                            - From Currency : %s
                            - UserId : %s
                            - To국가 최대 환전 가능 금액 : %s
                            - %s
                            """.formatted(
                            toCountry.getIso4217(),
                            traveler.getUserId().toString(),
                            exchangeConfig.getToMaxExchange(),
                            option
                    )
            );

            throw new CashmallowException("EXCHANGE_EXCEEDED_WALLET_LIMIT", option);
        }
    }

    private void validateDayLimit(Traveler traveler, BigDecimal fromAmt, Country fromCountry, String toCd)
            throws CashmallowException {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] daily = DateUtil.getDaily(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", daily[0]);
        params.put("toDate", daily[1]);

        Map<String, Object> sum = exchangeMapper.getExchangeFromAmtSumByPeriod(params);
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");

        BigDecimal totalAmt = fromAmtSum.add(fromAmt);
        if (totalAmt.compareTo(exchangeConfig.getFromDayMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    totalAmt, exchangeConfig.getFromDayMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [일] 한도를 초과하였습니다
                            - From Currency : %s
                            - UserId : %s
                            - 일일 최대 환전 가능 금액 : %s
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

    private void validateMonthLimit(Traveler traveler, Country fromCountry, String toCd)
            throws CashmallowException {

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] monthly = DateUtil.getMonthly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", monthly[0]);
        params.put("toDate", monthly[1]);

        Map<String, Object> sum = exchangeMapper.getExchangeFromAmtSumByPeriod(params);
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");

        /* Check max exchange amount per month based on from_cd */
        if (exchangeConfig.getFromMonthMaxExchange() != null &&
                fromAmtSum.compareTo(exchangeConfig.getFromMonthMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    fromAmtSum, exchangeConfig.getFromMonthMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [월] 한도를 초과하였습니다
                            - From Currency : %s
                            - UserId : %s
                            - 월간 최대 환전 가능 금액 : %s
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

        final String fromCd = fromCountry.getCode();
        final boolean isStatic = !StringUtils.equals(fromCountry.getDateCalculationStandard(), Const.DATE_CALCULATION_STANDARD_DYNAMIC);
        final String[] yearly = DateUtil.getYearly(CountryCode.of(fromCountry.getCode()).getZoneId(), isStatic);

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

        HashMap<String, Object> params = new HashMap<>();
        params.put("travelerId", traveler.getId());
        params.put("fromCd", fromCd);
        params.put("fromDate", yearly[0]);
        params.put("toDate", yearly[1]);

        Map<String, Object> sum = exchangeMapper.getExchangeFromAmtSumByPeriod(params);
        BigDecimal fromAmtSum = (BigDecimal) sum.get("fromAmtSum");
        fromAmtSum = fromAmtSum.add(fromAmt);
        if (exchangeConfig.getFromAnnualMaxExchange() != null &&
                fromAmtSum.compareTo(exchangeConfig.getFromAnnualMaxExchange()) > 0) {
            // 환전 한도 금액 세팅
            String option = String.format("{iso4217:%s, amt:%f, max:%d}", fromCountry.getIso4217(),
                    fromAmtSum, exchangeConfig.getFromAnnualMaxExchange().intValue());

            alarmService.i("환전한도초과",
                    """
                            [연] 한도를 초과하였습니다
                            - From Currency : %s
                            - UserId : %s
                            - 연간 최대 환전 가능 금액 : %s
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

    /**
     * 환전 v3
     * 쿠폰 개선 적용 2025.03.06
     * @param userId
     * @param exchange
     * @param couponUserId   null 이면 쿠폰 미사용.
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Exchange requestExchangeV3(Long userId, Exchange exchange, Long couponUserId) throws CashmallowException {
        final String method = "requestExchangeV3(): ";

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        checkExchangeEnabled(userId, exchange.getFromCd(), exchange.getFromAmt(), exchange.getToCd(),
                exchange.getToAmt(), exchange.getExchangeRate());

        Country fromCountry = countryService.getCountry(exchange.getFromCd());
        Country toCountry = countryService.getCountry(exchange.getToCd());

        CouponCalcResponse calcResponse = new CouponCalcResponse();
        // 쿠폰 정보 계산
        if (isUseCoupon(couponUserId)) {
            calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, exchange.getFromAmt(), exchange.getFeePerAmt(), couponUserId, userId, ServiceType.EXCHANGE, Const.TRUE);
        } else {
            calcResponse.setPaymentAmount(exchange.getFromAmt());
        }

        BigDecimal fee = exchange.getFee();
        BigDecimal fromAmt = calcResponse.getPaymentAmount();
        BigDecimal feeRateAmt = null;

        if (exchange.getFeeRateAmt() != null) {
            feeRateAmt = exchange.getFeeRateAmt();
        }

        if (CountryCode.HK.getCode().equals(exchange.getFromCd())) {
            exchange.setBankAccountId(Math.toIntExact(dbsProperties.accountId()));
        }

        logger.info("{} userId={}", method, userId);

        if (exchange.checkValidation()) {

            User user = userRepositoryService.getUserByUserId(userId);
            Long travelerId = traveler.getId();

            // 환전 진행 상태가 OP인 건이 있으면 신청이 안되도록 한다.
            List<Exchange> inProgressExchanges = exchangeMapper.getLastestExchangeInProgressByTravelerId(travelerId);

            if (!inProgressExchanges.isEmpty()) {
                logger.warn("{} An exchange for the traveler(userId={}) is on progress.", method, userId);
                throw new CashmallowException(MsgCode.PREVIOUS_EXCHANGE_IN_PROGRESS);
            }

            int affectedRow = 0;
            Long exchangeId;

            ExchangeCalcVO exchangeCalcVO = calcExchangeV6(exchange.getFromCd(), exchange.getToCd(), BigDecimal.valueOf(0), exchange.getToAmt(),
                    traveler, couponUserId, isUseCoupon(couponUserId) ? Const.Y : Const.N);

            if (StringUtils.isNotEmpty(exchangeCalcVO.getStatus())) {
                logger.warn("{}, 환전 계산 실패. status={}", method, exchangeCalcVO.getStatus());
                throw new CashmallowException(exchangeCalcVO.getMessage());
            }

            // To금액은 같아야한다.
            if (exchange.getToAmt().compareTo(exchangeCalcVO.getTo_money()) != 0) {
                logger.error("{} to_money관련 환전 요청 정보가 올바르지 않습니다. calcToAmt={}, requestToAmt={}", method,
                        exchangeCalcVO.getTo_money(), exchange.getToAmt());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 환율변동 혹은 from 금액이 다를경우 새로 계산한 금액으로 변경한다.
            // 수수료가 다를경우에도 새로 계산
            if (exchange.getExchangeRate().compareTo(exchangeCalcVO.getExchange_rate()) != 0
                    || exchange.getFromAmt().compareTo(exchangeCalcVO.getFrom_money()) != 0
                    || fee.compareTo(exchangeCalcVO.getFee()) != 0) {
                exchange.updateExchangeRate(exchangeCalcVO);
                couponUserId = exchangeCalcVO.getCouponUserId();
                fromAmt = exchangeCalcVO.getPaymentAmount();
                fee = exchangeCalcVO.getFee();
                feeRateAmt = exchangeCalcVO.getFee_rate_amt();
            }

            MappingPinRegVO mprvo = new MappingPinRegVO();
            mprvo.setCountry(exchange.getFromCd());
            mprvo.setPin_value(fromAmt);
            mprvo.setBank_account_id(exchange.getBankAccountId());

            Mapping mapping = exchangeCalculateService.generatePinValue(userId, mprvo);

            if (ObjectUtils.isEmpty(mapping)) {
                // Pin 생성 Fail 이면 그대로 리턴. 진행 안함.
                logger.error("{} cannot find mapping information in mapping table (userId={})", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            if (exchange.getFeeRateAmt() != null) {
                exchange.setFeeRateAmt(feeRateAmt);
            }

            exchange.setTravelerId(travelerId);
            exchange.setCreator(userId);
            exchange.setIdentificationNumber(traveler.getIdentificationNumber());
            exchange.setTrAccountBankbookPhoto(traveler.getAccountBankbookPhoto());
            exchange.setFee(fee);
            exchange.setFromAmt(fromAmt);
            exchange.setFeePerAmt(exchangeCalcVO.getFee_per_amt());
            exchange.setExStatus("OP");
            exchange.setTrAddress(traveler.getAddress());
            exchange.setTrAddressPhoto(traveler.getAddressPhoto());
            exchange.setTrAddressCity(traveler.getAddressCity());
            exchange.setTrAddressCountry(traveler.getAddressCountry());
            exchange.setTrAddressSecondary(traveler.getAddressSecondary());
            exchange.setTrPhoneNumber(user.getPhoneNumber());
            exchange.setTrPhoneCountry(user.getPhoneCountry());
            exchange.setCouponUserId(calcResponse.getCouponUserId());
            exchange.setCouponDiscountAmt(calcResponse.getDiscountAmount());
            exchange.setFeeRate(exchangeCalcVO.getFee_rate());

            affectedRow = exchangeRepositoryService.insertExchange(exchange);

            if (affectedRow != 1) {
                logger.error("{} to_money관련 환전 요청 정보가 올바르지 않습니다.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // 쿠폰 사용
            if (isUseCoupon(couponUserId)) {
                // User 쿠폰 사용
                couponMobileService.useCouponUser(fromCountry.getCode(), userId, couponUserId, exchangeCalcVO.getDiscountAmount(), ServiceType.EXCHANGE.getCode());
            }

            // 2. 환전 신청한 record의 id를 구한다.
            exchangeId = exchange.getId();

            if (exchangeId != null) {
                // 3. mapping 테이블의 PIN값을 찾아 환전id를 업데이트(연결)한다.
                HashMap<String, Object> params = new HashMap<>();
                params.put("exchangeId", exchangeId);
                params.put("mappingId", mapping.getId());
                params.put("travelerId", travelerId);
                affectedRow = mappingMapper.updateExchangeIdAfterReqExchange(params);

                if (affectedRow == 1) {
                    // 4. 환전 신청 정보를 읽는다.
                    return exchangeMapper.getExchangeByExchangeId(exchangeId);

                } else {
                    logger.error("{} mapping정보의 환전 정보를 업데이트할 수 없습니다. mappingId={}, pinvalue={}, fromAmt={}", method,
                            mapping.getId(), mapping.getPinValue(), exchange.getFromAmt());
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            } else {
                logger.error("{} 환전 ID = null", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else {
            logger.error("{} invalid exchange values (userId={})", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    private static boolean isUseCoupon(Long couponUserId) {
        return couponUserId != null && couponUserId > 0;
    }

    // 기능: 9.3. 여행자 환전 신청
    @Transactional(rollbackFor = CashmallowException.class)
    public Exchange requestExchangeKr(Long userId, Exchange exchange) throws CashmallowException {
        final String method = "requestExchangeKr(): ";

        checkExchangeEnabled(userId, exchange.getFromCd(), exchange.getFromAmt(), exchange.getToCd(),
                exchange.getToAmt(), exchange.getExchangeRate());

        Country fromCountry = countryService.getCountry(exchange.getFromCd());
        Country toCountry = countryService.getCountry(exchange.getToCd());

        CouponCalcResponse calcResponse = new CouponCalcResponse();
        // 쿠폰 정보 계산
        if (isUseCoupon(exchange.getCouponUserId())) {
            calcResponse = couponMobileService.calcCouponV2(fromCountry, toCountry, exchange.getFromAmt(), exchange.getFeePerAmt(), exchange.getCouponUserId(), userId, ServiceType.EXCHANGE, Const.TRUE);
        } else {
            calcResponse.setPaymentAmount(exchange.getFromAmt());
        }

        BigDecimal fee = exchange.getFee();
        BigDecimal fromAmt = calcResponse.getPaymentAmount();
        BigDecimal feeRateAmt = null;

        if (exchange.getFeeRateAmt() != null) {
            feeRateAmt = exchange.getFeeRateAmt();
        }

        logger.info("{} userId={}", method, userId);

        if (!exchange.checkValidationKr()) {
            logger.error("{} invalid exchange values (userId={})", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        Long travelerId = traveler.getId();

        // 환전 진행 상태가 OP인 건이 있으면 신청이 안되도록 한다.
        List<Exchange> inProgressExchanges = exchangeMapper.getLastestExchangeInProgressByTravelerId(travelerId);
        if (!inProgressExchanges.isEmpty()) {
            logger.error("{} An exchange for the traveler(userId={}) is on progress.", method, userId);
            throw new CashmallowException(MsgCode.PREVIOUS_EXCHANGE_IN_PROGRESS);
        }

        int affectedRow = 0;
        Long exchangeId;

        ExchangeCalcVO exchangeCalcVO = calcExchangeV6(exchange.getFromCd(), exchange.getToCd(), BigDecimal.valueOf(0), exchange.getToAmt(),
                traveler, exchange.getCouponUserId(), isUseCoupon(exchange.getCouponUserId()) ? Const.Y : Const.N);

        BigDecimal compareFee = exchangeCalcVO.getFee().subtract(fee).abs();
        BigDecimal feeRange = fromCountry.getMappingLowerRange().abs();

        // feeRange의 범위 안에 있어야하며, To금액은 같아야한다.
        if (feeRange.compareTo(compareFee) < 0 || exchange.getToAmt().compareTo(exchangeCalcVO.getTo_money()) != 0) {
            logger.error("{} to_money관련 송금 요청 정보가 올바르지 않습니다. 요청금액={}, 재계산 금액={}", method, exchange.getToAmt(), exchangeCalcVO.getTo_money());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        if (exchange.getFeeRateAmt() != null) {
            exchange.setFeeRateAmt(feeRateAmt);
        }

        exchange.setBankAccountId(openbankAccountId);
        exchange.setTravelerId(travelerId);
        exchange.setCreator(userId);
        exchange.setIdentificationNumber(traveler.getIdentificationNumber());
        exchange.setTrAccountBankbookPhoto(traveler.getAccountBankbookPhoto());
        exchange.setFee(fee);
        exchange.setFromAmt(fromAmt);
        exchange.setExStatus("OP");
        exchange.setTrAddress(traveler.getAddress());
        exchange.setTrAddressPhoto(traveler.getAddressPhoto());
        exchange.setTrAddressCity(traveler.getAddressCity());
        exchange.setTrAddressCountry(traveler.getAddressCountry());
        exchange.setTrAddressSecondary(traveler.getAddressSecondary());
        exchange.setTrPhoneNumber(user.getPhoneNumber());
        exchange.setTrPhoneCountry(user.getPhoneCountry());
        exchange.setCouponUserId(calcResponse.getCouponUserId());
        exchange.setCouponDiscountAmt(calcResponse.getDiscountAmount());

        affectedRow = exchangeRepositoryService.insertExchange(exchange);

        if (affectedRow != 1) {
            logger.error("{} to_money관련 환전 요청 정보가 올바르지 않습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 쿠폰 사용
        if (isUseCoupon(exchange.getCouponUserId())) {
            // User 쿠폰 사용
            couponMobileService.useCouponUser(fromCountry.getCode(), userId, exchange.getCouponUserId(), exchangeCalcVO.getDiscountAmount(), ServiceType.EXCHANGE.getCode());
        }

        exchangeId = exchange.getId();

        return exchangeMapper.getExchangeByExchangeId(exchangeId);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public ExchangeDepositReceipt uploadExchangeReceiptPhoto(long exchangeId, MultipartFile file)
            throws CashmallowException {
        String method = "uploadExchangeReceiptPhoto()";

        logger.info("{}: exchangeId={}", method, exchangeId);

        String receiptPhoto = fileService.upload(file, Const.FILE_SERVER_RECEIPT);

        ExchangeDepositReceipt exchangeDepositReceipt = new ExchangeDepositReceipt(exchangeId, receiptPhoto);

        int rows = exchangeMapper.insertExchangeDepositReceipt(exchangeDepositReceipt);

        if (rows != 1) {
            logger.error("{}: exchange_deposit_receipt table update failure", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);

        if (exchange.getExStatus().equals(ExStatus.DR.name())) {
            exchange.setExStatus(ExStatus.OP.toString());
            exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            int affectedRows = exchangeRepositoryService.updateExchange(exchange);

            if (affectedRows != 1) {
                logger.error("{}: 환전영수증 업로드중 ex_status 업데이트에 실패했습니다. exchange_id={}", method, exchange.getId());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return exchangeDepositReceipt;
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<ExchangeDepositReceipt> getExchangeDepositReceiptList(long exchangeId) {
        return exchangeMapper.getExchangeDepositReceiptList(exchangeId);
    }

    /**
     * 환전신청시 여행자 입금 계좌 정보 입력
     */
    public Exchange setExchangeTravelerBankInfo(Long userId, String trBankName, String trAccountName, String trAccountNo,
                                                BigDecimal trFromAmt, Timestamp trDepositDate) throws CashmallowException {
        String method = "setExchangeTravelerBankInfo(): ";

        Exchange exchange = getLatestExchangeOpStatusInfo(userId);

        if (exchange == null) {
            logger.warn("{}: 환전 정보를 찾을 수 없습니다. userId={}", method, userId);
            throw new CashmallowException("EXCHANGE_CANNOT_FIND_OP");
        }

        exchange.setTrBankName(trBankName);
        exchange.setTrAccountName(trAccountName);
        exchange.setTrAccountNo(trAccountNo);
        exchange.setTrFromAmt(trFromAmt);
        exchange.setTrDepositDate(trDepositDate);
        exchange.setTrReceiptPhoto("dummy data");

        int affectedRow = exchangeRepositoryService.updateExchangeTrAccountInfo(exchange);
        if (affectedRow != 1) {
            logger.warn("{}: 환전 정보를 찾을 수 없습니다. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        BankAccount bankAccount = companyMapper.getBankAccountByBankAccountId(exchange.getBankAccountId());

        // edited by kgy 20170915 : 입금정보 등록한 건에 대해서 메시지 보낸다.
        String msg = "환전 거래번호: " + exchange.getId() +
                "\n국가:" + exchange.getFromCd() + ", 회사은행:" + bankAccount.getBankName() +
                "\n유저ID:" + userId + ", 은행:" + trBankName + ", 이름:" + trAccountName + ", 코드:" + trAccountNo + ", 금액:" + trFromAmt +
                "\n적용환율:" + exchange.getExchangeRate() +
                "\n국가:" + exchange.getFromCd() + ", 금액:" + exchange.getFromAmt() + ", 수수료:" + exchange.getFee() +
                "\n대상국가:" + exchange.getToCd() + ", 대상금액:" + exchange.getToAmt();

        alarmService.aAlert("환전신청 입금정보등록", msg, userRepositoryService.getUserByUserId(userId));

        return exchange;
    }

    // 기능: 9.4. 마지막 환전 신청을 취소한다.
    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelLastExchangeReqByTravelerIdV2(Long userId) throws CashmallowException {
        String method = "cancelLastExchangeReqByTravelerIdV2()";
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler != null) {
            Long travelerId = traveler.getId();
            logger.info("{}: userId={}, travelerId={}", method, userId, travelerId);
            mappingMapper.cancelExchangePinValueByTravelerId(travelerId);

            // 2. 환전 신청을 취소한다.
            List<Exchange> exchanges = exchangeMapper.getLastestExchangeInProgressByTravelerId(travelerId);

            if (exchanges == null || exchanges.isEmpty()) {
                throw new CashmallowException("진행 중인 환전이 없습니다.");
            }

            Exchange exchange = exchanges.get(0);
            String prevExchangeStatus = exchange.getExStatus();

            if (!StringUtils.isEmpty(exchange.getTrAccountNo())) {
                throw new CashmallowException("입금등록 이후에는 환전 취소가 불가능합니다.");
            }

            exchange.setExStatus("TC");
            exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
            exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));

            int affectedRow = exchangeRepositoryService.updateExchange(exchange);

            // 쿠폰 원복
            CouponIssueUser couponUser = null;
            if (isUseCoupon(exchange.getCouponUserId())) {
                couponMobileService.cancelCouponUserV2(exchange.getCouponUserId(), prevExchangeStatus);
                couponUser = couponUserService.getCouponUserByIdAndStatus(exchange.getCouponUserId(), AvailableStatus.AVAILABLE);
            }

            if (affectedRow != 1) {
                throw new CashmallowException("취소할 환전 신청 내역이 없습니다.");
            }

            // 송금정보까지 등록한 경우 Alarm 을 보낸다.
            if (!StringUtils.isEmpty(exchange.getTrBankName())) {
                alarmService.aAlert("환전취소", "유저ID:" + userId, userRepositoryService.getUserByUserId(traveler));
            }

            // 환전신청 취소시 일본서버 추가
            if (CountryCode.JP.getCode().equals(exchange.getFromCd())) {
                // coupon_user(유저 쿠폰) 테이블은 sync_id 가 없으므로 해당 쿠폰을 찾기 위해 couponIssueSyncId 가 필요함.
                // coupon_issue(발급) 테이블에서 user_id 와 sync_id 는 유니크 하므로 2개의 파라미터로 coupon_user 테이블의 id 를 찾을 수 있음
                Long cancelCouponIssueSyncId = null;
                if (exchange.getCouponUserId() != null && exchange.getCouponUserId() != -1L && couponUser != null) { // 롤백 처리된 쿠폰인지 체크
                    cancelCouponIssueSyncId = couponUser.getCouponIssueId();
                }
                globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.EXCHANGE, exchange.getId(), exchange.getExStatus(), cancelCouponIssueSyncId);
            }

        } else {
            throw new CashmallowException("travelerId를 찾을 수 없습니다.");
        }
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Mapping getMappingByRemitId(long travelerId, long remitId) {
        Map<String, Object> params = new HashMap<>();
        params.put("travelerId", travelerId);
        params.put("remitId", remitId);

        return mappingMapper.getMappingByRemitId(params);
    }

    // -------------------------------------------------------------------------------
    // 62. 은행정보(mapping용)
    // -------------------------------------------------------------------------------

    // 기능: 62.1. 사용 은행 정보 등록
    @Transactional(rollbackFor = CashmallowException.class)
    public ApiResultVO putBankAccount(Long managerId, BankAccountVO pvo) throws CashmallowException {
        ApiResultVO result = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        String method = "putBankAccount(): ";
        String error = "";
        Object obj = null;
        logger.info(method + "managerId=" + managerId + ", pvo=" + pvo);

        if (pvo != null && pvo.checkValidation()) {
            try {
                Map<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("country", pvo.getCountry());
                int companyId = companyMapper.getCompanyIdByCountry(parameters);

                if (companyId != Const.NO_COMPANY_ID) {
                    pvo.setCompany_id(companyId);
                    int affectedRow = companyMapper.putBankAccount(pvo);

                    if (affectedRow != 1) {
                        error = "은행 정보 저장 중 오류가 발생했습니다. 적용된 row=" + affectedRow;
                        throw new CashmallowException(error);
                    }
                } else {
                    error = "지점 ID를 검색할 수 없습니다.";
                    throw new CashmallowException(error);
                }
            } catch (DuplicateKeyException e) {
                logger.error(e.getMessage(), e);
                error = "이미 등록된 정보입니다.";
                throw new CashmallowException(error);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                error = "은행 정보 저장 중 오류가 발생했습니다. 국가, 지사 정보 등이 올바른지 확인하십시오.";
                throw new CashmallowException(error);
            }

        } else {
            result.setResult(Const.CODE_FAILURE, Const.STATUS_INVALID_USER_ID, Const.MSG_INVALID_USER_ID);
            result.setInvalidParams();
            throw new CashmallowException(error);
        }

        result.setSuccessInfo(obj);
        return result;
    }


    /**
     * Batch 에 의해 요청된 환전 신청 취소 처리
     */
    public void cancelExchangeByBatch(Long exchangeId, Long travelerId) throws CashmallowException {

        String method = "cancelExchangeByBatch()";
        logger.info("{}: cancelExchange. exchangeId={}, travelerId={}", method, exchangeId, travelerId);

        Exchange exchange = exchangeMapper.getExchangeByExchangeId(exchangeId);

        ExStatus exStatus = ExStatus.valueOf(exchange.getExStatus());
        if (!(ExStatus.OP.equals(exStatus) || ExStatus.DR.equals(exStatus))) {
            throw new CashmallowException("The exStatus of the exchange data is not 'OP' or 'DR'. exchangeId=" + exchangeId);
        }

        if (!travelerId.equals(exchange.getTravelerId())) {
            throw new CashmallowException("The travelerId of the exchange data does not match. travelerId=" + travelerId
                    + ", exchange.getTravelerId()=" + exchange.getTravelerId());
        }

        cancelExchangeByCashmallow(exchange, method);
        if (CountryCode.JP.getCode().equals(exchange.getFromCd())) {
            // coupon_user(유저 쿠폰) 테이블은 sync_id 가 없으므로 해당 쿠폰을 찾기 위해 couponIssueSyncId 가 필요함.
            // coupon_issue(발급) 테이블에서 user_id 와 sync_id 는 유니크 하므로 2개의 파라미터로 coupon_user 테이블의 id 를 찾을 수 있음
            Long cancelCouponIssueSyncId = null;
            if (exchange.getCouponUserId() != null && exchange.getCouponUserId() != -1L) {
                CouponIssueUser couponUser = couponUserService.getCouponUserByIdAndStatus(exchange.getCouponUserId(), AvailableStatus.AVAILABLE);
                // 롤백 처리된 쿠폰인지 체크
                if (couponUser != null) {
                    cancelCouponIssueSyncId = couponUser.getCouponIssueId();
                }
            }
            globalQueueService.sendTransactionCancel(TransactionRecord.RelatedTxnType.EXCHANGE, exchange.getId(), exchange.getExStatus(), cancelCouponIssueSyncId);
        }
    }

    private void cancelExchangeByCashmallow(Exchange exchange, String method) throws CashmallowException {

        String prevExchangeStatus = exchange.getExStatus();

        exchange.setExStatus(ExStatus.CC.toString());
        exchange.setExStatusDate(Timestamp.valueOf(LocalDateTime.now()));
        exchange.setFcmYn("Y");
        exchange.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

        int affectedRows = exchangeRepositoryService.updateExchange(exchange);

        // 쿠폰 원복
        // 쿠폰 개선 적용 25.03.07
        if (isUseCoupon(exchange.getCouponUserId())) {
            couponMobileService.cancelCouponUserV2(exchange.getCouponUserId(), prevExchangeStatus);
        }

        if (affectedRows == 1) {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(exchange.getTravelerId());
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());
            notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.EX, FcmEventValue.CC, exchange.getId());
        } else {
            logger.error("{}: Failed to update exchange data. travelerId={}, exchange.getTravelerId()={}", method,
                    exchange.getTravelerId(), exchange.getTravelerId());
            throw new CashmallowException("Failed to update exchange data. travelerId=" + exchange.getTravelerId()
                    + ", exchange.getTravelerId()=" + exchange.getTravelerId());
        }
    }

    public Boolean isPossibleToCancelBankBookverified(Long travelerId) {
        List<Exchange> opExchanges = exchangeMapper.getLastestExchangeInProgressByTravelerId(travelerId);

        logger.info("isPossibleToCancelBankBookverified : size={}", opExchanges.size());

        // 진행중인 건이 없으면 True
        return opExchanges.isEmpty();
    }

    private BigDecimal getFeePerAmt(String fromCd, String toCd,
                                    BigDecimal toMoney, BigDecimal feePerExchange) {
        BigDecimal feePerAmt = countryService.calculateFee(fromCd, toCd, toMoney);
        if (feePerAmt == null) {
            feePerAmt = feePerExchange;
        }
        return feePerAmt;
    }

    public CurrencyRate getExchangeRate(String fromCd, String toCd) {
        Map<String, Object> params = new HashMap<>();

        params.put("code", toCd);
        Country toCountry = countryService.getCountryList(params).get(0);

        params.put("code", fromCd);
        Country fromCountry = countryService.getCountryList(params).get(0);

        // source 와 target 이 바뀐 것이 아님. to_money 기준으로 환전 금액을 계산하고 있어서 이렇게 함.
        // 즉, to_money 가 10000 원이면 from_money 는 10000 * 적용환율
        // 적용환율 : 기준환율에서 일정 비율로 조정된 환율.
        params.clear();
        params.put("source", fromCountry.getIso4217());
        params.put("target", toCountry.getIso4217());
        return currencyService.getCurrencyRate(params);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void changeExchangeBankAccount(long exchangeId, BankAccount bankAccount) throws CashmallowException {
        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);

        exchange.setBankAccountId(bankAccount.getId());
        exchangeRepositoryService.updateExchangeBankAccountId(exchange);
    }
}
