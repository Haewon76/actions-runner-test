package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;

import java.util.List;

@Builder
public record CouponDeleteRequest(
        List<Long> couponIdList
) {

}