package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;

import java.sql.Timestamp;
import java.util.List;

public record GlobalSystemCouponDto(
        String fromCountryCode
        , Long id
        , Long couponId              // 쿠폰 테이블 id
        , String targetType          // 발급 대상 ex) 전체 고객-all, 특정 고객-specific, 링크-link
        , String sendType            // 발급 타입 ex) 즉시 발급-direct, 예약 발급-reservation
        , Timestamp issueDateUtc     // 발급 시각 (UTC+0)
        , List<Long> userIds
        , Long inviteUserId

) {
    public GlobalSystemCouponDto(CouponIssue couponIssue, String fromCountryCode, List<Long> userIds, Long inviteUserId) {
        this(fromCountryCode
                , couponIssue.getId()
                , couponIssue.getCouponId()              //쿠폰 테이블 id
                , couponIssue.getTargetType()            // 발급 대상 ex) 전체 고객-all, 특정 고객-specific, 링크-link
                , couponIssue.getSendType()              // 발급 타입 ex) 즉시 발급-direct, 예약 발급-reservation
                , couponIssue.getIssueDateUtc()          // 발급 시각 (LocalDateTime) (즉시 발급일 경우 발급 수행하는 순간의 날짜와 시각. 예약 발급일 경우 예약 시간대에의 날짜와 시각. 유저가 쿠폰을 사용 가능한 날짜와 시작.)
                , userIds
                , inviteUserId
        );
    }
}
