package com.cashmallow.api.infrastructure.fcm;

// FCM Event Code
public enum FcmEventCode {
    AU, // 신분증, 여권, 환불계좌, 사업자번호, payback 계좌 인증
    EX, // 환전
    CO, // 인출
    RF, // 환불
    PB, // 페이백
    PY, // 결제
    RM,  // 해외 송금
    COUPON_ISSUE,           // 신규 쿠폰 발급
    COUPON_BIRTHDAY,        // 생일 쿠폰 발급
    COUPON_WELCOME_EXPIRE,  //  가입 쿠폰 만료
    COUPON_EXPIRE,          // 쿠폰 만료
    WL // 지갑
}
