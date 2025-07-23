package com.cashmallow.api.interfaces.dbs.controller;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.domain.model.company.PaygateRecord;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.dbs.DbsService;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsDepositRecordRequest;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsRemittanceNotificationRequest;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import com.cashmallow.common.JsonStr;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.cashmallow.api.domain.shared.Const.CODE_SUCCESS;
import static com.cashmallow.api.domain.shared.Const.STATUS_SUCCESS;

@Slf4j
@RestController
@RequestMapping("/dbs")
@RequiredArgsConstructor
public class DbsController {

    private final PaygateServiceImpl paygateService;
    private final DbsService dbsService;
    private final AlarmService alarmService;
    private final CompanyServiceImpl companyService;

    // DBS 입금내역 수신. 자동매핑 처리
    @PostMapping(value = "/bank-accounts/records")
    public String saveDbsRecord(@RequestBody @Valid DbsDepositRecordRequest depositRecordRequest,
                                HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("saveDbsRecord: {}", depositRecordRequest.toString());
        ApiResultVO voResult = new ApiResultVO();
        voResult.setCode(CODE_SUCCESS);
        voResult.setStatus(STATUS_SUCCESS);
        voResult.setMessage(STATUS_SUCCESS);

        PaygateRecord paygateRecord = paygateService.addDbsNotificationRecord(depositRecordRequest);

        // 입금내역이 TT인경우 추가 수수료로 인해 자동매핑 안함
        if (!depositRecordRequest.getDepositType().equals("TT")) {
            companyService.tryAutoMappingForDbsRecord(paygateRecord);
        }

        final String message = "입금 확인" +
                "\n금액: " + paygateRecord.getAmount() + " " + paygateRecord.getIso4217() +
                "\n홍콩시간: " + paygateRecord.getExecutedDate();
        alarmService.aAlert("입금", message, null);

        return JsonStr.toJsonString(voResult, response);
    }

    @PostMapping(value = "/remittance/notification")
    public String receiveDayRemittance(@RequestBody DbsRemittanceNotificationRequest notificationRequest,
                                       HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("receiveDayRemittance: {}", notificationRequest.toString());
        ApiResultVO voResult = new ApiResultVO();
        voResult.setCode(CODE_SUCCESS);
        voResult.setStatus(STATUS_SUCCESS);
        voResult.setMessage(STATUS_SUCCESS);

        dbsService.receiveDayRemittanceResult(notificationRequest);

        return JsonStr.toJsonString(voResult, response);
    }
}
