package com.cashmallow.api.interfaces.global;

import com.cashmallow.api.domain.model.country.CurrencyLimit;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import com.cashmallow.api.domain.model.traveler.GlobalTravelerWalletBalance;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponApplyCurrency;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponIssueCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUpdateRequest;
import com.cashmallow.api.interfaces.global.dto.*;
import com.cashmallow.api.interfaces.traveler.web.TravelerJpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/global")
public class GlobalController {
    private final long createdId = -1L; // JP 생성자 처리 불가
    private final GlobalService globalService;
    private final TravelerJpService travelerJpService;
    private final CouponUserService couponUserService;

    @PostMapping({
            "/jp/notice/content",
            "/kr/notice/content"
    })
    public GlobalBaseResponse addNotice(@RequestBody NoticeRequest request) {
        return GlobalBaseResponse.ok(globalService.addNotice(request));
    }

    @PostMapping({
            "/jp/country-fee"
    })
    public GlobalBaseResponse syncCountryFee(@RequestBody CountryFeeRequest request) {
        return GlobalBaseResponse.ok(globalService.syncCountryFee(request));
    }

    @PostMapping({
            "/jp/exchange-fee"
    })
    public GlobalBaseResponse syncExchangeFee(@RequestBody ExchangeConfigRequest request) {
        return GlobalBaseResponse.ok(globalService.syncExchangeFee(request));
    }

    /**
     * start of Traveler 관련 엔드포인트 처리
     */
    // 비밀번호 초기화(완료)
    @PostMapping("/jp/traveler/resetPassword/{travelerId}")
    public GlobalBaseResponse travelerResetPassword(@PathVariable Long travelerId) throws CashmallowException {
        return GlobalBaseResponse.ok(globalService.getTravelerResetPasswordUrl(travelerId));
    }

    // 회원 탈퇴(완료)
    @PostMapping("/jp/traveler/leave/{travelerId}")
    public GlobalBaseResponse travelerLeave(@PathVariable Long travelerId) throws CashmallowException {
        globalService.travelerLeave(travelerId);
        return GlobalBaseResponse.ok();
    }

    // 인증 취소상태(N)로 변경
    @PostMapping("/jp/traveler/cancel/certification/N/{travelerId}")
    public GlobalBaseResponse cancelCertificationTraveler(@PathVariable Long travelerId,
                                                          @RequestParam("managerName") String managerName) throws CashmallowException {
        globalService.cancelCertificationTraveler(travelerId, managerName);
        return GlobalBaseResponse.ok();
    }

    // 회원 정보 업데이트(완료)
    @PostMapping("/jp/traveler")
    public GlobalBaseResponse travelerUpdate(@RequestBody TravelerUpdateDto request) throws CashmallowException {
        TravelerUpdateResponseDto o = globalService.travelerUpdate(request);
        return GlobalBaseResponse.ok(o);
    }

    // 회원 정보 ekyc 업데이트
    @PostMapping("/jp/traveler/ekyc")
    public GlobalBaseResponse travelerEkycUpdate(@RequestBody TravelerEkycUpdateDto request) throws CashmallowException {
        try {
            globalService.travelerEkycUpdate(request);
            return GlobalBaseResponse.ok();
        } catch (CashmallowException e) {
            log.error("errorMessage: {}", e.getMessage());
            return GlobalBaseResponse.error(e);
        }
    }

    // 승인 요청(완료)
    @PostMapping("/jp/traveler/certification/approve/{travelerId}")
    public GlobalBaseResponse travelerCertificationApprove(@PathVariable Long travelerId,
                                                           @RequestBody GlobalTravelerCertificationRequest request) throws CashmallowException {
        globalService.travelerCertificationApprove(travelerId, request);
        return GlobalBaseResponse.ok();
    }

    // 승인 거부 요청(완료)
    @PostMapping("/jp/traveler/certification/reject/{travelerId}")
    public GlobalBaseResponse travelerCertificationReject(@PathVariable Long travelerId,
                                                          @RequestBody GlobalTravelerCertificationRequest request) throws CashmallowException {
        globalService.travelerCertificationReject(travelerId, request);
        return GlobalBaseResponse.ok();
    }

    // 인증 보류 요청 중 certificationStep 초기화
    @PostMapping("/jp/traveler/certification/reject/{travelerId}/reset")
    public GlobalBaseResponse travelerCertificationStepReset(@PathVariable Long travelerId,
                                                             @RequestBody GlobalTravelerCertificationRequest request) throws CashmallowException {
        travelerJpService.resetCertificationStep(travelerId, request);
        return GlobalBaseResponse.ok();
    }

