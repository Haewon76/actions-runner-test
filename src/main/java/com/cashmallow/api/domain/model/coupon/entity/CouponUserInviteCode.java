package com.cashmallow.api.domain.model.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@ToString
public class CouponUserInviteCode {
    Long id;
    Long userId;
    // String country;
    String inviteCode;
    // Long inviteCodeUserId;
    LocalDateTime createdAt;

    public CouponUserInviteCode() {}

    @Builder
    public CouponUserInviteCode(Long id, Long userId/*, String country*/, String inviteCode/*, Long inviteCodeUserId*/, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        // this.country = country;
        this.inviteCode = inviteCode;
        // this.inviteCodeUserId = inviteCodeUserId;
        this.createdAt = createdAt;
    }
}
