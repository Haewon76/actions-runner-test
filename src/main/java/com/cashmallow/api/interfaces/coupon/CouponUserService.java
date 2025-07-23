package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;

import java.util.List;


public interface CouponUserService {

    List<CouponUser> getUserListCouponByUserIdsAndCouponId(List<Long> users, Long id);

    List<CouponIssueUser> getUserCouponListByUserIdAndLikeCouponCode(Long userId, String couponCode);

    List<Long> getUserCouponLikeCouponCode(Long userId, Long inviteUserId, String couponCode);

    void sendCouponMessage(List<User> targetUsers);

    void sendCouponPushMessage(List<User> targetUsers, FcmEventCode eventCode, FcmEventValue eventValue);

    CouponIssueUser getCouponUserByIdAndStatus(Long couponUserId, AvailableStatus availableStatus);

    CouponUser getFirstIssuedUserCouponByCouponId(Long couponId);

    CouponIssueUser getCouponUserById(Long couponUserId);
}
