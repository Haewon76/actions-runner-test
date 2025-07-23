package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUserInviteCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest;

public interface CouponUserInviteCodeService {
    CouponUserInviteCode getCouponUserInviteCode(CouponUserInviteCodeRequest couponUserInviteCodeRequest);

    void getCouponUserInviteCodeV3(CouponUserInviteCodeRequest couponUserInviteCodeRequest) throws CashmallowException;

    CouponUserInviteCode getInviteCodeByUserId(Long userId);

    CouponUserInviteCode getUserIdByInviteCode(CouponUserInviteCodeRequest inviterUserRequest);

}
