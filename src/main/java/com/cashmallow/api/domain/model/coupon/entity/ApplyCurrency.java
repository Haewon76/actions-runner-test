package com.cashmallow.api.domain.model.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class ApplyCurrency {

    Long couponId;
    String targetIso3166;

    public ApplyCurrency() {}

    @Builder
    public ApplyCurrency(Long couponId, String targetIso3166) {
        this.couponId = couponId;
        this.targetIso3166 = targetIso3166;
    }
}
