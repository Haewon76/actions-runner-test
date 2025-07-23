package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@Builder
public record CouponSearchRequest(
    String fromCountryCode
    , String couponCodeName
    , LocalDateTime searchStartDate
    , LocalDateTime searchEndDate
    , String sortColumnCode
    , String sortColumnOrder
    , String isSystem       // 쿠폰 발급 조회 조건에서 사용
    , String isActive       // 쿠폰 발급 조회 조건에서 사용
    , long page
    , long size
    , long offset
) {

    public static CouponSearchRequest of(@Valid String fromCountryCode
                                        , String couponCodeName
                                        , String searchStartDate
                                        , String searchEndDate
                                        , String sortColumnCode
                                        , String sortColumnOrder
                                        , String isSystem       // 쿠폰 발급 조회 조건에서 사용
                                        , String isActive       // 쿠폰 발급 조회 조건에서 사용
                                        , @Valid long page
                                        , @Valid long size
    ){

        if (sortColumnOrder != null) {
            sortColumnOrder = sortColumnOrder.toUpperCase();
        }

        return CouponSearchRequest.builder()
                .fromCountryCode(fromCountryCode)
                .couponCodeName(couponCodeName)
                .searchStartDate(LocalDateTime.parse(searchStartDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .searchEndDate(LocalDateTime.parse(searchEndDate, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .sortColumnCode(sortColumnCode)
                .sortColumnOrder(sortColumnOrder)
                .isSystem(isSystem)
                .isActive(isActive)
                .page(page)
                .size(size)
                .offset((page - 1) * size)
                .build();
    }
}