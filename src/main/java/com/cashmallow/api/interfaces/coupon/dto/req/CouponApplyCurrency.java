package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class CouponApplyCurrency {
    Long couponId;
    List<String> applyCurrencyList;

    public CouponApplyCurrency() { }

    public CouponApplyCurrency(Long couponId, List<String> applyCurrencyList) {
        this.couponId = couponId;
        this.applyCurrencyList = applyCurrencyList;
    }
}