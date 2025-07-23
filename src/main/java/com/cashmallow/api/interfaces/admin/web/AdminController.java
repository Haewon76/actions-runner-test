package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.FileService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.CountryFee;
import com.cashmallow.api.domain.model.country.CurrencyLimit;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.model.user.UserUnmaskedLog;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.DatatablesRequest;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.admin.dto.TravelerAskVO;
import com.cashmallow.common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;

/**
 * Handles requests for the application home page.
 */
@Controller
@SuppressWarnings({"unchecked", "deprecation", "dep-ann"})
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserAdminServiceImpl userAdminService;

    @Autowired
    private TravelerServiceImpl travelerService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private CountryService countryService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    // -------------------------------------------------------------------------------
    // ADMIN
    // -------------------------------------------------------------------------------

    // 기능: Admin user 정보 읽기. 사용자가 ROLE_ADMIN 으로 등록되어 있어야 한다.
    @GetMapping(value = "/admin/admins/{userId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAdminUserInfo(@RequestHeader("Authorization") String token, @PathVariable long userId,
                                   HttpServletResponse response) {

        logger.info("getAdminUserInfo()");

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        if (token != null && userId == authService.getUserId(token)) {
            User user = userRepositoryService.getUserByUserId(userId);

            // remove password
            user.setPasswordHash(null);

            if (userService.isVerifyRole(userId, ROLE_ADMIN) && user.getCls().equals(CLS_ADMIN)) {

                ObjectMapper mapper = new ObjectMapper();
                mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
                Map<String, Object> obj = mapper.convertValue(user, new TypeReference<Map<String, Object>>() {
                });

                logger.debug("getAdminUserInfo(): result={}", obj);

                voResult.setSuccessInfo(obj);

            } else {
                voResult.setFailInfo("Don't have an Admin authority.");
            }
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 사용자 자신의 암호를 변경한다.
    @PatchMapping(value = "/admin/users/{userId}/password", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String changePassword(@RequestHeader("Authorization") String token, @PathVariable long userId,
                                 @RequestBody String requestBody, HttpServletRequest request, HttpServletResponse response) {

        String method = "jsonChangePassword()";

        long tUserId = authService.getUserId(token);

        if (userId != tUserId) {
            logger.info("{}: Invalid token. userId={}, tUserId={}", method, userId, tUserId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String currentPassword = (String) map.get("currentPassword");
        String newPassword = (String) map.get("newPassword");

        ApiResultVO voResult = userService.changePassword(token, currentPassword, newPassword);

        Locale locale = localeResolver.resolveLocale(request);
        Object[] args = new Object[]{MIN_PWD_LEN};
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), args, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/admin/countries", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getConutryList(@RequestHeader("Authorization") String token, String code, String service,
                                 HttpServletResponse response) {
        logger.info("getConutryList(): code={}, service={}", code, service);

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("service", service);
        List<Country> countries = countryService.getCountryList(params);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        List<Map<String, String>> results = new ArrayList<>();
        Map<String, String> result;
        for (Country country : countries) {
            result = mapper.convertValue(country, new TypeReference<Map<String, String>>() {
            });

            result.put("iso_3166", country.getIso3166());
            result.put("iso_4217", country.getIso4217());

            results.add(result);
        }

        ApiResultVO voResult = new ApiResultVO();
        voResult.setSuccessInfo(results);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = "/admin/countries", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registerCountry(@RequestHeader("Authorization") String token,
                                  @RequestBody String requestBody,
                                  HttpServletRequest request,
                                  HttpServletResponse response) throws JsonProcessingException {
        Long managerId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        String jsonStr = CustomStringUtil.decode(token, requestBody);

        ObjectMapper objectMapper = new ObjectMapper();
        Country country = objectMapper.readValue(jsonStr, Country.class);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.registerCountry(country, managerId, ip);
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PutMapping(value = "/admin/countries/{code}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateCountry(@RequestHeader("Authorization") String token,
                                @PathVariable String code,
                                @RequestBody String requestBody,
                                HttpServletRequest request,
                                HttpServletResponse response) throws JsonProcessingException {
        Long managerId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        String jsonStr = CustomStringUtil.decode(token, requestBody);


        ObjectMapper objectMapper = new ObjectMapper();
        Country updateCountry = objectMapper.readValue(jsonStr, Country.class);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.updateCountry(updateCountry, managerId, ip);
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/admin/country-fees", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCountryFeeList(@RequestHeader("Authorization") String token,
                                    @RequestParam(required = false) String fromCd,
                                    @RequestParam(required = false) String toCd,
                                    HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<CountryFee> countryFees = countryService.getCountryFeesByCd(fromCd, toCd, null);
        voResult.setSuccessInfo(countryFees);

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PostMapping(value = "/admin/country-fees", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String registerCountryFee(@RequestHeader("Authorization") String token,
                                     @RequestBody String requestBody,
                                     HttpServletRequest request,
                                     HttpServletResponse response) throws JsonProcessingException, CashmallowException {
        Long managerId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        String jsonStr = CustomStringUtil.decode(token, requestBody);

        ObjectMapper objectMapper = new ObjectMapper();
        CountryFee countryFee = objectMapper.readValue(jsonStr, CountryFee.class);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.registerCountryFee(countryFee, managerId, ip);
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @PutMapping(value = "/admin/country-fees/{countryFeeId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateCountryFee(@RequestHeader("Authorization") String token,
                                   @RequestBody String requestBody,
                                   @PathVariable Long countryFeeId,
                                   HttpServletRequest request,
                                   HttpServletResponse response) throws JsonProcessingException, CashmallowException {
        Long managerId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        String jsonStr = CustomStringUtil.decode(token, requestBody);

        ObjectMapper objectMapper = new ObjectMapper();
        CountryFee countryFee = objectMapper.readValue(jsonStr, CountryFee.class);
        countryFee.setId(countryFeeId);

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(managerId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.updateCountryFee(countryFee, managerId, ip);
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 여행자 여권/계좌 정보 조회 for 민명선/대리
    // notice: exp_date, passport_ok_date, account_ok_date 등 날짜 관련 컬럼은
    // Timestamp.gettime() 즉, long 값으로 전달되어야 한다.
    @PostMapping(value = "/admin/getPassportInfo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getPassportAndAccountInfo(@RequestHeader("Authorization") String token, HttpServletRequest request,
                                            HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Long managerId = authService.getUserId(token);
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("getPassportInfo(): jsonStr={}", jsonStr);
        TravelerAskVO pvo = new TravelerAskVO(jsonStr);
        try {
            SearchResultVO searchResultVO = travelerService.getCertificationInfo(managerId, pvo);
            voResult.setSuccessInfo(searchResultVO);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 여행자 계좌승인 조회
    @GetMapping(value = "/admin/TravelerAccountInfo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTravelerAccountInfo(@RequestHeader("Authorization") String token,
                                         @RequestParam Map<String, String> params, HttpServletRequest request, HttpServletResponse response) {
        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Long managerId = authService.getUserId(token);
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("getTravelerAccountInfo(): jsonStr={}", params.toString());
        TravelerAskVO pvo = new TravelerAskVO(params);

        try {
            SearchResultVO searchResultVO = travelerService.getAccountInfo(managerId, pvo);
            voResult.setSuccessInfo(searchResultVO);
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 여행자의 여권 인증 정보를 설정한다.

    /**
     * 여행자의 본인 인증 정보를 승인한다.
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @PatchMapping(value = "/admin/v2/travelers/{travelerId}/identity/verify", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String verifyIdentity(@RequestHeader("Authorization") String token,
                                 @PathVariable long travelerId, @RequestBody String requestBody,
                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "verifyIdentity()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        long managerId = authService.getUserId(token);

        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        logger.info("{}: requestBody={}", method, requestBody);
        JSONObject obj = new JSONObject(requestBody);
        String certificationOk = obj.getString("certification_ok");
        String message = null;
        if (obj.has("message")) {
            message = obj.getString("message");
        }

        if (userService.isVerifyRole(managerId, ROLE_ASSIMAN)) {
            try {
                String managerName = userAdminService.getAdminName(managerId);
                travelerService.verifyIdentityByAdmin(travelerId, managerName, certificationOk, message, false);
                voResult.setSuccessInfo();
            } catch (CashmallowException e) {
                logger.error(e.getMessage(), e);
                voResult.setFailInfo(e.getMessage());
            }
        } else {
            voResult.setFailInfo(MSG_NEED_AUTH);
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // 기능: 여행자의 계좌인증 승인
    @PatchMapping(value = "/admin/TravelerAccountInfo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String setTravelerAccountInfo(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                         HttpServletRequest request, HttpServletResponse response) {

        logger.info("pacthTravelerAccountInfo(): requestBody={}", requestBody);

        Long managerId = authService.getUserId(token);
        JSONObject obj = new JSONObject(requestBody);

        Long userId = Long.valueOf((String) obj.get("user_id"));
        String accountOk = obj.get("account_ok").toString();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        User user = userRepositoryService.getUserByUserId(userId);

        ApiResultVO resultVO = new ApiResultVO();
        try {
            traveler.setAccountOk(accountOk);
            traveler.setAccountOkDate(Timestamp.valueOf(LocalDateTime.now()));
            travelerService.updateTravelerInfo(managerId, traveler, user);

            resultVO.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    /**
     * Verify traveler's bank account
     *
     * @param token
     * @param travelerId
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PatchMapping(value = "/admin/v2/travelers/{travelerId}/bank-account/verify", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String verifyTravelerBankAccount(@RequestHeader("Authorization") String token,
                                            @PathVariable long travelerId,
                                            @RequestBody String requestBody,
                                            HttpServletRequest request, HttpServletResponse response) {

        String method = "verifyTravelerBankAccount()";

        logger.info("{}: requestBody={}", method, requestBody);

        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return JsonStr.toJsonString(voResult, response);
        }

        JSONObject obj = new JSONObject(requestBody);

        String accountOk = obj.get("account_ok").toString();
        String message = null;
        if (obj.has("message")) {
            message = obj.get("message").toString();
        }

        ApiResultVO resultVO = new ApiResultVO();
        try {
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
            if (userService.isVerifyRole(managerId, ROLE_SUPERMAN) || isBankBookCancellationAllowed(traveler)) {
                travelerService.verifyBankAccountByAdmin(traveler, accountOk, message);
                resultVO.setSuccessInfo();
            } else {
                resultVO.setFailInfo("Cannot be canceled. An exchange for the traveler is on progress.");
            }
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(resultVO, response);
    }

    private boolean isBankBookCancellationAllowed(Traveler traveler) {
        return !traveler.getAccountOk().equals("Y") || Boolean.TRUE.equals(exchangeService.isPossibleToCancelBankBookverified(traveler.getId()));
    }

    // 기능: 어드민 화면에서 여행자 통장 정보를 수정한다.
    @PatchMapping(value = "/admin/traveler/account", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateTravelerAccount(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                        HttpServletRequest request, HttpServletResponse response) {
        String method = "updateTravelerAccount()";

        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        JSONObject jsonObject = new JSONObject(requestBody);

        logger.info("{}: requestBody={}", method, requestBody);

        long userId = ((Integer) jsonObject.get("user_id")).longValue();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        traveler.setId(((Integer) jsonObject.get("traveler_id")).longValue());
        traveler.setUserId(userId);

        traveler.setAccountName((String) jsonObject.get("account_name"));
        traveler.setAccountNo((String) jsonObject.get("account_no"));
        traveler.setBankName((String) jsonObject.get("bank_name"));
        traveler.setBankInfoId(jsonObject.getLong("bank_info_id"));

        try {
            if (!userService.isVerifyRole(managerId, ROLE_ASSIMAN)) {
                throw new CashmallowException(MSG_NEED_AUTH);
            }
            travelerRepositoryService.updateTraveler(traveler);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @PatchMapping(value = "/admin/traveler/address", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateTravelerAddress(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                        HttpServletRequest request, HttpServletResponse response) {
        String method = "updateTravelerAddress()";

        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO();
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return JsonStr.toJsonString(voResult, response);
        }

        JSONObject json = new JSONObject(requestBody);

        logger.info("{}: requestBody={}", method, requestBody);

        Long travelerId = Long.valueOf(json.get("traveler_id").toString());
        String address = json.getString("address");
        String addressSecondary = json.getString("address_secondary");

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);

        if (traveler == null) {
            logger.info("{}: Invalid travelerId. travelerId={}", method, travelerId);
            voResult.setFailInfo(CODE_INVALID_USER_ID);
            return JsonStr.toJsonString(voResult, response);
        }

        traveler.setAddress(address);
        traveler.setAddressSecondary(addressSecondary);

        try {
            if (!userService.isVerifyRole(managerId, ROLE_ASSIMAN)) {
                throw new CashmallowException(MSG_NEED_AUTH);
            }
            travelerRepositoryService.updateTraveler(traveler);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/admin/traveler/certification/{travelerId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getTravelerCertificationImage(@RequestHeader("Authorization") String token,
                                                @PathVariable Long travelerId,
                                                HttpServletResponse response) {

        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO();
        if (isInvalidTokenForUserId(managerId, voResult)) {
            return JsonStr.toJsonString(voResult, response);
        }

        voResult.setSuccessInfo(travelerRepositoryService.getTravelerImage(travelerId));
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 여행자 여권 정보를 수정한다.
    @PostMapping(value = "/admin/traveler/passport", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String updateTravelerPassport(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                         HttpServletRequest request, HttpServletResponse response) {
        String method = "updateTravelerPassport()";

        Long managerId = authService.getUserId(token);

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        if (isInvalidTokenForUserId(managerId, voResult)) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("{}: jsonStr={}", method, jsonStr);
        Map<String, Object> map = JsonStr.toHashMap(jsonStr);

        long userId = ((Integer) map.get("user_id")).longValue();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        traveler.setId(((Integer) map.get("traveler_id")).longValue());
        traveler.setUserId(userId);

        String identificationNumber = (String) map.get("identification_number");
        String idNumberEncoded = securityService.encryptAES256(identificationNumber);
        traveler.setIdentificationNumber(idNumberEncoded);

        traveler.setEnFirstName((String) map.get("en_first_name"));
        traveler.setEnLastName((String) map.get("en_last_name"));
        traveler.setLocalFirstName((String) map.get("first_name"));
        traveler.setLocalLastName((String) map.get("last_name"));
        traveler.setPassportExpDate((String) map.get("passport_exp_date"));
        traveler.setPassportIssueDate((String) map.get("passport_issue_date"));
        traveler.setPassportCountry((String) map.get("passport_country"));
        traveler.setSex(Traveler.TravelerSex.valueOf((String) map.get("gender")));

        User user = userRepositoryService.getUserByUserId(userId);
        user.setId(((Integer) map.get("user_id")).longValue());
        user.setFirstName((String) map.get("first_name"));
        user.setLastName((String) map.get("last_name"));
        user.setCountry((String) map.get("country"));
        user.setBirthDate((String) map.get("birth_date"));

        try {
            travelerService.updateTravelerInfo(managerId, traveler, user);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // -------------------------------------------------------------------------------
    // 50. 가맹점 정산
    // -------------------------------------------------------------------------------

    // 기능: 50.2. 정산중인 가맹점이 있는 지 조회한다(국가별 )
    @PostMapping(value = "/admin/isCalcualtingCashOut", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String isCalcualtingCashOut(@RequestHeader("Authorization") String token, HttpServletRequest request,
                                       HttpServletResponse response) {
        String method = "isCalcualtingCashOut()";

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, voResult)) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        jsonStr = CustomStringUtil.decode(token, jsonStr);
        logger.info("{}: jsonStr={}", method, jsonStr);

        Map<String, Object> map = JsonStr.toHashMap(jsonStr);
        String country = (String) map.get("country");
        Timestamp beginDate = CommDateTime.objToTimestamp(map.get("begin_date"));
        Timestamp endDate = CommDateTime.objToTimestamp(map.get("end_date"));

        if (userService.isVerifyRole(userId, ROLE_ASSIMAN)) {
            Map<String, Object> m = cashOutService.isCalcualtingCashOut(country, beginDate, endDate);
            voResult.setSuccessInfo(m);
        } else {
            logger.error("{} userId={} no permission", method, userId);
            voResult.setFailInfo("해당 기능을 실행할 권한이 없습니다.");
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: 88.1 사용자 이미지 파일의 수정
    @PatchMapping(value = "/admin/file-server/photo", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String editPhoto(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                            HttpServletRequest request, HttpServletResponse response) {

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        logger.info("editPhoto(): jsonStr={}", jsonStr);

        JSONObject json = new JSONObject(jsonStr);

        String fileServerDir = json.get("fileServerDir").toString();
        String photo = json.get("photo").toString();
        String action = json.get("action").toString();
        String direction = json.get("direction").toString();

        ApiResultVO resultVO = new ApiResultVO();
        try {
            boolean isAdmin = true;
            if (FileService.Action.ROTATE.toString().equalsIgnoreCase(action)) {
                fileService.rotagePhoto(isAdmin, null, fileServerDir, photo, direction);
            }
            resultVO.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping("/admin/config-fees")
    @ResponseBody
    public String getExchangeConfig(@RequestHeader("Authorization") String token,
                                    @RequestParam(required = false) String fromCd,
                                    @RequestParam(required = false) String toCd,
                                    HttpServletResponse response) {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        List<ExchangeConfig> exchangeConfigList = countryService.getExchangeConfigByCode(fromCd, toCd);
        apiResultVO.setSuccessInfo(exchangeConfigList);

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    /**
     * 주어진 사용자 ID에 대한 토큰이 유효하지 않은지 확인
     *
     * @param userId      토큰에서 추출된 사용자 ID
     * @param apiResultVO 토큰이 유효하지 않은 경우 업데이트할 API 결과 객체
     * @return 토큰이 유효하지 않으면 true, 그렇지 않으면 false
     */
    private boolean isInvalidTokenForUserId(Long userId, ApiResultVO apiResultVO) {
        if (userId == NO_USER_ID) {
            logger.info("Invalid token. userId={}", userId);
            apiResultVO.setFailInfo(MSG_INVALID_USER_ID);
            return true;
        }
        return false;
    }

    @PostMapping("/admin/config-fees")
    @ResponseBody
    public String insertExchangeConfig(@RequestHeader("Authorization") String token,
                                       @RequestBody String requestBody,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws JsonProcessingException {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeConfig exchangeConfig = objectMapper.readValue(jsonStr, ExchangeConfig.class);
        exchangeConfig.setCreator(userId);

        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.insertExchangeConfig(exchangeConfig, userId, ip);
            apiResultVO.setSuccessInfo();
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @PutMapping("/admin/config-fees/{configFeeId}")
    @ResponseBody
    public String updateExchangeConfig(@RequestHeader("Authorization") String token,
                                       @PathVariable Long configFeeId,
                                       @RequestBody String requestBody,
                                       HttpServletRequest request,
                                       HttpServletResponse response) throws JsonProcessingException {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        String ip = CommonUtil.getRemoteAddr(request);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        ExchangeConfig exchangeConfig = objectMapper.readValue(jsonStr, ExchangeConfig.class);
        exchangeConfig.setId(configFeeId);
        exchangeConfig.setCreator(userId);

        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            countryService.updateExchangeConfig(exchangeConfig, userId, ip);
            apiResultVO.setSuccessInfo();
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @PostMapping("/admin/userUnmaskedLog")
    @ResponseBody
    public String saveMaskedLog(@RequestHeader("Authorization") String token,
                                @RequestBody String requestBody,
                                HttpServletRequest request,
                                HttpServletResponse response) throws JsonProcessingException {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        UserUnmaskedLog userUnmaskedLog = objectMapper.readValue(jsonStr, UserUnmaskedLog.class);
        userUnmaskedLog.setCreator(userId);

        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            userService.insertUserUnMaskedLog(userUnmaskedLog);
            apiResultVO.setSuccessInfo();
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @PostMapping("/admin/reset/userPassword")
    @ResponseBody
    public String resetUserPassword(@RequestHeader("Authorization") String token,
                                    @RequestBody String encryptedEmailAddress,
                                    HttpServletResponse response) throws CashmallowException {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String email = CustomStringUtil.decode(token, encryptedEmailAddress);

        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            final String urlLink = userService.passwordResetAndSendEmailForAdmin(email);
            apiResultVO.setSuccessInfo(urlLink);
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    /**
     * 관리자가 고객(유저)의 전화번호, 국가코드 등을 변경한다.
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     * @throws JsonProcessingException
     */
    @PatchMapping("/admin/update/userInfo")
    @ResponseBody
    public String updateUserInfo(@RequestHeader("Authorization") String token,
                                 @RequestBody String requestBody,
                                 HttpServletRequest request,
                                 HttpServletResponse response) throws JsonProcessingException {
        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        User user = objectMapper.readValue(jsonStr, User.class);

        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            userService.updateUserInfo(user);
            apiResultVO.setSuccessInfo();
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @GetMapping(value = "/admin/pending/balance")
    @ResponseBody
    public String getPendingBalance(@RequestHeader("Authorization") String token,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                    @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                    HttpServletRequest request,
                                    HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // 홍콩 시간 기준으로 조회 한다
        LocalDateTime newStartDate = LocalDateTime.of(startDate, LocalTime.MIN).atZone(ZoneId.of("Asia/Hong_Kong")).toLocalDateTime();
        LocalDateTime newEndDate = LocalDateTime.of(endDate, LocalTime.MAX).atZone(ZoneId.of("Asia/Hong_Kong")).toLocalDateTime();

        voResult.setSuccessInfo(cashOutService.getPendingBalances(newStartDate, newEndDate));
        return JsonStr.toJsonString(voResult, response);
    }

    @GetMapping(value = "/admin/pending/balance/detail")
    @ResponseBody
    public String getPendingBalanceDetail(@RequestHeader("Authorization") String token,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                          @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                          HttpServletRequest request,
                                          HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (isInvalidTokenForUserId(managerId, voResult)) {
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // 홍콩 시간 기준으로 조회 한다
        LocalDateTime newStartDate = LocalDateTime.of(startDate, LocalTime.MIN).atZone(ZoneId.of("Asia/Hong_Kong")).toLocalDateTime();
        LocalDateTime newEndDate = LocalDateTime.of(endDate, LocalTime.MAX).atZone(ZoneId.of("Asia/Hong_Kong")).toLocalDateTime();

        voResult.setSuccessInfo(cashOutService.getPendingBalanceDetails(newStartDate, newEndDate));
        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/admin/get-currency-limits", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getCurrencyLimits(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                    HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        ApiResultVO apiResultVO = new ApiResultVO();

        Long userId = authService.getUserId(token);
        if (isInvalidTokenForUserId(userId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        DatatablesRequest datatablesRequest = objectMapper.readValue(jsonStr, DatatablesRequest.class);

        if (userService.isVerifyRole(userId, ROLE_SUPERMAN)) {
            apiResultVO.setSuccessInfo(countryService.getCurrencyLimits(datatablesRequest));
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    @PutMapping(value = "/admin/save-currency-limit", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String saveCurrencyLimit(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                    HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        ApiResultVO apiResultVO = new ApiResultVO();

        Long managerId = authService.getUserId(token);
        if (isInvalidTokenForUserId(managerId, apiResultVO)) {
            return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        ObjectMapper objectMapper = new ObjectMapper();
        CurrencyLimit currencyLimit = objectMapper.readValue(jsonStr, CurrencyLimit.class);
        currencyLimit.setCreator(managerId);

        if (userService.isVerifyRole(managerId, ROLE_SUPERMAN)) {
            apiResultVO.setSuccessInfo(countryService.saveCurrencyLimit(currencyLimit));
        } else {
            apiResultVO.setFailInfo(MSG_NEED_AUTH);
        }

        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }
}
