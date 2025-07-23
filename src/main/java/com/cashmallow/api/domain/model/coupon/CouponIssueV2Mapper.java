package com.cashmallow.api.domain.model.coupon;

import com.cashmallow.api.domain.model.coupon.entity.ApplyCurrency;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.*;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CouponIssueV2Mapper {

    List<CouponIssueManagement> getCouponIssueList(CouponSearchRequest couponSearchRequest);

    void createCouponIssue(CouponIssue couponIssue);

    Long createUsersCoupon(CouponUser couponUser, List<Long> users);

    List<ApplyCurrency> applyCurrencyByCouponList(@Param("couponIds") List<Long> couponIds);

    Long getCouponIssueCountTotal(CouponSearchRequest couponSearchRequest);

    Long updateReservedCouponIssueUsers(@Param("availableStatus") String availableStatus, @Param("couponIssueIds") List<Long> couponIssueIds);

    List<CouponIssueUser> getUsersByCouponIssueId(@Param("couponIssueId") Long couponIssueId, @Param("sortColumnCode") String sortColumnCode, @Param("sortColumnOrder") String sortColumnOrder);

    List<User> getUserListByEvent(@Param("fromCountryCode") String fromCountryCode
                                  , @Param("beforeBirthday") String beforeBirthday
                                  , @Param("userIds") List<Long> userIds);

    List<Long> getCouponIssuedBirthdayUserByCouponId(@Param("couponId") Long couponId, @Param("currentYear") int currentYear);

    Long updateExpireCoupon(@Param("userList") List<CouponIssueUserExpire> userList);

    CouponIssue getCouponIssuedById(Long issueId);

    int deleteCouponIssuedById(Long issueId);

    int deleteCouponIssuedUserByCouponIssueId(Long issueId);

    int updateStatusByCouponIssueUserSyncIds(
            @Param("userCouponList") List<UpdateStatusUserCoupon> userCouponList
            , @Param("availableStatus") String availableStatus);
}
