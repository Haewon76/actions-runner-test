package com.cashmallow.api.interfaces.coupon.dto.req;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CouponUseUpdateRequest(
        Long couponUserId,
        BigDecimal discountAmount,
        String availableStatus,
        LocalDateTime couponUsedDate
) {
}
