package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class CouponCreateManagementRequest {
    private Long couponId;              // coupon_v2 테이블의 쿠폰 ID
    private String fromCountryCode;     // 캐시멜로 관리 국가코드 (ex: 001)
    private String couponType;          // 쿠폰 타입(Welcome-가입, Birthday-생일, ThankYouMyFriend-초대,ThankYouToo-초대완료)
    private String description;         // 쿠폰 정책 설명
    private String startDateLocal;   // 로컬시각 시작일자
    private String endDateLocal;     // 로컬시각 종료일자
    // private Timestamp startDate;        // UTC+0 시작일자
    // private Timestamp endDate;          // UTC+0 종료일자
    private Long createdId;             // 쿠폰 정책 등록한 개발자 id

    public CouponCreateManagementRequest() { }

    @Builder
    public CouponCreateManagementRequest(Long couponId, String fromCountryCode, String couponType, String description, String startDateLocal, String endDateLocal, Timestamp startDate, Timestamp endDate, Long createdId) {
        this.couponId = couponId;
        this.fromCountryCode = fromCountryCode;
        this.couponType = couponType;
        this.description = description;
        this.startDateLocal = startDateLocal;
        this.endDateLocal = endDateLocal;
        // this.startDate = startDate;
        // this.endDate = endDate;
        this.createdId = createdId;
    }

}