package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.application.UserService;
import com.cashmallow.api.application.impl.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.company.TransactionRecord;
import com.cashmallow.api.domain.model.company.TransactionRecord.FundingStatus;
import com.cashmallow.api.domain.model.company.TransactionRecord.RelatedTxnType;
import com.cashmallow.api.domain.model.customercenter.Notice;
import com.cashmallow.api.domain.model.customercenter.NoticeContent;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.exchange.Mapping;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser.InactiveType;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.Remittance.RemittanceStatusCode;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerVerificationStatusResponse;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.OtpService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.aml.complyadvantage.ComplyAdvantageAmlService;
import com.cashmallow.api.interfaces.aml.octa.OctaAmlService;
import com.cashmallow.api.interfaces.authme.dto.TravelerImage;
import com.cashmallow.api.interfaces.authme.dto.UserAllData;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.ROLE_ADMIN;
import static com.cashmallow.api.domain.shared.Const.ROLE_SUPERMAN;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

/**
 * Handles requests for the application home page.
 */
@Controller
@RequestMapping("/admin")
public class AdminControllerV2 {

    private static final Logger logger = LoggerFactory.getLogger(AdminControllerV2.class);

    // Services
    @Autowired
    private InactiveUserServiceImpl inactiveUserService;

    @Autowired
    private CustomerCenterServiceImpl customerCenterService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AuthService authService;

    @Autowired
    private ExchangeServiceImpl exchangeService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private RemittanceServiceImpl remittanceService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    // 다국어 메시지 처리
    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private CompanyServiceImpl companyService;

    @Autowired
    private OctaAmlService octaAmlService;

    @Autowired
    private ComplyAdvantageAmlService complyAdvantageAmlService;

    @Autowired
    private RefundServiceImpl refundService;

    @Autowired
    private RemittanceAdminService remittanceAdminService;

    @Autowired
    private MallowlinkRemittanceServiceImpl mallowlinkRemittanceService;

    @Autowired
    private OtpService otpService;


    //-------------------------------------------------------------------------------
    // ADMIN
    //-------------------------------------------------------------------------------

