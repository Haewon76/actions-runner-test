package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import com.cashmallow.api.interfaces.coupon.dto.req.*;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponReadResponse;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponResponse;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.common.DateUtil;
import com.cashmallow.common.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@RestController
@RequestMapping(value = "/admin/coupons")
@RequiredArgsConstructor
public class CouponControllerV2 {

    private final AuthService authService;
    private final CouponServiceV2 couponService;

    private final GlobalQueueService globalQueueService;
    private final CountryService countryService;

    /**
     * admin 에서 호출하므로 데이터 암호화 필요 없음.
     **/
    @GetMapping
    public ResponseEntity<DatatablesResponse<CouponReadResponse>> getCoupons(@RequestHeader(name = "Authorization") String token
                             , @RequestParam String fromCountryCode
                             , @RequestParam(required = false) String couponCodeName
                             , @RequestParam String searchStartDate
                             , @RequestParam String searchEndDate
                             , @RequestParam(required = false) String sortColumnCode
                             , @RequestParam(required = false) String sortColumnOrder
                             , @RequestParam(required = false) String isSystem
                             , @RequestParam(required = false) String isActive
                             , @RequestParam(defaultValue = "1") int page
                             , @RequestParam(defaultValue = "20") int size) throws Exception {

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        DatatablesResponse<CouponReadResponse> result = null;

        try {
            // 쿠폰 발급과 같은 객체 사용함. 쿠폰 쪽에서는 시스템 구분과 활성화여부로 검색하지 않으므로 Empty String 고정
            CouponSearchRequest couponSearchRequest = CouponSearchRequest.of(fromCountryCode, couponCodeName
                    , searchStartDate, searchEndDate, sortColumnCode, sortColumnOrder, isSystem, isActive, page, size);

            log.info("couponSearchRequest={}", couponSearchRequest.toString());

            Long totalCount = couponService.getCouponTotalCount(couponSearchRequest);
            List<CouponReadResponse> couponList = couponService.getCouponList(couponSearchRequest);

            result = new DatatablesResponse<>();
            result.setDraw(page);
            result.setRecordsTotal(totalCount);
            result.setRecordsFiltered(couponSearchRequest.offset());
            result.setData(couponList);
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("쿠폰 목록 조회에 성공하였습니다.");

        } catch (CashmallowException e) {
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage("Error: Get Coupon List");

        } catch (Exception e) {
            log.error("getCoupons(): 쿠폰 목록 조회 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Get Coupon List");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    @GetMapping("/{fromCountryCode}")
    public ResponseEntity<DatatablesResponse<Boolean>> checkDuplicateCode(@RequestHeader(name = "Authorization") String token
            , @PathVariable String fromCountryCode
            , @RequestParam String couponCode) {

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        DatatablesResponse<Boolean> result = new DatatablesResponse<>();

        try {

            log.info("couponCode={}", couponCode);

            Long totalCount = couponService.checkDuplicateCode(fromCountryCode, couponCode);
            Boolean duplicated = Boolean.TRUE;
            if(totalCount < 1) {
                duplicated = Boolean.FALSE;
            }

            List<Boolean> couponList = new ArrayList<>();
            couponList.add(duplicated);

            result.setSuccess(couponList, "동일한 쿠폰 코드가 "+totalCount+"개 조회 되었습니다.");

        } catch (Exception e) {
            log.error("checkDuplicateCode(): 쿠폰 코드 중복 체크 조회 오류:{}", e.getMessage(), e);

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Coupon Duplicate Check");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    @PostMapping
    public ResponseEntity<DatatablesResponse<CouponResponse>> createCoupon(@RequestHeader(name = "Authorization") String token,
                                                                           @RequestBody CouponCreateRequest requestBody) {

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        DatatablesResponse<CouponResponse> result = new DatatablesResponse<>();

        try {
            // 관리자 계정 바인딩
            requestBody = requestBody.withCreatedId(managerId);

            // fromCountryCode = 004 이고 시스템쿠폰 일때 (홍콩 DB 적재)
            // HK admin 에서 JP 시스템쿠폰 추가할때 타는 로직
            if (requestBody.applyCurrencyList().isEmpty() && requestBody.isSystem().equals(Y)) {
                // 전체 서비스 통화. 시스템 쿠폰이므로 전체 통화로 넣어줌.
                // HK admin 에서 시스템 쿠폰을 추가할 때에는 적용 통화 리스트가 넘어오지 않으므로 추가해줌.
                Map<String, Object> params  = new HashMap<>();
                params.put("service", "Y");
                List<String> countryList = countryService.getCountryList(params).stream()
                        .map(Country::getIso3166).toList();

                List<String> countrySetList = countryList.stream().collect(Collectors.toSet()).stream().toList();

                requestBody = requestBody.withApplyCurrencyList(countrySetList);
            }

            CouponResponse createdCoupon = couponService.createCoupon(requestBody);

            List<CouponResponse> savedCoupon = new ArrayList<>();
            savedCoupon.add(createdCoupon);

            result.setSuccess(savedCoupon, "쿠폰이 생성되었습니다.");

            // fromCountryCode = 004 이고 시스템쿠폰 일때 (일본 DB에 적재)
            // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함 (admin 에서 시스템쿠폰 추가할때 타는 로직)
            if (CountryCode.JP.getCode().equals(createdCoupon.getFromCountryCode()) && createdCoupon.getIsSystem().equals(Y)) {
                Coupon coupon = couponService.getCouponById(createdCoupon.getId());
                Set<String> applyCurrencySet = couponService.getApplyCurrencyListByCouponId(coupon.getId()).stream().collect(Collectors.toSet());
                globalQueueService.globalJpSystemCouponManage(coupon, applyCurrencySet);
            }

        } catch (CashmallowException e) {
            log.error("createCoupon(): 쿠폰 생성 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());

        } catch (Exception e) {
            log.error("createCoupon(): 쿠폰 생성 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Cannot Coupon Create");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * 쿠폰 정보 수정. 활성화 여부만 수정 가능.
     **/
    @PutMapping("/{fromCountryCode}")
    public ResponseEntity<DatatablesResponse<String>> updateCouponActive(@RequestHeader(name = "Authorization") String token,
                                     @PathVariable String fromCountryCode, @RequestBody CouponUpdateRequest requestBody)  {

        String method = "updateCouponActive()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        log.info("{}: active={}", method, requestBody.toString());

        DatatablesResponse<String> result = null;

        try {
            couponService.updateCouponActive(fromCountryCode, requestBody.getUpdateList());

            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("활성화 상태가 변경되었습니다.");

        } catch (CashmallowException e) {
            log.error("updateCouponActive(): requestBody={}, 쿠폰 활성화 여부 수정 오류:{}", requestBody.getUpdateList(), e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());

        } catch (Exception e) {
            log.error("updateCouponActive(): 쿠폰 활성화 여부 수정 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Cannot Activate Coupon");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * 쿠폰 삭제. 활성화 되어있고, 발급 이력 없는 쿠폰만 삭제 가능.
     **/
    @DeleteMapping
    public ResponseEntity<DatatablesResponse<String>> deleteCoupon(@RequestHeader(name = "Authorization") String token,
                                          @RequestParam("couponId") List<Long> couponIds) {

        String method = "deleteCoupon()";

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        log.info("{}: couponIds={}", method, couponIds.toString());

        DatatablesResponse<String> result = null;

        try {
            couponService.deleteCoupon(couponIds);

            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("쿠폰 삭제가 완료되었습니다.");

        } catch (CashmallowException e) {
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());

        } catch (Exception e) {
            log.error("deleteCoupon(): 쿠폰 삭제 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Cannot Delete Coupon");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    @GetMapping("/issuable")
    public ResponseEntity<DatatablesResponse<Coupon>> getIssuableCoupons(@RequestHeader(name = "Authorization") String token
            , @RequestParam String fromCountryCode
            , @RequestParam String searchStartDate
            , @RequestParam String searchEndDate) {

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);

            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN))
                    .body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN), Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        DatatablesResponse<Coupon> result = null;

        try {
            LocalDate currentDate = DateUtil.toLocalDate(fromCountryCode);
            List<Coupon> couponList = couponService.getIssuableCoupons(fromCountryCode, currentDate, searchStartDate, searchEndDate);

            result = new DatatablesResponse<>();
            result.setDraw(0);
            result.setRecordsTotal(couponList.size());
            result.setData(couponList);
            result.setStatus(Integer.parseInt(Const.CODE_SUCCESS));
            result.setCode(Const.STATUS_SUCCESS);
            result.setMessage("발급 가능한 쿠폰 목록 조회에 성공하였습니다.");

        } catch (CashmallowException e) {
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage("Error: Get Coupon List");

        } catch (Exception e) {
            log.error("getCoupons(): 발급 가능한 쿠폰 목록 조회 오류:{}", e.getMessage(), e);
            result = new DatatablesResponse<>();

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(Const.CODE_SERVER_ERROR +" Error: Get Coupon List");
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }
}