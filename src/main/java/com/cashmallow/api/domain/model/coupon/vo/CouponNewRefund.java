package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class CouponNewRefund {
    private final Long exchangeOrRemitId;
    private final Long couponUserId;
    private final Long couponDiscountAmount;

    public CouponNewRefund(Long exchangeOrRemitId, Long couponUserId, Long couponDiscountAmount) {
        this.exchangeOrRemitId = exchangeOrRemitId;
        this.couponUserId = couponUserId;
        this.couponDiscountAmount = couponDiscountAmount;
    }
}
