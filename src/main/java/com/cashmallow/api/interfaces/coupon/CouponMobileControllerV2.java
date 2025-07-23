package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.coupon.dto.CouponIssueMobileResponse;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueUserResponse;
import com.cashmallow.common.CustomStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

import static com.cashmallow.api.domain.shared.Const.NO_COUPONS_AVAILABLE;
import static com.cashmallow.api.domain.shared.Const.NO_INPUT_COUPON;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;


@RestController
@Slf4j
@RequestMapping(value = "/coupon")
@RequiredArgsConstructor
public class CouponMobileControllerV2 {

    private final AuthService authService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    private final CouponMobileServiceV2 couponMobileService;
    private final UserRepositoryService userRepositoryService;
    private final CountryService countryService;

    @GetMapping(value = "/{currency}/{couponCode}")
    public String getSystemCoupon(@RequestHeader(name = "Authorization") String accessToken,
                                  @PathVariable String currency,
                                  @PathVariable SystemCouponType couponCode,
                                  HttpServletResponse response,
                                  HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);
        Long userId = authService.getUserId(accessToken);

        if (userId == Const.NO_USER_ID) {
            log.info("getSystemCoupon(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(accessToken, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        try {

            CouponIssueMobileResponse couponIssueMobileResponse
                    = couponMobileService.getMobileSystemCoupon(currency, SystemCouponType.thankYouMyFriend.getCode(), userId);

            if (couponIssueMobileResponse == null) {
                resultVO.setResult(Const.CODE_SUCCESS, Const.COUPON_ALREADY_REGISTERED
                        , messageSource.getMessage("NO_COUPONS_AVAILABLE", null, "No Coupons Available..", locale));
            } else {
                resultVO.setSuccessInfo(couponIssueMobileResponse);
            }

        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
    }

    @PostMapping(value ="/mobile")
    public String addMobileCoupon(@RequestHeader("Authorization") String accessToken,
                                  @RequestBody String requestBody,
                                  HttpServletResponse response,
                                  HttpServletRequest request) {

        Locale locale = localeResolver.resolveLocale(request);
        Long userId = authService.getUserId(accessToken);

        if (userId == Const.NO_USER_ID) {
            log.info("addMobileCoupon(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(accessToken, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        try {
            String jsonStr = CustomStringUtil.decode(accessToken, requestBody);
            JSONObject body = new JSONObject(jsonStr);
            String couponCode = body.getString("couponCode");
            String fromCurrency = body.getString("fromCurrency");

            if (StringUtils.isEmpty(couponCode) || StringUtils.isEmpty(fromCurrency)) {
                log.error("{}: couponCode is invalid. fromCurrency={}", couponCode, fromCurrency);
                throw new CashmallowException("NO_INPUT_COUPON");
            }

            User user = userRepositoryService.getUserByUserId(userId);
            // countryCode(캐시멜로 관리 코드): 001, 004
            String countryCode = user.getCountry();
            Country country = countryService.getCountry(countryCode);
            // iso3166 코드: HK, JP
            String iso3166 = country.getIso3166();
            ZoneId zoneId = CountryCode.fromIso3166(iso3166).getZoneId();

            if (StringUtils.isNotEmpty(couponCode)) {
                // 쿠폰코드 대문자로 upperCase
                resultVO = couponMobileService.issueMobileCouponsV3(couponCode.toUpperCase(), iso3166, country.getIso4217(), userId, zoneId, locale, null);
            } else {
                throw new CashmallowException("DATA_NOT_FOUND_ERROR");
            }

        } catch (CashmallowException e) {
            log.error("addMobileCoupon():{}", e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        } catch (Exception e) {
            log.error("addMobileCoupon():{}", e.getMessage(), e);
            resultVO.setFailInfo(INTERNAL_SERVER_ERROR);
        }

        return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
    }

    @GetMapping(value = {"/mobile/{serviceType}", "/v2/mobile/{serviceType}"})
    public String getCouponListIssueUserV2(@RequestHeader(name = "Authorization") String accessToken,
                                     HttpServletResponse response,
                                     @PathVariable ServiceType serviceType,
                                     @RequestParam(required = false) BigDecimal fee,
                                     @RequestParam(required = false) String fromCurrency,
                                     @RequestParam(required = false) BigDecimal fromMoney,
                                     HttpServletRequest request) {


        Locale locale = localeResolver.resolveLocale(request);
        Long userId = authService.getUserId(accessToken);

        if (userId == Const.NO_USER_ID) {
            log.debug("getCouponIssueUser(): checkTokenInSession(): NOT_STORED_TOKEN CODE_INVALID_TOKEN !!!!!");
            return CustomStringUtil.encryptJsonString(accessToken, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        try {

            // 쿠폰함 조회
            List<CouponIssueUserResponse> couponIssueUserResponses = null;
            if (fromMoney == null) {
                couponIssueUserResponses = couponMobileService.getCouponIssueUserMyPageV2(userId, fromCurrency, locale);
                log.debug("쿠폰함 조회: getCouponIssueUserMyPageV2: {}", couponIssueUserResponses);
            // 환전,송금 요청 전 쿠폰 선택할 때 목록 조회하여 계산
            } else {
                couponIssueUserResponses = couponMobileService.getCouponIssueUserV2(userId, serviceType, fromCurrency, fromMoney.subtract(fee), fee, locale, Const.FALSE);
                log.debug("쿠폰 할인된 가격 조회: getCouponIssueUserV2: {}", couponIssueUserResponses);
            }

            if (couponIssueUserResponses == null) {
                resultVO.setResult(Const.CODE_SUCCESS, NO_COUPONS_AVAILABLE, messageSource.getMessage(NO_COUPONS_AVAILABLE, null, "No Coupons Available.", locale));
            } else {
                resultVO.setSuccessInfo(couponIssueUserResponses);
            }

        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(accessToken, resultVO, response);
    }
}