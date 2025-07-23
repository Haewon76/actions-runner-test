package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CouponSystemManagementRequest {
    private String fromCountryCode;
    private String couponType;
    private LocalDate currentDate;

    public CouponSystemManagementRequest() {}

    @Builder
    public CouponSystemManagementRequest(String fromCountryCode, String couponType, LocalDate currentDate) {
        this.fromCountryCode = fromCountryCode;
        this.couponType = couponType;
        this.currentDate = currentDate;
    }
}
