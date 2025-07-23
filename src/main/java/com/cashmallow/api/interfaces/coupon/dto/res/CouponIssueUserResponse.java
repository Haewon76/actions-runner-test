package com.cashmallow.api.interfaces.coupon.dto.res;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.cashmallow.api.domain.model.coupon.vo.CouponMobileUser;
import com.cashmallow.common.DateUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

@Getter
@ToString
public class CouponIssueUserResponse {
    private final Long couponUserId;
    private final Long couponIssueId;
    private final Long couponId;
    private final String sourceCurrency;
    private final String name;
    private final String code;
    private final DiscountType discountType;
    private final BigDecimal discountFactor;
    private final BigDecimal discountValue;
    private final BigDecimal maxDiscountAmount;
    private final Timestamp issueDate;
    private final String startDate;
    private final String endDate;
    private final BigDecimal minRequiredAmount;
    private final String systemCouponYn;
    private final String couponUsePossibleYn;
    private final ServiceType serviceType;
    private final String description;
    private final String thumbnail;
    private final String createdAt;
    private final Long userId;
    private final boolean availability;
    private final List<String> toCurrencies;

    @Builder
    public CouponIssueUserResponse(Long couponUserId, Long couponIssueId, Long couponId, String sourceCurrency, List<String> toCurrencies, String name, String code, DiscountType discountType, BigDecimal discountFactor, BigDecimal discountValue, BigDecimal maxDiscountAmount, Timestamp issueDate, String startDate, String endDate, BigDecimal minRequiredAmount, String systemCouponYn, String couponUsePossibleYn, ServiceType serviceType, String description, String thumbnail, String createdAt, Long userId, boolean availability) {
        this.couponUserId = couponUserId;
        this.couponIssueId = couponIssueId;
        this.couponId = couponId;
        this.sourceCurrency = sourceCurrency;
        this.toCurrencies = toCurrencies;
        this.name = name;
        this.code = code;
        this.discountType = discountType;
        this.discountFactor = discountFactor;
        this.discountValue = discountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.issueDate = issueDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.minRequiredAmount = minRequiredAmount;
        this.systemCouponYn = systemCouponYn;
        this.couponUsePossibleYn = couponUsePossibleYn;
        this.serviceType = serviceType;
        this.description = description;
        this.thumbnail = thumbnail;
        this.createdAt = createdAt;
        this.userId = userId;
        this.availability = availability;
    }

    public static CouponIssueUserResponse ofMoney(CouponMobileUser coupon, BigDecimal fee, boolean availability, List<String> applyCurrencyList) {

        BigDecimal couponDiscountFactor = null;
        BigDecimal couponDiscountValue = null;
        if (coupon.getCouponDiscountType().equals(DiscountType.FEE_WAIVER.getCode())) {
            couponDiscountFactor = fee;
            couponDiscountValue = fee;
        } else {
            couponDiscountFactor = coupon.getCouponDiscountValue();
            couponDiscountValue = coupon.getCouponDiscountValue();
        }

        return CouponIssueUserResponse.builder()
                                        .couponUserId(coupon.getCouponUserId())
                                        .couponIssueId(coupon.getCouponIssueId())
                                        .couponId(coupon.getCouponId())
                                        .sourceCurrency(CountryCode.of(coupon.getFromCountryCode()).getCurrency())
                                        .name(coupon.getCouponName())
                                        .code(coupon.getCouponCode())
                                        .discountType(DiscountType.fromCode(coupon.getCouponDiscountType()))
                                        .discountFactor(couponDiscountFactor)
                                        .discountValue(couponDiscountValue)
                                        .maxDiscountAmount(coupon.getMaxDiscountAmount())
                                        .issueDate(coupon.getIssueDateUtc())
                                        .startDate(DateUtil.fromLocalDateToY_M_D(coupon.getCouponCalStartDate()))
                                        .endDate(DateUtil.fromLocalDateToY_M_D(coupon.getCouponCalEndDate()))
                                        .minRequiredAmount(coupon.getMinRequiredAmount())
                                        .systemCouponYn(coupon.getIsSystem())
                                        .couponUsePossibleYn(coupon.getIsActive())
                                        .serviceType(ServiceType.fromString(coupon.getServiceType()))
                                        .description(coupon.getCouponDescription())
                                        .thumbnail(null)
                                        .createdAt(DateUtil.fromTimestampToY_M_D_H_M_S(coupon.getFromCountryCode(), coupon.getCreatedDate()))
                                        .userId(coupon.getUserId())
                                        .availability(availability)
                                        .toCurrencies(applyCurrencyList)
                        .build();
    }

    public static CouponIssueUserResponse ofMobile(CouponMobileUser coupon, BigDecimal fee, boolean availability, List<String> applyCurrencyList) {

        BigDecimal couponDiscountFactor = null;
        BigDecimal couponDiscountValue = null;
        if (coupon.getCouponDiscountType().equals(DiscountType.FEE_WAIVER.getCode())) {
            couponDiscountFactor = fee;
            couponDiscountValue = fee;
        } else {
            couponDiscountFactor = coupon.getCouponDiscountValue();
            couponDiscountValue = coupon.getCouponCalDiscountValue();
        }

        return CouponIssueUserResponse.builder()
                .couponUserId(coupon.getCouponUserId())
                .couponIssueId(coupon.getCouponIssueId())
                .couponId(coupon.getCouponId())
                .sourceCurrency(CountryCode.of(coupon.getFromCountryCode()).getCurrency())
                .name(coupon.getCouponName())
                .code(coupon.getCouponCode())
                .discountType(DiscountType.fromCode(coupon.getCouponDiscountType()))
                .discountFactor(couponDiscountFactor)
                .discountValue(couponDiscountValue)
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .issueDate(coupon.getIssueDateUtc())
                .startDate(DateUtil.fromLocalDateToY_M_D(coupon.getCouponCalStartDate()))
                .endDate(DateUtil.fromLocalDateToY_M_D(coupon.getCouponCalEndDate()))
                .minRequiredAmount(coupon.getMinRequiredAmount())
                .systemCouponYn(coupon.getIsSystem())
                .couponUsePossibleYn(coupon.getIsActive())
                .serviceType(ServiceType.fromString(coupon.getServiceType()))
                .description(coupon.getCouponDescription())
                .thumbnail(null)
                .createdAt(DateUtil.fromTimestampToY_M_D_H_M_S(coupon.getFromCountryCode(), coupon.getCreatedDate()))
                .userId(coupon.getUserId())
                .availability(availability)
                .toCurrencies(applyCurrencyList)
                .build();
    }
}
