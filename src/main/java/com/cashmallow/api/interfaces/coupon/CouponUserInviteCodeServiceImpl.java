package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.entity.CouponUserInviteCode;
import com.cashmallow.api.domain.model.coupon.CouponUserInviteCodeMapper;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest;
import com.cashmallow.common.RandomUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponUserInviteCodeServiceImpl implements CouponUserInviteCodeService {

    private final CouponUserInviteCodeMapper couponUserInviteCodeMapper;

    @Override
    @Transactional
    public CouponUserInviteCode getCouponUserInviteCode(CouponUserInviteCodeRequest couponUserInviteCodeRequest) {
        CouponUserInviteCode couponUserInviteCode = couponUserInviteCodeMapper.getCouponUserInviteCode(couponUserInviteCodeRequest);

        if (couponUserInviteCode != null) {
            return couponUserInviteCode;
        }

        if (StringUtils.isNotBlank(couponUserInviteCodeRequest.getAbbreviation()) && couponUserInviteCodeRequest.getUserId() != null) {
            couponUserInviteCode = CouponUserInviteCode.builder()
                    .inviteCode(makeInviteCode(couponUserInviteCodeRequest.getAbbreviation()))
                    .userId(couponUserInviteCodeRequest.getUserId())
                    .createdAt(LocalDateTime.now())
                    .build();

        }
        return couponUserInviteCode;
    }

    @Transactional
    public String makeInviteCode(String code) {
        while (true) {
            String inviteCode = code + RandomUtil.generateRandomString(RandomUtil.CAPITAL_ALPHA_NUMERIC, 6);
            CouponUserInviteCodeRequest couponUserInviteCodeRequest = new CouponUserInviteCodeRequest();
            couponUserInviteCodeRequest.setInviteCode(inviteCode);

            CouponUserInviteCode couponUserInviteCode = couponUserInviteCodeMapper.getCouponUserInviteCode(couponUserInviteCodeRequest);
            if (couponUserInviteCode == null) {
                return inviteCode;
            }
        }
    }

    @Override
    @Transactional
    public void getCouponUserInviteCodeV3(
            com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest couponUserInviteCodeRequest) throws CashmallowException {
        log.info("getCouponUserInviteCodeV3 request: {}", couponUserInviteCodeRequest.toString());

        String inviteCode = makeInviteCodeV3(couponUserInviteCodeRequest.getIso3166());

        CouponUserInviteCode couponUserInviteCode = CouponUserInviteCode.builder()
                .userId(couponUserInviteCodeRequest.getUserId())
                .inviteCode(inviteCode)
                .build();

        Long savedInviteCode = couponUserInviteCodeMapper.insertCouponUserInviteCode(couponUserInviteCode);
        if (savedInviteCode < 1) {
            throw new CashmallowException("Failed to invite coupon user invite code");
        }
    }

    @Override
    public CouponUserInviteCode getInviteCodeByUserId(Long userId) {
        return couponUserInviteCodeMapper.getInviteCodeByUserId(userId);
    }

    @Override
    public CouponUserInviteCode getUserIdByInviteCode(CouponUserInviteCodeRequest inviterUserRequest) {
        return couponUserInviteCodeMapper.getUserIdByInviteCode(inviterUserRequest);
    }

    /**
     * 가입 시에 초대 코드가 생성되어야 한다.
     **/
    @Transactional
    public String makeInviteCodeV3(String iso3166) {
        log.info("makeInviteCodeV3 request: {}", iso3166);

        while (true) {
            // 초대 코드 생성: FR + [(숫자 + 영문 대문자) Random 6자리] + HK
            String inviteCode = SystemCouponType.thankYouMyFriend.getAbbreviation()
                    + RandomUtil.generateRandomString(RandomUtil.CAPITAL_ALPHA_NUMERIC, 6) + iso3166;

            // 이미 존재하는 초대 코드일 시, 초대 코드 재생성
            CouponUserInviteCode couponUserInviteCode
                    = couponUserInviteCodeMapper.getCouponUserInviteCode(
                    CouponUserInviteCodeRequest.inviteCodeRequest(inviteCode)
            );
            if (couponUserInviteCode == null) {
                log.info("inviteCode={}", inviteCode);
                return inviteCode;
            }
        }
    }

}