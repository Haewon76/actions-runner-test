package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.application.impl.CountryServiceImpl;
import com.cashmallow.api.application.impl.RemittanceServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.impl.AuthServiceImpl;
import com.cashmallow.api.domain.model.company.BankAccount;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.country.enums.Currency;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.remittance.*;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.admin.property.JpPostProperties;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.coupon.CouponIssueServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceBankServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.BankBranchesData;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.BankData;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.WalletData;
import com.cashmallow.api.interfaces.traveler.dto.ExchangeCalcVO;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.INVALID_COUPON;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * Handles requests for the application home page.
 */
@Controller
public class TravelerRemittanceController {

    private final Logger logger = LoggerFactory.getLogger(TravelerRemittanceController.class);

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private RemittanceServiceImpl remittanceService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private OctaAmlService octaAmlService;

    @Autowired
    private MallowlinkRemittanceServiceImpl mallowlinkRemittanceService;

    @Autowired
    private MallowlinkRemittanceBankServiceImpl mallowlinkRemittanceValidateService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private Gson gsonPretty;

    @Autowired
    @Qualifier("objectMapperSnake")
    private ObjectMapper objectMapperSnake;

    @Value("${openbank.bankAccountId}")
    private long openbankAccountId;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    private JpPostProperties jpPostProperties;
    @Autowired
    private CouponIssueServiceV2 couponIssueServiceV2;
    @Autowired
    private CouponUserService couponUserService;
    @Autowired
    private TravelerServiceImpl travelerServiceImpl;


