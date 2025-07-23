package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.UserService;
import com.cashmallow.api.application.impl.InactiveUserServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.authme.AuthMeProperties;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.api.interfaces.authme.dto.AuthMeCustomerWebhookResponse;
import com.cashmallow.api.interfaces.traveler.dto.EditTravelerRequest;
import com.cashmallow.api.interfaces.traveler.dto.MatchPasswordRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelersRequest;
import com.cashmallow.api.interfaces.traveler.dto.VerifyBankAccountRequest;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.MediaType;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.domain.shared.MsgCode.TRAVELER_CANNOT_FIND_BY_USER_ID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/traveler/v3")
public class TravelerV3Controller {

    private final AuthService authService;
    private final AuthMeService authMeService;
    private final EnvUtil envUtil;
    private final AlarmService alarmService;

    private final TravelerServiceImpl travelerService;
    private final UserRepositoryService userRepositoryService;

    private final TravelerRepositoryService travelerRepositoryService;
    private final UserService userService;
    private final InactiveUserServiceImpl inactiveUserService;
    private final RedisService redisService;

    private final LocaleResolver localeResolver;
    private final MessageSource messageSource;

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final JsonUtil jsonUtil;
    private final AuthMeProperties authMeProperties;

    /**
     * 본인 인증 요청
     * <p>
     * 본인인증 요청, 신분증, 여권
     * (AuthMe 통해서 나머지 데이터 처리 예정)
     */
    @PostMapping(value = "/travelers/request", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerTravelerV4(@RequestHeader("Authorization") String token,
                                     @RequestPart(value = "traveler", required = false) String requestBody,
                                     HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        log.debug("token={}", token);
        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        Locale locale = localeResolver.resolveLocale(request);

        String decode = CustomStringUtil.decode(token, requestBody);
        log.info("decode={}", decode);
        if (decode == null) {
            // 회원가입시 복호화 안될 때 슬렉 알림.
            String msg = "registerTraveler requestBody 복호화 실패." +
                    "\nuserId:" + userId +
                    "\ntoken:" + token +
                    "\nrequestBody:" + requestBody;
            alarmService.e("본인 인증 복호화 오류", msg);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        if (envUtil.isDev()) {
            alarmService.i("registerTravelerV4", "userId: " + userId + ", " + decode);
        }

        TravelersRequest travelersRequest = objectMapper.readValue(decode, TravelersRequest.class);

        ApiResultVO voResult = new ApiResultVO(CODE_FAILURE);
        try {
            Traveler resultTraveler = travelerService.registerTravelerV4(userId, travelersRequest, locale);
            voResult.setSuccessInfo(resultTraveler);
        } catch (CashmallowException e) {
            String message = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            log.error(message, e.getMessage(), e);
            voResult.setFailInfo(message);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            String message = messageSource.getMessage(INTERNAL_SERVER_ERROR, null, locale);
            voResult.setFailInfo(message);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        log.debug("response={}", jsonUtil.toJson(voResult));

        log.info("registerTravelerV4 {}. userId={}", voResult.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * Request traveler's bank account certification
     *
     * @param token
     * @param travelerId
     * @param accountPicture
     * @param account
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = "/travelers/{travelerId}/bank-account", produces = GlobalConst.PRODUCES)
    public String verifyBankAccountV3(@RequestHeader("Authorization") String token,
                                      @PathVariable Long travelerId,
                                      @RequestPart MultipartFile accountPicture,
                                      @RequestParam String account,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "updateBankAccount()";
        log.info("encryptedBody={}, accountPicture={}", account, accountPicture.getOriginalFilename());

        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO voResult = new ApiResultVO();

        String fileName = accountPicture != null ? accountPicture.getOriginalFilename() : null;

        log.info("{}: accountPicture={}, jsonStr={}", method, fileName, account);
        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        if (traveler == null) {
            log.info("{}: Traveler id null", method);
            String message = messageSource.getMessage(TRAVELER_CANNOT_FIND_BY_USER_ID, null, "Passport information is not registered.", locale);
            voResult.setFailInfo(message);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        if (traveler.getUserId() != userId) {
            log.info("{}: Invalid token", method);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        String decode = CustomStringUtil.decode(token, account);
        log.info("decode={}", decode);

        VerifyBankAccountRequest accountRequest = null;
        try {
            accountRequest = objectMapper.readValue(decode, VerifyBankAccountRequest.class);
        } catch (Exception e) {
            log.info("Invalid param={}", decode);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_PARAMS), response);
        }

        // validation
        BeanPropertyBindingResult errors = new BeanPropertyBindingResult(accountRequest, "accountRequest");
        validator.validate(accountRequest, errors);
        if (errors.hasErrors()) {
            String errorsMsg = errors.getFieldErrors().stream().map(e -> e.getField() + ":" + e.getDefaultMessage()).collect(Collectors.joining());
            log.info("errorsMsg={}", errorsMsg);
            voResult.setFailInfo(errorsMsg);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        // address
        traveler.setAddressCountry(accountRequest.getAddressCountry().name());
        traveler.setAddressCity(accountRequest.getAddressCity());
        traveler.setAddress(accountRequest.getAddress());
        traveler.setAddressSecondary(accountRequest.getAddressSecondary());

        // bankAccount
        traveler.setBankInfoId(accountRequest.getBankInfoId());
        traveler.setAccountName(accountRequest.getAccountName()); // HK (last name + first name)
        traveler.setAccountNo(accountRequest.getAccountNo());
        traveler.setBankName(accountRequest.getBankName());
        traveler.setAccountOk("N");

        try {
            travelerService.updateAddressPhotoV3(traveler, accountPicture);
            traveler = travelerService.updateBankAccount(traveler, accountPicture);
            voResult.setSuccessInfo(traveler);

        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        log.info("verifyBankAccountV3 {}. userId={}", voResult.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping("/jobs")
    public String getJobs(HttpServletResponse response) {
        Locale locale = LocaleContextHolder.getLocale();

        List<Job.JobDto> jobs = Job.getJobs(locale);
        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setSuccessInfo(jobs);

        return JsonStr.toJsonString(apiResultVO, response);
    }

    private List<String> validTravelerRequest(TravelersRequest request) {
        Errors errors = new BeanPropertyBindingResult(request, "request");
        validator.validate(request, errors);

        if (errors.hasErrors()) {
            List<FieldError> fieldErrors = errors.getFieldErrors();
            fieldErrors.forEach(e -> log.info("{}: {}", e.getField(), e.getDefaultMessage()));

            List<String> errorString = fieldErrors
                    .stream()
                    .map(e -> e.getField() + ":" + e.getDefaultMessage())
                    .collect(Collectors.toList());
            return errorString;
        }
        return null;
    }

    // 기능: 22.1. traveler 정보 읽기
    @GetMapping(value = "/travelers/me", produces = GlobalConst.PRODUCES)
    public String getTravelerInfoV3(@RequestHeader("Authorization") String token,
                                    HttpServletRequest request, HttpServletResponse response) throws CashmallowException {

        log.info("getTravelerInfo()");

        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("getTravelerInfo(): CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO(CODE_INVALID_TOKEN);
        Locale locale = localeResolver.resolveLocale(request);
        Map<String, Object> resultMap = travelerService.getTravelerMapByUserId(userId, locale);
        voResult.setSuccessInfo(resultMap);

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    /**
     * 비밀번호 재확인으로 권한 체크. e.g. 내정보 수정 접근
     *
     * @param token
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PostMapping(value = {"/matchPassword"}, produces = GlobalConst.PRODUCES)
    public String matchPassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                @RequestBody String requestBody,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {

        Locale locale = localeResolver.resolveLocale(request);
        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_PARAMS);

        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }


        String jsonStr = CustomStringUtil.decode(token, requestBody);
        log.debug("jsonStr={}", jsonStr);

        MatchPasswordRequest matchPasswordRequest = objectMapper.readValue(jsonStr, MatchPasswordRequest.class);

        try {
            Map<String, String> resultMap = userService.matchPassword(userId, matchPasswordRequest.getLoginId(), matchPasswordRequest.getPassword());
            log.info("resultMap={}", resultMap);
            resultVO.setSuccessInfo(resultMap);
        } catch (CashmallowException e) {
            log.info("matchPasswordRequest.getLoginId()={}, e.getMessage()={}", matchPasswordRequest.getLoginId(), e.getMessage());
            String message = messageSource.getMessage(e.getMessage(), null, locale);
            resultVO.setFailInfo(message);
        }

        log.info("passwordMatch {}. userId={}", resultVO.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PutMapping("/me")
    public String editTraveler(@RequestHeader("Authorization") String token,
                               @RequestBody String traveler,
                               HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {

        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("Invalid token");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(CODE_INVALID_TOKEN), response);
        }

        String decoded = CustomStringUtil.decode(token, traveler);
        EditTravelerRequest editTravelerRequest = objectMapper.readValue(decoded, EditTravelerRequest.class);

        log.debug("EditTravelerRequest:{}", jsonUtil.toJson(editTravelerRequest));

        ApiResultVO apiResultVO = new ApiResultVO();
        Locale locale = localeResolver.resolveLocale(request);
        try {
            travelerService.editTravelerInfo(userId, editTravelerRequest);
            apiResultVO.setSuccessInfo();
        } catch (CashmallowException e) {
            log.error("FAIL update user and traveler. error:" + e.getMessage(), e);
            String message = messageSource.getMessage(e.getMessage(), null, "Internal Server Error. Please contact the Cashmallow customer center.", locale);
            apiResultVO.setFailInfo(message);
            if (STATUS_TRAVELER_INFO_MODIFY_FAIL.equals(e.getMessage())) {
                apiResultVO.setStatus(e.getMessage());
            }
        }

        log.info("userInfo changed {}. userId={}", apiResultVO.getStatus(), userId);
        return CustomStringUtil.encryptJsonString(token, apiResultVO, response);
    }

    /**
     * Activate or Delete user account by traveler.
     *
     * @param token
     * @param request
     * @param response
     * @return
     */
    @DeleteMapping(value = "/me", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deactivateUser(@RequestHeader("Authorization") String token,
                                 @RequestParam String otp,
                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "deactivateUser()";
        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        // 토큰 체크
        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("{}: checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!", method);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        // otp 체크
        boolean match = redisService.isMatch(RedisService.REDIS_KEY_PASSWORD_MATCH, String.valueOf(userId), otp);
        if (!match) {
            log.info("otp가 일치 하지 않음. 올바르지 않은 요청. userId:{}", userId);
            resultVO.setFailInfo(INTERNAL_SERVER_ERROR);
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        // Traveler can delete own account only.
        try {
            User user = inactiveUserService.deactivateUser(userId, userId, InactiveUser.InactiveType.DEL);
            resultVO.setSuccessInfo(user);
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        resultVO.setMessage(messageSource.getMessage(resultVO.getMessage(), null, resultVO.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @GetMapping(value = "/kyc/token/{certificationType}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAuthMeToken(@RequestHeader("Authorization") String token,
                                 @PathVariable("certificationType") CertificationType certificationType,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {

        ApiResultVO resultVO = new ApiResultVO(CODE_INVALID_TOKEN);

        // 토큰 체크
        long userId = authService.getUserId(token);
        if (userId == NO_USER_ID) {
            log.info("getAuthMeToken(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(token, resultVO, response);
        }

        try {
            User user = userRepositoryService.getUserByUserId(userId);
            resultVO.setSuccessInfo(authMeService.getApiToken(user.getCountryCode().name() + user.getId(), user.getCountryCode(), certificationType));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

    @PostMapping(value = "/kyc/token/webhook", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getAuthMeWebhook(
            @RequestHeader("X-Authme-Signature") String signature,
            @RequestBody String json) {

        AuthMeCustomerWebhookResponse authMeWebhook = jsonUtil.fromJson(json, AuthMeCustomerWebhookResponse.class);
        log.info("getAuthMeWebhook(): type={}, customerID={}, status={}",
                authMeWebhook.type(), authMeWebhook.customerId(), authMeWebhook.data().status());
        if (authMeWebhook.isChangeStateApprovedOrRejected()) {
            // Approved or Rejected 에 대한 로그 기록
            authMeService.insertAuthmeWebhookLog(authMeWebhook, json);

            // Approve, Reject 상태 변경시 업데이트
            // authMeService.checkTimeoutAndUpdateStatus(authMeWebhook.customerId());
        } else if (authMeWebhook.isManualApproved()) {
            // 수동 Approved에 대한 로그 기록
            authMeService.insertAuthmeWebhookLog(authMeWebhook, json);

            // 수동 업데이트 처리
            authMeService.checkTimeoutAndUpdateStatusManual(authMeWebhook.customerId());
        }

        return "SUCCESS";
    }

}
