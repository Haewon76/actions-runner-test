package com.cashmallow.api.interfaces.coupon.dto.res;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.common.TimezoneUtil;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record CouponReadResponse(
        Long id,
        String isSystem,                // 시스템 쿠폰 여부 (varchar 2)
        String fromCountryCode,         // From 국가 코드 (varchar 4)
        String fromCurrency,

        String couponName,              // 쿠폰 명칭
        String couponCode,              // 쿠폰 코드
        String couponDescription,       // 쿠폰 설명

        String couponDiscountType,      // 쿠폰 할인 유형  ex) 고정할인-fixedAmount, 비율할인-rateAmount, 수수료면제-feeWaiver
        String discountTypeKr,

        BigDecimal couponDiscountValue,       // 고정 할인 금액, % 할인율

        BigDecimal maxDiscountAmount,   // 최대 할인 금액
        BigDecimal minRequiredAmount,   // 쿠폰 사용 가능한 최소 결제 금액
        String expireType,              // 쿠폰 사용기간 타입  ex) 기간지정-dateRange, 발급일로부터 n일-daysFromIssue
        LocalDate couponStartDate,         // 쿠폰 사용 기간 시작일
        LocalDate couponEndDate,           // 쿠폰 사용 기간 종료일
        Long expirePeriodDays,          // 발급후 만료일
        String serviceType,             // 거래 유형  ex) 모두-all, 송금/환전-both, 송금-remittance, 환전-exchange
        String isActive,                // 활성화 여부 (varchar 2)
        Long createdId,                 // 쿠폰 생성한 운영자 id
        Long updatedId,                 // 쿠폰 변경한 운영자 id
        String createdDate              // 쿠폰 생성일
) {

    public static CouponReadResponse of(Coupon coupon) {

        String discountTypeKr = DiscountType.fromString(coupon.getCouponDiscountType()).getKr();

        LocalDate couponStartDate = null;
        LocalDate couponEndDate = null;
        if (ExpireType.DATE_RANGE.getCode().equals(coupon.getExpireType())) {
            couponStartDate = coupon.getCouponStartDate();
            couponEndDate = coupon.getCouponEndDate();
        }

        return CouponReadResponse.builder()
                            .id(coupon.getId())
                            .isSystem(coupon.getIsSystem())
                            .fromCountryCode(coupon.getFromCountryCode())
                            .fromCurrency(CountryCode.of(coupon.getFromCountryCode()).getCurrency())
                            .couponName(coupon.getCouponName())
                            .couponCode(coupon.getCouponCode())
                            .couponDescription(coupon.getCouponDescription())
                            .couponDiscountType(coupon.getCouponDiscountType())
                            .discountTypeKr(discountTypeKr)
                            .couponDiscountValue(coupon.getCouponDiscountValue())
                            .maxDiscountAmount(coupon.getMaxDiscountAmount())
                            .minRequiredAmount(coupon.getMinRequiredAmount())
                            .expireType(coupon.getExpireType())
                            .couponStartDate(couponStartDate)
                            .couponEndDate(couponEndDate)
                            .expirePeriodDays(coupon.getExpirePeriodDays())
                            .serviceType(coupon.getServiceType())
                            .isActive(coupon.getIsActive())
                            .createdId(coupon.getCreatedId())
                            .updatedId(coupon.getUpdatedId())
                            .createdDate(TimezoneUtil.countryTimeZone(coupon.getFromCountryCode(), coupon.getCreatedDate()))
                .build();
    }

}
