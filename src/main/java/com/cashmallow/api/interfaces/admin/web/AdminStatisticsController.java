package com.cashmallow.api.interfaces.admin.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.admin.AdminStatsServiceImpl;
import com.cashmallow.api.interfaces.admin.dto.DailyMoneyTransferStatisticsResponse;
import com.cashmallow.api.interfaces.admin.dto.MonthlyMoneyTransferDashBoardResponse;
import com.cashmallow.api.interfaces.statistics.MoneyTransferStatisticsService;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/v2")
@RequiredArgsConstructor
@Slf4j
public class AdminStatisticsController {

    private final AdminStatsServiceImpl statisticsService;

    private final MoneyTransferStatisticsService moneyTransferStatisticsService;

    private final AuthService authService;

    private final ObjectMapper objectMapper;

    private final AlarmService alarmService;

    private ObjectMapper objectMapperYYYYMM;
    private ObjectMapper objectMapperYYYYMMDD;

    @PostConstruct
    void init() {
        objectMapperYYYYMM = objectMapper.copy();
        objectMapperYYYYMM.setDateFormat(new SimpleDateFormat("yyyy-MM"));

        objectMapperYYYYMMDD = objectMapper.copy();
        objectMapperYYYYMMDD.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));
    }

    @GetMapping("/reconciliation")
    public String getReconciliation(@RequestHeader("Authorization") String token,
                                    @RequestParam("year") int year,
                                    @RequestParam("from") String from,
                                    HttpServletResponse response) {

        Long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            log.info("Invalid token. managerId={}", managerId);
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.CODE_INVALID_TOKEN), response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.MSG_NEED_AUTH), response);
        }

        ApiResultVO apiResultVO = new ApiResultVO();
        try {
            apiResultVO.setSuccessInfo(statisticsService.getReconciliation(CountryCode.of(from), year));
            return JsonStr.toJsonString(apiResultVO, response);

        } catch (CashmallowException e) {
            log.debug(e.getMessage(), e);
            alarmService.e("대사용 통계 에러", e.getMessage() + e);
            apiResultVO.setFailInfo(e.getMessage());
            return JsonStr.toJsonString(apiResultVO, response);
        }
    }

    @GetMapping("/moneyTransferStatistics")
    public String getMoneyTransferStatistics(@RequestHeader("Authorization") String token,
                                             @RequestParam int year,
                                             @RequestParam String fromCd,
                                             HttpServletResponse response) {

        String method = "getMoneyTransferStatistics()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            return CustomStringUtil.encryptJsonString(token, new ApiResultVO(Const.MSG_NEED_AUTH), response);
        }

        try {
            LocalDate startDate = LocalDate.of(year, 1, 1);
            LocalDate endDate = LocalDate.of(year, 12, 31);
            DailyMoneyTransferStatisticsResponse result = moneyTransferStatisticsService
                    .getMoneyTransferStatisticsList(fromCd, startDate, endDate);
            voResult.setSuccessInfo(objectMapperYYYYMMDD.writeValueAsString(result));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

    @GetMapping("/moneyTransferStatisticsDashboard")
    public String getMoneyTransferStatisticsDashboard(@RequestHeader("Authorization") String token,
                                                      HttpServletResponse response,
                                                      @RequestParam int year) {
        String method = "getMoneyTransferStatisticsDashboard()";

        ApiResultVO voResult = new ApiResultVO(Const.CODE_INVALID_TOKEN);

        long managerId = authService.getUserId(token);

        if (managerId == Const.NO_USER_ID) {
            log.info("{}: Invalid token. managerId={}", method, managerId);
            return CustomStringUtil.encryptJsonString(token, voResult, response);
        }

        List<String> auths = authService.getUserAuthInfo(token);
        if (!auths.contains(Const.ROLE_ASSIMAN)) {
            return JsonStr.toJsonString(new ApiResultVO(Const.MSG_NEED_AUTH), response);
        }

        try {
            LocalDate date = LocalDate.of(year, 1, 1);
            MonthlyMoneyTransferDashBoardResponse result = moneyTransferStatisticsService.getMoneyTransferStatisticsDashBoard(date);
            voResult.setSuccessInfo(objectMapperYYYYMM.writeValueAsString(result));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            voResult.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, voResult, response);
    }

}
