package com.cashmallow.api.interfaces.mallowlink.controller;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookRefundRequest;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookResultRequest;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookResultResponse;
import com.cashmallow.api.interfaces.mallowlink.remittance.MallowlinkRemittanceServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/mallowlink/v1")
@RequiredArgsConstructor
public class MallowlinkWebhookController {

    private final MallowlinkRemittanceServiceImpl remittanceService;
    private final MallowlinkWithdrawalServiceImpl withdrawalService;

    @PostMapping("/webhook")
    public ResponseEntity<WebhookResultResponse> webhookResult(@RequestBody WebhookResultRequest request) throws CashmallowException {

        log.info("request:{}", request);

        switch (request.type()) {
            case WITHDRAWAL -> withdrawalService.webhookResult(request);
            case REMITTANCE -> remittanceService.webhookResult(request);
        }

        return ResponseEntity.ok().body(WebhookResultResponse.success());
    }

    @PostMapping("/webhook/refund")
    public ResponseEntity<WebhookResultResponse> webhookRefundResult(@RequestBody WebhookRefundRequest request) throws CashmallowException {

        log.info("request:{}", request);

        switch (request.type()) {
            case WITHDRAWAL -> withdrawalService.webhookRefundResult(request);
            // case REMITTANCE -> remittanceService.webhookRefundResult(request);
        }

        return ResponseEntity.ok().body(WebhookResultResponse.success());
    }
}
