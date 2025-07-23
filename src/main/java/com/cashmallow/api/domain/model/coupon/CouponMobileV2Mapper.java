package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.CouponMobileUser;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponMobileUserRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUseUpdateRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponMobileV2Mapper {

    List<CouponMobileUser> getCouponListIssueUsers(CouponMobileUserRequest couponMobileUserRequest);

    int useCouponUser(CouponUseUpdateRequest couponUseRequest);

    int cancelCouponUser(Long couponUserId);

    int updateCouponStatus(@Param("couponUserId") Long couponUserId, @Param("availableStatus") String availableStatus);
    int updateListCouponStatus(@Param("couponUserIds") List<Long> couponUserIds, @Param("availableStatus") String availableStatus);

    CouponUser getUserCouponByUserIdAndCouponId(@Param("userId") Long userId, @Param("couponId") Long couponId);

    CouponUser getUserCouponById(Long couponUserId);
}