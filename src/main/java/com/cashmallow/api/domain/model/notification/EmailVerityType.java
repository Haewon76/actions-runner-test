package com.cashmallow.api.domain.model.notification;

import lombok.Getter;

@Getter
public enum EmailVerityType {
    BLOCKED(5),    // 차단됨 (로그인 실패 횟수 5회 초과)
    VERIFY(-1000),    // 이메일 인증 코드 확인 용
    NORMAL(0),
    RESET(0),      // 비밀번호 초기화시
    ;     // 기본

    private final int maxFailCount;

    EmailVerityType(int maxFailCount) {
        this.maxFailCount = maxFailCount;
    }

    public static EmailVerityType findByType(int failCount) {
        if (failCount >= BLOCKED.maxFailCount) {
            return BLOCKED;
        }

        if (VERIFY.maxFailCount == failCount) {
            return VERIFY;
        }

        return NORMAL;
    }
}
