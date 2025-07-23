package com.cashmallow.api.interfaces.coupon.dto.res;

import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@ToString
public class CouponSystemManageResponse {
    private Long id;
    private Long couponId;
    private String fromCountryCode;
    private String couponType;
    private String couponCodeBody;
    private String description;
    private LocalDate startDateLocal;
    private LocalDate endDateLocal;
    private Timestamp startDateUtc;
    private Timestamp endDateUtc;
    private String couponCode;
    private String couponName;
    private String couponDiscountType;
    private BigDecimal couponDiscountValue;
    private BigDecimal maxDiscountAmount;
    private BigDecimal minRequiredAmount;
    private String expireType;
    private LocalDate couponStartDateLocal;
    private LocalDate couponEndDateLocal;
    private Timestamp couponStartDateUtc;
    private Timestamp couponEndDateUtc;
    private Long expirePeriodDays;
    private String serviceType;
    private Long createdId;
    private Long updatedId;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public CouponSystemManageResponse() {}

    @Builder
    public CouponSystemManageResponse(Long id, Long couponId, String fromCountryCode, String couponType, String couponCodeBody, String description, LocalDate startDateLocal, LocalDate endDateLocal, Timestamp startDateUtc, Timestamp endDateUtc, String couponCode, String couponName, String couponDiscountType, BigDecimal couponDiscountValue, BigDecimal maxDiscountAmount, BigDecimal minRequiredAmount, String expireType
            , LocalDate couponStartDateLocal, LocalDate couponEndDateLocal, Timestamp couponStartDateUtc, Timestamp couponEndDateUtc, Long expirePeriodDays, String serviceType, Long createdId, Long updatedId, Timestamp createdDate, Timestamp updatedDate) {
        this.id = id;
        this.couponId = couponId;
        this.fromCountryCode = fromCountryCode;
        this.couponType = couponType;
        this.couponCodeBody = couponCodeBody;
        this.description = description;
        this.startDateLocal = startDateLocal;
        this.endDateLocal = endDateLocal;
        this.startDateUtc = startDateUtc;
        this.endDateUtc = endDateUtc;
        this.couponCode = couponCode;
        this.couponName = couponName;
        this.couponDiscountType = couponDiscountType;
        this.couponDiscountValue = couponDiscountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minRequiredAmount = minRequiredAmount;
        this.expireType = expireType;
        this.couponStartDateLocal = couponStartDateLocal;
        this.couponEndDateLocal = couponEndDateLocal;
        this.couponStartDateUtc = couponStartDateUtc;
        this.couponEndDateUtc = couponEndDateUtc;
        this.expirePeriodDays = expirePeriodDays;
        this.serviceType = serviceType;
        this.createdId = createdId;
        this.updatedId = updatedId;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public static CouponSystemManageResponse of(CouponSystemManagement dateRangeCoupon, Coupon coupon) {
        return CouponSystemManageResponse.builder()
                                        .id(dateRangeCoupon.getId())
                                        .couponId(dateRangeCoupon.getCouponId())
                                        .fromCountryCode(dateRangeCoupon.getFromCountryCode())
                                        .couponType(dateRangeCoupon.getCouponType())
                                        .couponCodeBody(dateRangeCoupon.getCouponCodeBody())
                                        .description(dateRangeCoupon.getDescription())
                                        .startDateLocal(dateRangeCoupon.getStartDateLocal())
                                        .endDateLocal(dateRangeCoupon.getEndDateLocal())
                                        .startDateUtc(dateRangeCoupon.getStartDate())
                                        .endDateUtc(dateRangeCoupon.getEndDate())
                                        .couponCode(coupon.getCouponCode())
                                        .couponName(coupon.getCouponName())
                                        .couponDiscountType(coupon.getCouponDiscountType())
                                        .couponDiscountValue(coupon.getCouponDiscountValue())
                                        .maxDiscountAmount(coupon.getMaxDiscountAmount())
                                        .minRequiredAmount(coupon.getMinRequiredAmount())
                                        .expireType(coupon.getExpireType())
                                        .couponStartDateLocal(coupon.getCouponStartDate())
                                        .couponEndDateLocal(coupon.getCouponEndDate())
                                        .couponStartDateUtc(coupon.getCouponStartDateUtc())
                                        .couponEndDateUtc(coupon.getCouponEndDateUtc())
                                        .expirePeriodDays(coupon.getExpirePeriodDays())
                                        .serviceType(coupon.getServiceType())
                                        .createdId(dateRangeCoupon.getCreatedId())
                                        .updatedId(dateRangeCoupon.getUpdatedId())
                                        .createdDate(dateRangeCoupon.getCreatedDate())
                                        .updatedDate(dateRangeCoupon.getUpdatedDate())
                                .build();
    }

    public static CouponSystemManageResponse ofNewCoupon(Coupon coupon) {
        return CouponSystemManageResponse.builder()
                .couponId(coupon.getId())
                .couponCode(coupon.getCouponCode())
                .couponName(coupon.getCouponName())
                .couponDiscountType(coupon.getCouponDiscountType())
                .couponDiscountValue(coupon.getCouponDiscountValue())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .minRequiredAmount(coupon.getMinRequiredAmount())
                .expireType(coupon.getExpireType())
                .couponStartDateLocal(coupon.getCouponStartDate())
                .couponEndDateLocal(coupon.getCouponEndDate())
                .couponStartDateUtc(coupon.getCouponStartDateUtc())
                .couponEndDateUtc(coupon.getCouponEndDateUtc())
                .expirePeriodDays(coupon.getExpirePeriodDays())
                .serviceType(coupon.getServiceType())
                .build();
    }
}