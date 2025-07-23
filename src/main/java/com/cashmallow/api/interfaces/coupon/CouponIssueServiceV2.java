package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUserExpire;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponIssueCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueReadResponse;

import java.time.LocalDate;
import java.util.List;

public interface CouponIssueServiceV2 {

    List<CouponIssueReadResponse> getCouponIssueList(CouponSearchRequest CouponSearchRequest);

    CouponIssue createCouponIssue(CouponIssueCreateRequest couponIssueCreateRequest) throws CashmallowException;

    Long getCouponTotalCount(CouponSearchRequest couponSearchRequest);

    Long updateReservedCouponIssueUsers(String availableStatus, List<Long> couponIssueIds);

    List<CouponIssueUser> getUsersByCouponIssueId(Long couponIssueId, String sortColumnCode, String sortColumnOrder) throws CashmallowException;

    List<Long> getCouponIssuedBirthdayUserByCouponId(Long couponId, int currentYear);

    List<User> getUserListByBirthday(String fromCountryCode, LocalDate beforeBirthday);

    List<User> getUserListByExpire(String fromCountryCode, List<Long> userIds);

    Long updateExpireCoupon(List<CouponIssueUserExpire> userList);

    CouponIssue getCouponIssuedById(Long issueId);

    int deleteCouponIssuedById(Long issueId);

    int deleteCouponIssuedUserByCouponIssueId(Long issueId);

    int deleteApplyCurrencyByCouponId(Long couponId, List<String> applyCurrencyList);

    int updateStatusByCouponIssueUserSyncIds(List<UpdateStatusUserCoupon> userCouponList, String availableStatus);
}