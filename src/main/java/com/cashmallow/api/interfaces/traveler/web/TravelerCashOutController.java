package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.impl.CashOutServiceImpl;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashOut.CoStatus;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner.KindOfStorekeeper;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.PartnerCancelService;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.WithdrawalResponse;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.api.interfaces.traveler.dto.RequestCashOutVO;
import com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyService;
import com.cashmallow.api.interfaces.traveler.web.cashout.CashoutAgencyV2;
import com.cashmallow.api.interfaces.traveler.web.cashout.CashoutGuideV2;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.cashmallow.api.domain.shared.MsgCode.*;

/**
 * Handles requests for the application home page.
 */
@Controller
public class TravelerCashOutController {

    private final Logger logger = LoggerFactory.getLogger(TravelerCashOutController.class);

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private TravelerServiceImpl travelerService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private SevenBankServiceImpl sevenBankService;

    @Autowired
    private CoatmServiceImpl coatmService;

    @Autowired
    private MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private PartnerCancelService partnerCancelService;

    @Autowired
    private CashoutAgencyService cashoutAgencyService;

    // -------------------------------------------------------------------------------
    // 12. 여행자 인출
    // -------------------------------------------------------------------------------

    /**
     * Get a withdrawal guide according to kindOfStorekeeper
     */
    @GetMapping(value =  "/traveler/v2/storekeepers/{storekeeperId}/cashout-guide", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCashOutGuide(@RequestHeader("Authorization") String token,
                                  @PathVariable long storekeeperId,
                                  @RequestParam(required = false) Long walletId,
                                  HttpServletRequest request, HttpServletResponse response) {

        String method = "getCashOutGuide()";
        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: Invalide token. storekeeperId={}", method, storekeeperId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("{}: storekeeperId={}", method, storekeeperId);

        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(storekeeperId);

        List<CashoutAgencyV2> agencies = new ArrayList<>();
        BigDecimal cashoutAmt = partnerCancelService.getCashoutReservedAmt(walletId);
        agencies.addAll(cashoutAgencyService.getV2Agencies(KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper()), cashoutAmt));
        voResult.setSuccessInfo(new CashoutGuideV2(agencies));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 여행자 인출 신청. ATM(Seven Bank) 인출 신청 포함
    @Deprecated
    @PostMapping(value = {"/traveler/cashouts", "/traveler/cashout"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestCashOut(@RequestHeader("Authorization") String token,
                                 @RequestBody String requestBody,
                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "requestCashOut()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("{}: userId={} TravelerController.requestTravelerCashOut(): jsonStr={}", method, userId, jsonStr);

        RequestCashOutVO rCashOutVO = (RequestCashOutVO) JsonStr.toObject(RequestCashOutVO.class.getName(), jsonStr);
        try {
            WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(rCashOutVO.getWithdrawal_partner_id());
            KindOfStorekeeper kindOfStorekeeper = KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper());
            if (KindOfStorekeeper.M001.equals(kindOfStorekeeper)) {
                Map<String, Object> result = sevenBankService.requestCashOut(userId, withdrawalPartner, rCashOutVO);
                voResult.setSuccessInfo(result);
            } else if (KindOfStorekeeper.M002.equals(kindOfStorekeeper)) {
                Map<String, Object> result = coatmService.requestCashOut(userId, withdrawalPartner, rCashOutVO);
                voResult.setSuccessInfo(result);
            } else {
                cashOutService.requestTravelerCashOut(userId, withdrawalPartner, rCashOutVO);
                voResult.setSuccessInfo();
            }
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = {"/traveler/cashout/qr"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestCashOutQr(@RequestHeader("Authorization") String token,
                                   @RequestBody String requestBody,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "requestCashOutQr()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);
        Locale locale = localeResolver.resolveLocale(request);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: userId={} TravelerController.requestCashOutQr(): requestBody={}", method, userId, requestBody);

        JSONObject jo = new JSONObject(requestBody);
        Long withdrawalPartnerId = jo.getLong("withdrawal_partner_id");
        Long walletId = jo.getLong("wallet_id");

        WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
        KindOfStorekeeper kindOfStorekeeper = KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper());
        return switch (kindOfStorekeeper) {
            case SCB -> {
                String qrData = jo.getString("qrData");

                try {
                    mallowlinkWithdrawalService.qr(userId, walletId, qrData);
                    voResult.setSuccessInfo();
                    yield JsonStr.toJsonString(voResult, response);
                } catch (Exception e) {
                    if (StringUtils.equals(e.getMessage(), ML_INVALID_QR_CODE)) {
                        String message = messageSource.getMessage(ML_INVALID_QR_CODE, null, "Invalid QR Code", locale);
                        voResult.setFailInfo(message);
                        voResult.setStatus(ML_INVALID_QR_CODE);
                        yield JsonStr.toJsonString(voResult, response);
                    }

                    if (StringUtils.equals(e.getMessage(), INVALID_ATM)) {
                        String message = messageSource.getMessage(INVALID_ATM, null, "Unable withdrawal in VTM machine.<br>Please use ATM/CDM machine.", locale);
                        voResult.setFailInfo(message.replaceAll("<br>", "\n"));
                        voResult.setStatus(INVALID_ATM);
                        yield JsonStr.toJsonString(voResult, response);
                    }

                    logger.error(e.getMessage(), e);
                    voResult.setFailInfo(INTERNAL_SERVER_ERROR);
                    voResult.setStatus(INTERNAL_SERVER_ERROR);
                    yield JsonStr.toJsonString(voResult, response);
                }
            }
            default -> {
                voResult.setFailInfo("Invalid Partner");
                yield JsonStr.toJsonString(voResult, response);
            }
        };
    }

    // 여행자 인출 신청. ATM(Seven Bank) 인출 신청 포함
    // cashouts - AOS, cashout - IOS 에서 각각 사용 중
    @PostMapping(value = {"/traveler/v2/cashouts", "/traveler/v2/cashout"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestCashOutV2(@RequestHeader("Authorization") String token,
                                   @RequestBody String requestBody,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "requestCashOutV2()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);
        Locale locale = localeResolver.resolveLocale(request);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: userId={} TravelerController.requestTravelerCashOutV2(): requestBody={}", method, userId, requestBody);

        JSONObject jo = new JSONObject(requestBody);

        BigDecimal travelerCashoutAmt = jo.getBigDecimal("traveler_cash_out_amt");
        Long withdrawalPartnerId = jo.getLong("withdrawal_partner_id");
        int withdrawalPartnerAgencyId = 0;
        try {
            withdrawalPartnerAgencyId = jo.getInt("withdrawal_partner_agency_id"); // 옵셔널 AJ인 경우 필수
        } catch (Exception ignore) {
        }
        Long walletId = jo.getLong("wallet_id");
        String countryCode = jo.getString("country");
        // todo app dateFomat 수정 필요.
        // String cashoutReservedDate = jo.getString("cashout_reserved_date");
        String cashoutReservedDate = ZonedDateTime.now().withZoneSameInstant(CountryCode.of(countryCode).getZoneId()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Integer requestTime = jo.getInt("request_time");

        String contactType = "";
        String contactId = "";

        if (jo.has("contact_type")) {
            contactType = jo.getString("contact_type");
            contactId = jo.getString("contact_id");
        }

        final String walletKey = RedisService.REDIS_KEY_TRAVELER_WALLET + walletId;
        try {
            // 키가 레디스에 있으면 중복 요청
            if (!redisService.putIfAbsent(walletKey, "wallet reserve", 5, TimeUnit.SECONDS)) {
                voResult.setFailInfo(messageSource.getMessage(CASHOUT_IN_PROGRESS, null, voResult.getMessage(), locale));
                return JsonStr.toJsonString(voResult, response);
            }


            User user = userRepositoryService.getUserByUserId(userId);
            WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(withdrawalPartnerId);
            KindOfStorekeeper kindOfStorekeeper = KindOfStorekeeper.valueOf(withdrawalPartner.getKindOfStorekeeper());
            switch (kindOfStorekeeper) {
                case C, A, P001 -> {
                    // 레거시 인출
                    if (contactType != null && contactId != null) {
                        cashOutService.requestTravelerCashOutV2(userId, withdrawalPartner, travelerCashoutAmt, walletId, countryCode,
                                cashoutReservedDate, requestTime, contactType, contactId);
                    }
                    voResult.setSuccessInfo();
                }
                case M001 -> {
                    // QBC
                    Map<String, Object> resultSeven = sevenBankService.requestCashOutV2(userId, withdrawalPartner, travelerCashoutAmt, walletId, countryCode,
                            cashoutReservedDate, requestTime);
                    voResult.setSuccessInfo(resultSeven);
                }
                case M002 -> {
                    // Coatm
                    Map<String, Object> resultCoatm = coatmService.requestCashOutV2(userId, withdrawalPartner, travelerCashoutAmt, walletId, countryCode,
                            cashoutReservedDate, requestTime);
                    voResult.setSuccessInfo(resultCoatm);
                }
                default -> {
                    // mallowlink
                    WithdrawalResponse withdrawalResponse = mallowlinkWithdrawalService.requestCashOut(userId, withdrawalPartner, walletId, withdrawalPartnerAgencyId);
                    voResult.setSuccessInfo(withdrawalResponse);
                }
            }

            if (CountryCode.JP.equals(user.getCountryCode())) {
                CashOut cashOut = cashoutRepositoryService.getCashOutLastOpByWalletId(walletId);
                globalQueueService.sendWithdrawal(cashOut);
            }

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        // error message localization
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 여행자의 진행 중 인출 정보 조회
     *
     * @param token
     * @param coStatus
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = {"/traveler/cashouts", "/traveler/cashout"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCashOutInfo(@RequestHeader("Authorization") String token, @RequestParam(value = "coStatus") String coStatus,
                                 HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (Const.NO_USER_ID == userId) {
            logger.info("getCashOutInfo(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.info("getCashOutInfo(): coStatus={}", coStatus);

        try {
            Map<String, Object> cashoutMap = null;
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            if (traveler != null && "OP".equals(coStatus)) {
                cashoutMap = cashOutService.getCashoutOpByTraveler(traveler);
            }
            resultVO.setSuccessInfo(cashoutMap);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping(value = "/traveler/cashout/{storekeeper}/{walletId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCashOutOtpInfo(@RequestHeader("Authorization") String token,
                                    @PathVariable KindOfStorekeeper storekeeper,
                                    @PathVariable Long walletId,
                                    HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (Const.NO_USER_ID == userId) {
            logger.info("getCashOutInfo(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        try {
            // 유효한 OTP가 있는지 확인
            WithdrawalResponse cachedWithdrawalResponse = partnerCancelService.getCachedWithdrawalResponse(storekeeper, walletId);
            if (cachedWithdrawalResponse == null) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
            resultVO.setSuccessInfo(cachedWithdrawalResponse);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * Regenerate QR code
     *
     * @param token
     * @param cashoutId
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = "/traveler/cashouts/{cashoutId}/regenerate-qr-code", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String regenerateCashoutQrCode(@RequestHeader("Authorization") String token, @PathVariable Long cashoutId,
                                          HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (Const.NO_USER_ID == userId) {
            logger.info("getCashOutInfo(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.info("regenerateCashoutQrCode(): cashoutId={}", cashoutId);

        try {
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
            CashOut cashout = cashOutService.regenerateCashoutQrCode(traveler, cashoutId);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            Map<String, Object> object = mapper.convertValue(cashout, new TypeReference<Map<String, Object>>() {
            });
            object.put("cash_out_id", cashout.getId());

            resultVO.setSuccessInfo(object);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * Cancel cash-out reservation
     *
     * @param token
     * @param cashoutId
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = "/traveler/v2/cashouts/{cashoutId}/cancel", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelRequestedCashOutV2(@RequestHeader("Authorization") String token,
                                           @RequestHeader(value = "Socash-Authorization", required = false) String socashToken,
                                           @RequestBody String requestBody,
                                           @PathVariable Long cashoutId, HttpServletRequest request, HttpServletResponse response) {

        String method = "cancelRequestedCashOutV2()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (Const.NO_USER_ID == userId) {
            logger.info("{}: NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: userId={}, cashoutId={}, jsonStr={}", method, userId, cashoutId, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(requestBody);
        KindOfStorekeeper kindOfStorekeeper = KindOfStorekeeper.valueOf((String) map.get("kind_of_storekeeper"));

        try {
            switch (kindOfStorekeeper) {
                case C, A, M002, P001 -> cashOutService.cancelCashoutByTraveler(userId, cashoutId);
                case M001 -> sevenBankService.cancelCashoutSevenBank(userId, cashoutId, CoStatus.TC.name());
                default -> mallowlinkWithdrawalService.cancel(userId, cashoutId, CoStatus.TC);
            }
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            voResult.setFailInfo(e.getMessage());
            if (e.getMessage().equals(Const.ALREADY_COMPLETE)) {
                voResult.setStatus(Const.ALREADY_COMPLETE);
            } else {
                logger.error(e.getMessage(), e);
            }
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

}
