package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponSystemManagementMapper {

    CouponSystemManagement getUsingCouponDateRange(CouponSystemManagementRequest couponSystemManagementRequest);
    CouponSystemManagement getUsingCoupon(CouponSystemManagementRequest couponSystemManagementRequest);
    List<CouponSystemManagement> getUsingCouponAllCouponType(@Param("fromCountryCode") String fromCountryCode, @Param("couponTypes") List<String> couponTypes);

    List<Coupon> getNewSystemCouponList(CouponSystemManagementRequest couponSystemManagementRequest);

    void createManageSystemCoupon(CouponSystemManagement couponSystemManagement);
    int updateManageSystemCoupon(@Param("updatedId") Long updatedId, @Param("couponId") Long couponId, @Param("isApplied") String isApplied);

    CouponSystemManagement getManageCouponByCouponId(Long couponId);
    List<CouponSystemManagement> getUsingOrLaterCouponList(CouponSystemManagementRequest couponSystemManagementRequest);
}