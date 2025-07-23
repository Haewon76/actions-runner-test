package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.coupon.dto.CouponCalcResponse;
import com.cashmallow.api.interfaces.coupon.dto.CouponIssueMobileResponse;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueUserResponse;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;

public interface CouponMobileServiceV2 {

    CouponIssueMobileResponse getMobileSystemCoupon(String currency, String couponCodePrefix, Long userId) throws CashmallowException;

    List<CouponIssueUserResponse> getCouponIssueUserV2(Long userId, ServiceType serviceType, String fromCurrency, BigDecimal fromMoney, BigDecimal fee, Locale locale, boolean amountWithFeeFlag) throws CashmallowException;

    List<CouponIssueUserResponse> getCouponIssueUserMyPageV2(Long userId, String fromCurrency, Locale locale);

    ApiResultVO issueMobileCouponsV3(String couponCode, String iso3166, String fromCurrency, Long userId, ZoneId zoneId, Locale locale, Long couponUserId) throws CashmallowException;

    void addSystemCouponApplyCurrency(String fromCountryCode, Coupon coupon);

    Coupon isApplyingSystemCoupon(String iso3166, CouponSystemManagementRequest couponSystemManagementRequest);

    CouponCalcResponse calcCouponV2(Country fromCountry, Country toCountry, BigDecimal fromMoney, BigDecimal feePerAmt, Long couponUserId, Long userId, ServiceType serviceType, boolean amountWithFeeFlag) throws CashmallowException;

    void useCouponUser(String fromCountryCode, Long userId, Long couponUserId, BigDecimal discountAmount, String serviceType) throws CashmallowException;

    Long cancelCouponUserV2(Long couponUserId, String exchangeOrRemittanceStatus) throws CashmallowException;

    String getCouponCode(String couponCodePrefix, String iso3166, String couponCodeBody);

    int updateCouponStatus(Long couponUserId, AvailableStatus availableStatus);
    int updateListCouponStatus(List<Long> couponUserIds, AvailableStatus availableStatus);
}