    /**
     * Get Exchange limit(min, max) with toCountry currency by fromCd and toCd
     *
     * @param token
     * @param toCd
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/remittance/banks", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceBankLists(@RequestHeader("Authorization") String token,
                                         @RequestParam(value = "to_cd") String toCd,
                                         HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceBankLists()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        // If login user then update token expired date
        authService.getUserAuthInfo(token);

        logger.info("{}: toCd={}", method, toCd);

        List<BankData> bankList;
        CountryCode countryCode = CountryCode.of(toCd);
        bankList = mallowlinkRemittanceValidateService.getBankList(countryCode, BigDecimal.TEN);

        voResult.setSuccessInfo(bankList);

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * Handles HTTP GET requests for the /traveler/remittance/wallets endpoint.
     * <p>
     * This method retrieves the list of remittance wallets for a given country code.
     * It checks the validity of the provided token and updates the token's expiration date if valid.
     * If the token is invalid, it returns an error response.
     *
     * @param token    The authorization token from the request header.
     * @param toCd     The country code for which to retrieve the wallet list.
     * @param request  The HTTP servlet request.
     * @param response The HTTP servlet response.
     * @return A JSON string representing the result of the operation, including the list of wallets if successful.
     */
    @GetMapping(value = "/traveler/remittance/wallets", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceWalletLists(@RequestHeader("Authorization") String token,
                                           @RequestParam(value = "to_cd") String toCd,
                                           HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceWalletLists()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        // If login user then update token expired date
        authService.getUserAuthInfo(token);

        logger.info("{}: toCd={}", method, toCd);

        List<WalletData> bankList;
        CountryCode countryCode = CountryCode.of(toCd);
        bankList = mallowlinkRemittanceValidateService.getWalletList(countryCode);

        voResult.setSuccessInfo(bankList);

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 은행 지점 검색, 현재 일본만 제공 나머지는 빈값을 보낸다.
     *
     * @param token
     * @param toCd
     * @param bankId
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/remittance/banks/branches", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceBankBranches(@RequestHeader("Authorization") String token,
                                            @RequestParam(value = "to_cd") String toCd,
                                            @RequestParam(value = "bank_id") String bankId,
                                            HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceBankBranches()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        // If login user then update token expired date
        authService.getUserAuthInfo(token);

        logger.info("{}: toCd={} bankId={}", method, toCd, bankId);

        // JP, BD가 아닐시 지점 정보 빈값으로 보내기로함
        List<BankBranchesData> branches = switch (CountryCode.of(toCd)) {
            case JP, BD -> mallowlinkRemittanceValidateService.getBankBranches(CountryCode.of(toCd), bankId);
            default -> new ArrayList<>();
        };

        voResult.setSuccessInfo(branches);

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/traveler/remittance/exchange-configs", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceExchangeConfig(HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceExchangeConfig()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        logger.debug("{}: API has been called.", method);

        List<ExchangeConfig> exchangeConfigs = countryService.getCanRemittanceFeeRateList();
        ArrayList<ExchangeConfig> feeRatesWithMessage = new ArrayList<>();
        Locale locale = localeResolver.resolveLocale(request);
        for (ExchangeConfig f : exchangeConfigs) {
            // TODO: 송금 초과한 메세지를 번역 받아서 나중에 RemittanceNotice로 변경해야함.
            String notice = messageSource.getMessage(f.getExchangeNotice(), null, f.getExchangeNotice(), locale);
            f.setRemittanceNotice(notice);
            feeRatesWithMessage.add(f);
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<Map<String, String>> obj = mapper.convertValue(feeRatesWithMessage, new TypeReference<List<Map<String, String>>>() {
        });

        voResult.setSuccessInfo(obj);

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/traveler/countries/{country}/remittance-limit", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceLimitByFromCd(@RequestHeader("Authorization") String token,
                                             @PathVariable String country,
                                             @RequestParam(value = "to_cd") String toCd,
                                             @RequestParam(value = "remittance_type", required = false) String remittanceType,
                                             HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceLimitByFromCd";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        // If login user then update token expired date
        authService.getUserAuthInfo(token);

        logger.info("getRemittanceLimitByFromCd(): country={}, toCd={}", country, toCd);

        // 송금 타입에 따라 제한금액 달라지므로 추가함
        Remittance.RemittanceType remitType = null;
        if (remittanceType == null) {
            remitType = Remittance.RemittanceType.BANK; // 기본값은 BANK
        } else {
            remitType = Remittance.RemittanceType.valueOf(remittanceType);
        }

        try {
            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(country, toCd);
            Country fromCountry = countryService.getCountry(country);
            Country toCountry = countryService.getCountry(toCd);
            Map<String, String> result = remittanceService.getRemittanceExchangeLimit(fromCountry, toCountry, exchangeConfig);
            voResult.setSuccessInfo(result);

            if (CountryCode.NP.getCode().equals(toCd)) {
                // NPR 송금 BANK, WALLET, CASH_PICKUP 제한금액
                // TODO: 추후 기획 완료 시, 테이블에서 조회함. WALLET 이 먼저 나가므로 추가함.
                if (remitType == Remittance.RemittanceType.WALLET) {
                    result.put("max", "50000");
                } else if (remitType == Remittance.RemittanceType.CASH_PICKUP) {
                    result.put("max", "999999");
                } else {
                    result.put("max", "1500000");
                }
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * V4 송금 계산
     * 쿠폰 개선 적용 2025.03.07
     **/
    @GetMapping(value ={ "/traveler/v3/remittance/calculate", "/traveler/v4/remittance/calculate"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String calcRemittanceV4(@RequestHeader("Authorization") String token,
                                   @RequestParam(value = "from_cd") String fromCd, @RequestParam(value = "to_cd") String toCd,
                                   @RequestParam(value = "from_money") BigDecimal fromMoney, @RequestParam(value = "to_money") BigDecimal toMoney,
                                   @RequestParam(value = "remittance_type", required = false) String remittanceType,
                                   @RequestParam(required = false) Long couponUserId,
                                   @RequestParam String couponUseYn,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "calcRemittanceV3,V4()";
        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        Locale locale = localeResolver.resolveLocale(request);

        logger.info("{} : fromCd={}, toCd={}, fromMoney={}, toMoney={}", method, fromCd, toCd, fromMoney, toMoney);

        Remittance.RemittanceType remitType = null;
        if (remittanceType == null) {
            remitType = Remittance.RemittanceType.BANK; // 기본값은 BANK
        } else {
            remitType = Remittance.RemittanceType.valueOf(remittanceType);
        }

        ApiResultVO voResult = new ApiResultVO();
        try {

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            if (userId == Const.NO_USER_ID || traveler == null) {
                ExchangeCalcVO exchangeCalcVO = remittanceService.calcRemittanceAnonymous(fromCd, toCd, fromMoney, toMoney, remitType, userId);
                voResult.setSuccessInfo(exchangeCalcVO);
            } else {

                ExchangeCalcVO exchangeCalcVO = remittanceService.calcRemittanceV4(fromCd, toCd, fromMoney, toMoney, remitType, traveler, couponUserId, couponUseYn);
                voResult.setSuccessInfo(exchangeCalcVO);
                if (!StringUtils.isEmpty(exchangeCalcVO.getStatus())) {
                    voResult.setStatus(exchangeCalcVO.getStatus());
                    voResult.setMessage(exchangeCalcVO.getMessage());
                }

                logger.info("{} : exchangeCalcVO={}", method, exchangeCalcVO);
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/traveler/remittance/purpose", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittancePurpose(@RequestHeader("Authorization") String token,
                                       HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getRemittancePurpose(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        Locale locale = localeResolver.resolveLocale(request);

        List<Map<String, String>> remitPurpose = remittanceService.getRemittancePurpose(locale);

        voResult.setSuccessInfo(remitPurpose);

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/traveler/remittance/source", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceFundSource(@RequestHeader("Authorization") String token,
                                          HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getRemittanceFundSource(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        Locale locale = localeResolver.resolveLocale(request);
        List<Map<String, String>> remitSource = remittanceService.getRemittanceFundSource(locale);

        voResult.setSuccessInfo(remitSource);

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = {"/traveler/v2/remittances"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceHistory(@RequestHeader("Authorization") String token,
                                       @RequestParam int page, @RequestParam int size,
                                       HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            voResult.setSuccessInfo();
            return JsonStr.toJsonString(voResult, response);
        }

        long travelerId = traveler.getId();
        logger.info("getRemittanceHistory() : travelerId={}", travelerId);

        int totalCount = remittanceRepositoryService.countRemittanceListByTravelerId(travelerId);

        String sort = "id DESC";
        List<Remittance> remittances = remittanceRepositoryService.getRemittanceListByTravelerId(travelerId, page, size);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<Object> content = mapper.convertValue(remittances, new TypeReference<List<Object>>() {
        });

        SearchResultVO vo = new SearchResultVO(page, size, sort);
        vo.setResult(content, totalCount, page);

        voResult.setSuccessInfo(vo);

        logger.debug("remittanceHistory={}", gsonPretty.toJson(vo));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Get remittance in progress
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = {"/traveler/v2/remittances/in-progress"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceProgress(@RequestHeader("Authorization") String token,
                                        HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("getRemittanceProgress() : userId={}", userId);

        ApiResultVO voResult = new ApiResultVO();

        try {
            Remittance remittance = null;

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            if (traveler != null) {
                remittance = remittanceRepositoryService.getRemittanceInprogress(traveler.getId());
            }

            if (remittance != null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                Map<String, Object> obj = mapper.convertValue(remittance, new TypeReference<Map<String, Object>>() {
                });

                BankAccount bankAccount = companyService
                        .getBankAccountByBankAccountId(remittance.getBankAccountId().intValue());

                obj.put("country", bankAccount.getCountry());
                obj.put("bank_code", bankAccount.getBankCode());
                obj.put("bank_name", bankAccount.getBankName());
                obj.put("branch_name", bankAccount.getBranchName());
                obj.put("bank_account_no", bankAccount.getBankAccountNo());
                obj.put("account_type", bankAccount.getAccountType());
                obj.put("first_name", bankAccount.getFirstName());
                obj.put("last_name", bankAccount.getLastName());
                // 응답 받을때 RemittanceRequestVO 와 같은 형태로 추가
                // receiver_bank_code -> receiver_type_code
                // receiver_bank_account_no -> receiver_type_number
                obj.put("receiver_type_code", remittance.getReceiverBankCode());
                obj.put("receiver_type_number", remittance.getReceiverBankAccountNo());
                // receiver_address_state_province : State/Province/Region 필수 추가로 인해 필드 추가 (USD 만 필수)
                obj.put("receiver_address_state_province", remittance.getReceiverAddressStateProvince());
                // NPR 작업에서 추가
                obj.put("remittance_type", remittance.getRemittanceType());

                // EUR 통화 송금 시, 필수값 return 해줌: response 시에 traveler 송금보내는 사람 state 넣어주기 위해 추가함.
                RemittanceTravelerSnapshot remittanceTravelerSnapshot
                        = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());
                obj.put("address_state_province", remittanceTravelerSnapshot.getAddressStateProvince());

                if (CountryCode.JP.getCode().equals(bankAccount.getCountry())) {
                    if (jpPostProperties.bankName().equalsIgnoreCase(bankAccount.getBankName())) {
                        // jp Post일경우 계좌번호, 지점명 추가
                        obj.put("jp_post_account_no", jpPostProperties.accountNo());
                        obj.put("jp_post_branch_name", jpPostProperties.branchName());
                    }
                    obj.put("jp_account_type", "普通");
                }

                voResult.setSuccessInfo(obj);
            } else {
                voResult.setSuccessInfo();
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), localeResolver.resolveLocale(request)));
        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * V3 송금
     * 쿠폰 개선 적용 2025.03.06
     **/
    @PostMapping(value = {"/traveler/v2/remittance", "/traveler/v3/remittance"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRemittanceV3(@RequestHeader("Authorization") String token,
                                      @RequestBody String requestBody,
                                      HttpServletRequest request, HttpServletResponse response) {
        String method = "requestRemittanceV3()";

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);

        logger.info("{}: requestBody={}", method, requestBody);

        JSONObject jsonBody = new JSONObject(requestBody);
        //        Long userId = 406L;

        // 필요목록
        /*
         * bank_account_id - 입금용 회사통장
         * exchange_rate - 환율
         * fee, fee_per_amt, fee_rate_amt - 3종 수수료
         * from_cd - from국가
         * from_amt - 유저 입금금액
         * from_money - 수수료 제외한 금액
         * to_cd - to국가
         * to_amt, to_money - 받을 금액
         * bank_account_id - Cashmallow가 입금받을 계죄의 id
         * 그외 받을 사람의 기타 정보들
         */

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        try {
            ObjectMapper objMapper = new ObjectMapper();
            objMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            // Remittance 와 분리하기 위한 RequestVO
            RemittanceRequestVO remittanceRequestVO = objMapper.readValue(requestBody, RemittanceRequestVO.class);

            // 쿠폰 사용가능 여부를 다시 한 번 체크
            if (remittanceRequestVO.getCouponUserId() != null && remittanceRequestVO.getCouponUserId() != -1L) {
                CouponIssueUser couponIssueUser = couponUserService.getCouponUserById(remittanceRequestVO.getCouponUserId());
                if (!AvailableStatus.AVAILABLE.name().equals(couponIssueUser.getAvailableStatus())) {
                    throw new CashmallowException(INVALID_COUPON);
                }
            }

            // NPR 송금 BANK, WALLET, CASH_PICKUP 제한금액
            // TODO: 추후 기획 완료 시, 테이블에서 조회함. WALLET 이 먼저 나가므로 추가함.
            if (CountryCode.NP.getCode().equals(remittanceRequestVO.getToCd())) {
                String result = travelerServiceImpl.remittanceAmountLimitCheckByPartnerSub(Currency.NPR.name(), remittanceRequestVO.getRemittanceType(), remittanceRequestVO.getToAmt(), locale);
                if (result != null) {
                    throw new CashmallowException(result);
                }
            }

            if (!remittanceRequestVO.checkValidationV2()) {
                logger.error("{} invalid v2 remittance values (userId={}), json: {}", method, userId, jsonUtil.toJson(remittanceRequestVO));
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // Remittance 에 주입
            Remittance requestRemittance = RemittanceRequestVO.of(remittanceRequestVO);

            if (requestRemittance.getReceiverBankCode().isEmpty()) {
                logger.error("{}: 수신자의 bankCode가 공백이 들어왔습니다. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            if (CountryCode.KR.getCode().equals(requestRemittance.getToCd())) {
                validateKoreaMobileNo(requestRemittance.getReceiverPhoneCountry(), requestRemittance.getReceiverPhoneNo(), response);
            }

            BigDecimal fromMoney = jsonBody.getBigDecimal("from_money");
            BigDecimal fromAmt = fromMoney.add(requestRemittance.getFee());

            if (requestRemittance.getFromAmt().compareTo(fromAmt) != 0) {
                logger.error("{}: 송금 신청된 금액이 잘못 되었습니다. fromAmt={}, fromMoney={}, feePerAmt={}, feeRateAmt={}",
                        method, requestRemittance.getFromAmt(), fromMoney, requestRemittance.getFeePerAmt(), requestRemittance.getFeeRateAmt());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            if (CountryCode.JP.getCode().equals(requestRemittance.getFromCd()) && ObjectUtils.isEmpty(requestRemittance.getRemitRelationship())) {
                logger.error("{}: remitRelationship is null. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            if (requestRemittance.getToCd().equals(CountryCode.KR.getCode())) {
                requestRemittance.setFinancePartnerId(1L);
            } else {
                requestRemittance.setFinancePartnerId(2L);
            }

            final BigDecimal originExchangeRate = requestRemittance.getExchangeRate();
            final BigDecimal originFromAmt = requestRemittance.getFromAmt();
            final BigDecimal originFee = requestRemittance.getFee();
            Remittance remittance = remittanceService.requestRemittanceV3(
                    requestRemittance, remittanceRequestVO.getAddressStateProvince(), requestRemittance.getCouponUserId());

            BankAccount bankAccount = companyService.getBankAccountByBankAccountId(Math.toIntExact(remittance.getBankAccountId()));
            logger.info("{} : travelerId={}, convert Start", method, remittance.getTravelerId());
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> obj = mapper.convertValue(remittance, new TypeReference<Map<String, Object>>() {
            });

            obj.put("country", bankAccount.getCountry());
            obj.put("bank_code", bankAccount.getBankCode());
            obj.put("bank_name", bankAccount.getBankName());
            obj.put("bank_account_no", bankAccount.getBankAccountNo());
            obj.put("account_type", bankAccount.getAccountType());
            obj.put("first_name", bankAccount.getFirstName());
            obj.put("last_name", bankAccount.getLastName());
            obj.put("branch_name", bankAccount.getBranchName());
            // EUR 통화 송금 시, 필수값 return 해줌
            obj.put("address_state_province", remittanceRequestVO.getAddressStateProvince());

            if (CountryCode.JP.getCode().equals(bankAccount.getCountry())) {
                if (jpPostProperties.bankName().equalsIgnoreCase(bankAccount.getBankName())) {
                    // jp Post일경우 계좌번호, 지점명 추가
                    obj.put("jp_post_account_no", jpPostProperties.accountNo());
                    obj.put("jp_post_branch_name", jpPostProperties.branchName());
                }
                obj.put("jp_account_type", "普通");
            }

            voResult.setSuccessInfo(obj);
            if (!originExchangeRate.equals(remittance.getExchangeRate()) || originFromAmt.compareTo(remittance.getFromAmt()) != 0
                    || originFee.compareTo(remittance.getFee()) != 0) {
                voResult.setStatus(Const.STATUS_CHANGED_EXCHANGE_RATE);
                voResult.setMessage(messageSource.getMessage(MsgCode.EXCHANGE_CHANGED_EXCHANGE_RATE, null, voResult.getMessage(), locale));
            }

            // 일본송금 데이터 전송
            if (CountryCode.JP.getCode().equals(remittance.getFromCd())) {
                String bankName = companyService.getBankAccountByBankAccountId(Math.toIntExact(remittance.getBankAccountId())).getBankName();
                RemittanceTravelerSnapshot snapshot = remittanceRepositoryService.getRemittanceTravelerSnapshotByRemittanceId(remittance.getId());

                Long couponIssueSyncId = null;
                if (requestRemittance.getCouponUserId() != null && requestRemittance.getCouponUserId() != -1L) {
                    CouponIssueUser couponIssueUser = couponUserService.getCouponUserById(requestRemittance.getCouponUserId());
                    remittance.setCouponUserId(couponIssueUser.getId());
                    remittance.setCouponDiscountAmt(couponIssueUser.getCouponUsedAmount());
                    couponIssueSyncId = couponIssueUser.getCouponIssueId();
                }
                globalQueueService.sendRemittance(remittance, couponIssueSyncId, bankName, remittanceRequestVO.getAddressStateProvince(), snapshot);
            }

        } catch (CashmallowException e) {
            if (StringUtils.equalsAny(e.getMessage(), "EXCHANGE_CHANGED_EXCHANGE_RATE")) {
                logger.warn(e.getMessage());
            } else {
                logger.error(e.getMessage(), e);
            }

            voResult.setFailInfo(e.getMessage());
            if (e.getMessage().equals(MsgCode.PREVIOUS_REMITTANCE_IN_PROGRESS)) {
                voResult.setStatus(e.getMessage());
            }

            ArrayList<String> array = new ArrayList<>();
            if (StringUtils.isNotBlank(e.getOption())) {
                JSONObject jo = new JSONObject(e.getOption());
                array.add(jo.getString("iso4217"));
                String amtString = NumberFormat.getNumberInstance(locale).format(jo.get("amt"));
                array.add(amtString);
                String maxString = NumberFormat.getNumberInstance(locale).format(jo.get("max"));
                array.add(maxString);
            }

            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), array.toArray(), voResult.getMessage(), locale));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }


    /**
     * 송금 신규 등록일 경우 앱에서 호출
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/kr/traveler/remittance", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRemittanceFromKr(@RequestHeader("Authorization") String token,
                                          @RequestBody String requestBody,
                                          HttpServletRequest request, HttpServletResponse response) {
        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("Invalid token. token={}", token);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);

        logger.info("requestBody={}", requestBody);

        JSONObject jsonBody = new JSONObject(requestBody);

        // 필요목록
        /*
         * exchange_rate - 환율
         * fee, fee_per_amt, fee_rate_amt - 3종 수수료
         * from_cd - from국가
         * from_amt - 유저 입금금액
         * from_money - 수수료 제외한 금액
         * to_cd - to국가
         * to_amt, to_money - 받을 금액
         * bank_account_id - Cashmallow가 입금받을 계좌의 id
         * 그외 받을 사람의 기타 정보들
         */


        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        try {
            Remittance requestRemittance = objectMapperSnake.readValue(requestBody, Remittance.class);
            requestRemittance.setBankAccountId(openbankAccountId);

            if (requestRemittance.getReceiverBankCode().isEmpty()) {
                logger.error("수신자의 bankCode가 공백이 들어왔습니다. userId={}", userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            if (CountryCode.KR.getCode().equals(requestRemittance.getToCd())) {
                logger.error("올바르지 않은 수취 국가, userId={}", userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            String receiverBankAccountNo = requestRemittance.getReceiverBankAccountNo().replaceAll("[^0-9]", "");
            requestRemittance.setReceiverBankAccountNo(receiverBankAccountNo);

            BigDecimal fromMoney = jsonBody.getBigDecimal("from_money");
            BigDecimal fromAmt = fromMoney.add(requestRemittance.getFee());

            if (requestRemittance.getFromAmt().compareTo(fromAmt) != 0) {
                logger.error("송금 신청된 금액이 잘못 되었습니다. fromAmt={}, fromMoney={}, feePerAmt={}, feeRateAmt={}",
                        requestRemittance.getFromAmt(), fromMoney, requestRemittance.getFeePerAmt(), requestRemittance.getFeeRateAmt());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // todo
            // amountLimitService.checkRemittanceEnabled(userId, requestRemittance.getFromCd(), requestRemittance.getFromAmt(),
            //         requestRemittance.getToCd(), requestRemittance.getToAmt(), requestRemittance.getExchangeRate());

            if (requestRemittance.getToCd().equals(CountryCode.KR.getCode())) {
                requestRemittance.setFinancePartnerId(1L);
            } else {
                requestRemittance.setFinancePartnerId(2L);
            }

            // AML 조회 실행
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            requestRemittance.setTravelerId(traveler.getId());
            List<Map<String, Object>> resultMap = octaAmlService.validateRemittanceAmlList(requestRemittance);
            if (resultMap.size() == 0) {
                logger.info("Receiver firstName:{}, lastName:{}, birthDate{}, result:{}", requestRemittance.getReceiverFirstName(), requestRemittance.getReceiverLastName(), requestRemittance.getReceiverBirthDate(), resultMap);
                requestRemittance.setIsConfirmedReceiverAml("Y");
            } else {
                requestRemittance.setIsConfirmedReceiverAml("N");
            }

            String addressStateProvince = ""; // EUR 통화 송금 시에 필수값.. 추후에 한국쪽 서비스하면 작업 해야 됨
            // TODO 쿠폰 적용 추후 추가
            Remittance remittance = remittanceService.requestRemittanceKr(requestRemittance, fromMoney, addressStateProvince);

            logger.info("travelerId={}, convert Start", remittance.getTravelerId());
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> obj = mapper.convertValue(remittance, new TypeReference<Map<String, Object>>() {
            });

            // remittanceService.completeDeposit(remittance.getId());
            // companyService.tryAutoMappingForRemittance(remittance.getId());

            voResult.setSuccessInfo(obj);

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
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        return JsonStr.toJsonString(voResult, response);
    }

    private boolean validateKoreaMobileNo(String phoneCounty, String mobileNo, HttpServletResponse response) throws CashmallowException {

        String no = mobileNo;

        if (!"KOR".equals(phoneCounty)) {
            throw new CashmallowException("REMITTANCE_NOT_VALID_KOREA_MOBILE_NO");
        }

        if (no.startsWith("0")) {
            no = no.substring(1);
        }

        if (no.length() < 9 || no.length() > 10) {
            throw new CashmallowException("REMITTANCE_NOT_VALID_KOREA_MOBILE_NO");
        }

        List<String> prefixes = Arrays.asList("10", "11", "16", "17", "18", "19");

        if (!prefixes.contains(no.substring(0, 2))) {
            throw new CashmallowException("REMITTANCE_NOT_VALID_KOREA_MOBILE_NO");
        }

        return true;
    }

    /**
     * V3 송금 취소
     * 쿠폰 개선 적용 2025.03.07
     * Cancel remittance by traveler.
     *
     * @param token
     * @param remitId
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = {"/traveler/v2/remittances/{remitId}/cancel", "/traveler/v3/remittances/{remitId}/cancel"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelRemittanceV3(@RequestHeader("Authorization") String token,
                                   @PathVariable Long remitId,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "cancelRemittanceV2,V3()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: userId={}", method, userId);

        if (!isRemittanceOwner(userId, remitId, response)) {
            logger.error("{}: The user does not have the remittance. userId={}, remitId={}", method, userId, remitId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        try {
            Remittance remittance = remittanceService.cancelRemittanceByTraveler(remitId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> obj = mapper.convertValue(remittance, new TypeReference<Map<String, Object>>() {
            });

            voResult.setSuccessInfo(obj);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            Locale locale = localeResolver.resolveLocale(request);
            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PutMapping(value = {"/traveler/v3/remittances/{remitId}/deposit"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String completeDepositV3(@RequestHeader("Authorization") String token,
                                    @PathVariable Long remitId,
                                    HttpServletRequest request, HttpServletResponse response) {

        String method = "completeDepositV3()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: userId={}", method, userId);

        if (!isRemittanceOwner(userId, remitId, response)) {
            logger.error("{}: The user does not have the remittance. userId={}, remitId={}", method, userId, remitId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        try {
            Remittance remittance = remittanceService.completeDeposit(remitId);
            // from JP는 매핑을 Global-JP에서 시행, 그외는 직접 진행
            if (!CountryCode.JP.getCode().equals(remittance.getFromCd())) {
                companyService.tryAutoMappingForRemittance(remitId);
            }

            voResult.setSuccessInfo("");
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            Locale locale = localeResolver.resolveLocale(request);
            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 입금 정보 등록 시 영수증 사진 업로드
     *
     * @param token
     * @param remitId
     * @param pictureLists
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/traveler/v2/remittances/{remitId}/deposit-receipts", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String uploadReceiptPhoto(@RequestHeader("Authorization") String token,
                                     @PathVariable long remitId,
                                     @RequestPart("picture") List<MultipartFile> pictureLists,
                                     HttpServletRequest request, HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        try {
            String method = "uploadReceiptPhoto()";

            logger.info("{}: pictureListsCount={}", method, pictureLists.size());

            for (MultipartFile picture : pictureLists) {
                String filename = (picture != null ? picture.getOriginalFilename() : null);
                logger.info("{}: picture={}", method, filename);
            }

            Long userId = authService.getUserId(token);

            if (userId == Const.NO_USER_ID) {
                logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
                return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
            }

            if (!isRemittanceOwner(userId, remitId, response)) {
                logger.error("{}: The user does not have the remittance. userId={}, remitId={}", method, userId, remitId);
                return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
            }

            for (MultipartFile picture : pictureLists) {
                remittanceService.uploadReceiptPhoto(remitId, picture);
            }

            List<RemittanceDepositReceipt> receipts = remittanceService.getRemittanceDepositReceiptList(remitId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
            });

            voResult.setSuccessInfo(obj);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            Locale locale = localeResolver.resolveLocale(request);
            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 입금 영수증 조회. List로 리턴 됨.
     *
     * @param token
     * @param remitId
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/v2/remittances/{remitId}/deposit-receipts", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getReceiptPhotos(@RequestHeader("Authorization") String token,
                                   @PathVariable long remitId,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "getReceiptPhotos()";

        logger.info("{}: remitId={}", method, remitId);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        if (!isRemittanceOwner(userId, remitId, response)) {
            logger.error("{}: The user does not have the remittance. userId={}, remitId={}", method, userId, remitId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        List<RemittanceDepositReceipt> receipts = remittanceService.getRemittanceDepositReceiptList(remitId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        List<HashMap<String, Object>> obj = mapper.convertValue(receipts, new TypeReference<List<HashMap<String, Object>>>() {
        });

        voResult.setSuccessInfo(obj);

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * Re-register remittance.
     *
     * @param token
     * @param remitId
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = {"/traveler/v2/remittances/{remitId}"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String reregisterRemittance(@RequestHeader("Authorization") String token,
                                       @PathVariable Long remitId,
                                       @RequestBody String requestBody,
                                       HttpServletRequest request, HttpServletResponse response) {

        String method = "reregisterRemittance()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("{}: userId={}", method, userId);

        if (!isRemittanceOwner(userId, remitId, response)) {
            logger.error("{}: The user does not have the remittance. userId={}, remitId={}", method, userId, remitId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            // Remittance 와 분리하기 위한 RequestVO
            RemittanceRequestVO remittanceRequestVO = mapper.readValue(requestBody, RemittanceRequestVO.class);

            // Remittance 에 주입
            Remittance params = RemittanceRequestVO.of(remittanceRequestVO);

            if (CountryCode.KR.getCode().equals(params.getToCd())) {
                validateKoreaMobileNo(params.getReceiverPhoneCountry(), params.getReceiverPhoneNo(), response);
            }

            Remittance returnRemit = mallowlinkRemittanceService.reRegisterRemittance(remitId, params);

            Map<String, Object> obj = mapper.convertValue(returnRemit, new TypeReference<>() {
            });
            voResult.setSuccessInfo(obj);

        } catch (CashmallowException | JsonProcessingException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());

            Locale locale = localeResolver.resolveLocale(request);
            voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * If the token's user is owner of the remittance, return true.
     *
     * @param userId
     * @param remitId
     * @param response
     * @return
     */
    private boolean isRemittanceOwner(Long userId, long remitId, HttpServletResponse response) {
        String method = "isOwner()";

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

        try {
            if (traveler == null || remittance == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Cannot find traveler or remittance data.");
                return false;
            }

            if (traveler.getId().equals(remittance.getTravelerId())) {
                return true;
            }

            logger.info("{}: User does not have the permission. userId={}, remitId={}",
                    method, userId, remitId);

            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can not access the remittance data with your token.");

        } catch (IOException e) {
            logger.error("failed to send error. error=" + e.getMessage(), e);
        }

        return false;
    }

    @GetMapping(value = "/traveler/remittance/relationship", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceRelationship(@RequestHeader("Authorization") String token,
                                            HttpServletRequest request, HttpServletResponse response) {

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getRemittanceFundSource(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();

        Locale locale = localeResolver.resolveLocale(request);
        List<Map<String, String>> remittanceRelationship = remittanceService.getRemittanceRelationship(locale);

        voResult.setSuccessInfo(remittanceRelationship);

        return JsonStr.toJsonString(voResult, response);
    }
}
