package com.cashmallow.api.domain.model.coupon.vo;

import lombok.Getter;


@Getter
public enum StatusType {
    REGISTRATION,   // 등록
    USED,                   // 사용완료
    EXPIRED,             // 만료
    REVOKED,             // 회수
    AVAILABLE
    ;
}
