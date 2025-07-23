package com.cashmallow.api.interfaces.dbs.client;

import com.cashmallow.api.interfaces.dbs.model.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "dbsClient",
        url = "${dbs.url}",
        configuration = DbsFeignConfig.class
)
public interface DbsClient {

    @GetMapping("/health")
    String checkHealth();

    @PostMapping("/cashmallow/remittance")
    DbsRemittanceResponse requestRemittance(DbsRemittanceRequest request);

    @PostMapping("/cashmallow/balance/enquiry")
    DbsBalanceResponse requestBalance(DbsBalanceRequest request);

    @PostMapping("/cashmallow/exchange/rate")
    CashmallowFxQuotationResponse getDbsFxQuotation(CashmallowFxQuotationRequest request);

    @PostMapping("/cashmallow/exchange/rate/approve")
    CashmallowFxQuotationApproveResponse getDbsFxQuotationApprove(CashmallowFxQuotationApproveRequest request);

    @GetMapping("/cashmallow/exchange/progress")
    DbsFxTransactionResponse getNotCompletedFxTransaction();

    @GetMapping("/cashmallow/currencies")
    FxInfoResponse getDbsFxInfo(@RequestParam("remittanceType") String remittanceType);

}
