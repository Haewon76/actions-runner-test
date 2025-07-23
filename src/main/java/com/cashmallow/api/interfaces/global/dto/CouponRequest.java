package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.vo.ApplyCurrencyType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@ToString
public class CouponRequest {
    private Long id;
    private String couponCode;
    private String isActive;
    private DiscountType couponDiscountType;
    private BigDecimal couponDiscountValue;
    private LocalDate couponStartDate;
    private LocalDate couponEndDate;
    private Integer expirePeriodDays;
    private ExpireType expireType;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minRequiredAmount;
    private String couponName;
    private ServiceType serviceType;
    private String fromCountryCode;
    private Long syncId;
    private String isSystem;
    private Set<String> applyCurrencyList;
    private ApplyCurrencyType applyCurrencyType;
    private String couponDescription;
    private Long createdId;
    private Long updatedId;
}