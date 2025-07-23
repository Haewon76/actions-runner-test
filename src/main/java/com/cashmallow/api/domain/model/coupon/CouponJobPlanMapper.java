package com.cashmallow.api.domain.model.coupon;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponJobPlanMapper {

    Long updateSuccessJobPlan(@Param("jobKeyList") List<String> jobKeyList);

    Long updateJobKeyCouponIssue(@Param("couponIssueId") Long couponIssueId, @Param("jobKey") String jobKey);
}
