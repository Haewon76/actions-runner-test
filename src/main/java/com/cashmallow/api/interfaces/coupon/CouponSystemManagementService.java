package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;

import java.util.List;

public interface CouponSystemManagementService {

    CouponSystemManagement getUsingCouponDateRange(CouponSystemManagementRequest couponSystemManagementRequest);
    CouponSystemManagement getUsingCoupon(CouponSystemManagementRequest couponSystemManagementRequest);
    List<CouponSystemManagement> getUsingCouponAllCouponType(String fromCountryCode);

    void createManageSystemCoupon(CouponSystemManagement couponSystemManagement);

    List<Coupon> getNewSystemCouponList(CouponSystemManagementRequest couponSystemManagementRequest);

    int updateManageSystemCoupon(Long createdId, Long couponId, String isApplied);

    CouponSystemManagement getManageCouponByCouponId(Long couponId);
    List<CouponSystemManagement> getUsingOrLaterCouponList(CouponSystemManagementRequest couponSystemManagementRequest);

}
