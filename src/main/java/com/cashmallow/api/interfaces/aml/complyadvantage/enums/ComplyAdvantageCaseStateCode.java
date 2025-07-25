package com.cashmallow.api.interfaces.aml.complyadvantage.enums;

public enum ComplyAdvantageCaseStateCode {
    ONBOARDING_NOT_STARTED, //Not started-
    ONBOARDING_IN_PROGRESS, // In progress
    ONBOARDING_POSITIVE_END_STATE, //No risk- Accept
    ONBOARDING_MANAGEABLE_RISK_POSITIVE_END_STATE, //Manageable risk- Accept
    ONBOARDING_NEGATIVE_END_STATE, //Unacceptable Risk -Reject
    MONITORING_NOT_STARTED,
    MONITORING_IN_PROGRESS,
    MONITORING_ESCALATED,
    MONITORING_BLOCKED,
    MONITORING_RISK_DETECTED,
    MONITORING_NO_RISK_DETECTED_POSITIVE_END_STATE,
    MONITORING_RISK_DETECTED_POSITIVE_END_STATE,
    MONITORING_RISK_DETECTED_NEGATIVE_END_STATE,
    PAYMENT_SCREENING_NOT_STARTED,
    PAYMENT_SCREENING_IN_PROGRESS,
    PAYMENT_SCREENING_ESCALATED,
    PAYMENT_SCREENING_BLOCKED,
    PAYMENT_SCREENING_AWAITING_INFORMATION,
    PAYMENT_SCREENING_PROCESS_PAYMENT_END_STATE,
    PAYMENT_SCREENING_REJECT_PAYMENT_END_STATE,
    USER_DEFINED
}
