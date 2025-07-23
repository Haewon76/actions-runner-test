package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.CouponSystemManagementMapper;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponSystemManagementServiceImpl implements CouponSystemManagementService {
    
    private final CouponSystemManagementMapper couponSystemManagementMapper;

    @Override
    public CouponSystemManagement getUsingCouponDateRange(CouponSystemManagementRequest couponSystemManagementRequest) {
        return couponSystemManagementMapper.getUsingCouponDateRange(couponSystemManagementRequest);
    }

    @Override
    public CouponSystemManagement getUsingCoupon(CouponSystemManagementRequest couponSystemManagementRequest) {
        return couponSystemManagementMapper.getUsingCoupon(couponSystemManagementRequest);
    }

    @Override
    public List<CouponSystemManagement> getUsingCouponAllCouponType(String fromCountryCode) {

        List<String> couponTypes = new ArrayList<>();
        couponTypes.add(SystemCouponType.welcome.getCode());
        couponTypes.add(SystemCouponType.birthday.getCode());
        couponTypes.add(SystemCouponType.thankYouMyFriend.getCode());
        couponTypes.add(SystemCouponType.thankYouToo.getCode());

        return couponSystemManagementMapper.getUsingCouponAllCouponType(fromCountryCode, couponTypes);
    }


    @Override
    public List<Coupon> getNewSystemCouponList(CouponSystemManagementRequest couponSystemManagementRequest) {
        return couponSystemManagementMapper.getNewSystemCouponList(couponSystemManagementRequest);
    }

    @Override
    @Transactional
    public void createManageSystemCoupon(CouponSystemManagement couponSystemManagement) {
        couponSystemManagementMapper.createManageSystemCoupon(couponSystemManagement);
    }

    @Override
    @Transactional
    public int updateManageSystemCoupon(Long updatedId, Long couponId, String isApplied) {
        return couponSystemManagementMapper.updateManageSystemCoupon(updatedId, couponId, isApplied);
    }

    @Override
    public CouponSystemManagement getManageCouponByCouponId(Long couponId) {
        return couponSystemManagementMapper.getManageCouponByCouponId(couponId);
    }

    @Override
    public List<CouponSystemManagement> getUsingOrLaterCouponList(CouponSystemManagementRequest couponSystemManagementRequest) {
        return couponSystemManagementMapper.getUsingOrLaterCouponList(couponSystemManagementRequest);
    }

}
