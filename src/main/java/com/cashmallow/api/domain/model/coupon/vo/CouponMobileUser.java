package com.cashmallow.api.domain.model.coupon.vo;

import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CouponMobileUser {

    private Long couponUserId;
    private Long userId;
    private Long inviteUserId;
    private Long couponIssueId;
    private Long couponId;
    private String fromCountryCode;
    private String isSystem;
    private String isActive;
    private String serviceType;
    private String couponCode;
    private String couponName;
    private String couponDescription;
    private LocalDate couponCalStartDate; // 시작일: issueDate (발급일 혹은 예약발급일)
    private LocalDate couponCalEndDate;   // 종료일: 계산한 만료일
    private BigDecimal couponCalDiscountValue;
    private String couponDiscountType;
    private BigDecimal couponDiscountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minRequiredAmount;
    private String expireType;
    private String jobKey;
    private LocalDateTime issueDate;
    private Timestamp issueDateUtc;
    private Integer expirePeriodDays;
    private LocalDate couponStartDate;
    private LocalDate couponEndDate;
    private Timestamp createdDate;

    @Builder
    public CouponMobileUser(Long couponUserId, Long userId, Long inviteUserId, Long couponIssueId, Long couponId, String fromCountryCode, String isSystem, String isActive, String serviceType, String couponCode, String couponName, String couponDescription, LocalDate couponCalStartDate, LocalDate couponCalEndDate, BigDecimal couponCalDiscountValue, String couponDiscountType, BigDecimal couponDiscountValue, BigDecimal maxDiscountAmount, BigDecimal minRequiredAmount, String expireType, String jobKey, LocalDateTime issueDate, Timestamp issueDateUtc, Integer expirePeriodDays, LocalDate couponStartDate, LocalDate couponEndDate, Timestamp createdDate) {
        this.couponUserId = couponUserId;
        this.userId = userId;
        this.inviteUserId = inviteUserId;
        this.couponIssueId = couponIssueId;
        this.couponId = couponId;
        this.fromCountryCode = fromCountryCode;
        this.isSystem = isSystem;
        this.isActive = isActive;
        this.serviceType = serviceType;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.couponDescription = couponDescription;
        this.couponCalStartDate = couponCalStartDate;
        this.couponCalEndDate = couponCalEndDate;
        this.couponCalDiscountValue = couponCalDiscountValue;
        this.couponDiscountType = couponDiscountType;
        this.couponDiscountValue = couponDiscountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minRequiredAmount = minRequiredAmount;
        this.expireType = expireType;
        this.jobKey = jobKey;
        this.issueDate = issueDate;
        this.issueDateUtc = issueDateUtc;
        this.expirePeriodDays = expirePeriodDays;
        this.couponStartDate = couponStartDate;
        this.couponEndDate = couponEndDate;
        this.createdDate = createdDate;
    }
}
