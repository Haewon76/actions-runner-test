package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUserInviteCode;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest;

public interface CouponUserInviteCodeMapper {

    CouponUserInviteCode getCouponUserInviteCode(CouponUserInviteCodeRequest couponUserInviteCodeRequest);

    Long insertCouponUserInviteCode(CouponUserInviteCode couponUserInviteCode);

    CouponUserInviteCode getInviteCodeByUserId(Long userId);

    CouponUserInviteCode getUserIdByInviteCode(CouponUserInviteCodeRequest couponUserInviteCodeRequest);
}