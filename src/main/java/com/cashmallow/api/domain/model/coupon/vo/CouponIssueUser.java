package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;


@Getter
@ToString
public class CouponIssueUser {
    private Long id;
    private Long couponId;
    private Long targetUserId;
    private Long inviteUserId;
    private Timestamp createdDate;
    private LocalDateTime couponUsedDate;
    private Timestamp couponUsedDateUtc;
    private BigDecimal couponUsedAmount;
    private String availableStatus;
    private Long couponIssueId;

    public CouponIssueUser() {}

    @Builder
    public CouponIssueUser(Long id, Long couponId, Long targetUserId, Long inviteUserId, Timestamp createdDate, LocalDateTime couponUsedDate, Timestamp couponUsedDateUtc, BigDecimal couponUsedAmount, String availableStatus, Long couponIssueId) {
        this.id = id;
        this.couponId = couponId;
        this.targetUserId = targetUserId;
        this.inviteUserId = inviteUserId;
        this.createdDate = createdDate;
        this.couponUsedDate = couponUsedDate;
        this.couponUsedDateUtc = couponUsedDateUtc;
        this.couponUsedAmount = couponUsedAmount;
        this.availableStatus = availableStatus;
        this.couponIssueId = couponIssueId;
    }
}
