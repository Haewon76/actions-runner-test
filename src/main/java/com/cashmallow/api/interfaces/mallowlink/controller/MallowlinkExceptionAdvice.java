package com.cashmallow.api.interfaces.mallowlink.controller;


import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.WebhookResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RequiredArgsConstructor
@Order(0)
@RestControllerAdvice(assignableTypes = MallowlinkWebhookController.class)
public class MallowlinkExceptionAdvice {

    private final AlarmService alarmService;

    @ExceptionHandler(CashmallowException.class)
    public ResponseEntity<WebhookResultResponse> cashmallowExceptionHandler(CashmallowException e) {
        log.error("CashmallowException webhookResult:{}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(WebhookResultResponse.of(e));
    }

    @ExceptionHandler(MallowlinkException.class)
    public ResponseEntity<WebhookResultResponse> mallowlinkExceptionHandler(MallowlinkException e) {
        log.error("MallowlinkException webhookResult:{}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(WebhookResultResponse.of(e));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebhookResultResponse> errorHandle(Exception e) {
        log.error("Exception webhookResult:{}", e.getMessage(), e);
        alarmService.i("Exception MallowlinkWebhook", e.getMessage());
        return ResponseEntity.badRequest().body(WebhookResultResponse.of(new MallowlinkException(MallowlinkExceptionType.INTERNAL_SERVER_ERROR)));
    }
}
