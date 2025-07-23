package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.CouponV2Mapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUpdateRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponReadResponse;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponResponse;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImplV2 implements CouponServiceV2 {

    private final CouponV2Mapper couponMapper;

    @Override
    @Transactional(readOnly = true)
    public List<CouponReadResponse> getCouponList(CouponSearchRequest couponSearchRequest) throws CashmallowException {

        try {
            return couponMapper.getCouponList(couponSearchRequest)
                    .stream().flatMap(data -> Stream.of(CouponReadResponse.of(data)))
                    .collect(Collectors.toList());

        } catch (SQLException e) {
            throw new CashmallowException(e.getMessage());
        }
    }

    @Override
    @Transactional
    public CouponResponse createCoupon(@Valid CouponCreateRequest couponCreateRequest) throws CashmallowException {

        // 1. 쿠폰 코드 중복 체크 (쿠폰 코드는 Unique 해야 한다.)
        Coupon isDuplicatedCoupon = couponMapper.getCouponByCouponCode(couponCreateRequest.fromCountryCode(), "", "", couponCreateRequest.couponCode());
        if (isDuplicatedCoupon != null) {
            throw new CashmallowException("쿠폰 코드가 이미 존재합니다. 쿠폰 코드는 중복 등록이 불가능합니다.");
        }

        // 2. 쿠폰 등록
        Coupon coupon = couponCreateRequest.toEntity(couponCreateRequest.createdId());
        Long couponId = couponMapper.createCoupon(coupon);
        if(couponId == null || couponId < 1) {
            throw new CashmallowException("쿠폰 생성 오류");
        }

        Long insertCurrencies = couponMapper.insertCouponApplyCurrency(coupon.getId(), couponCreateRequest.applyCurrencyList());
        if(insertCurrencies < 1) {
            throw new CashmallowException("대상 통화 쿠폰 생성 오류");
        }

        return CouponResponse.of(coupon);
    }

    @Override
    @Transactional
    public Coupon getCouponById(Long couponId) {
        return couponMapper.getCouponById(couponId);
    }

    @Override
    @Transactional
    public List<Long> getUsersByFromCountryCode(String fromCountryCode) {
        return couponMapper.getUsersByFromCountryCode(fromCountryCode);
    }

    @Override
    @Transactional
    public void updateCouponActive(String fromCountryCode, List<CouponUpdateRequest.IsActive> isActive) throws CashmallowException {

        Long updated = 0L;
        for (CouponUpdateRequest.IsActive data: isActive) {
            updated += couponMapper.updateCouponActive(fromCountryCode, data.getCouponId(), data.getIsActive());
        }
        if(updated != isActive.size()) {
            throw new CashmallowException("활성화 여부 수정 오류");
        }
    }

    @Override
    @Transactional
    public int deleteCoupon(List<Long> couponIds) throws CashmallowException {
        Long countIssued = couponMapper.getIsCouponIssued(couponIds);
        if(countIssued > 0) {
            throw new CashmallowException("삭제가 불가능한 쿠폰입니다. 사용자에게 발급 이력이 있는 쿠폰은 삭제할 수 없습니다.");
        }

        Long countActive = couponMapper.getIsActive(couponIds);
        if(countActive > 0) {
            throw new CashmallowException("삭제가 불가능한 쿠폰입니다. 활성화 상태의 쿠폰은 삭제할 수 없습니다.");
        }

        int deleted = couponMapper.deleteCoupon(couponIds);
        if(deleted <= 0) {
            throw new CashmallowException("쿠폰 삭제에 실패하였습니다.");
        }

        couponMapper.deleteApplyCurrencyByCouponId(couponIds);
        return deleted;
    }

    @Override
    public List<Coupon> getIssuableCoupons(String fromCountryCode, LocalDate currentDate, String searchStartDate, String searchEndDate) {
        String today = DateUtil.fromLocalDateToY_M_D(currentDate);
        return couponMapper.getIssuableCoupons(fromCountryCode, today, searchStartDate, searchEndDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getCouponTotalCount(CouponSearchRequest couponSearchRequest) {
        return couponMapper.getCouponTotalCount(couponSearchRequest);
    }

    // 정확한 쿠폰 코드로 검색. Coupon 코드로 검색: Unique 걸려있으므로 유일함
    @Override
    public Coupon getCouponByCouponCode(String fromCountryCode, String isSystem, String isActive, String couponCode) {
        return couponMapper.getCouponByCouponCode(fromCountryCode, isSystem, isActive, couponCode);
    }

    @Override
    @Transactional(readOnly = true)
    public Long checkDuplicateCode(String fromCountryCode, String couponCode) {
        return couponMapper.checkDuplicateCode(fromCountryCode, couponCode);
    }

    @Override
    public List<String> getApplyCurrencyListByCouponId(Long couponId) {
        return couponMapper.getApplyCurrencyListByCouponId(couponId);
    }

    @Override
    public int deleteApplyCurrencyByCouponId(Long couponId, List<String> applyCurrencyList) {
        return couponMapper.deleteApplyCurrencyByCouponId(couponId, applyCurrencyList);
    }

}