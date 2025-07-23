package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class GlobalUpdateStatusUserCouponDto {
    String availableStatus;
    List<UpdateStatusUserCoupon> userCouponList;

    public GlobalUpdateStatusUserCouponDto() {}

    public GlobalUpdateStatusUserCouponDto(String availableStatus, List<UpdateStatusUserCoupon> userCouponList) {
        this.availableStatus = availableStatus;
        this.userCouponList = userCouponList;
    }
}
