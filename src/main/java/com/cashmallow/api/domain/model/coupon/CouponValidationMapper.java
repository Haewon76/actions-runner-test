package com.cashmallow.api.domain.model.coupon;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponValidationMapper {

    int countUsedCouponByCouponCodeAndUserIds(@Param("userIds") List<Long> userIds
            , @Param("availableStatus") String availableStatus, @Param("couponCode") String couponCode);
}
