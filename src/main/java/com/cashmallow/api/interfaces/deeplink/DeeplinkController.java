package com.cashmallow.api.interfaces.deeplink;

import com.cashmallow.api.application.impl.DeeplinkService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.CouponUserInviteCode;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.deeplink.DeeplinkDto;
import com.cashmallow.api.domain.model.deeplink.DeeplinkStat;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.coupon.CouponServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserInviteCodeService;
import com.cashmallow.api.interfaces.coupon.dto.req.CouponUserInviteCodeRequest;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.cashmallow.common.CommDateTime.*;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/deeplink")
public class DeeplinkController {

    private final CouponUserInviteCodeService couponUserInviteCodeService;
    private final DeeplinkService deeplinkService;
    private final AuthService authService;
    private final Gson gson;
    private final EnvUtil envUtil;
    private final CouponServiceV2 couponServiceV2;
    @Value("${host.cdn.url}")
    private String cdnUrl;
    @Value("${deeplink.defaultThumbnail}")
    private String defaultThumbnail;


    // http://localhost:10000/api/deeplink?utm_source=Cashmallow&utm_medium=QR
    @GetMapping
    public String click(@RequestParam(value = "utm_medium", required = false, defaultValue = "unknown") String utmMedium
            , @RequestParam(value = "utm_source", required = false, defaultValue = "unknown") String utmSource
            , final HttpServletRequest request) throws UnsupportedEncodingException {
        return "redirect:" + deeplinkService.click(new DeeplinkDto().setUtmMedium(utmMedium).setUtmSource(utmSource).setThumbnail(defaultThumbnail), request);
    }

    // http://localhost:10000/api/deeplink/coupon/thankYouToo
    // http://localhost:10000/api/deeplink/coupon/FRKJDWKP
    // https://ldev.cashmallow.com/thankYouToo (DEV)
    // https://ldev.cashmallow.com/FRKJDWKP (DEV)
    // https://l.cashmallow.com/thankYouToo (PRD)
    @GetMapping("/coupon/{code}")
    public String coupon(final Model model,
                         @PathVariable String code,
                         final HttpServletRequest request) throws UnsupportedEncodingException {
        final DeeplinkDto deeplinkDtoCoupon = new DeeplinkDto()
                .setUtmMedium(code)
                .setUtmSource("COUPON")
                .setThumbnail(defaultThumbnail);

        if (code.startsWith(SystemCouponType.thankYouMyFriend.getAbbreviation())) {
            final CouponUserInviteCode couponUserInviteCode = couponUserInviteCodeService.getCouponUserInviteCode(CouponUserInviteCodeRequest.inviteCodeRequest(code));
            if (couponUserInviteCode != null) {
                code = SystemCouponType.thankYouMyFriend.name();
            }
        }

        // final Optional<CouponIssue> couponByCode = couponIssueService.getCouponByCode(code);
        // if (couponByCode.isPresent()) {
        //     // 쿠폰에 해당하는 썸네일과 설명을 세팅한다
        //     coupon.setThumbnail(cdnUrl + "/coupon/thumbnail/" + couponByCode.get().getThumbnail());
        //     String couponThumbnailUrl = envUtil.getCouponThumbnailUrl(couponByCode.get().getThumbnail());
        //     coupon.setThumbnail(couponThumbnailUrl);
        //     if (StringUtils.isEmpty(couponByCode.get().getThumbnail())) {
        //         coupon.setThumbnail(defaultThumbnail);
        //     }
        //
        //     model.addAttribute("thumbnail", cdnUrl + "/coupon/thumbnail/" + couponByCode.get().getThumbnail());
        //     model.addAttribute("thumbnail", couponThumbnailUrl);
        //     model.addAttribute("description", couponByCode.get().getDescription());
        //     model.addAttribute("redirectUrl", deeplinkService.click(coupon, request));
        //
        //     return "coupon/coupon";
        // }

        // TODO: 안 쓰는 파일 지우면서 오류 안나도록 수정함. 추후 deeplink 작업 시작 시, 추가 작업 필요. (현재 동작에는 이상 없음)
        String fromCountryCode = null;
        if (code.endsWith(CountryCode.HK.name())) {
            fromCountryCode = CountryCode.HK.getCode();
        } else if (code.endsWith(CountryCode.JP.name())) {
            fromCountryCode = CountryCode.JP.getCode();
        }

        Coupon coupon = couponServiceV2.getCouponByCouponCode(fromCountryCode, Const.Y, Const.N, code);

        if (coupon != null) {
            deeplinkDtoCoupon.setThumbnail(defaultThumbnail);

            model.addAttribute("thumbnail", deeplinkDtoCoupon.getThumbnail());
            model.addAttribute("description", coupon.getCouponDescription());
            model.addAttribute("redirectUrl", deeplinkService.click(deeplinkDtoCoupon, request));

            return "coupon/coupon";
        }


        // 쿠폰이 없으면, 캐시멜로 홈페이지로 이동
        return "redirect:https://www.cashmallow.com";
    }

    @PostMapping(value = "/install")
    @ResponseBody
    public String install(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                          // @RequestBody DeeplinkDto body,
                          @RequestBody String requestBody, final HttpServletResponse response, final HttpServletRequest request) {

        // String requestBody = CryptAES.encode(token, gson.toJson(body));

        if (!authService.isHexaStr(token)) {
            log.info("Invalid token");
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        deeplinkService.install(CustomStringUtil.decode(token, requestBody), request);

        return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN).setSuccessInfo(), response);
    }

    @PostMapping(value = {"/register", "/login"})
    @ResponseBody
    public String registerAndLogin(@RequestHeader(HttpHeaders.AUTHORIZATION) String token,
                                   // @RequestBody DeeplinkDto body,
                                   @RequestBody String requestBody, final HttpServletResponse response, final HttpServletRequest request) {

        // String requestBody = CryptAES.encode(token, gson.toJson(body));

        Long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID) {
            ApiResultVO voResult = new ApiResultVO();
            log.info("Invalid token. userId={}", userId);
            voResult.setFailInfo(Const.MSG_INVALID_USER_ID);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        DeeplinkDto deeplink = gson.fromJson(CustomStringUtil.decode(token, requestBody), DeeplinkDto.class);
        deeplink.setUserId(userId);
        // 로그인(login) 엔드포인트 호출 시 로그인 처리
        if (request.getRequestURI().contains("login")) {
            deeplinkService.login(deeplink, request);
        } else {
            // 가입(register) 엔드포인트 호출 시 로그인 처리
            deeplinkService.register(deeplink, request);
        }

        return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN).setSuccessInfo(), response);
    }

    // http://localhost:10000/api/deeplink/stat?startDate=20230101&endDate=20230606
    @GetMapping(value = "/stat")
    @ResponseBody
    public List<DeeplinkStat> stat(final HttpServletRequest request, @RequestParam(required = false, defaultValue = "20230501") String startDate, @RequestParam(required = false, defaultValue = "20230531") String endDate) {
        final Date sDate = getStartDate(startDate);
        final Date eDate = getEndDate(endDate);
        final List<DeeplinkStat> deeplinkStat = deeplinkService.getDeeplinkStat(dateToStringUTC(sDate), dateToStringUTC(eDate), request);
        return deeplinkStat.stream().peek(m -> {
            m.setStartDate(dateToString(sDate));
            m.setEndDate(dateToString(eDate));
        }).collect(Collectors.toList());

    }
}
