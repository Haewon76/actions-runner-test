package com.cashmallow.api.domain.model.coupon.entity;

import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateManagementRequest;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.time.LocalDate;

@Getter
@ToString
public class CouponSystemManagement {
    private Long id;                    //
    private Long couponId;              // coupon_v2 테이블의 쿠폰 ID
    private String fromCountryCode;     // 캐시멜로 관리 국가코드 (ex: 001)
    private String couponType;          // 쿠폰 타입(Welcome-가입, Birthday-생일, ThankYouMyFriend-초대,ThankYouToo-초대완료)
    private String couponCodeBody;      // 쿠폰코드 구분값
    private String description;         // 쿠폰 정책 설명
    private LocalDate startDateLocal;   // 로컬시각 시작일자
    private LocalDate endDateLocal;     // 로컬시각 종료일자
    private Timestamp startDate;        // UTC+0 시작일자
    private Timestamp endDate;          // UTC+0 종료일자
    @Setter
    private Long createdId;             // 적용한 운영자 id
    private Long updatedId;             // 변경 적용한 운영자 id
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public CouponSystemManagement() { }

    @Builder
    public CouponSystemManagement(Long id, Long couponId, String fromCountryCode, String couponType, String couponCodeBody, String description, LocalDate startDateLocal, LocalDate endDateLocal, Timestamp startDate, Timestamp endDate, Long createdId, Long updatedId, Timestamp createdDate, Timestamp updatedDate) {
        this.id = id;
        this.couponId = couponId;
        this.fromCountryCode = fromCountryCode;
        this.couponType = couponType;
        this.couponCodeBody = couponCodeBody;
        this.description = description;
        this.startDateLocal = startDateLocal;
        this.endDateLocal = endDateLocal;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdId = createdId;
        this.updatedId = updatedId;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }

    public static CouponSystemManagement of(CouponCreateManagementRequest request, String couponCodeBody, LocalDate startDateLocal, LocalDate endDateLocal, Timestamp startDate, Timestamp endDate, Long managerId) {
        return CouponSystemManagement.builder()
                                        .couponId(request.getCouponId())
                                        .fromCountryCode(request.getFromCountryCode())
                                        .couponType(request.getCouponType())
                                        .couponCodeBody(couponCodeBody)
                                        .description(request.getDescription())
                                        .startDateLocal(startDateLocal)
                                        .endDateLocal(endDateLocal)
                                        .startDate(startDate)
                                        .endDate(endDate)
                                        .updatedId(managerId)
                                    .build();
    }
}
