package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponSystemManagement;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponCreateManagementRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSystemManagementRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponSystemManageResponse;
import com.cashmallow.common.DateUtil;
import com.cashmallow.common.HeaderUtil;
import com.cashmallow.common.TimezoneUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.Const.N;

@Slf4j
@RestController
@RequestMapping(value = "/admin/coupons/system")
@RequiredArgsConstructor
public class CouponSystemManagementController {

    private final AuthService authService;
    private final UserRepositoryService userRepositoryService;

    private final CouponServiceV2 couponService;
    private final CouponMobileServiceV2 couponMobileService;
    private final CouponSystemManagementService couponSystemManagementService;

    /**
     * HK Admin 시스템 쿠폰 메뉴 (HK, JP 시스템 쿠폰 모두 HK Admin 에서 수행)
     * [시스템 쿠폰 관리(조회/해제)]
     **/
    @GetMapping("/is-applied")
    public ResponseEntity<DatatablesResponse<CouponSystemManageResponse>> getUsingManageSystemCoupon(@RequestHeader(name = "Authorization") String token
            , @RequestParam String fromCountryCode
            , @RequestParam String couponType
    ) {

        String method = "getUsingManageSystemCoupon()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.warn("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        // 조회는 ADMIN 권한 가졌을 경우 가능
        int existed = userRepositoryService.getUserRoleByUserId(managerId, ROLE_ADMIN);
        if (existed < 1) {
            log.warn("{}: 해당 메뉴는 admin 관리자만 접근 가능합니다. {} managerId={}", method, ACCESS_DENIED, managerId);
            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, "해당 작업은 Admin 관리자만 수행 가능합니다."));
        }

        DatatablesResponse<CouponSystemManageResponse> result = null;

        try {

            log.info("fromCountryCode={}, couponType={}", fromCountryCode, couponType);

            LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);

            CouponSystemManagementRequest request
                    = CouponSystemManagementRequest.builder()
                    .currentDate(currentDate)
                    .fromCountryCode(fromCountryCode)
                    .build();

            List<CouponSystemManageResponse> response = new ArrayList<>();

            request.setCouponType(SystemCouponType.welcome.getCode());
            if (SystemCouponType.welcome.getCode().equals(request.getCouponType())) {
                List<CouponSystemManagement> dateRangeCoupon = couponSystemManagementService.getUsingOrLaterCouponList(request);
                if (!dateRangeCoupon.isEmpty()) {
                    List<CouponSystemManageResponse> systemCoupon = dateRangeCoupon.stream().map(
                            data -> {
                                Coupon dateRangeCouponEntity = couponService.getCouponById(data.getCouponId());
                                return CouponSystemManageResponse.of(data, dateRangeCouponEntity);
                            }).toList();
                    response.addAll(systemCoupon);
                } else {
                    CouponSystemManagement defaultCoupon = couponSystemManagementService.getUsingCoupon(request);
                    Coupon defaultCouponEntity = couponService.getCouponById(defaultCoupon.getCouponId());
                    CouponSystemManageResponse systemCoupon = CouponSystemManageResponse.of(defaultCoupon, defaultCouponEntity);
                    response.add(systemCoupon);
                }
            }
            request.setCouponType(SystemCouponType.birthday.getCode());
            if (SystemCouponType.birthday.getCode().equals(request.getCouponType())) {
                List<CouponSystemManagement> dateRangeCoupon = couponSystemManagementService.getUsingOrLaterCouponList(request);
                if (!dateRangeCoupon.isEmpty()) {
                    List<CouponSystemManageResponse> systemCoupon = dateRangeCoupon.stream().map(
                            data -> {
                                Coupon dateRangeCouponEntity = couponService.getCouponById(data.getCouponId());
                                return CouponSystemManageResponse.of(data, dateRangeCouponEntity);
                            }).toList();
                    response.addAll(systemCoupon);
                } else {
                    CouponSystemManagement defaultCoupon = couponSystemManagementService.getUsingCoupon(request);
                    Coupon defaultCouponEntity = couponService.getCouponById(defaultCoupon.getCouponId());
                    CouponSystemManageResponse systemCoupon = CouponSystemManageResponse.of(defaultCoupon, defaultCouponEntity);
                    response.add(systemCoupon);
                }
            }
            request.setCouponType(SystemCouponType.thankYouMyFriend.getCode());
            if (SystemCouponType.thankYouMyFriend.getCode().equals(request.getCouponType())) {
                List<CouponSystemManagement> dateRangeCoupon = couponSystemManagementService.getUsingOrLaterCouponList(request);
                if (!dateRangeCoupon.isEmpty()) {
                    List<CouponSystemManageResponse> systemCoupon = dateRangeCoupon.stream().map(
                            data -> {
                                Coupon dateRangeCouponEntity = couponService.getCouponById(data.getCouponId());
                                return CouponSystemManageResponse.of(data, dateRangeCouponEntity);
                            }).toList();
                    response.addAll(systemCoupon);
                } else {
                    CouponSystemManagement defaultCoupon = couponSystemManagementService.getUsingCoupon(request);
                    Coupon defaultCouponEntity = couponService.getCouponById(defaultCoupon.getCouponId());
                    CouponSystemManageResponse systemCoupon = CouponSystemManageResponse.of(defaultCoupon, defaultCouponEntity);
                    response.add(systemCoupon);
                }
            }
            request.setCouponType(SystemCouponType.thankYouToo.getCode());
            if (SystemCouponType.thankYouToo.getCode().equals(request.getCouponType())) {
                List<CouponSystemManagement> dateRangeCoupon = couponSystemManagementService.getUsingOrLaterCouponList(request);
                if (!dateRangeCoupon.isEmpty()) {
                    List<CouponSystemManageResponse> systemCoupon = dateRangeCoupon.stream().map(
                            data -> {
                                Coupon dateRangeCouponEntity = couponService.getCouponById(data.getCouponId());
                                return CouponSystemManageResponse.of(data, dateRangeCouponEntity);
                            }).toList();
                    response.addAll(systemCoupon);
                } else {
                    CouponSystemManagement defaultCoupon = couponSystemManagementService.getUsingCoupon(request);
                    Coupon defaultCouponEntity = couponService.getCouponById(defaultCoupon.getCouponId());
                    CouponSystemManageResponse systemCoupon = CouponSystemManageResponse.of(defaultCoupon, defaultCouponEntity);
                    response.add(systemCoupon);
                }
            }

            result = new DatatablesResponse<>();
            result.setData(response);
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("적용된 시스템 쿠폰이 "+response.size()+"개 조회 되었습니다.");

        } catch (Exception e) {
            log.error("getManageSystemCoupon(): 적용된 시스템 쿠폰 조회 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +"Error");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * HK Admin 시스템 쿠폰 메뉴 (HK, JP 시스템 쿠폰 모두 HK Admin 에서 수행)
     * [시스템 쿠폰 변경 적용] 메뉴의 [쿠폰 선택] selectBox
     * 쿠폰 목록 조회
     **/
    @GetMapping
    public ResponseEntity<DatatablesResponse<CouponSystemManageResponse>> getManageSystemCoupon(@RequestHeader(name = "Authorization") String token
            , @RequestParam String fromCountryCode
            , @RequestParam String couponType
    ) {

        String method = "getManageSystemCoupon()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.warn("invalid token. token={}, managerId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        // 조회는 ADMIN 권한 가졌을 경우 가능
        int existed = userRepositoryService.getUserRoleByUserId(managerId, ROLE_ADMIN);
        if (existed < 1) {
            log.warn("{}: 해당 메뉴는 admin 관리자만 접근 가능합니다. {} managerId={}", method, ACCESS_DENIED, managerId);
            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, "해당 작업은 Admin 관리자만 수행 가능합니다."));
        }

        DatatablesResponse<CouponSystemManageResponse> result = null;

        try {

            log.info("fromCountryCode={}, couponType={}", fromCountryCode, couponType);

            LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);
            CouponSystemManagementRequest request
                    = CouponSystemManagementRequest.builder()
                    .currentDate(currentDate)
                    .fromCountryCode(fromCountryCode)
                    .couponType(couponType)
                    .build();

            List<CouponSystemManageResponse> response = new ArrayList<>();

            List<Coupon> newCouponList = couponSystemManagementService.getNewSystemCouponList(request);
            List<CouponSystemManageResponse> toResponseNewCouponList = newCouponList.stream().map(CouponSystemManageResponse::ofNewCoupon).toList();
            if (SystemCouponType.welcome.getCode().equals(request.getCouponType())) {
                response.addAll(toResponseNewCouponList);
            }

            if (SystemCouponType.birthday.getCode().equals(request.getCouponType())) {
                response.addAll(toResponseNewCouponList);
            }

            if (SystemCouponType.thankYouMyFriend.getCode().equals(request.getCouponType())) {
                response.addAll(toResponseNewCouponList);
            }

            if (SystemCouponType.thankYouToo.getCode().equals(request.getCouponType())) {
                response.addAll(toResponseNewCouponList);
            }

            result = new DatatablesResponse<>();
            result.setData(response);
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("등록된 시스템 쿠폰이 "+response.size()+"개 조회 되었습니다.");

        } catch (Exception e) {
            log.error("getManageSystemCoupon(): 등록된 시스템 쿠폰 조회 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +"Error");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * HK Admin 시스템 쿠폰 메뉴 (HK, JP 시스템 쿠폰 모두 HK Admin 에서 수행)
     * [시스템 쿠폰 변경 적용] 메뉴의 [저장] 버튼
     **/
    @PostMapping
    public ResponseEntity<DatatablesResponse<Boolean>> createManageSystemCoupon(@RequestHeader(name = "Authorization") String token
            , @RequestBody CouponCreateManagementRequest createRequest
    ) {

        String method = "createManageSystemCoupon()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, managerId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        int existed = userRepositoryService.getUserRoleByUserId(managerId, ROLE_SUPERMAN);
        if (existed < 1) {
            log.warn("{}: 해당 메뉴는 총관리자만 접근 가능합니다. {} managerId={}", method, ACCESS_DENIED, managerId);
            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, "해당 작업은 총관리자만 수행 가능합니다."));
        }

        DatatablesResponse<Boolean> result = null;

        try {
            LocalDate startDateLocal = null;
            LocalDate endDateLocal = null;
            Timestamp startDate = null;
            Timestamp endDate = null;

            if (createRequest.getStartDateLocal() != null && createRequest.getEndDateLocal() != null) {
                startDateLocal = DateUtil.fromY_M_D(createRequest.getStartDateLocal());
                endDateLocal = DateUtil.fromY_M_D(createRequest.getEndDateLocal());
                startDate = TimezoneUtil.fromLocalDate(createRequest.getFromCountryCode(), startDateLocal);
                endDate = TimezoneUtil.fromLocalDate(createRequest.getFromCountryCode(), endDateLocal);
            }

            LocalDate currentDate = DateUtil.toLocalDate(createRequest.getFromCountryCode());
            CouponSystemManagementRequest searchRequest
                    = CouponSystemManagementRequest.builder()
                    .fromCountryCode(createRequest.getFromCountryCode())
                    .couponType(createRequest.getCouponType())
                    .currentDate(currentDate)
                    .build();

            CountryCode countryCode = CountryCode.of(createRequest.getFromCountryCode());
            // couponCodePrefix + iso3166 + [couponCodeBody] = Welcome + HK + _50_250531
            // ex: Welcome(HK)_50_250531
            // result: Welcome(HK)
            String couponCode = couponMobileService.getCouponCode(createRequest.getCouponType(), countryCode.name(), null);
            // result: _50_250531
            Coupon coupon = couponService.getCouponById(createRequest.getCouponId());
            if (coupon == null) {
                result = new DatatablesResponse<>();

                result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
                result.setCode(Const.STATUS_FAILURE);
                result.setMessage("새로 적용할 시스템 쿠폰이 없습니다.");
                throw new CashmallowException("새로 적용할 시스템 쿠폰이 없습니다.");
            }

            String couponCodeBody = coupon.getCouponCode().replace(couponCode, "");
            CouponSystemManagement couponSystemManagement = CouponSystemManagement.of(createRequest, couponCodeBody, startDateLocal, endDateLocal, startDate, endDate, managerId);

            if (startDateLocal != null && endDateLocal != null) {

                if (startDateLocal.isBefore(currentDate) || endDateLocal.isBefore(currentDate)){
                    result = new DatatablesResponse<>();

                    result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
                    result.setCode(Const.STATUS_FAILURE);
                    result.setMessage("시작일자와 종료일자를 올바르게 입력해주세요.");
                    throw new CashmallowException("시작일자와 종료일자를 올바르게 입력해주세요.");
                }

                // 적용하려는 쿠폰의 적용 시작일자가 기존에 적용되어 있는 프로모션 기간과 겹치는지 조회
                searchRequest.setCurrentDate(startDateLocal);
                CouponSystemManagement startDateCheckCoupon = couponSystemManagementService.getUsingCouponDateRange(searchRequest);

                LocalDate checkStartDate = null;
                LocalDate checkEndDate = null;

                if (startDateCheckCoupon != null) {
                    checkStartDate = startDateCheckCoupon.getStartDateLocal();
                    checkEndDate = startDateCheckCoupon.getEndDateLocal();
                }

                // 적용하려는 쿠폰의 적용 종료일자가 기존에 적용되어 있는 프로모션 기간과 겹치는지 조회
                searchRequest.setCurrentDate(endDateLocal);
                CouponSystemManagement endDateCheckCoupon = couponSystemManagementService.getUsingCouponDateRange(searchRequest);
                if (endDateCheckCoupon != null) {
                    checkStartDate = endDateCheckCoupon.getStartDateLocal();
                    checkEndDate = endDateCheckCoupon.getEndDateLocal();
                }

                if (startDateCheckCoupon != null || endDateCheckCoupon != null) {
                    String warnMessage = String.format("해당 기간에 진행 중이거나 진행 예정인 프로모션이 있습니다. %s ~ %s", checkStartDate, checkEndDate);
                    log.warn(warnMessage);
                    result = new DatatablesResponse<>();

                    result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
                    result.setCode(Const.STATUS_FAILURE);
                    result.setMessage(warnMessage);
                    throw new CashmallowException(warnMessage);
                }
            } else {
                // 날짜 기한 없는 시스템 쿠폰의 경우 새로 등록하면 기존 쿠폰 적용 해제
                CouponSystemManagement prevCoupon = couponSystemManagementService.getUsingCoupon(searchRequest);
                if (prevCoupon != null) {
                    int updated = couponSystemManagementService.updateManageSystemCoupon(managerId, prevCoupon.getCouponId(), N);
                    if (updated < 1) {
                        result = new DatatablesResponse<>();
                        result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
                        result.setCode(Const.STATUS_FAILURE);
                        result.setMessage("기존에 적용된 시스템 쿠폰 등록 해제가 불가능합니다.");
                        throw new CashmallowException("기존에 적용된 시스템 쿠폰 등록 해제가 불가능합니다.");
                    }
                }
            }

            // 적용
            CouponSystemManagement applied = couponSystemManagementService.getManageCouponByCouponId(createRequest.getCouponId());
            if (applied != null) {
                couponSystemManagementService.updateManageSystemCoupon(managerId, createRequest.getCouponId(), Y);
            } else {
                // 새로 등록할 때에 createdId 넣어줌
                couponSystemManagement.setCreatedId(managerId);
                couponSystemManagementService.createManageSystemCoupon(couponSystemManagement);
            }

            result = new DatatablesResponse<>();
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("시스템 쿠폰이 변경 적용 되었습니다.");

        } catch (CashmallowException e) {
            log.error("createManageSystemCoupon(): {}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * HK Admin 시스템 쿠폰 메뉴 (HK, JP 시스템 쿠폰 모두 HK Admin 에서 수행)
     * [시스템 쿠폰 관리(조회/해제)] 메뉴의 [적용 해제] 버튼
     * 쿠폰 목록 조회
     **/
    @PutMapping("/is-not-applied/{fromCountryCode}/{couponId}")
    public ResponseEntity<DatatablesResponse<String>> isNotAppliedManageSystemCoupon(@RequestHeader(name = "Authorization") String token
            , @PathVariable String fromCountryCode, @PathVariable Long couponId) {

        String method = "isNotAppliedManageSystemCoupon()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, managerId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        DatatablesResponse<String> result = new DatatablesResponse<>();

        try {

            int existed = userRepositoryService.getUserRoleByUserId(managerId, ROLE_SUPERMAN);
            if (existed < 1) {
                log.warn("{}: 해당 메뉴는 총관리자만 접근 가능합니다. {} managerId={}", method, ACCESS_DENIED, managerId);
                return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                        , Const.CODE_INVALID_TOKEN, "해당 작업은 총관리자만 수행 가능합니다."));
            }

            List<CouponSystemManagement> systemCouponList = couponSystemManagementService.getUsingCouponAllCouponType(fromCountryCode);
            List<CouponSystemManagement> checkedCoupon = systemCouponList.stream().filter(f -> f.getCouponId().equals(couponId)).toList();
            if (!checkedCoupon.isEmpty()) {
                result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
                result.setCode(Const.STATUS_FAILURE);
                result.setMessage("기한 제한 없는 시스템 쿠폰은 반드시 1개씩 등록되어있어야 하므로 해제가 불가능합니다.");
                throw new CashmallowException("기한 제한 없는 시스템 쿠폰은 반드시 1개씩 등록되어있어야 하므로 해제가 불가능합니다.");
            }

            couponSystemManagementService.updateManageSystemCoupon(managerId, couponId, N);
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("적용된 시스템 쿠폰이 해제 되었습니다.");

        } catch (CashmallowException e) {
            log.error("isNotAppliedManageSystemCoupon(): {}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }
}