package com.cashmallow.api.domain.model.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@ToString
public class CouponIssue {

    private Long id;
    private Long couponId;              //쿠폰 테이블 id
    private String targetType;          // 발급 대상 ex) 전체 고객-all, 특정 고객-specific, 링크-link
    private String sendType;            // 발급 타입 ex) 즉시 발급-direct, 예약 발급-reservation
    private String jobKey;              // 예약 발급일 때
    private LocalDateTime issueDate;    // 발급 시각 (LocalDateTime) (즉시 발급일 경우 발급 수행하는 순간의 날짜와 시각. 예약 발급일 경우 예약 시간대에의 날짜와 시각. 유저가 쿠폰을 사용 가능한 날짜와 시작.)
    private Timestamp issueDateUtc;     // 발급 시각 (UTC+0)
    private Long createdId;             // 쿠폰 발급한 운영자 id

    public CouponIssue() {}

    @Builder
    public CouponIssue(Long id, Long couponId, String targetType, String sendType, String jobKey, LocalDateTime issueDate, Timestamp issueDateUtc, Long createdId) {
        this.id = id;
        this.couponId = couponId;
        this.targetType = targetType;
        this.sendType = sendType;
        this.jobKey = jobKey;
        this.issueDate = issueDate;
        this.issueDateUtc = issueDateUtc;
        this.createdId = createdId;
    }

    public CouponIssue withJobKey(String jobKey) {
        return CouponIssue.builder()
                .id(this.id)
                .couponId(this.couponId)
                .targetType(this.targetType)
                .sendType(this.sendType)
                .jobKey(jobKey)
                .issueDate(this.issueDate)
                .issueDateUtc(this.issueDateUtc)
                .createdId(this.createdId)
                .build();
    }
}
