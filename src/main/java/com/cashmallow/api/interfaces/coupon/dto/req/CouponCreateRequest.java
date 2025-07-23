package com.cashmallow.api.interfaces.coupon.dto.req;

import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.common.TimezoneUtil;
import feign.template.UriUtils;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.cashmallow.api.domain.shared.Const.N;

@Builder
public record CouponCreateRequest(
        @NotBlank
        String isSystem,                // 시스템 쿠폰 여부 (varchar 1)
        @NotBlank
        String fromCountryCode,         // From 국가 코드 (varchar 3)
        @NotBlank
        String couponName,              // 쿠폰 명칭
        @NotBlank
        String couponCode,              // 쿠폰 코드
        String couponDescription,       // 쿠폰 설명
        @NotBlank
        String couponDiscountType,      // 쿠폰 할인 유형  ex) 고정할인-fixedAmount, 비율할인-rateAmount, 수수료면제-feeWaiver
        BigDecimal couponDiscountValue, // 고정 할인 금액, % 할인율
        BigDecimal maxDiscountAmount,   // 최대 할인 금액
        BigDecimal minRequiredAmount,   // 쿠폰 사용 가능한 최소 결제 금액
        @NotBlank
        String expireType,              // 쿠폰 사용기간 타입  ex) 기간지정-dateRange, 발급일로부터 n일-daysFromIssue
        LocalDate couponStartDate,      // 쿠폰 사용 기간 시작일
        LocalDate couponEndDate,        // 쿠폰 사용 기간 종료일
        Long expirePeriodDays,          // 발급후 만료일
        @NotBlank
        String serviceType,             // 거래 유형  ex) 모두-all, 송금-remittance, 환전-exchange, 송금/환전-both, 모두-all
        @NotBlank
        Long createdId,                 // 쿠폰 생성한 운영자 id
        List<String> applyCurrencyList  // 쿠폰 적용 화폐 (복수 선택 가능)
) {

    public CouponCreateRequest withCreatedId(Long createdId) {
        return CouponCreateRequest.builder()
                                    .isSystem(this.isSystem)
                                    .fromCountryCode(this.fromCountryCode)
                                    .couponName(this.couponName)
                                    .couponCode(this.couponCode)
                                    .couponDescription(this.couponDescription)
                                    .couponDiscountType(this.couponDiscountType)
                                    .couponDiscountValue(this.couponDiscountValue)
                                    .maxDiscountAmount(this.maxDiscountAmount)
                                    .minRequiredAmount(this.minRequiredAmount)
                                    .expireType(this.expireType)
                                    .couponStartDate(this.couponStartDate)
                                    .couponEndDate(this.couponEndDate)
                                    .expirePeriodDays(this.expirePeriodDays)
                                    .serviceType(this.serviceType)
                                    .createdId(createdId)
                                    .applyCurrencyList(this.applyCurrencyList)
                                .build();
    }

    public CouponCreateRequest withApplyCurrencyList(List<String> applyCurrencyList) {
        return CouponCreateRequest.builder()
                .isSystem(this.isSystem)
                .fromCountryCode(this.fromCountryCode)
                .couponName(this.couponName)
                .couponCode(this.couponCode)
                .couponDescription(this.couponDescription)
                .couponDiscountType(this.couponDiscountType)
                .couponDiscountValue(this.couponDiscountValue)
                .maxDiscountAmount(this.maxDiscountAmount)
                .minRequiredAmount(this.minRequiredAmount)
                .expireType(this.expireType)
                .couponStartDate(this.couponStartDate)
                .couponEndDate(this.couponEndDate)
                .expirePeriodDays(this.expirePeriodDays)
                .serviceType(this.serviceType)
                .createdId(this.createdId)
                .applyCurrencyList(applyCurrencyList)
                .build();
    }

    public Coupon toEntity(Long managerId) {
        return Coupon.builder()
                    .isSystem(isSystem)
                    .isActive(N)
                    .fromCountryCode(fromCountryCode)
                    .couponName(replaceToBlank(couponName))
                    .couponCode(couponCode)
                    .couponDescription(replaceToBlank(couponDescription))
                    .couponDiscountType(couponDiscountType)
                    .couponDiscountValue(couponDiscountValue)
                    .maxDiscountAmount(maxDiscountAmount)
                    .minRequiredAmount(minRequiredAmount)
                    .expireType(expireType)
                    .couponStartDate(couponStartDate)
                    .couponEndDate(couponEndDate)
                    .couponStartDateUtc(TimezoneUtil.fromLocalDate(fromCountryCode, couponStartDate))
                    .couponEndDateUtc(TimezoneUtil.fromLocalDate(fromCountryCode, couponEndDate))
                    .expirePeriodDays(expirePeriodDays)
                    .serviceType(serviceType)
                    .createdId(managerId)
                    .updatedId(managerId)
                .build();
    }

    // 공백이 '+' 로 치환되는 부분 공백으로 다시 들어가도록 처리
    private String replaceToBlank(String message) {
        if (message == null) {
            message = "";
        }
        return message.replace("+", " ");
    }
}