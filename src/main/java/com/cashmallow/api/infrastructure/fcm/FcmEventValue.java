package com.cashmallow.api.infrastructure.fcm;

// FCM Event Value
public enum FcmEventValue {
    // 인증
    AI, // 여행자 신분증 인증
    AP, // 여행자 여권 인증
    AT, // 여행자 환불 계좌 인증
    AB, // 가맹점 사업자 번호 인증
    AS, // 가맹점 payback 계좌 인증
    AC, // JP 여행자 본인인증시 계좌사진 요청

    // 환전, 인출, 송금
    OP, // 신청
    CF, // 완료
    TC, // 여행자 취소시 가맹점 알림
    CC, // 캐시멜로 취소시 여행자 알림
    SC, // 가맹점 취소시 여행자 알림
    DP, // 송금 입금 매핑 완료
    RR,  // 송금 리젝
    DR,  // 송금영수증 재등록
    AR,   // 일본 환불계좌 재등록 요청

    // 일본 지갑 만료
    XP,  // 일본 지갑 만료
    BF   // 일본 지갑만료 7일전
}
