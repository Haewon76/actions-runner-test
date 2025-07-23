package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.vo.CouponMobileUser;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.shared.CashmallowException;

import java.util.List;


/**
 * 순환 참조 방지하기 위해 분리함.
 * validation 로직 분리.
 **/
public interface CouponValidationService {

    void validCouponUser(String fromCountryCode, Long userId, Long couponUserId, String serviceType, String availableStatus) throws CashmallowException;

    void validateCouponUseDate(CouponMobileUser couponUser) throws CashmallowException;

    boolean hasUsedSystemCoupon(Long userId, SystemCouponType couponCodePrefix, String identificationNumber, int month);

    List<Long> hasRegisteredCouponByUserList(List<Long> userIds, String couponCodePrefix);

    boolean hasRegisteredCoupon(Long userId, String couponCodePrefix, String identificationNumber);

    List<Long> hasInactiveTransactionHistoryByUserList(List<Long> userIds);

    boolean hasInactiveTransactionHistory(String identificationNumber);
}
