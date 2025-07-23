package com.cashmallow.api.interfaces.global.dto;

import java.util.List;
import java.util.Set;

public record GlobalSystemCouponApplyCurrencyDto(
        Long couponId                       // 쿠폰 id
        , List<String> applyCurrencyList     // 쿠폰에 대한 적용 통화국가 iso3166
) {}
