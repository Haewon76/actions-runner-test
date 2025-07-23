package com.cashmallow.api.domain.model.coupon.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;


@Getter
@ToString
public class Coupon {

    private Long id;
    private String isSystem;                // 시스템 쿠폰 여부 (varchar 2)
    private String fromCountryCode;         // From 국가 코드 (varchar 4)
    private String couponName;              // 쿠폰 명칭
    private String couponCode;              // 쿠폰 코드
    private String couponDescription;       // 쿠폰 설명
    private String couponDiscountType;      // 쿠폰 할인 유형  ex) 고정할인-fixedAmount, 비율할인-rateAmount, 수수료면제-feeWaiver
    private BigDecimal couponDiscountValue; // 쿠폰 적용값 (고정 할인 금액, % 할인율)
    private BigDecimal maxDiscountAmount;   // 최대 할인 금액
    private BigDecimal minRequiredAmount;   // 쿠폰 사용 가능한 최소 결제 금액
    private String expireType;              // 쿠폰 사용기간 타입  ex) 기간지정-dateRange, 발급일로부터 n일-daysFromIssue
    private LocalDate couponStartDate;      // 쿠폰 사용 기간 시작일 (LocalDate)
    private LocalDate couponEndDate;        // 쿠폰 사용 기간 종료일 (LocalDate)
    private Timestamp couponStartDateUtc;   // 쿠폰 사용 기간 시작일 (Timestamp)
    private Timestamp couponEndDateUtc;     // 쿠폰 사용 기간 종료일 (Timestamp)
    private Long expirePeriodDays;          // 발급후 만료일
    private String serviceType;             // 거래 유형  ex) 모두-all, 송금/환전-both, 송금-remittance, 환전-exchange
    private String isActive;                // 활성화 여부 (varchar 2)
    private Long createdId;                 // 쿠폰 생성한 운영자 id
    private Long updatedId;                 // 쿠폰 변경한 운영자 id
    private Timestamp createdDate;          // 쿠폰 생성일

    public Coupon() { }

    @Builder
    public Coupon(Long id, String isSystem, String fromCountryCode, String couponName, String couponCode, String couponDescription
            , String couponDiscountType, BigDecimal couponDiscountValue, BigDecimal maxDiscountAmount, BigDecimal minRequiredAmount, String expireType
            , LocalDate couponStartDate, LocalDate couponEndDate, Timestamp couponStartDateUtc, Timestamp couponEndDateUtc
            , Long expirePeriodDays, String serviceType, String isActive, Long createdId, Long updatedId
            , Timestamp createdDate) {
        this.id = id;
        this.isSystem = isSystem;
        this.fromCountryCode = fromCountryCode;
        this.couponName = couponName;
        this.couponCode = couponCode;
        this.couponDescription = couponDescription;

        this.couponDiscountType = couponDiscountType;
        this.couponDiscountValue = couponDiscountValue;
        this.maxDiscountAmount = maxDiscountAmount;
        this.minRequiredAmount = minRequiredAmount;
        this.expireType = expireType;

        this.couponStartDate = couponStartDate;
        this.couponEndDate = couponEndDate;
        this.couponStartDateUtc = couponStartDateUtc;
        this.couponEndDateUtc = couponEndDateUtc;

        this.expirePeriodDays = expirePeriodDays;
        this.serviceType = serviceType;
        this.isActive = isActive;
        this.createdId = createdId;
        this.updatedId = updatedId;

        this.createdDate = createdDate;
    }
}
