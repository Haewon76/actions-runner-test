package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponUserMapper {
    CouponIssueUser getCouponUserByIdAndStatus(@Param("couponUserId") Long couponUserId, @Param("availableStatus") String availableStatus);

    CouponIssueUser getCouponUserById(@Param("couponUserId") Long couponUserId);

    List<CouponUser> getUserListCouponByUserIdsAndCouponId(@Param("userIds") List<Long> userIds, @Param("couponId") Long couponId);

    List<CouponIssueUser> getUserCouponListByUserIdAndLikeCouponCode(@Param("userId") Long userId, @Param("couponCode") String couponCode);

    List<Long> getUserCouponLikeCouponCode(@Param("userId") Long userId, @Param("inviteUserId") Long inviteUserId, @Param("couponCode") String couponCode);

    CouponUser getFirstIssuedUserCouponByCouponId(Long couponId);
}
