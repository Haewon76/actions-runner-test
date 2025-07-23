package com.cashmallow.api.domain.model.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@ToString
public class CouponUser {

    private Long id;
    private Long couponIssueId;      // 쿠폰 발급 테이블 id
    private Long couponId;           // 쿠폰 테이블 id
    private Long targetUserId;       // 발급 대상 user id
    private Long inviteUserId;       // 초대한 user id
    private String availableStatus;     // 쿠폰 상태값 ex)미사용, 사용, 만료
    private BigDecimal couponUsedAmount;   // 사용한 쿠폰 금액
    private LocalDateTime couponUsedDate;
    private Timestamp couponUsedDateUtc;

    public CouponUser() {}

    @Builder
    public CouponUser(Long id, Long couponIssueId, Long couponId, Long targetUserId, Long inviteUserId, String availableStatus, BigDecimal couponUsedAmount, LocalDateTime couponUsedDate, Timestamp couponUsedDateUtc) {
        this.id = id;
        this.couponIssueId = couponIssueId;
        this.couponId = couponId;
        this.targetUserId = targetUserId;
        this.inviteUserId = inviteUserId;
        this.availableStatus = availableStatus;
        this.couponUsedAmount = couponUsedAmount;
        this.couponUsedDate = couponUsedDate;
        this.couponUsedDateUtc = couponUsedDateUtc;
    }
}
