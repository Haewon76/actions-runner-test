package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.shared.CashmallowException;

public interface CouponIssueQueueService {

    void couponIssueListByReservation(String issueId) throws CashmallowException;

    void issueBirthDaySystemCoupon(String fromCountryCode) throws CashmallowException;

    void pushExpireCoupon(String fromCountryCode) throws CashmallowException;

    void updateExpireCoupon(String fromCountryCode) throws CashmallowException;

}
