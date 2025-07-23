package com.cashmallow.api.interfaces.coupon.dto.res;

import com.cashmallow.api.domain.model.coupon.vo.CouponIssueManagement;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.common.DateUtil;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.*;


@Builder
public record CouponIssueReadResponse(
    Long couponId,
    String fromCountryCode,
    String isSystem,
    String isActive,
    String targetType,
    String sendType,
    String issueDate,
    String couponCode,
    String couponName,
    Long usedCount,
    BigDecimal couponUsedAmount,
    String couponDiscountType,
    BigDecimal couponDiscountValue,
    BigDecimal maxDiscountAmount,
    BigDecimal minRequiredAmount,
    String serviceType,
    String createdName,
    Long couponIssueId,
    List<String> applyCurrencies,
    Long applyCurrencyCount
) {

    public static CouponIssueReadResponse of(CouponIssueManagement couponIssue, Optional<User> createdUser, List<String> applyCurrencies) {

        return CouponIssueReadResponse.builder()
                    .couponId(couponIssue.getCouponId())
                    .fromCountryCode(couponIssue.getFromCountryCode())
                    .isSystem(couponIssue.getIsSystem())
                    .isActive(couponIssue.getIsActive())
                    .targetType(couponIssue.getTargetType())
                    .sendType(couponIssue.getSendType())
                    .issueDate(DateUtil.fromLocalDateTime(couponIssue.getIssueDate()))
                    .couponCode(couponIssue.getCouponCode())
                    .couponName(couponIssue.getCouponName())
                    .usedCount(couponIssue.getUsedCount())
                    .couponUsedAmount(couponIssue.getCouponUsedAmount())
                    .couponDiscountType(couponIssue.getCouponDiscountType())
                    .couponDiscountValue(couponIssue.getCouponDiscountValue())
                    .maxDiscountAmount(couponIssue.getMaxDiscountAmount())
                    .minRequiredAmount(couponIssue.getMinRequiredAmount())
                    .serviceType(couponIssue.getServiceType())
                    .createdName(couponIssue.getCreatedId() == -1L ? "System" : createdUser.get().getLastName() + " " + createdUser.get().getFirstName())
                    .couponIssueId(couponIssue.getCouponIssueId())
                    .applyCurrencies(applyCurrencies)
                    .applyCurrencyCount((long) applyCurrencies.size())
                .build();
    }
}
