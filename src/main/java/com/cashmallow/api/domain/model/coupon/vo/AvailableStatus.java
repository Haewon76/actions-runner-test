package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Getter;

@Getter
public enum AvailableStatus {
    AVAILABLE,      // 사용가능
    RESERVATION,    // 예약
    USED,           // 사용됨
    EXPIRED,        // 만료
    REVOKED         // 회수 (발급 되었으나 사용 조건이 맞지 않았을 경우: ex Welcome 쿠폰)
}