package com.cashmallow.api.interfaces.aml.complyadvantage.enums;

public enum ComplyAdvantageStatusCode {
    NOT_STARTED, // 시작되지 않음.
    IN_PROGRESS, // 워크플로 진행중
    COMPLETED, // 워크플로 완료, step_output에서 확인가능
    SKIPPED, // 기준 미달시 단계 스킵. customer 생성시 고객 위험이 금지된 스크리닝 단계에 적용됩니다, 워크플로상엔 없고, Customer Step단계에 존재.
    ERRORED //  워크플로가 오류와 함께 완료.
}
