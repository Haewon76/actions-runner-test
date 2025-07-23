package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.shared.CashmallowException;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponJobPlanService {

    void insertJobPlan(String fromCountryCode, String jobKey, String cronExpression) throws CashmallowException;

    Long updateSuccessJobPlan(List<String> jobKeyList);

    String getCronExpression(LocalDateTime issueDate);

    String getJobKey(String jobKeyFormat, String iso3166, Long couponIssueId);

    Long updateJobKeyCouponIssue(Long couponIssueId, String jobKey);
}
