package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.impl.CustomerCenterServiceImpl;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.customercenter.NoticeContent;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.common.CommNet;
import com.cashmallow.common.JsonStr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Objects;

@Controller
public class CustomerCenterController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerCenterController.class);

    @Autowired
    private CustomerCenterServiceImpl customerCenterService;

    @Autowired
    private AuthService authService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    private MessageSource messageSource;

    // 공지사항 목록 조회
    @GetMapping(value = {
            "/traveler/customer-center/notice-contents",
            "/traveler/customer-center/notice-contents/v2"
    }, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getNoticeContents(@RequestHeader("Authorization") String token,
                                    @RequestParam("page") Integer page, @RequestParam("size") Integer size, @RequestParam("sort") String sort,
                                    @RequestParam(value = "isPosting", required = false) Boolean isPosting,
                                    @RequestParam(value = "fromDate", required = false) String fromDate, @RequestParam(value = "toDate", required = false) String toDate,
                                    @RequestParam(value = "postingStatus", required = false) String postingStatus, @RequestParam(value = "searchValue", required = false) String searchValue,
                                    HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {  // NOTE : 로그인 하지 않으면 조회 불가
            logger.info("getNoticeContents(): CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.debug("getNoticeContents(): jsonStr={}", jsonStr);

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String deviceLangKey = locale.getLanguage();
            SearchResultVO searchResultVo = customerCenterService.getNoticeContents(deviceLangKey, userId, isPosting, fromDate, toDate, Objects.requireNonNullElse(postingStatus, "GOING"), searchValue, page, size, sort);
            voResult.setSuccessInfo(searchResultVo);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo("INTERNAL_SERVER_ERROR");
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    // 개별 공지사항 조회 - 고객 가입 국가 기준 조회
    @GetMapping(value = "/traveler/customer-center/notice-contents/{noticeContentId}", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getNoticeContent(@RequestHeader("Authorization") String token, @PathVariable Long noticeContentId, HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {  // NOTE : 로그인 하지 않으면 조회 불가
            logger.info("getNoticeContent(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.info("getNoticeContent(): jsonStr={}", jsonStr);

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String deviceLangKey = locale.getLanguage();
            NoticeContent noticeContent = customerCenterService.getNoticeContentByExactLanguageTypeTraveler(noticeContentId, userId, deviceLangKey);
            voResult.setSuccessInfo(noticeContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }

    // 팝업 공지사항 조회 (사용자 언어 세팅에 따라 조회)
    @GetMapping(value = {
            "/traveler/customer-center/notice-contents/popup",
            "/traveler/customer-center/notice-contents/popup/v2"
    }, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getPopupNotice(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {  // NOTE : 로그인 하지 않으면 조회 불가
            logger.info("getPopupNotice(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return JsonStr.toJsonString(voResult, response);
        }

        String jsonStr = CommNet.extractPostRequestBody(request);
        logger.info("getPopupNotice(): jsonStr={}", jsonStr);

        Locale locale = localeResolver.resolveLocale(request);

        try {
            String deviceLangKey = locale.getLanguage();
            NoticeContent noticeContent = customerCenterService.getPopupNotice(userId, deviceLangKey);
            voResult.setSuccessInfo(noticeContent);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        voResult.setMessage(messageSource.getMessage(voResult.getMessage(), null, voResult.getMessage(), locale));

        return JsonStr.toJsonString(voResult, response);
    }
}
