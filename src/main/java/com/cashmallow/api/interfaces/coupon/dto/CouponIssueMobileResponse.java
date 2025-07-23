package com.cashmallow.api.interfaces.coupon.dto;

import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CouponIssueMobileResponse {

    private Long id;

    private String sourceCurrency;

    private String name;

    private String code;

    private String inviteCode;

    private String link;

    private DiscountType discountType;

    private String discountTypeKr;

    private BigDecimal discountValue;

    private BigDecimal maxDiscountAmount;

    private ExpireType expireType;

    private String startDate;

    private String endDate;

    private Integer expireDays;

    private BigDecimal minRequiredAmount;

    private String description;
    
    private String thumbnail;

    public static CouponIssueMobileResponse inviteCouponValue(Coupon coupon, String currency, String inviteCode) {
        CouponIssueMobileResponse couponResponse = new CouponIssueMobileResponse();
        couponResponse.setCode(coupon.getCouponCode());
        couponResponse.setSourceCurrency(currency);
        couponResponse.setInviteCode(inviteCode);
        couponResponse.setDiscountType(DiscountType.fromCode(coupon.getCouponDiscountType()));
        couponResponse.setDiscountValue(coupon.getCouponDiscountValue());
        return couponResponse;
    }

}
