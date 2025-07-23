package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.*;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.CountryFee;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser.InactiveType;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.InvalidPasswordException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.terms.TermsHistoryService;
import com.cashmallow.api.interfaces.traveler.OsType;
import com.cashmallow.api.interfaces.traveler.dto.UserRegV2VO;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.AddressJapanServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.AddressKoreanServiceImpl;
import com.cashmallow.common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * Handles requests for the application home page.
 */
@Controller
@SuppressWarnings({"unused", "deprecation","dep-ann"})
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private static final long SESSION_TIMEOUT = 600000; // millisecond

    @Value("${cashmallow.homepage.whitelist}")
    private Set<String> homepageWhitelist;

    @Value("${google.geoKey.aos}")
    private String googleAosGeoApiKey;
    @Value("${google.geoKey.ios}")
    private String googleIosGeoApiKey;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private FileService fileService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    @Autowired
    ExchangeServiceImpl exchangeService;

    @Autowired
    RefundServiceImpl refundService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private InactiveUserServiceImpl inactiveUserService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private AddressKoreanServiceImpl addressKoreanService;

    @Autowired
    private AddressEnglishServiceImpl addressEnglishService;

    @Autowired
    private AddressJapanServiceImpl addressJapanService;

    @Autowired
    private TermsHistoryService termsHistoryService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private Gson gsonPretty;

    @Autowired
    private Gson gson;

    @Value("${host.cdn.url}")
    private String cdnUrl;

    private final List<IpAddressMatcher> whitelist = new ArrayList<>();

    @Autowired
    private JsonUtil jsonUtil;

    @PostConstruct
    private void init() {
        logger.info("homepageWhitelist={}", homepageWhitelist);
        homepageWhitelist.stream().map(IpAddressMatcher::new).forEach(whitelist::add);
    }

    /**
     * Client의 FCM token을 등록/갱신한다.
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/traveler/users/{userId}/fcm-token", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addFcmToken(@RequestHeader("Authorization") String token,
                              @PathVariable long userId, @RequestBody String requestBody,
                              HttpServletRequest request, HttpServletResponse response) {

        String method = "addFcmToken()";

        String jsonStr = CustomStringUtil.decode(token, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        String fcmToken = (String) map.get("fcm_token");
        String devType = (String) map.get("dev_type");

        long tUserId = authService.getUserId(token);

        if (userId != tUserId) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();
        try {
            notificationService.addFcmToken(userId, fcmToken, devType);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.warn(e.getMessage(), e);

            Locale locale = localeResolver.resolveLocale(request);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);

            voResult.setFailInfo(errMsg);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Change password
     *
     * @param token
     * @param userId
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = "/traveler/users/{userId}/password", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String changePassword(@RequestHeader("Authorization") String token,
                                 @PathVariable long userId, @RequestBody String requestBody,
                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "jsonChangePassword()";

        long tokenUserId = authService.getUserId(token);

        if (tokenUserId != userId) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String currentPassword = (String) map.get("currentPassword");
        String newPassword = (String) map.get("newPassword");

        ApiResultVO resultVO = new ApiResultVO();
        Locale locale = localeResolver.resolveLocale(request);
        try {
            userService.changePassword(userId, currentPassword, newPassword);
            resultVO.setSuccessInfo();
        } catch (InvalidPasswordException e) {
            String message = messageSource.getMessage(e.getMessage(), null, locale);
            resultVO.setFailInfo(message);
            resultVO.setStatus(STATUS_INVALID_PASSWORD);
        } catch (CashmallowException e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        Object[] args = new Object[]{MIN_PWD_LEN};
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), args, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * add profile photo
     *
     * @param token
     * @param userId
     * @param picture
     * @param response
     * @return
     */
    @PostMapping(value = "/traveler/users/{userId}/profile-photo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addProfilePhoto(@RequestHeader("Authorization") String token,
                                  @PathVariable String userId, @RequestPart("picture") MultipartFile picture,
                                  HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Long userId1 = authService.getUserId(token);
        if (userId1 == NO_USER_ID || !userId1.equals(Long.valueOf(userId))) {
            voResult = new ApiResultVO(CODE_INVALID_TOKEN);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String originalFilename = picture != null ? picture.getOriginalFilename() : null;
        logger.info("updateProfilePhoto(): userId={}, originalFilename={}", userId1, originalFilename);

        String fileUrl = null;
        try {
            fileUrl = userService.uploadProfilePhoto(userId1, picture);
            voResult.setSuccessInfo(fileUrl);
        } catch (CashmallowException e) {
            logger.warn(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * 초대코드 규칙 변경 및 쿠폰 발급 추가로 인한 version up
     * 쿠폰 개선 적용 2025.03.06
     **/
    @PostMapping(value = {"/v2/traveler/users", "/v3/traveler/users"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String signUpUserV3(@RequestHeader("Authorization") String token,
                               @RequestBody String requestBody,
                               HttpServletRequest request,
                               HttpServletResponse response) throws RuntimeException {
        ApiResultVO voResult = new ApiResultVO();
        try {
            String jsonStr = CustomStringUtil.decode(token, requestBody);
            UserRegV2VO userRegV2VO = gson.fromJson(jsonStr, UserRegV2VO.class);

            logger.debug("signUpUserV3() : json={}", jsonStr);

            User user = new User();
            BeanUtils.copyProperties(userRegV2VO, user);

            String login = userRegV2VO.getLogin().replaceAll("[^A-Za-z0-9]", "");
            user.setLogin(login);
            user.setAgreeTerms("Y");
            user.setAgreePrivacy("Y");
            if (request.getHeader("cm-device-id") != null) {
                user.setInstanceId(request.getHeader("cm-device-id"));
            }
            if (request.getHeader("cm-bundle-version") != null) {
                user.setBundleVersion(request.getHeader("cm-bundle-version"));
            }

            Long userId = userService.registerUserV3(user, userRegV2VO.getPassword(),
                    userRegV2VO.getRecommenderEmail(), userRegV2VO.getTermsTypeList());

            logger.info("signUpUserV3 userId:{}, user.Email:{}, user.country:{}, user.instanceId:{}, user.langKey:{}", user.getId(), user.getEmail(), user.getCountry(), user.getInstanceId(), user.getLangKey());

            Map<String, String> result = new HashMap<>();
            result.put("user_id", String.valueOf(userId));
            voResult.setSuccessInfo(result);
        } catch (InvalidPasswordException e) {
            logger.warn(e.getMessage());
            voResult.setFailInfo(e.getMessage());
        } catch (CashmallowException e) {
            logger.warn(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PutMapping(value = {"/traveler/users"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateUserInfo(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "updateUserInfo()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == NO_USER_ID) {
            logger.info("{} : Invalid Token.", method);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: requestBody={}", method, requestBody);

        JSONObject body = new JSONObject(requestBody);
        String phoneNumber = "";
        String phoneCountry = "";

        if (body.has("phone_number")) {
            phoneNumber = body.getString("phone_number");
            phoneCountry = body.getString("phone_country");
        }


        User user = userRepositoryService.getUserByUserId(userId);

        if (phoneNumber != null && !phoneNumber.equals("")) {
            user.setPhoneNumber(phoneNumber);
            user.setPhoneCountry(phoneCountry);
        }

        try {
            int result = userRepositoryService.updateUser(user);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            Map<String, Object> userMap = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {
            });

            if (result != 1) {
                voResult.setFailInfo(INTERNAL_SERVER_ERROR);
            } else {
                voResult.setSuccessInfo(userMap);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    @PutMapping(value = "/v2/traveler/user/terms-and-privacy", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateTermsAndPrivacyV2(@RequestHeader("Authorization") String token,
                                          HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == NO_USER_ID) {
            logger.info("{} : Invalid Token.", "updateTermsAndPrivacyV2()");
            return JsonStr.toJsonString(voResult, response);
        }

        ApiResultVO apiResultVO = new ApiResultVO();
        try {
            userService.updateTerms(userId);
            apiResultVO.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.warn(e.getMessage(), e);
            apiResultVO.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(apiResultVO, response);
    }

    /**
     * Activate or Delete user account by traveler.
     *
     * @param token
     * @param userId
     * @param inactiveType
     * @param request
     * @param response
     * @return
     */
    @PutMapping(value = "/traveler/users/{userId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String activateUser(@RequestHeader("Authorization") String token,
                               @PathVariable long userId, @RequestParam String inactiveType,
                               HttpServletRequest request, HttpServletResponse response) {

        String method = "activateUser()";
        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID || userId != authService.getUserId(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        // Traveler can delete own account only.
        if (InactiveType.DEL.equals(InactiveType.valueOf(inactiveType))) {
            try {
                User user = inactiveUserService.deactivateUser(userId, userId, InactiveType.DEL);
                resultVO.setSuccessInfo(user);
            } catch (CashmallowException e) {
                logger.warn(e.getMessage(), e);
                resultVO.setFailInfo(e.getMessage());
            }
        } else {
            resultVO.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * Get login user info
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/traveler/user", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getUser(@RequestHeader("Authorization") String token,
                          HttpServletRequest request, HttpServletResponse response) {

        String method = "getUser()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null) {
            voResult.setFailInfo("DATA_NOT_FOUND_ERROR");

        } else {
            user.setPasswordHash(null);
            user.setInstanceId(null);
            user.setProfilePhotoUrl(cdnUrl, user.getProfilePhoto());

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            Map<String, Object> userMap = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {
            });
            voResult.setSuccessInfo(userMap);
        }

        String result = CustomStringUtil.encryptJsonString(token, voResult, response);

        logger.debug("{}: result={}", method, result);

        return result;
    }

    @Deprecated
    @GetMapping(value = "/countries/sources/{fromCd}/targets/{toCd}/enabled", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getExchangeEnabled(@RequestHeader("Authorization") String token, @PathVariable String fromCd, @PathVariable String toCd,
                                     HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("getFeeRate(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            //            return Str.toJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("getFeeRate()");

        ExchangeConfig exchangeConfig = countryService.getExchangeConfig(fromCd, toCd);
        if (exchangeConfig != null) {
            if ("Y".equals(exchangeConfig.getEnabledExchange())) {
                voResult.setSuccessInfo("Y");
            } else {
                voResult.setFailInfo(exchangeConfig.getExchangeNotice());
            }
        } else {
            voResult.setFailInfo("INTERNAL_SERVER_ERROR");
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
        //        return Str.toJsonString(token, voResult, response);
    }

    // -------------------------------------------------------------------------------
    // 80. 서비스 국가
    // -------------------------------------------------------------------------------

    // http://localhost:58080/api/countries?token=6.6.yskim
    // http://localhost:58080/api/countries?token=6.6.yskim&service=Y
    // http://localhost:58080/api/countries?token=6.6.yskim&service=N
    // http://localhost:58080/api/countries?token=6.6.yskim&service=
    // http://localhost:58080/api/countries?token=6.6.yskim&service=A

    @GetMapping(value = "/traveler/countries", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCountryList(@RequestHeader("Authorization") final String token,
                                 @RequestParam(required = false) final String code,
                                 @RequestParam final String service,
                                 @RequestParam(value = "can_signup", required = false) final String canSignup,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response) {

        Optional<String> validUser = authService.isValidUser(token, response);
        if (validUser.isPresent()) {
            return validUser.get();
        }

        ApiResultVO voResult = new ApiResultVO(CODE_FAILURE);
        voResult.setSuccessInfo(countryService.getCountryExtVoList(code, service, canSignup));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // -------------------------------------------------------------------------------
    // 99. 공통
    // -------------------------------------------------------------------------------

    /**
     * 사무실 IP인지 체크해서, 데이터 리턴
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/ip")
    @ResponseBody
    public String ipCheck(@RequestHeader("Authorization") String token,
                          @RequestHeader(value = "cm-os-type", required = false, defaultValue = "AOS") OsType osType,
                          final HttpServletRequest request,
                          final HttpServletResponse response) {
        if (!authService.isHexaStr(token)) {
            logger.debug("NOT_STORED_TOKEN CODE_INVALID_TOKEN!!!!!");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        String remoteAddr = CommonUtil.getRemoteAddr(request);
        ApiResultVO voResult = new ApiResultVO(CODE_FAILURE);
        Map<String, Object> map = new HashMap<>();
        map.put("ip", remoteAddr);
        map.put("allow", envUtil.isDev() || whitelist.stream().anyMatch(matcher -> matcher.matches(remoteAddr)));
        map.put("distanceLimitByKm", 10);

        voResult.setSuccessInfoWithDefaultMessage(map);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping("/countryFees")
    @ResponseBody
    public String getCountryFees(@RequestHeader("Authorization") String token,
                                 @RequestParam(required = false) String fromCd,
                                 @RequestParam(required = false) String toCd,
                                 HttpServletResponse response) {
        String method = "getCountryFees()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.debug("{}: API has been called.", method);

        try {
            List<CountryFee> countryFeeList = countryService.getCountryFeesByCd(fromCd, toCd, "Y");
            resultVO.setSuccessInfo(countryFeeList);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/v1/address/korean")
    @ResponseBody
    public String getAddressWithKorean(@RequestHeader("Authorization") String token,
                                       @RequestParam String address,
                                       HttpServletResponse response) {
        String method = "getAddressToEnglish()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.debug("{}: API has been called.", method);

        try {
            resultVO.setSuccessInfo(addressKoreanService.getAddress(address));
            logger.debug(gsonPretty.toJson(resultVO));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/v1/address/english")
    @ResponseBody
    public String getAddressWithEnglish(@RequestHeader("Authorization") String token,
                                        @RequestParam String roadAddressPart,
                                        HttpServletResponse response) {
        String method = "getAddressToEnglish()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.debug("{}: API has been called.", method);

        try {
            resultVO.setSuccessInfo(addressEnglishService.getAddress(roadAddressPart).stream().findFirst().orElse(null));
            logger.debug(gsonPretty.toJson(resultVO));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/v2/address/global")
    @ResponseBody
    public String getSearchResultForGlobal(@RequestHeader("Authorization") String token,
                                           @RequestParam String address,
                                           HttpServletResponse response) {
        String method = "getAddressToEnglish()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.debug("{}: API has been called.", method);

        try {
            resultVO.setSuccessInfo(addressEnglishService.getSearchResultForGlobal(address));
            logger.debug(gsonPretty.toJson(resultVO));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/v2/address/jp")
    @ResponseBody
    public String getSearchResultForJapan(@RequestHeader("Authorization") String token,
                                          @RequestParam String zipCode,
                                          HttpServletResponse response) {
        String method = "getSearchResultForJapan()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        logger.debug("{}: API has been called.", method);

        try {
            resultVO.setSuccessInfo(addressJapanService.getAddress(zipCode));
            logger.debug(gsonPretty.toJson(resultVO));
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/countryInfo")
    @ResponseBody
    public String getCountryInfo(@RequestHeader("Authorization") String token,
                                 HttpServletResponse response) throws JsonProcessingException {
        String method = "getCountryFees()";

        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);
        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        return countryService.getCountryInfoJson();
    }

    @GetMapping("/traveler/terms")
    @ResponseBody
    public String getTermsList(@RequestHeader("Authorization") String token,
                               @RequestParam String countryCode,
                               @RequestParam String view,
                               HttpServletRequest request,
                               HttpServletResponse response) {
        Locale locale = localeResolver.resolveLocale(request);
        String method = "getTermsList()";
        long userId = authService.getUserId(token);

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);
        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        try {
            // SIGN_UP인 경우는 2가지 1) 회원가입화면, 2) 로그인후 변경된 약관이 있을 경우
            Boolean showSignup = "SIGN_UP".equals(view) ? true : null;
            if (userId != NO_USER_ID && Boolean.TRUE.equals(showSignup)) {
                // 로그인후 변경된 약관이 있을경우 읽지않은 최신 약관
                resultVO.setSuccessInfo(userService.getUnreadTermsList(userId, countryCode, locale));
            } else {
                // 더보기 화면에서 조회 or
                Boolean isShowMenu = "ETC".equals(view) || showSignup; // 더보기 화면에서 호출하는 경우(ETC)
                resultVO.setSuccessInfo(termsHistoryService.getRecentVersionHistories(countryCode, isShowMenu, locale));
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PostMapping(value = "/send/slack", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String sendSlackInfo(@RequestHeader("Authorization") String token,
                                @RequestBody(required = false) String message,
                                HttpServletRequest request,
                                HttpServletResponse response) {

        String method = "sendSlackInfo()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == NO_USER_ID && !authService.isHexaStr(token)) {
            logger.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        alarmService.i("app", message);

        voResult.setSuccessInfo();

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

}
