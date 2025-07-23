package com.cashmallow.api.interfaces.coupon.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
public class CouponCalcResponse {
    
    private Long couponUserId;

    private BigDecimal paymentAmount;

    private BigDecimal discountAmount;


}