    // 기능: user 정보 읽기.
    @GetMapping(value = "/users/{userId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getUser(@RequestHeader("Authorization") String token, @PathVariable long userId, HttpServletRequest request, HttpServletResponse response) {

        String method = "getUser()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        User user = userRepositoryService.getUserByUserId(userId);

        voResult.setSuccessInfo(user);
        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: user 정보와 traveler정보 같이 찾기.
    @GetMapping(value = "/sanctions", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getSanctionsList(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        String method = "getSanctionsList()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String userId = request.getParameter("user_id");

        if (userId == null || userId.equals("")) {
            logger.info("{}: Invalid parameters. userId={}", method, userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
        }

        logger.info("{}: managerId={}, userId={}", method, managerId, userId);

        try {
            List<Map<String, Object>> resultMap = octaAmlService.getTravelerAmlList(Long.valueOf(userId));
            voResult.setSuccessInfo(resultMap);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping(value = "/sanctions/complyadvantage", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getComplyAdvantageMediaSearch(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        String method = "getComplyAdvantageMediaSearch()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String userId = request.getParameter("user_id");

        if (userId == null || userId.equals("")) {
            logger.info("{}: Invalid parameters. userId={}", method, userId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
        }

        logger.info("{}: managerId={}, userId={}", method, managerId, userId);

        try {
            boolean hasRisk = complyAdvantageAmlService.hasRiskCustomer(Long.valueOf(userId));
            voResult.setSuccessInfo(hasRisk);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 기능: user 정보 찾기.
    @GetMapping(value = "/search/users", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String searchUsers(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        String method = "searchUsers()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String q = request.getParameter("q");

        logger.info("searchUsers(): managerId={} searchKeyword={}", managerId, q);

        List<User> list = null;

        try {
            list = userService.searchUsers(q);
            voResult.setSuccessInfo(list);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        String result = CustomStringUtil.encryptJsonString(token, voResult, response);

        return result;
    }

    /**
     * Delete, dormant or activate user account
     *
     * @param token
     * @param userId
     * @param activated
     * @param requestBody
     * @param request
     * @param response
     * @return
     */
    @PatchMapping(value = "/users/{userId}", params = {"activated"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String activateUser(@RequestHeader("Authorization") String token,
                               @PathVariable long userId, @RequestParam String activated,
                               @RequestBody String requestBody,
                               HttpServletRequest request, HttpServletResponse response) {

        String method = "activateUser()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String jsonStr = CustomStringUtil.decode(token, requestBody);
        JSONObject json = new JSONObject(jsonStr);

        // default inactive type is 'DEL'(Delete user account)
        InactiveType inactiveType = null;
        if (json.has("inactive_type")) {
            inactiveType = InactiveType.valueOf(json.getString("inactive_type"));
        }

        voResult = new ApiResultVO();
        User user = userRepositoryService.getUserByUserId(userId);

        try {
            if ("Y".equals(activated)) {
                user = inactiveUserService.activateUser(userId, managerId);
                voResult.setSuccessInfo(user);
            } else if ("N".equals(activated)) {
                user = inactiveUserService.deactivateUser(userId, managerId, inactiveType);
                voResult.setSuccessInfo(user);
            } else {
                voResult.setFailInfo("User's activated value is null.");
            }
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);

            Locale locale = localeResolver.resolveLocale(request);
            String errMsg = messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
            errMsg += " (User ID : " + user.getId() + ")";
            voResult.setFailInfo(errMsg);
        }

        String result = CustomStringUtil.encryptJsonString(token, voResult, response);


        return result;
    }

    // 공지사항 목록 검색 
    @GetMapping(value = "/customer-center/notices", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String searchNoticeContents(@RequestHeader("Authorization") String token,
                                       @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestParam("sort") String sort,
                                       @RequestParam(value = "isPosting", required = false) Boolean isPosting,
                                       @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate,
                                       @RequestParam(value = "postingStatus", required = false) String postingStatus, @RequestParam(value = "searchValue", required = false) String searchValue,
                                       @RequestParam(value = "languageType", required = false) String languageType,
                                       HttpServletRequest request, HttpServletResponse response) {

        String method = "searchNoticeContents()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return JsonStr.toJsonString(voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.info("searchNoticeContents(): jsonStr={}", jsonStr);

        Locale locale = localeResolver.resolveLocale(request);

        if (StringUtils.isEmpty(languageType)) {
            languageType = locale.toString();
        }

        try {
            SearchResultVO searchResultVo = customerCenterService.searchNoticeContents(languageType, isPosting, fromDate, toDate, Objects.requireNonNullElse(postingStatus, "GOING"), searchValue, page, size, sort);
            voResult.setSuccessInfo(searchResultVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 개별 공지사항 조회 (지정된 languageType으로 조회)
    @GetMapping(value = "/customer-center/notices/{noticeId}/notice-contents/{languageType}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getNoticeContentByLanguageType(@RequestHeader("Authorization") String token, @PathVariable Long noticeId, @PathVariable String languageType,
                                                 HttpServletRequest request, HttpServletResponse response) {

        String method = "getNoticeContentByLanguageType()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return JsonStr.toJsonString(voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.info("getNoticeContentByLanguageType() : jsonStr={}", jsonStr);

        try {
            NoticeContent noticeContent = customerCenterService.getNoticeContentByExactLanguageType(noticeId, languageType);
            voResult.setSuccessInfo(noticeContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 공지사항 등록 
    @PostMapping(value = "/customer-center/notices", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String addNoticeContent(@RequestHeader("Authorization") String token,
                                   HttpServletRequest request, HttpServletResponse response) {

        String method = "addNoticeContent()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        // 공지 사항 변경은 Const.ROLE_ASSIMAN 권한이 필요함.
        if (auths == null || !auths.contains(Const.ROLE_ASSIMAN)) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String encoded = CommNet.extractPostRequestBody(request);
        String jsonStr = CustomStringUtil.decode(token, encoded);
        logger.info("{} jsonStr={}", method, jsonStr);

        JSONObject jo = new JSONObject(jsonStr);

        NoticeContent noticeContent = new NoticeContent();
        // 신규 등록된 공지는 id가 없다.
        noticeContent.setLanguageType(jo.getString("languageType")); // languageType format : en, en_US, etc. use language code only or use under bar after language code.
        noticeContent.setTitle(jo.getString("title"));
        noticeContent.setContent(jo.getString("content"));
        noticeContent.setModifier(userId);

        noticeContent.setBeginDate(jo.getString("beginDate"));
        noticeContent.setEndDate(jo.getString("endDate"));
        noticeContent.setIsPopup(jo.getBoolean("isPopup"));

        try {
            customerCenterService.addNoticeContent(noticeContent);
            voResult.setSuccessInfo(noticeContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 공지사항 수정
    @PatchMapping(value = "/customer-center/notices/{noticeId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String modifyNoticeContent(@RequestHeader("Authorization") String token, @PathVariable String noticeId,
                                      HttpServletRequest request, HttpServletResponse response) {

        String method = "modifyNoticeContent()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        // 공지 사항 변경은 Const.ROLE_ASSIMAN 권한이 필요함.
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        String encoded = CommNet.extractPostRequestBody(request);
        String jsonStr = CustomStringUtil.decode(token, encoded);
        logger.info("{} jsonStr={}", method, jsonStr);

        JSONObject jo = new JSONObject(jsonStr);

        NoticeContent noticeContent = new NoticeContent();

        // 공지 id가 같은지 체크
        if (!jo.getString("id").equals(noticeId)) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
        }

        noticeContent.setId(Long.parseLong(noticeId));
        noticeContent.setLanguageType(jo.getString("languageType")); // languageType format : en, en_US, etc. use language code only or use under bar after language code.
        noticeContent.setTitle(jo.getString("title"));
        noticeContent.setContent(jo.getString("content"));
        noticeContent.setModifier(userId);

        noticeContent.setBeginDate(jo.getString("beginDate"));
        noticeContent.setEndDate(jo.getString("endDate"));
        noticeContent.setIsPopup(jo.getBoolean("isPopup"));

        try {
            customerCenterService.addNoticeContent(noticeContent);
            voResult.setSuccessInfo(noticeContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 공지사항 삭제 (언어별 noticeContent도 일괄 삭제)
    @DeleteMapping(value = "/customer-center/notices/{noticeId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String deleteNotice(@RequestHeader("Authorization") String token, @PathVariable Long noticeId,
                               HttpServletRequest request, HttpServletResponse response) {

        String method = "deleteNotice()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. userId={}", method, userId);
            return JsonStr.toJsonString(voResult, response);
        }


        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.info("deleteNotice() : jsonStr={}", jsonStr);

        List<String> auths = authService.getUserAuthInfo(token);
        // 공지 사항 변경은 Const.ROLE_ASSIMAN 권한이 필요함.
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        try {
            Notice notice = new Notice();
            notice.setId(noticeId);
            int affectRows = customerCenterService.deleteNotice(notice);
            Map<String, Object> returnMap = new HashMap<>();
            returnMap.put("affectRows", affectRows);
            voResult.setSuccessInfo(returnMap);

        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        Locale locale = localeResolver.resolveLocale(request);
        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 사용자가 신청한 환전을 ADMIN에서 캔슬함.(상태값이 OP인 것만.)
    @PostMapping(value = "/exchange/cancel", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelExchangeByAdmin(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                        HttpServletRequest request, HttpServletResponse response) {
        String method = "cancelExchangeInProgress()";

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject body = new JSONObject(requestBody);
        Long exchangeId = Long.valueOf(body.getString("exchange_id"));

        ApiResultVO voResult = new ApiResultVO();
        try {
            exchangeService.cancelExchangeByAdmin(exchangeId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // 사용자가 신청한 송금을 ADMIN에서 AML 확인 후 재송금(상태값이 RC인 것만.)
    @PostMapping(value = "/remittance/receiver-aml", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String confirmReceiverAmlByAdmin(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                            HttpServletRequest request, HttpServletResponse response) {
        String method = "confirmReceiverAmlByAdmin()";

        Long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject body = new JSONObject(requestBody);
        Long remitId = Long.valueOf(body.getString("remit_id"));

        ApiResultVO voResult = new ApiResultVO();
        try {
            remittanceAdminService.confirmReceiverAmlByAdmin(remitId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // 사용자가 신청한 송금을 ADMIN에서 캔슬함.(상태값이 OP, DR인 것만.)
    @PostMapping(value = "/remittance/cancel", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String cancelRemittanceByAdmin(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                          HttpServletRequest request, HttpServletResponse response) {
        String method = "cancelRemittanceByAdmin()";

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject body = new JSONObject(requestBody);
        Long remitId = Long.valueOf(body.getString("remit_id"));

        ApiResultVO voResult = new ApiResultVO();
        try {
            remittanceService.cancelRemittanceByAdmin(remitId);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // 사용자가 등록한 송금영수증이 이상할 경우 재등록요청
    @PostMapping(value = "/remittance/receipt-photo/re-register", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String reregisterRemitReceiptPhoto(@RequestHeader("Authorization") String token, @RequestBody String requestBody,
                                              HttpServletRequest request, HttpServletResponse response) {
        String method = "reregisterRemitReceiptPhoto()";

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject body = new JSONObject(requestBody);
        Long remitId = Long.valueOf(body.getString("remit_id"));
        String remitStatus = "DP";
        String message = null;
        try {
            remitStatus = body.getString("remit_status");
            message = body.getString("message");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        ApiResultVO voResult = new ApiResultVO();
        try {
            logger.info("{}: remitId={}, remitStatus={}, message={}", method, remitId, remitStatus, message);
            remittanceService.reregisterRemitReceiptPhoto(remitId, remitStatus, message);

            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    // 사용자가 등록한 송금영수증이 이상할 경우 재등록요청
    @PostMapping(value = "/exchange/receipt-photo/re-register", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String reregisterExchangeReceiptPhoto(@RequestHeader("Authorization") String token,
                                                 @RequestBody String requestBody, HttpServletRequest request, HttpServletResponse response) {
        String method = "reregisterRemitReceiptPhoto()";

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        JSONObject body = new JSONObject(requestBody);
        Long exchangeId = Long.valueOf(body.getString("exchange_id"));
        String message = null;
        String status = null;

        try {
            status = body.getString("status");
            message = body.getString("message");
        } catch (Exception e) {
            logger.info(e.getMessage());
        }

        ApiResultVO voResult = new ApiResultVO();
        try {
            Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(exchangeId);
            exchange.setMessage(message);
            exchange.setStatus(status);
            exchangeService.reregisterExchangeReceiptPhoto(exchange);
            voResult.setSuccessInfo();
        } catch (CashmallowException e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return JsonStr.toJsonString(voResult, response);
    }

    /**
     * 수동 송금 신청
     *
     * @param token
     * @param remitId
     * @param request
     * @param response
     * @return
     * @throws IOException
     */
    @PostMapping(value = "/remittances/{remit_id}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String requestRemittance(@RequestHeader("Authorization") String token,
                                    @PathVariable("remit_id") long remitId,
                                    HttpServletRequest request, HttpServletResponse response) throws IOException {
        ApiResultVO vo = new ApiResultVO(Const.CODE_FAILURE);

        try {

            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(remitId);

            RemittanceStatusCode remitStatus = remittance.getRemitStatus();
            if (!RemittanceStatusCode.DP.equals(remitStatus)) {
                throw new CashmallowException("RemittanceStatusCode is not DP. remitStatus=" + remitStatus);
            }


            Mapping mapping = exchangeService.getMappingByRemitId(remittance.getTravelerId(), remitId);
            if (mapping == null) {
                throw new CashmallowException("Mapping is null. remitStatus=" + remitStatus);
            }

            Map<String, String> params = new HashMap<>();
            params.put("relatedTxnType", RelatedTxnType.REMITTANCE.name());
            params.put("relatedTxnId", String.valueOf(remitId));
            params.put("fundingStatus", FundingStatus.CONFIRM.name());

            List<TransactionRecord> txnRecords = companyService.getTransactionRecordsList(params);
            int count = (txnRecords == null) ? 0 : txnRecords.size();
            if (count != 1) {
                throw new CashmallowException("TransactionRecord fundingStatus is not valid. CONFIRM count=" + count);
            }

            mallowlinkRemittanceService.requestRemittance(remittance);
        } catch (CashmallowException e) {
            logger.error("remitId=" + remitId + ", " + e.getMessage(), e);
            vo.setFailInfo(e.getMessage());
            response.sendError(HttpStatus.SC_BAD_REQUEST, e.getMessage());
        }

        vo = new ApiResultVO(Const.CODE_SUCCESS);
        return JsonStr.toJsonString(vo, response);
    }

    @GetMapping(value = "/remittance/sanctions", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getRemittanceSanctionsList(@RequestHeader("Authorization") String token,
                                             @RequestParam String remitId,
                                             HttpServletRequest request, HttpServletResponse response) {

        String method = "getRemittanceSanctionsList()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return JsonStr.toJsonString(voResult, response);
        }

        // remitId가 null 인경우 오류 처리
        if (StringUtils.isEmpty(remitId)) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_PARAMS), response);
        }

        if (!authService.containsRole(token, Const.ROLE_ASSIMAN)) {
            voResult.setFailInfo2NeedAuth();
            return JsonStr.toJsonString(voResult, response);
        }

        logger.info("{}: remitId={}", method, remitId);

        try {
            Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(Long.valueOf(remitId));

            List<Map<String, Object>> resultMap = octaAmlService.validateRemittanceAmlList(remittance);
            voResult.setSuccessInfo(resultMap);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        logger.debug("voResult: {}", voResult.getMessage());
        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/refund/updateTidOut", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String refundUpdateTidOut(@RequestHeader("Authorization") String token, @RequestBody String requestBody, HttpServletRequest request, HttpServletResponse response) {

        String method = "refundUpdateTidOut()";

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, userId);
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO voResult = new ApiResultVO();
        if (userService.isVerifyRole(userId, ROLE_ADMIN, ROLE_SUPERMAN)) {
            // reuqest body decode
            String jsonStr = CustomStringUtil.decode(token, requestBody);
            Map<String, Object> map = JsonStr.toHashMap(jsonStr);
            Long refundId = Long.valueOf(map.get("refundId").toString());
            String paygateRecOutId = (String) map.get("paygateRecOutId");

            int result = refundService.setRefundTidOutId(refundId, paygateRecOutId);
            if (result == 1) {
                voResult.setSuccessInfo();
            } else {
                voResult.setFailInfo("refund update fail");
            }
            voResult.setSuccessInfo();
        } else {
            voResult.setFailInfo(Const.MSG_NEED_AUTH);
        }


        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    // 유저의 모든 정보 전달 authme 사진 까지 전부 전달
    @GetMapping(value = "/search/user/{id}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getUserAllData(@RequestHeader("Authorization") String token,
                                 HttpServletResponse response,
                                 @PathVariable Long id,
                                 @RequestParam String otp,
                                 @RequestParam boolean whitelisted) {

        String method = "searchUsers()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            logger.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (auths == null || auths.indexOf(Const.ROLE_ASSIMAN) < 0) {
            return JsonStr.toJsonString(new ApiResultVO(Const.CODE_NEED_AUTH), response);
        }

        try {
            // 사무실인 경우 OTP 무시, 그 외에는 OTP 체크
            if (whitelisted || otpService.isValidOtp(authService.getUser(token).getEmail(), otp)) {
                User user = userRepositoryService.getUserByUserId(id);
                Traveler traveler = travelerRepositoryService.getTravelerByUserId(user.getId());
                List<TravelerImage> travelerImages = new ArrayList<>();
                List<TravelerVerificationStatusResponse> allTravelerVerificationStatus = new ArrayList<>();
                if (traveler != null) {
                    if ("W".equalsIgnoreCase(traveler.getCertificationOk())) {
                        traveler = null;
                    } else {
                        traveler.decryptData(securityService);
                        travelerImages = travelerRepositoryService.getTravelerImages(traveler.getId());
                        allTravelerVerificationStatus = travelerRepositoryService.getTravelerVerificationStatuses(traveler.getId());
                    }
                }
                voResult.setSuccessInfo(new UserAllData(user, traveler, travelerImages, allTravelerVerificationStatus));
            } else {
                voResult.setFailInfo("OTP is not valid.");
                return CustomStringUtil.encryptJsonString(token, voResult, response);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }
}

