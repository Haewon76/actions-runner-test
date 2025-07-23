package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.BundleService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.SystemService;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.bundle.Bundle;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.refund.JpRefundAccountInfo;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.system.AppVersion;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.dbs.DbsService;
import com.cashmallow.api.interfaces.edd.UserEddServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.api.interfaces.traveler.dto.*;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.OLD_VERSION_APP_ERROR;

/**
 * Handles requests for the application home page.
 */
@SuppressWarnings("deprecation")
@Controller
public class TravelerController {

    private final Logger logger = LoggerFactory.getLogger(TravelerController.class);

    @Value("${host.cdn.url}")
    private String hostUrl;

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private TravelerServiceImpl travelerService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private SevenBankServiceImpl sevenBankService;

    @Autowired
    private CoatmServiceImpl coatmService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SystemService systemService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private DbsService dbsService;

    @Autowired
    private MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BundleService rnBundleService;

    @Autowired
    private UserEddServiceImpl userEddService;

    @Autowired
    private CountryServiceImpl countryService;

    /**
     * Get the minimum supported version info.
     *
     * @param token
     * @param applicationId
     * @param deviceType
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/versions", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAppVersionInfo(@RequestHeader("Authorization") String token,
                                    @RequestHeader("User-Agent") String userAgent,
                                    @RequestParam("application_id") String applicationId,
                                    @RequestParam("device_type") String deviceType,
                                    HttpServletRequest request, HttpServletResponse response) {

        String method = "getAppVersionInfo()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonStringSnake(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        AppVersion appVersion = systemService.getAppVersion(applicationId, deviceType);

        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(OLD_VERSION_APP_ERROR, null,
                "You are using an outdated version of the app that is no longer supported. Please update to the new version.",
                locale);

        appVersion.setMessage(message);

        ApiResultVO resultVO = new ApiResultVO();
        resultVO.setSuccessInfo(appVersion);

        return JsonStr.toJsonStringSnake(resultVO, response);
    }

    @GetMapping(value = "/traveler/v2/versions", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAppVersionInfoV2(@RequestHeader("Authorization") String token,
                                    @RequestHeader("User-Agent") String userAgent,
                                    @RequestParam("application_id") String applicationId,
                                    @RequestParam("device_type") String deviceType,
                                    @RequestParam("application_version") String applicationVersion,
                                    HttpServletRequest request, HttpServletResponse response) {

        String method = "getAppVersionInfoV2()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: Invalid token. token={}", method, token);
            return JsonStr.toJsonStringSnake(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        AppVersion appVersion = systemService.getAppVersion(applicationId, deviceType);

        Locale locale = localeResolver.resolveLocale(request);
        String message = messageSource.getMessage(OLD_VERSION_APP_ERROR, null,
                "You are using an outdated version of the app that is no longer supported. Please update to the new version.",
                locale);

        appVersion.setMessage(message);

        // 번들 정보 가져오기
        Bundle info = rnBundleService.getBundleInfo(deviceType, applicationVersion);
        if (info != null) {
            appVersion.setBundleUrl(hostUrl + FILE_SERVER_BUNDLE + FILE_SEPARATOR + info.getFileName());
            appVersion.setBundleHashSha1(info.getHashSha1());
        }

        ApiResultVO resultVO = new ApiResultVO();
        resultVO.setSuccessInfo(appVersion);

;        return JsonStr.toJsonStringSnake(resultVO, response);
    }

    // -------------------------------------------------------------------------------
    // 20. 여행자
    // -------------------------------------------------------------------------------


    /**
     * upload new passport photo
     *
     * @param token
     * @param travelerId
     * @param picture
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/traveler/travelers/{travelerId}/certification-photo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addCertificationPhoto(@RequestHeader("Authorization") String token,
                                        @PathVariable String travelerId, @RequestPart("picture") MultipartFile mf, HttpServletRequest request, HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId != Const.NO_USER_ID) {
            try {
                Traveler tokenTraveler = travelerRepositoryService.getTravelerByUserId(userId);
                if (!tokenTraveler.getId().equals(Long.valueOf(travelerId))) {
                    voResult = new ApiResultVO(Const.CODE_INVALID_PARAMS);
                    logger.error("pathParam과 token의 유저ID가 다릅니다. pathId={}, tokenTravelerId={}", userId, tokenTraveler.getId());
                    return CustomStringUtil.encryptJsonString(token, voResult, response);
                }

                Traveler traveler = travelerService.updateCertificationPhoto(userId, mf, "addCertificationPhoto");

                User user = userRepositoryService.getUserByUserId(userId);

                ObjectMapper mapper = new ObjectMapper();
                mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

                Map<String, Object> resultMap = mapper.convertValue(traveler, new TypeReference<Map<String, Object>>() {
                });

                resultMap.put("first_name", user.getFirstName());
                resultMap.put("last_name", user.getLastName());

                resultMap = addToParameterForOldVersion(resultMap);

                voResult.setSuccessInfo(resultMap);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                voResult.setFailInfo(e.getMessage());
            }
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Get traveler
     *
     * @param token
     * @param travelerId
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/travelers/{travelerId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTraveler(@RequestHeader("Authorization") String token,
                              @PathVariable("travelerId") long travelerId,
                              HttpServletRequest request, HttpServletResponse response) throws CashmallowException {

        String method = "getTraveler()";

        long userId = authService.getUserId(token);
        if (userId < 0) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Locale locale = localeResolver.resolveLocale(request);
        Map<String, Object> resultMap = travelerService.getTravelerMapByUserId(userId, locale);
        resultVO.setSuccessInfo(resultMap);

        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    // 2020-07-31 traver 테이블의 컬럼네임 수정으로 인해 구버전의 JSON과 형태가 달라져서 에러를 피하기 위해서 만듬.
    @Deprecated
    private Map<String, Object> addToParameterForOldVersion(Map<String, Object> resultMap) {
        String firstName = "";
        String lastName = "";
        String passportFirstName = "";
        String passportLastName = "";
        String passportNumber = "";

        if (resultMap.get("local_last_name") != null) {
            firstName = resultMap.get("local_first_name").toString();
            lastName = resultMap.get("local_last_name").toString();
        }

        if (resultMap.get("en_last_name") != null) {
            passportFirstName = resultMap.get("en_first_name").toString();
            passportLastName = resultMap.get("en_last_name").toString();
        }

        String passportOk = resultMap.get("certification_ok").toString();

        if (resultMap.containsKey("certification_photo")) {
            resultMap.put("passport_photo", resultMap.get("certification_photo").toString());
        }

        return resultMap;
    }

    // -------------------------------------------------------------------------------
    // 7. 여행자 지갑
    // -------------------------------------------------------------------------------

    /**
     * Get the traveler's wallets
     *
     * @param token
     * @param travelerId
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/travelers/{travelerId}/traveler-wallets", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTravelerWallets(@RequestHeader("Authorization") String token,
                                     @PathVariable long travelerId,
                                     HttpServletRequest request, HttpServletResponse response) throws CashmallowException {

        String method = "getTravelerWallets()";

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        long userId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        if (traveler == null || userId != traveler.getUserId()) {
            logger.info("{}: Invalid token. userId={}, travelerId={}", method, userId, travelerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("{}: userId={}, travelerId={}", method, userId, traveler.getId());

        // 진행중인 인출 건 모두 취소 처리
        mallowlinkWithdrawalService.cancelAllWithdrawal(traveler);

        List<TravelerWalletVO> walletVoList = walletRepositoryService.getTravelerWalletVoListByTravelerId(traveler.getId());

        voResult.setSuccessInfo(walletVoList);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // edited by Aelx 20170810 기능: 9.4.2 환전신청시 캐시멜로 계좌 조회
    @PostMapping(value = "/json/traveler/getBankInfo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getBankAccountList(@RequestHeader("Authorization") String token,
                                     @RequestBody String requestBody,
                                     HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getBankInfo(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("getBankInfo() : {}",  jsonStr);
        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String fromCd = (String) map.get("from_cd");

        List<Map<String, Object>> bankAccounts = companyService.getBankAccountList(fromCd, "Y");
        voResult.setSuccessInfo(bankAccounts);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 10.1.1 국가별 모든 가맹점 리스트
    @GetMapping(value = "/traveler/storekeepers", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getStorekeeperListByCountry(@RequestHeader("Authorization") String token,
                                              @RequestParam String country,
                                              HttpServletRequest request, HttpServletResponse response) {

        String method = "getStorekeeperListByCountry()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("{}: country={}", method, country);

        List<WithdrawalPartner> withdrawalPartners = partnerService.getWithdrawalPartnerListByCountry(country, "Y");

        if (withdrawalPartners != null && !withdrawalPartners.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<HashMap<String, Object>> obj = mapper.convertValue(withdrawalPartners, new TypeReference<List<HashMap<String, Object>>>() {
            });
            voResult.setSuccessInfo(obj);
        } else {
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(MsgCode.CASHOUT_ERROR_REQUEST_STOPPED_SERVICE, null, locale);
            voResult.setFailInfo(message);
            // voResult.setSuccessInfo(null);
        }
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * 가맹점 ATM 리스트 조회
     */
    @GetMapping(value = "/traveler/storekeepers/{storekeeperId}/atms", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getWithdrawalPartnerCashpointListNearby(@RequestHeader("Authorization") String token,
                                                          @PathVariable Long storekeeperId,
                                                          @RequestParam Double lat, @RequestParam Double lng,
                                                          @RequestParam(value = "amount", required = false) String amount,
                                                          HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getWithdrawalPartnerCashpointListNearby(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("getWithdrawalPartnerCashpointListNearby(): storekeeperId={}, lat={}, lng={}", storekeeperId, lat, lng);

        try {
            WithdrawalPartner withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(storekeeperId);
            List<WithdrawalPartnerCashpoint> withdrawalPartnerCashpoints = switch (withdrawalPartner.getStorekeeperType()) {
                case A, C, P001 -> partnerService.getWithdrawalPartnerCashpointListByWithdrawalPartnerId(storekeeperId, lat, lng);
                case M001 -> sevenBankService.getAtmList(storekeeperId, lat, lng); // QBC
                case M002 -> coatmService.getAtmList(storekeeperId, lat, lng);    // Coocon
                default -> mallowlinkWithdrawalService.getAtmList(storekeeperId, lat, lng);
            };

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            List<Map<String, Object>> maps = new ArrayList<>();
            for (WithdrawalPartnerCashpoint sa : withdrawalPartnerCashpoints) {
                maps.add(mapper.convertValue(sa, new TypeReference<Map<String, Object>>() {
                }));
            }

            voResult.setSuccessInfo(maps);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        // error message localization
        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        logger.debug("getWithdrawalPartnerCashpointListNearby(): voResult={}", new JSONObject(voResult));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 8.2.2 가맹점 정보 상세 조회 API - 가맹점 ID.로 가맹점 상세 정보 조회.
    @GetMapping(value = "/traveler/storekeepers/{storekeeperId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getWithdrawalPartner(@RequestHeader("Authorization") String token,
                                       @PathVariable long storekeeperId,
                                       HttpServletRequest request, HttpServletResponse response) {

        String method = "getWithdrawalPartner()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("{}: userId={}, WithdrawalPartnerId={}", method, userId, storekeeperId);

        Map<String, Object> withdrawalPartner = partnerService.getWithdrawalPartnerExtByWithdrawalPartnerId(storekeeperId);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        Object obj = mapper.convertValue(withdrawalPartner, new TypeReference<Map<String, Object>>() {
        });

        voResult.setSuccessInfo(obj);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // -------------------------------------------------------------------------------
    // 28. 여행자 환불 계좌 정보
    // -------------------------------------------------------------------------------

    // traveler/reset-device
    @GetMapping(value = NotificationService.URLs.CONFIRM_DEVICE_RESET, produces = GlobalConst.PRODUCES_HTML)
    @ResponseBody
    public String confirmDeviceResetByTraveler(@RequestParam("id") long userId, @RequestParam String accountToken,
                                               @RequestParam String captcha,
                                               HttpServletRequest request, HttpServletResponse response
            , Model model) {

        logger.info("confirmDeviceResetByTraveler(): userId={}, accountToken={}", userId, accountToken);
        logger.debug("captcha={}", captcha);

        Locale locale = localeResolver.resolveLocale(request);

        String messageHtml;
        try {
            messageHtml = travelerService.confirmDeviceResetByTraveler(userId, accountToken, locale, captcha, false);
        } catch (CashmallowException e) {
            messageHtml = e.getOption();
        }

        // model.addAttribute("html", messageHtml);
        // return "htmlWapper";
        return messageHtml;
    }

    @GetMapping(value = NotificationService.URLs.CONFIRM_DEVICE_RESET_CAPTCHA)
    public String captchaDeviceReset(@RequestParam long id,
                                     @RequestParam(required = false, name = "accountToken") String accountToken1,
                                     @RequestParam(required = false, name = "amp;accountToken") String accountToken2,
                                     Model model, HttpServletRequest request) {
        String accountToken = accountToken1 != null ? accountToken1 : accountToken2;

        logger.info("captchaDeviceReset(): userId={}, accountToken={}", id, accountToken);

        String url = String.format("%s%s?id=%d&accountToken=%s", request.getContextPath(), NotificationService.URLs.CONFIRM_DEVICE_RESET, id, accountToken);

        User user = userRepositoryService.getUserByUserId(id);
        String continueNewDevice = messageSource.getMessage("RESET_DEVICE_CONTINUE_NEW_DEVICE", null, user.getCountryLocale());

        model.addAttribute("resetURL", url);
        model.addAttribute("locale", user.getCountryLocale().getLanguage());
        model.addAttribute("continueNewDevice", continueNewDevice);

        return "captcha/reset_device_captcha";
    }

    // -------------------------------------------------------------------------------
    // 29. 환불
    // -------------------------------------------------------------------------------

    // 연관된 지갑 일괄 환불 계산, 송금 환불 계산.
    @GetMapping(value = "/traveler/v3/refunds/calculate", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String calcRefund(@RequestHeader("Authorization") String token,
                             @RequestParam(value = "wallet_id", required = false) Long walletId,
                             @RequestParam(value = "remit_id", required = false) Long remitId,
                             HttpServletRequest request, HttpServletResponse response) {

        Long userId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        if (userId == Const.NO_USER_ID) {
            logger.info("getRequestRefund(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(voResult, response);
        }

        try {
            ExchangeCalcVO vo;
            if (walletId != null) {
                vo = refundService.calcRefundExchange(walletId);
            } else if (remitId != null) {
                vo = refundService.calcRefundRemit(remitId);
            } else {
                throw new CashmallowException("INTERNAL_SERVER_ERROR");
            }
            logger.debug("calcRefund(): {}", vo.toString());
            voResult.setSuccessInfo(vo);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = {"/traveler/{travelerId}/jp/refunds/account"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getJpRefundAccount(@RequestHeader("Authorization") String token,
                                        @PathVariable Long travelerId,
                                        HttpServletRequest request, HttpServletResponse response) {
        String method = "getJpRefundAccount()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        // token과 pathVariable의 정보 일치여부
        Traveler requestTraveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (!requestTraveler.getId().equals(travelerId)) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_USER_ID), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);

        JpRefundAccountInfo jpRefundAccountInfo = refundRepositoryService.getJpRefundAccountInfoByTravelerId(travelerId);
        voResult.setSuccessInfo(jpRefundAccountInfo);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = {"/traveler/jp/refunds/account"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registerJpRefundAccount(@RequestHeader("Authorization") String token, @RequestBody String encryptedRequestBody,
                                          HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String method = "registerJpRefundAccount()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        Locale locale = localeResolver.resolveLocale(request);

        String jsonString = CustomStringUtil.decode(token, encryptedRequestBody);

        logger.info("{}: requestBody={}", method, jsonString);

        JpRefundAccountInfoRequest jpRefundAccountInfoRequest = objectMapper.readValue(jsonString, JpRefundAccountInfoRequest.class);

        try {
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            refundService.registerJpRefundAccountInfo(jpRefundAccountInfoRequest, traveler.getId());

            voResult.setSuccessInfo("");
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            voResult.setFailInfo(errMsg);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PutMapping(value = {"/traveler/jp/refunds/account/{jpRefundAccountId}"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateJpRefundAccount(@RequestHeader("Authorization") String token, @RequestBody String encryptedRequestBody,
                                          @PathVariable Long jpRefundAccountId,
                                          HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String method = "updateJpRefundAccount()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        Locale locale = localeResolver.resolveLocale(request);

        String jsonString = CustomStringUtil.decode(token, encryptedRequestBody);

        logger.info("{}: requestBody={}", method, jsonString);

        JpRefundAccountInfoRequest jpRefundAccountInfoRequest = objectMapper.readValue(jsonString, JpRefundAccountInfoRequest.class);

        try {
            refundService.updateJpRefundAccountInfo(jpRefundAccountInfoRequest, userId, jpRefundAccountId);

            voResult.setSuccessInfo("");
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            voResult.setFailInfo(errMsg);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * Request refund JP
     *
     * @param token
     * @param encryptedRequestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = {"/traveler/jp/refunds"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRefundForJP(@RequestHeader("Authorization") String token, @RequestBody String encryptedRequestBody,
                                     HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String method = "requestRefundForJP()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        Locale locale = localeResolver.resolveLocale(request);

        // RefundJpRequest
        String jsonString = CustomStringUtil.decode(token, encryptedRequestBody);

        logger.info("{}: requestBody={}", method, jsonString);

        RefundJpRequest refundJpRequest = objectMapper.readValue(jsonString, RefundJpRequest.class);

        try {
            refundService.requestJpNewRefund(refundJpRequest, userId);

            voResult.setSuccessInfo("");
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            voResult.setFailInfo(errMsg);

            if (REFUND_CHANGED_EXCHANGE_RATE.equals(e.getMessage())) {
                voResult.setStatus(STATUS_CHANGED_EXCHANGE_RATE);
            }
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * Request refund V6
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = {"/traveler/v5/refunds", "/traveler/v6/refunds"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRefundV6(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                  HttpServletRequest request, HttpServletResponse response) {
        String method = "requestRefundV6()";
        logger.info("{}: requestBody={}", method, requestBody);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. token={}", method, token);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(Const.CODE_FAILURE);
        Locale locale = localeResolver.resolveLocale(request);

        JSONObject jo = new JSONObject(requestBody);

        NewRefund requestNewRefund = new NewRefund();
        requestNewRefund.setFromCd(jo.getString("from_cd"));
        requestNewRefund.setFromAmt(jo.getBigDecimal("from_amt"));
        requestNewRefund.setToCd(jo.getString("to_cd"));
        requestNewRefund.setToAmt(jo.getBigDecimal("to_amt").setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFee(jo.getBigDecimal("fee").setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setExchangeRate(jo.getBigDecimal("exchange_rate").setScale(6, RoundingMode.HALF_UP));
        requestNewRefund.setFeePerAmt(jo.getBigDecimal("fee_per_amt").setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setFeeRateAmt(jo.getBigDecimal("fee_rate_amt").setScale(2, RoundingMode.HALF_UP));
        requestNewRefund.setRefundStatus(NewRefund.RefundStatusCode.AP);

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        try {
            if (requestNewRefund.getToCd().equals(CountryCode.JP.getCode())) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // traveler의 BankInfoId가 없으면 환불 불가
            if (validateTravelerBankBookVerified(locale, traveler, voResult)) {
                return JsonStr.toJsonString(voResult, response);
            }

            if (jo.has("wallet_id")) {
                requestNewRefund.setWalletId(jo.getLong("wallet_id"));
                refundService.requestNewRefund(traveler, requestNewRefund, RelatedTxnType.EXCHANGE);
            } else if (jo.has("remit_id")) {
                requestNewRefund.setRemitId(jo.getLong("remit_id"));
                refundService.requestNewRefund(traveler, requestNewRefund, RelatedTxnType.REMITTANCE);
            }

            dbsService.tryAutoRefund(requestNewRefund);

            voResult.setSuccessInfo("");
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            voResult.setFailInfo(errMsg);

            if (REFUND_CHANGED_EXCHANGE_RATE.equals(e.getMessage())) {
                voResult.setStatus(STATUS_CHANGED_EXCHANGE_RATE);
            }
        }

        return JsonStr.toJsonString(voResult, response);
    }

    private boolean validateTravelerBankBookVerified(Locale locale, Traveler traveler, ApiResultVO voResult) throws CashmallowException {
        if (ObjectUtils.isEmpty(traveler.getBankInfoId())) {
            if (Boolean.TRUE.equals(exchangeService.isPossibleToCancelBankBookverified(traveler.getId()))) {
                String message = messageSource.getMessage(MsgCode.TRAVELER_REFUND_BANK_ACCOUNT_RE_VERIFICATION, null, locale);
                travelerService.verifyBankAccountByAdmin(traveler, "R", message);
                voResult.setFailInfo(message);
                return true;
            } else {
                String message = messageSource.getMessage(MsgCode.REFUND_ERROR_REQUEST_PROCESS_IN_EXCHANGE, null, locale);
                voResult.setFailInfo(message);
                return true;
            }
        }
        return false;
    }

    @DeleteMapping(value = {"/traveler/v4/refunds/{refundId}", "/traveler/jp/refunds/{refundId}"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRefundCancelV4(@RequestHeader("Authorization") String token,
                                        @PathVariable long refundId,
                                        HttpServletRequest request, HttpServletResponse response) {

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("requestRefundCancelV4(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        logger.info("requestRefundCancelV4(): userId={}, refundId={}", userId, refundId);

        ApiResultVO voResult = new ApiResultVO();
        try {
            refundService.cancelNewRefundByTraveler(refundId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    // TODO: 24.09.13이후 앱 강제업데이트시 v4 refund 삭제
    @GetMapping(value = {"/traveler/v4/refunds", "/traveler/v5/refunds", "/traveler/v6/refunds"}, params = {"wallet_id"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getNewRefundByWalletId(@RequestHeader("Authorization") String token,
                                         @RequestParam("wallet_id") Long walletId,
                                         HttpServletRequest request, HttpServletResponse response) {

        String method = "getNewRefundByWalletId()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: walletId={}", method, walletId);

        try {
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            if (traveler == null) {
                logger.error("{}: traveler 정보를 찾을 수 없습니다. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            NewRefund newRefund = refundRepositoryService.getNewRefundInProgressByWalletId(walletId, traveler.getId());
            voResult.setSuccessInfo(RefundResponse.of(newRefund));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo("INTERNAL_SERVER_ERROR");
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // TODO: 24.09.13이후 앱 강제업데이트시 v4 refund 삭제
    @GetMapping(value = {"/traveler/v4/refunds", "/traveler/v5/refunds", "/traveler/v6/refunds"}, params = {"remit_id"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getNewRefundByRemitId(@RequestHeader("Authorization") String token,
                                        @RequestParam("remit_id") long remitId,
                                        HttpServletRequest request, HttpServletResponse response) {

        String method = "getNewRefundByRemitId()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return JsonStr.toJsonString(voResult, response);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            voResult.setSuccessInfo();
            return JsonStr.toJsonString(voResult, response);
        }

        long travelerId = traveler.getId();

        logger.info("{}: remitId={}, travelerId={}", method, remitId, travelerId);

        try {
            NewRefund newRefund = refundRepositoryService.getNewRefundNotCancelByRemitId(remitId, travelerId);
            voResult.setSuccessInfo(RefundResponse.of(newRefund));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo("INTERNAL_SERVER_ERROR");
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value ={ "/traveler/edd"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String validateTravelerEdd(@RequestHeader("Authorization") String token,
                                      @RequestParam(value = "from_cd") String fromCd, @RequestParam(value = "to_cd") String toCd,
                                      @RequestParam(value = "from_money") BigDecimal fromMoney, @RequestParam(value = "to_money") BigDecimal toMoney,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "validateTravelerEdd()";
        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        Locale locale = localeResolver.resolveLocale(request);

        logger.info("{} : fromCd={}, toCd={}, fromMoney={}, toMoney={}", method, fromCd, toCd, fromMoney, toMoney);

        ApiResultVO voResult = new ApiResultVO();
        try {
            if (StringUtils.isBlank(fromCd) || StringUtils.isBlank(toCd)) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
            if (fromMoney.compareTo(BigDecimal.ZERO) < 0 || toMoney.compareTo(BigDecimal.ZERO) < 0) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);

            Country fromCountry = countryService.getCountry(fromCd);

            TravelerEddValidationVO eddValidationVO = userEddService.verificationUserEdd(fromCountry, fromMoney, userId, traveler.getId(), exchangeConfig, locale);

            voResult.setSuccessInfo();
            if (StringUtils.equals(Const.STATUS_FAILURE, eddValidationVO.getStatus())) {
                voResult.setSuccessInfo(eddValidationVO);
                voResult.setStatus(eddValidationVO.getStatus());
                voResult.setMessage(eddValidationVO.getMessage());
            }
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

}
