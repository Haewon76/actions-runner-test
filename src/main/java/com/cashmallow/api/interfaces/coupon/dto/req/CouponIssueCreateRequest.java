package com.cashmallow.api.interfaces.coupon.dto.req;

import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.common.DateUtil;
import com.cashmallow.common.TimezoneUtil;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CouponIssueCreateRequest(
        String fromCountryCode,     // 캐시멜로 관리 국가코드 ex) 001
        Long couponId,              // 쿠폰 테이블 id
        String targetType,          // 쿠폰 대상 ex)고객 전체, 특정 고객
        String sendType,            // 발급 타입 ex)즉시 발급, 예약 발급
        String issueDate,           // 발급 시간 (즉시 발급일 경우 발급 수행하는 순간으 날짜가 찍힘. 예약 발급일 경우 예약 시간대에 배치로 발급됨. coupon 테이블 coupon_start_date, coupon_end_date 와 다름)
        Long createdId,             // 쿠폰 발급한 운영자 id
        List<Long> users,           // 쿠폰 발급 대상 유저들
        Long inviteUserId
) {

    public static CouponIssueCreateRequest withExceptOfIssueDate(String fromCountryCode, Long couponId, String targetType, String sendType, String issueDate, Long createdId, List<Long> users, Long inviteUserId) {
        return new CouponIssueCreateRequest(
                fromCountryCode,
                couponId,
                targetType,
                sendType,
                issueDate,
                createdId,
                users,
                inviteUserId
        );
    }

    public CouponIssueCreateRequest withCreatedId(Long createdId) {
        return new CouponIssueCreateRequest(
                this.fromCountryCode,
                this.couponId,
                this.targetType,
                this.sendType,
                this.issueDate,
                createdId,
                this.users,
                this.inviteUserId
        );
    }

    public CouponIssueCreateRequest withUsers(List<Long> users) {
        return new CouponIssueCreateRequest(
                this.fromCountryCode,
                this.couponId,
                this.targetType,
                this.sendType,
                this.issueDate,
                this.createdId,
                users,
                this.inviteUserId
        );
    }

    public CouponIssue toEntity() throws CashmallowException {

        LocalDateTime issueDateTime;
        if (SendType.DIRECT.getCode().equals(sendType)) {
            issueDateTime = DateUtil.toLocalDateTime(fromCountryCode);
        } else {
            issueDateTime = DateUtil.fromY_M_D_H_M_S(issueDate);
        }

        return CouponIssue.builder()
                .couponId(couponId)
                .targetType(targetType)
                .sendType(sendType)
                .issueDate(issueDateTime)
                .issueDateUtc(TimezoneUtil.fromLocalDateTime(fromCountryCode, issueDateTime))
                .createdId(createdId)
                .build();
    }
}