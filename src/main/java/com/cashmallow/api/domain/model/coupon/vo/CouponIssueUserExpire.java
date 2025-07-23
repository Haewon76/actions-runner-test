package com.cashmallow.api.domain.model.coupon.vo;


import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@ToString
public class CouponIssueUserExpire {
    private String fromCountryCode;
    private String couponCode;
    private String couponName;
    private String expireType;
    private LocalDate couponStartDate;
    private LocalDate couponEndDate;
    private Integer expirePeriodDays;
    private Long targetUserId;
    private Long couponIssueUserId;
    private Long couponIssueId;
    private Long couponId;
    private LocalDate issueDate;

    public CouponIssueUserExpire() {}

    @Builder
    public CouponIssueUserExpire(String fromCountryCode, String couponCode, String couponName, String expireType
            , LocalDate couponStartDate, LocalDate couponEndDate, Integer expirePeriodDays, Long targetUserId, Long couponIssueUserId
            , Long couponIssueId, Long couponId
            , LocalDate issueDate) {
        this.fromCountryCode = fromCountryCode;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.expireType = expireType;
        this.couponStartDate = couponStartDate;
        this.couponEndDate = couponEndDate;
        this.expirePeriodDays = expirePeriodDays;
        this.targetUserId = targetUserId;
        this.couponIssueUserId = couponIssueUserId;
        this.couponIssueId = couponIssueId;
        this.couponId = couponId;
        this.issueDate = issueDate;
    }

    public static CouponIssueUserExpire toLocalDate(CouponMobileUser data) {
        // 만료 체크는 날짜로 하므로 DB 에서의 LocalDateTime 을 자바에서는 LocalDate 로 변환
        LocalDate issueDate = data.getIssueDate().toLocalDate();

        return CouponIssueUserExpire.builder()
                    .fromCountryCode(data.getFromCountryCode())
                    .couponCode(data.getCouponCode())
                    .couponName(data.getCouponName())
                    .expireType(data.getExpireType())
                    .couponStartDate(data.getCouponStartDate())
                    .couponEndDate(data.getCouponEndDate())
                    .expirePeriodDays(data.getExpirePeriodDays())
                    .targetUserId(data.getUserId())
                    .couponIssueUserId(data.getCouponUserId())
                    .couponIssueId(data.getCouponIssueId())
                    .couponId(data.getCouponId())
                    .issueDate(issueDate)
                .build();
    }
}
