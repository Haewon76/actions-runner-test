package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.SQLException;
import java.util.List;


@Mapper
public interface CouponV2Mapper {

    List<Coupon> getCouponList(CouponSearchRequest couponSearchRequest) throws SQLException;

    List<Long> getUsersByFromCountryCode(String fromCountryCode);

    Long getCouponTotalCount(CouponSearchRequest couponSearchRequest);

    Long createCoupon(Coupon coupon);

    Long insertCouponApplyCurrency(Long couponId, List<String> applyCurrencyList);

    Coupon getCouponById(Long couponId);

    Long updateCouponActive(@Param("fromCountryCode") String fromCountryCode, @Param("couponId") Long couponId, @Param("isActive") String isActive);

    Long getIsCouponIssued(@Param("couponIds") List<Long> couponIds);

    Long getIsActive(@Param("couponIds") List<Long> couponIds);

    int deleteCoupon(@Param("couponIds") List<Long> couponIds);

    List<Coupon> getIssuableCoupons(@Param("fromCountryCode") String fromCountryCode, @Param("currentDate") String currentDate,
                                                @Param("searchStartDate") String searchStartDate, @Param("searchEndDate") String searchEndDate);

    Long deleteApplyCurrencyByCouponId(@Param("couponIds") List<Long> couponIds);

    Coupon getCouponByCouponCode(String fromCountryCode, String isSystem, String isActive, String couponCode);

    Long checkDuplicateCode(String fromCountryCode, String couponCode);

    List<String> getApplyCurrencyListByCouponId(Long couponId);

    int deleteApplyCurrencyByCouponId(@Param("couponId") Long couponId, @Param("applyCurrencyList") List<String> applyCurrencyList);

}
