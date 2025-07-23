package com.cashmallow.api.infrastructure.alarm;

public enum SlackChannel {
    ERROR,          // 개발팀 ERROR, Exception
    INFO,           // 개발팀 INFO
    ADMIN_ALERT,    // 운영팀 중요 알림
    ADMIN_MESSAGE,    // 운영팀 불필요한 알림
    ADMIN_EDD,    // 운영팀 EDD 대상자 알림
    SYSTEM_ALERT,   // 운영팀 시스템 로그 알림
    BUILD,           // 빌드 메시지

    ADMIN_ALERT_JP,    // JP 운영팀 중요 알림
    ADMIN_MESSAGE_JP,    // JP 운영팀 불필요한 알림
    ADMIN_EDD_JP,    // JP 운영팀 EDD 대상자 알림
    ;
}
