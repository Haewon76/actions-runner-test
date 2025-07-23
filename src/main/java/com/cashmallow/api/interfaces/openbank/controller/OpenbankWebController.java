package com.cashmallow.api.interfaces.openbank.controller;

import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.openbank.dto.ClientInfoVO;
import com.cashmallow.api.interfaces.openbank.dto.client.OpenbankTokenResponse;
import com.cashmallow.api.interfaces.openbank.service.OpenbankServiceImpl;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_OPENBANK;


@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping
public class OpenbankWebController {

    private final OpenbankServiceImpl openBankService;
    private final RedisService redisService;
    private final Gson gson;
    private final MessageSource messageSource;

    // private final String openbankRedisProperty = "openbankRedisProperty";

    // 금융결제원 사용자 인증 성공 시(ARS 인증완료 시) 토큰발급
    // @ResponseBody

    // todo 표준 규격으로 변경

    /**
     * 금융결제원 리다이렉션이 들어오는 엔드포인트 (인바운드)
     *
     * @param code
     * @param scope
     * @param paramState
     * @param clientInfoStr
     * @param error
     * @param errorMsg
     * @param request
     * @param response
     * @return
     */
    @GetMapping(value = "/login/oauth2/code/kftc")
    public String authSuccess(Model model,
                              @RequestParam(value = "code", required = false) String code,
                              @RequestParam(value = "scope", required = false) String scope,
                              @RequestParam(value = "state", required = false) String paramState,
                              @RequestParam(value = "client_info", required = false) String clientInfoStr,
                              @RequestParam(value = "error", required = false) String error,
                              @RequestParam(value = "error_description", required = false) String errorMsg,
                              HttpServletRequest request, HttpServletResponse response) {
        try {
            String decode = new String(Base64.getUrlDecoder().decode(clientInfoStr.getBytes(StandardCharsets.UTF_8)));
            ClientInfoVO clientInfo = gson.fromJson(decode, ClientInfoVO.class);

            log.info("clientInfo:{}", clientInfo);
            long travelerId = clientInfo.getTravelerId();
            boolean isAndroid = clientInfo.getDeviceType().equalsIgnoreCase("AD");
            model.addAttribute("isAndroid", isAndroid);
            model.addAttribute("isSuccess", false);

            if (request.getParameter("error") != null) {
                log.info("error={}, errorMsg={}", error, errorMsg);
                if ("access_denied".equals(errorMsg)) {
                    log.info("error={}", "Customer clicked cancel!");
                }
                model.addAttribute("isSuccess", false);
                return "openbank/result";
            }

            log.info("request={}", request.toString());
            log.info("code={}, state={}, clientInfo={}", code, paramState, clientInfo);
            // String paramString = URLDecoder.decode(clientInfo, "UTF-8");

            // state 체크
            String redisKey = redisService.generateRedisKey("travelerId", Long.toString(travelerId), REDIS_KEY_OPENBANK);
            String state = redisService.get(redisKey);
            log.info("get state={} redisKey={}", state, redisKey);
            if (!StringUtils.equals(state, paramState)) {
                log.error("state만료: state 값이 일치하지 않습니다!!");
                model.addAttribute("isSuccess", false);
                return "openbank/result";
            }

            // 토큰 발급
            OpenbankTokenResponse openbankTokenResponse = openBankService.issueOpenBankToken(code, travelerId);
            log.info("travelerId:{}, deviceType:{}, 오픈뱅크 토큰 발급 완료", clientInfo.getTravelerId(), clientInfo.getDeviceType());

            // 계좌 정보 획득 및 업데이트
            openBankService.updateOpenbankUserInfo(travelerId, openbankTokenResponse.getAccessToken(), openbankTokenResponse.getUserSeqNo());
            log.info("travelerId:{}, deviceType:{}, 오픈뱅크 계좌 정보 업데이트 완료", clientInfo.getTravelerId(), clientInfo.getDeviceType());

            model.addAttribute("isSuccess", true);
        } catch (Exception e) {
            log.error("authSuccess: " + e.getMessage(), e);
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("isSuccess", false);
        }

        return "openbank/result";
    }
}
