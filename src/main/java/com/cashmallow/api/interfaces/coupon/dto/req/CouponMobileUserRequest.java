package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;


@Builder
public record CouponMobileUserRequest(
        Long userId,            // 유저 ID
        Long couponUserId,      // 유니크한 유저 쿠폰 ID
        String fromCountryCode, // 캐시멜로에서 관리하는 국가 코드  ex) 001: 홍콩
        String serviceType,     // 거래 유형(remittance-송금, exchange-환전, all-모두, both-송금과 환전)
        String sendType,        // 발급 유형(direct-즉시, reservation-예약)
        String availableStatus, // 사용가능 여부(AVAILABLE-사용가능, RESERVATION-예약, EXPIRED-만료됨, USED-사용됨)
        BigDecimal fee,         // 수수료(할인 정책 중 수수료면제~feeWaiver 일때만 사용)
        Long issueId,           // 쿠폰 발급 ID
        LocalDate currentDate,  // 현재 로컬 날짜
        String isActive         // 쿠폰 활성화여부
) {

}
