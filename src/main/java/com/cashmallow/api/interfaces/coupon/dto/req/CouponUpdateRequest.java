package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CouponUpdateRequest {
    List<IsActive> updateList = new ArrayList<>();

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IsActive {
        private Long couponId;
        private String isActive;
    }
}
