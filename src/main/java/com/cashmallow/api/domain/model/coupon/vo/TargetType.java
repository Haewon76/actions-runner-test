package com.cashmallow.api.domain.model.coupon.vo;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

// 쿠폰 발금 테이블(coupon_issue_dev) 발급 대상(target_type) 컬럼 ENUM
@Getter
@RequiredArgsConstructor
public enum TargetType {
    EVERYONE("everyone", "전체 고객"),     // 전체 고객
    SPECIFIC("specific", "특정 고객"),     // 특정 고객
    LINK("link", "링크")                  // 링크   ex) 인플루언서
    ;

    private String code;
    private String name;

    TargetType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String from(String code) {
        return TargetType.valueOf(code).name();
    }
}
