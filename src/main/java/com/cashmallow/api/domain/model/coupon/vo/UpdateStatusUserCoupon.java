package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateStatusUserCoupon {
    Long couponIssueUserSyncId;
    Long couponIssueSyncId;
    Long couponIssueId;
    Long userId;

    public UpdateStatusUserCoupon() { }

    @Builder
    public UpdateStatusUserCoupon(Long couponIssueUserSyncId, Long couponIssueSyncId, Long couponIssueId, Long userId) {
        this.couponIssueUserSyncId = couponIssueUserSyncId;
        this.couponIssueSyncId = couponIssueSyncId;
        this.couponIssueId = couponIssueId;
        this.userId = userId;
    }

    public static UpdateStatusUserCoupon ofExpire(CouponIssueUserExpire data) {
        return UpdateStatusUserCoupon.builder()
                .couponIssueUserSyncId(data.getCouponIssueUserId()) // 쿠폰 고유 아이디(JP DB 적재를 위한 식별 ID)
                .couponIssueSyncId(data.getCouponIssueId())         // 쿠폰 발급 아이디(JP DB 적재를 위한 식별 ID)
                .userId(data.getTargetUserId())
                .build();
    }

    public static UpdateStatusUserCoupon ofAvailable(CouponIssueUser data) {
        return UpdateStatusUserCoupon.builder()
                .couponIssueUserSyncId(data.getId())        // 쿠폰 고유 아이디(JP DB 적재를 위한 식별 ID)
                .couponIssueSyncId(data.getCouponIssueId()) // 쿠폰 발급 아이디(JP DB 적재를 위한 식별 ID)
                .userId(data.getTargetUserId())             // 쿠폰 발급 받은 유저 아이디
                .build();
    }

    public static UpdateStatusUserCoupon ofRevoke(CouponIssueUser data) {
        return UpdateStatusUserCoupon.builder()
                .couponIssueUserSyncId(data.getId())             // 쿠폰 고유 아이디(JP DB 적재를 위한 식별 ID)
                .couponIssueSyncId(data.getCouponIssueId())      // 쿠폰 발급 아이디(JP DB 적재를 위한 식별 ID)
                .userId(data.getTargetUserId())
                .build();
    }
}
