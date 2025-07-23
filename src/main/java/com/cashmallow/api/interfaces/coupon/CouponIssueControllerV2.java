package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.coupon.entity.CouponUser;
import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.cashmallow.api.domain.model.coupon.entity.CouponIssue;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.admin.dto.DatatablesResponse;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponSearchRequest;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponIssueCreateRequest;
import com.cashmallow.api.interfaces.coupon.dto.res.CouponIssueReadResponse;
import com.cashmallow.common.HeaderUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;

@Slf4j
@RestController
@RequestMapping(value = "/admin/coupons")
@RequiredArgsConstructor
public class CouponIssueControllerV2 {

    private final AuthService authService;
    private final CouponIssueServiceV2 couponIssueService;
    private final UserRepositoryService userRepositoryService;
    private final CouponUserService couponUserService;
    private final CouponJobPlanService couponJobPlanService;
    private final CouponServiceV2 couponService;

    @GetMapping("/issues")
    public ResponseEntity<DatatablesResponse<CouponIssueReadResponse>> getCouponIssueList(@RequestHeader(name = "Authorization") String token
                                                                         , @RequestParam String fromCountryCode
                                                                         , @RequestParam(required = false) String couponCodeName
                                                                         , @RequestParam String searchStartDate
                                                                         , @RequestParam String searchEndDate
                                                                         , @RequestParam(required = false) String sortColumnCode
                                                                         , @RequestParam(required = false) String sortColumnOrder
                                                                         , @RequestParam String isSystem
                                                                         , @RequestParam String isActive
                                                                         , @RequestParam(defaultValue = "1") int page
                                                                         , @RequestParam(defaultValue = "20") int size
            , HttpServletResponse response) {

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);
            DatatablesResponse<CouponIssueReadResponse> failResponse = new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN,  new ArrayList<>());
            failResponse.setData(Collections.emptyList());

            return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(failResponse);
        }

        CouponSearchRequest couponSearchRequest = CouponSearchRequest.of(fromCountryCode, couponCodeName
                , searchStartDate, searchEndDate, sortColumnCode, sortColumnOrder, isSystem, isActive, page, size);
        log.info("couponSearchRequest={}", couponSearchRequest.toString());


        Long totalCount = couponIssueService.getCouponTotalCount(couponSearchRequest);
        List<CouponIssueReadResponse> couponList = couponIssueService.getCouponIssueList(couponSearchRequest);

        // 통화로 order by 할 때는 갯수로 order by 하므로 따로 처리
        if ("apply_currency".equals(couponSearchRequest.sortColumnCode()) && "ASC".equals(sortColumnOrder)) {
            // 오름차순
            couponList.stream().sorted(Comparator.comparing(CouponIssueReadResponse::applyCurrencyCount));
        } else if ("apply_currency".equals(couponSearchRequest.sortColumnCode()) && "DESC".equals(sortColumnOrder)) {
            // 내림차순
            couponList.stream().sorted(Comparator.comparing(CouponIssueReadResponse::applyCurrencyCount).reversed());
        }

        log.info("couponList={}", couponList.toString());

        String message = "";
        if(totalCount==0) {
            message = "검색 결과가 없습니다.";
        }

        DatatablesResponse<CouponIssueReadResponse> result
                = new DatatablesResponse<>(response.getStatus(), Const.STATUS_SUCCESS, message, new ArrayList<>(couponList));
        result.setDraw(page);
        result.setRecordsTotal(totalCount);
        result.setRecordsFiltered(couponSearchRequest.offset());

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    @PostMapping("/issue")
    public ResponseEntity<DatatablesResponse<CouponIssue>> createCouponIssue(@RequestHeader(name = "Authorization") String token,
                                                    @RequestBody String requestBody) throws JsonProcessingException {
        Long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);
            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        int existed = userRepositoryService.getUserRoleByUserId(managerId, ROLE_SUPERMAN);
        if (existed < 1) {
            log.warn("createCouponIssue: 해당 작업은 총관리자만 수행 가능합니다. {} managerId={}", ACCESS_DENIED, managerId);
            return ResponseEntity.status(Integer.parseInt(Const.CODE_INVALID_TOKEN)).body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, "해당 작업은 총관리자만 수행 가능합니다."));
        }

        ObjectMapper objectMapper = new ObjectMapper();
        CouponIssueCreateRequest couponIssueCreateRequest = objectMapper.readValue(requestBody, CouponIssueCreateRequest.class);
        couponIssueCreateRequest = couponIssueCreateRequest.withCreatedId(managerId);

        DatatablesResponse<CouponIssue> result = new DatatablesResponse<>();

        try {

            // 인플루언서 쿠폰일 경우, admin 에서 인플루언서에게만 최초 발급함
            Coupon coupon = couponService.getCouponById(couponIssueCreateRequest.couponId());
            if (coupon.getCouponCode().startsWith(SystemCouponType.influencer.getAbbreviation())) {
                CouponUser influenceCouponUser = couponUserService.getFirstIssuedUserCouponByCouponId(coupon.getId());

                // 최초에 발급 받은 사람이므로 targetUserId 로 가져옴
                if (influenceCouponUser != null) {
                    log.error("influenceCouponUserId={}", influenceCouponUser.getTargetUserId());
                    throw new CashmallowException("이미 인플루언서에게 발급되었습니다. 인플루언서 쿠폰은 admin 에서 해당 인플루언서에게만 최초 발급 가능합니다. userId="+influenceCouponUser.getTargetUserId());
                }
            }

            // 유저 쿠폰 발급
            CouponIssue createdCouponIssue = couponIssueService.createCouponIssue(couponIssueCreateRequest);

            // 예약 발급일 때 Job Plan 생성
            if (SendType.RESERVATION.getCode().equals(createdCouponIssue.getSendType())) {
                String cronExpress = couponJobPlanService.getCronExpression(createdCouponIssue.getIssueDate());
                // Job Plan 등록: 쿠폰 발급 저장 트랜잭션이 끝난 후에 실행되어야 하므로 여기서 실행
                couponJobPlanService.insertJobPlan(couponIssueCreateRequest.fromCountryCode(), createdCouponIssue.getJobKey(), cronExpress);
            }

            List<CouponIssue> savedCouponIssue = new ArrayList<>();
            savedCouponIssue.add(createdCouponIssue);

            result.setSuccess(savedCouponIssue, "쿠폰이 발급되었습니다.");

        } catch (CashmallowException e) {
            result.setStatus(Integer.parseInt(Const.CODE_FAILURE));
            result.setCode(Const.STATUS_FAILURE);
            result.setMessage(e.getMessage());

        } catch (Exception e) {
            log.error("createCouponIssue(): 쿠폰 발급 오류:{}", e.getMessage(), e);

            result.setStatus(Integer.parseInt(Const.CODE_SERVER_ERROR));
            result.setCode(Const.STATUS_SERVER_ERROR);
            result.setMessage(e.getMessage());
        }

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }

    /**
     * 쿠폰 발급 ID 에 해당하는 쿠폰 유저 목록 조회
     **/
    @GetMapping(value = "/issues/{couponIssueId}/users")
    public ResponseEntity<DatatablesResponse<CouponIssueUser>> getUsersByCouponIssueId(@RequestHeader(name = "Authorization") String token
                                                                                      , @PathVariable Long couponIssueId
                                                                                      , @RequestParam(required = false) String sortColumnCode
                                                                                      , @RequestParam(required = false) String sortColumnOrder
                                                                                      , HttpServletResponse response) throws CashmallowException {

        long managerId = authService.getUserId(token);
        if (managerId == Const.NO_USER_ID) {
            log.info("invalid token. token={}, userId={}", token, managerId);
            return ResponseEntity.ok().body(new DatatablesResponse<>(Integer.parseInt(Const.CODE_INVALID_TOKEN)
                    , Const.CODE_INVALID_TOKEN, Const.MSG_INVALID_TOKEN));
        }

        List<CouponIssueUser> couponIssueUserList = couponIssueService.getUsersByCouponIssueId(couponIssueId, sortColumnCode, sortColumnOrder);

        DatatablesResponse<CouponIssueUser> result
                = new DatatablesResponse<>(response.getStatus(), Const.STATUS_SUCCESS, "", new ArrayList<>(couponIssueUserList));

        return ResponseEntity.ok().headers(HeaderUtil.setHeader(token)).body(result);
    }
}