    // Traveler 화폐별 잔액 조회
    @GetMapping("/jp/traveler/balance/{travelerId}")
    public GlobalBaseResponse travelerBalance(@PathVariable Long travelerId) {
        List<GlobalTravelerWalletBalance> travelerBalance = globalService.getTravelerBalance(travelerId);
        return GlobalBaseResponse.ok(travelerBalance);
    }

    // end of Traveler 관련 엔드포인트 처리

    @PostMapping("/jp/traveler/edd/{travelerId}")
    public GlobalBaseResponse syncAddTravelerEdd(@PathVariable Long travelerId) throws CashmallowException {
        return GlobalBaseResponse.ok(globalService.syncAddTravelerEdd(travelerId));
    }

    /**
     * 쿠폰 동기화를 처리하는 메서드입니다.
     *
     * @param request 쿠폰 정보를 담고 있는 요청 객체
     * @return 동기화 결과를 나타내는 GlobalBaseResponse 객체
     * @throws CashmallowException 쿠폰 동기화 중 발생할 수 있는 예외
     */
    @PostMapping("/jp/coupon")
    public GlobalBaseResponse syncCoupon(@RequestBody CouponCreateRequest request) throws CashmallowException {
        return GlobalBaseResponse.ok(globalService.syncCoupon(request.withCreatedId(createdId)));
    }

    /**
     * 쿠폰 발급을 동기적으로 처리하는 메서드입니다.
     *
     * @param request 쿠폰 발급 요청 데이터가 포함된 CouponIssueCreateRequest 객체
     * @return 쿠폰 발급 처리 결과를 포함한 GlobalBaseResponse 객체
     * @throws CashmallowException 처리 도중 발생할 수 있는 예외
     */
    @PostMapping(value = "/jp/coupon/issue")
    public GlobalBaseResponse syncCouponIssue(@RequestBody CouponIssueCreateRequest request) {
        try {
            return GlobalBaseResponse.ok(globalService.syncCouponIssue(request.withCreatedId(request.createdId())));
        } catch (CashmallowException e) {
            log.error("errorMessage: {}", e.getMessage());
            return GlobalBaseResponse.error(e);
        }
    }

    /**
     * 쿠폰 활성화 상태를 업데이트하는 메서드.
     *
     * @param request 쿠폰 활성화 상태를 업데이트하기 위한 요청 객체
     * @return 업데이트 결과를 포함한 GlobalBaseResponse 객체
     */
    @PutMapping("/jp/coupon/activated")
    public GlobalBaseResponse updateCouponActivated(@RequestBody CouponUpdateRequest request) throws CashmallowException {
        globalService.updateCouponIssuePossible(request);
        return GlobalBaseResponse.ok();
    }

    /**
     * 쿠폰 미사용 삭제
     *
     */
    @DeleteMapping("/jp/coupon/deleted")
    public GlobalBaseResponse deleteCoupons(@RequestParam("couponIds") List<Long> couponIds) throws CashmallowException {
        globalService.deleteCoupons(couponIds);
        return GlobalBaseResponse.ok();
    }

    /**
     * 한도 제한금액 설정 edit
     *
     * @param currencyLimitRequest 입력값
     * @return ID
     */
    @PutMapping("/jp/currency-limit")
    GlobalBaseResponse syncSaveCurrencyLimit(@RequestBody CurrencyLimit currencyLimitRequest) {
        return GlobalBaseResponse.ok(globalService.saveCurrencyLimit(currencyLimitRequest));
    }

    /**
     * JP 신규 시스템 쿠폰 생성 실패 시에 호출하여 HK 쪽에 추가 된 데이터 삭제 (롤백)
     *
     * @param couponId
     **/
    @PostMapping("/jp/coupon/{couponId}/system/compensating-transaction")
    public GlobalBaseResponse couponCompensatingTransaction(@PathVariable Long couponId) throws CashmallowException {
        log.info("JP 신규 시스템 쿠폰 생성 실패하여 보상 트랜잭션 시작: {}", couponId);
        boolean success = globalService.couponCompensatingTransaction(couponId);
        if (!success) {
            // 실패 시, 재시도
            boolean retrySuccess = globalService.couponCompensatingTransaction(couponId);
            if (!retrySuccess) {
                log.error("JP 신규 시스템 쿠폰 생성, 보상 트랜잭션 실패: {}", couponId);
                return GlobalBaseResponse.serverError(couponId);
            }
        }
        return GlobalBaseResponse.ok();
    }


