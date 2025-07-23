package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Getter
@ToString
public class CouponIssueManagement {
    private Long couponId;
    private String fromCountryCode;
    private String isSystem;
    private String isActive;
    private String targetType;
    private String sendType;
    private LocalDateTime issueDate;
    private String couponCode;
    private String couponName;
    private Long usedCount;
    private BigDecimal couponUsedAmount;
    private String couponDiscountType;
    private BigDecimal couponDiscountValue; // 쿠폰 적용값 (고정 할인 금액, % 할인율)
    private BigDecimal maxDiscountAmount;
    private BigDecimal minRequiredAmount;
    private String serviceType;
    private Long createdId;
    private Long couponIssueId;

    public CouponIssueManagement() {}

    @Builder
    public CouponIssueManagement(Long couponId, String fromCountryCode, String isSystem, String isActive, String targetType, String sendType, LocalDateTime issueDate,
                                 String couponCode, String couponName, Long usedCount, BigDecimal couponUsedAmount, String couponDiscountType,
                                 BigDecimal couponDiscountValue, BigDecimal maxDiscountAmount, BigDecimal minRequiredAmount,
                                 String serviceType, Long createdId, Long couponIssueId) {
        this.couponId = couponId;
        this.fromCountryCode = fromCountryCode;
        this.isSystem = isSystem;
        this.isActive = isActive;
        this.targetType = targetType;
        this.sendType = sendType;
        this.issueDate = issueDate;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.usedCount = usedCount;
        this.couponUsedAmount = couponUsedAmount;
        this.couponDiscountType = couponDiscountType;
        this.couponDiscountValue = couponDiscountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minRequiredAmount = minRequiredAmount;
        this.serviceType = serviceType;
        this.createdId = createdId;
        this.couponIssueId = couponIssueId;
    }
}
