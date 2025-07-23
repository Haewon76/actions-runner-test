package com.cashmallow.api.interfaces.coupon;


import com.cashmallow.api.domain.model.coupon.CouponMobileV2Mapper;
import com.cashmallow.api.domain.model.coupon.CouponValidationMapper;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponMobileUser;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.inactiveuser.InactiveTraveler;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUser;
import com.cashmallow.api.domain.model.inactiveuser.InactiveUserMapper;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponMobileUserRequest;
import com.cashmallow.common.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.cashmallow.api.domain.shared.Const.INVALID_COUPON;
import static com.cashmallow.api.domain.shared.Const.NO_COUPONS_AVAILABLE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponValidationServiceImpl implements CouponValidationService {

    private final UserRepositoryService userRepositoryService;
    private final InactiveUserMapper inactiveUserMapper;
    private final ExchangeRepositoryService exchangeRepositoryService;
    private final RemittanceRepositoryService remittanceRepositoryService;

    private final CouponValidationMapper couponValidationMapper;
    private final CouponMobileV2Mapper couponMobileV2Mapper;
    private final TravelerRepositoryService travelerRepositoryService;

    // 해당 유저, from 국가, to 통화(Currency), 서비스(송금, 환전), 쿠폰 상태 등이 사용가능한 쿠폰인지 Valid
    public void validCouponUser(String fromCountryCode, Long userId, Long couponUserId, String serviceType, String availableStatus) throws CashmallowException {
        CouponMobileUserRequest couponMobileUserRequest = new CouponMobileUserRequest(
                userId,
                couponUserId,
                fromCountryCode,
                serviceType,
                "",
                availableStatus,
                null,
                null,
                null,
                Const.Y
        );

        List<CouponMobileUser> couponUsers = couponMobileV2Mapper.getCouponListIssueUsers(couponMobileUserRequest);
        if (couponUsers.isEmpty()) {
            throw new CashmallowException(INVALID_COUPON);
        }

        // 특정 couponUserId 로 1개만 선택하므로 0번째 데이터 꺼내옴
        CouponMobileUser couponUser = couponUsers.get(0);
        validateCouponUseDate(couponUser);
    }

    public void validateCouponUseDate(CouponMobileUser couponUser) throws CashmallowException {
        LocalDate now = DateUtil.toLocalDate(couponUser.getFromCountryCode());
        // 쿠폰 사용시작일(couponCalStartDate)이 오늘보다 미래이거나 쿠폰 만료일(couponCalEndDate)이 오늘보다 과거일 경우 사용불가
        if (couponUser.getCouponCalStartDate().isAfter(now) || couponUser.getCouponCalEndDate().isBefore(now)) {
            throw new CashmallowException(NO_COUPONS_AVAILABLE);
        }
    }

    // Welcome 쿠폰
    // userId 에 해당하는 유저가 n개월 내에 같은 계정으로 과거 가입 이력있는지 조회 (현재 정책 1개월)
    // 과거에 시스템 쿠폰을 사용한 이력이 있으면 true, 없으면 false
    @Override
    public boolean hasUsedSystemCoupon(Long userId, SystemCouponType couponCodePrefix, String identificationNumber, int month) {
        boolean result = false;

        // 새로 가입하는 유저
        User newUser = userRepositoryService.getUserByUserId(userId);
        // 1. 같은 계정으로 과거 가입 이력있는지 조회
        List<InactiveUser> inactiveUser = inactiveUserMapper.getInactiveUserListByLogin(newUser.getLogin());
        if (!inactiveUser.isEmpty()) {
            // 2. 탈퇴한지 1개월이 지났는지 확인 (가장 최근 탈퇴일자만 필요하므로 0번째 index 가져옴)
            ZonedDateTime createdDateUTC = ZonedDateTime.ofInstant(inactiveUser.get(0).getCreatedDate().toInstant(), ZoneId.of("UTC"));
            // 2-1. 탈퇴한지 한달이 지난 날짜 계산
            createdDateUTC = createdDateUTC.plusMonths(month);
            // 2-2. 탈퇴날짜에 1달은 더한 날짜가 현재 날짜보다 미래면 true (즉, 탈퇴한지 1달이 안 지났으면 true)
            if (createdDateUTC.isAfter(ZonedDateTime.now())) {
                // 3. 쿠폰 사용여부 확인
                int usedCoupon = couponValidationMapper.countUsedCouponByCouponCodeAndUserIds(
                        inactiveUser.stream().map(InactiveUser::getId).toList(), AvailableStatus.USED.name(), couponCodePrefix.getCode());
                if (usedCoupon > 0) {
                    result = true;
                }
            }
        }
        // 4. 가장 마지막에 탈퇴한 traveler 본인인증 신분증 넘버 체크
        if (identificationNumber != null) {
            List<InactiveTraveler> inactiveTraveler = inactiveUserMapper.getInactiveTravelerListByIdentificationNumber(identificationNumber);
            // 새로 가입하는 유저의 경우 inactiveTraveler 가 존재하지 않으므로 존재하는 경우에만 확인
            if (!inactiveTraveler.isEmpty()) {
                // 4-1. 탈뢰한지 1개월이 지났는지 확인 (가장 최근 탈퇴일자만 필요하므로 0번째 index 가져옴)
                ZonedDateTime createdDateUTC = ZonedDateTime.ofInstant(inactiveTraveler.get(0).getCreatedDate().toInstant(), ZoneId.of("UTC"));
                // 4-2. 탈퇴한지 한달이 지난 날짜 계산
                createdDateUTC = createdDateUTC.plusMonths(month);
                // 4-3. 탈퇴날짜에 1달은 더한 날짜가 현재 날짜보다 미래면 true (즉, 탈퇴한지 1달이 안 지났으면 true)
                if (createdDateUTC.isAfter(ZonedDateTime.now())) {

                    int usedCoupon = couponValidationMapper.countUsedCouponByCouponCodeAndUserIds(
                            inactiveTraveler.stream().map(InactiveTraveler::getUserId).toList(), AvailableStatus.USED.name(), couponCodePrefix.getCode());
                    if (usedCoupon > 0) {
                        result = true;
                    }
                }
            }
        }

        return result;
    }

    // hasRegisteredCoupon() 결과가 true 인 유저들
    @Override
    public List<Long> hasRegisteredCouponByUserList(List<Long> userIds, String couponCodePrefix) {
        List<Traveler> travelers = travelerRepositoryService.getTravelersByUserIds(userIds);
        if (travelers.isEmpty()) {
            return List.of();
        } else {
            travelers.removeIf(tr ->
                    !hasRegisteredCoupon(tr.getUserId(), couponCodePrefix, tr.getIdentificationNumber())
            );
            log.debug("hasRegisteredCouponByUserList() travelers={}", travelers);
            return travelers.stream().map(Traveler::getId).toList();
        }
    }

    // 중복으로 등록 혹은 발급 가능한 쿠폰인지 확인
    // 가입 이력 있거나 ThankYouMyFriend, Influencer 쿠폰을 등록한 이력이 있으면 true, 없으면 false
    // true 면 회수 처리
    @Override
    public boolean hasRegisteredCoupon(Long userId, String couponCodePrefix, String identificationNumber) {
        boolean result = false;

        // 새로 가입하는 유저
        User newUser = userRepositoryService.getUserByUserId(userId);
        // 1-1. 같은 계정으로 과거 가입 이력있는지 조회
        List<InactiveUser> inactiveUser = inactiveUserMapper.getInactiveUserListByLogin(newUser.getLogin());
        if (!inactiveUser.isEmpty()) {
            // 1-2. 같은 계정으로 쿠폰 등록여부 확인
            int usedCoupon = couponValidationMapper.countUsedCouponByCouponCodeAndUserIds(
                    inactiveUser.stream().map(InactiveUser::getId).toList(), null, couponCodePrefix);
            if (usedCoupon > 0) {
                result = true;
            }
        }
        // 2-1. 탈퇴한 traveler 본인인증 신분증 넘버 체크
        // 본인인증을 했을 경우에만 신분증 넘버 체크하도록 추가
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(newUser.getId());
        if (traveler != null && traveler.getCertificationOk().equals("Y") && identificationNumber != null) {
            List<InactiveTraveler> inactiveTravelers = inactiveUserMapper.getInactiveTravelerListByIdentificationNumber(identificationNumber);
            // 2-2. 새로 가입하는 유저의 경우 inactiveTraveler 가 존재하지 않으므로 존재하는 경우에만 확인
            if (!inactiveTravelers.isEmpty()) {
                // 2-3. 신분증 넘버로 조회된 모든 유저의 쿠폰 등록 여부 체크
                int usedCoupon = couponValidationMapper.countUsedCouponByCouponCodeAndUserIds(
                        inactiveTravelers.stream().map(InactiveTraveler::getUserId).toList(), null, couponCodePrefix);
                if (usedCoupon > 0) {
                    result = true;
                }
            }
        }
        return result;
    }

    // hasInactiveTransactionHistory() 결과가 true 인 유저들
    @Override
    public List<Long> hasInactiveTransactionHistoryByUserList(List<Long> userIds) {
        List<Traveler> travelers = travelerRepositoryService.getTravelersByUserIds(userIds);
        if (travelers.isEmpty()) {
            return List.of();
        } else {
            travelers.removeIf(tr ->
                    !hasInactiveTransactionHistory(tr.getIdentificationNumber())
            );
            log.debug("hasInactiveTransactionHistoryByUserList() travelers={}", travelers);
            return travelers.stream().map(Traveler::getId).toList();
        }
    }

    // 중복으로 등록 혹은 발급 가능한 쿠폰인지 확인
    // 과거 탈퇴 계정에 거래내역 존재하면 회수 처리함
    // 거래내역 있으면 true, 없으면 false
    @Override
    public boolean hasInactiveTransactionHistory(String identificationNumber) {
        boolean result = false;

        // 1. 탈퇴한 traveler 본인인증 신분증 넘버 체크
        if (identificationNumber != null) {
            List<InactiveTraveler> inactiveTravelers = inactiveUserMapper.getInactiveTravelerListByIdentificationNumber(identificationNumber);
            // 2. 새로 가입하는 유저의 경우 inactiveTraveler 가 존재하지 않으므로 존재하는 경우에만 확인
            if (!inactiveTravelers.isEmpty()) {
                List<Long> inactiveTravelerIds = inactiveTravelers.stream().map(InactiveTraveler::getId).toList();
                // 3. 신분증 넘버로 조회된 모든 거래 내역 체크
                int countExchange = exchangeRepositoryService.countExchangeListByTravelerIds(inactiveTravelerIds);
                int countRemittance = remittanceRepositoryService.countRemittanceListByTravelerIds(inactiveTravelerIds);
                if (countExchange + countRemittance > 0) {
                    result = true;
                }
            }
        }
        return result;
    }


}