    /**
     * JP 시스템 쿠폰 발급 실패 시에 호출하여 HK 쪽에 추가 된 데이터 삭제 (롤백)
     *
     * @param issueId
     **/
    @PostMapping("/jp/coupon/{issueId}/compensating-transaction")
    public GlobalBaseResponse couponCompensatingTransactionIssue(@PathVariable Long issueId) {
        log.info("JP 시스템 쿠폰 발급 실패하여 보상 트랜잭션 시작: {}", issueId);
        boolean success = globalService.couponCompensatingTransactionIssue(issueId);
        if (!success) {
            // 실패 시, 재시도
            boolean retrySuccess = globalService.couponCompensatingTransactionIssue(issueId);
            if (!retrySuccess) {
                log.error("JP 시스템 쿠폰 발급 실패, 보상 트랜잭션 실패: {}", issueId);
                return GlobalBaseResponse.serverError(issueId);
            }
        }
        return GlobalBaseResponse.ok();
    }

    /**
     *  JP 시스템 쿠폰 통화 업데이트 실패 시에 호출하여 HK 쪽에 추가 된 데이터 삭제 (롤백)
     *
     * @Param couponApplyCurrency(couponId, applyCurrencyList)
     **/
    @PostMapping("/jp/coupon/apply-currency/compensating-transaction")
    public GlobalBaseResponse couponCompensatingTransactionApplyCurrency(@RequestBody CouponApplyCurrency couponApplyCurrency) {
        log.info("JP 시스템 쿠폰 통화 업데이트 실패하여 보상 트랜잭션 시작: couponId={}, couponApplyCurrency={}", couponApplyCurrency.getCouponId(), couponApplyCurrency.getApplyCurrencyList());

        boolean success = globalService.couponCompensatingTransactionApplyCurrency(couponApplyCurrency);
        if (!success) {
            // 실패 시, 재시도
            boolean retrySuccess = globalService.couponCompensatingTransactionApplyCurrency(couponApplyCurrency);
            if (!retrySuccess) {
                log.error("JP 시스템 쿠폰 통화 업데이트 실패, 보상 트랜잭션 실패: couponId={}, couponApplyCurrency={}", couponApplyCurrency.getCouponId(), couponApplyCurrency.getApplyCurrencyList());
                return GlobalBaseResponse.serverError(couponApplyCurrency.toString());
            }
        }
        return GlobalBaseResponse.ok();
    }

    /**
     *  JP 시스템 쿠폰 사용여부 상태 업데이트(EXPIRED, AVAILABLE) 실패 시에 호출하여 HK 쪽에 데이터 재수정하여 원복 (롤백)
     *
     * @Param globalUpdateStatusUserCouponDto(availableStatus, userCouponList)
     **/
    @PostMapping("/jp/coupon/update-status/compensating-transaction")
    public GlobalBaseResponse couponCompensatingTransactionUpdateStatus(@RequestBody GlobalUpdateStatusUserCouponDto globalUpdateStatusUserCouponDto) {
        List<UpdateStatusUserCoupon> userCouponList = globalUpdateStatusUserCouponDto.getUserCouponList();

        log.info("JP 시스템 쿠폰 사용여부 상태 업데이트(EXPIRED, AVAILABLE) 실패하여 보상 트랜잭션 시작: globalUpdateStatusUserCouponDto={}", userCouponList.toString());

        boolean success = globalService.couponCompensatingTransactionUpdateStatus(userCouponList, AvailableStatus.AVAILABLE.name());
        if (!success) {
            // 실패 시, 재시도
            boolean retrySuccess = globalService.couponCompensatingTransactionUpdateStatus(userCouponList, AvailableStatus.AVAILABLE.name());
            if (!retrySuccess) {
                log.error("JP 시스템 쿠폰 사용여부 상태 업데이트(EXPIRED, AVAILABLE) 실패, 보상 트랜잭션 실패: userCouponList={}", userCouponList);
                return GlobalBaseResponse.serverError(userCouponList.toString());
            }
        }
        return GlobalBaseResponse.ok();
    }

    @GetMapping("/jp/coupon-issue-user/{couponUserId}")
    public CouponIssueUser getCouponUserById(@PathVariable Long couponUserId) {
        return couponUserService.getCouponUserById(couponUserId);
    }
}
