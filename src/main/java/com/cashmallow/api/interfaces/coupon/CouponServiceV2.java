package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUpdateRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponReadResponse;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponResponse;

import java.time.LocalDate;
import java.util.List;

public interface CouponServiceV2 {

    List<CouponReadResponse> getCouponList(CouponSearchRequest couponSearchRequest) throws CashmallowException;

    CouponResponse createCoupon(CouponCreateRequest couponCreateRequest) throws CashmallowException;

    Coupon getCouponById(Long couponId);

    List<Long> getUsersByFromCountryCode(String fromCountryCode);

    void updateCouponActive(String fromCountryCode, List<CouponUpdateRequest.IsActive> updateList) throws CashmallowException;

    int deleteCoupon(List<Long> couponIds) throws CashmallowException;

    List<Coupon> getIssuableCoupons(String fromCountryCode, LocalDate currentDate, String searchStartDate, String searchEndDate) throws CashmallowException;

    Long getCouponTotalCount(CouponSearchRequest couponSearchRequest);

    // 정확한 쿠폰코드, 시스템 쿠폰이 아닌 것 조회 (이벤트 쿠폰 or 인플루언서 쿠폰)
    Coupon getCouponByCouponCode(String fromCountryCode, String isSystem, String isActive, String couponCode);

    Long checkDuplicateCode(String fromCountryCode, String couponCode);

    List<String> getApplyCurrencyListByCouponId(Long couponId);

    int deleteApplyCurrencyByCouponId(Long couponId, List<String> applyCurrencyList);
}