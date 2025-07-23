package com.cashmallow.api.interfaces.mallowlink.controller.dto;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WebhookResultResponse(
        String code,
        String message,
        String detailMessage
) {

    public static WebhookResultResponse success() {
        return new WebhookResultResponse("0000", "Success", null);
    }

    public static WebhookResultResponse of(MallowlinkException e) {
        return new WebhookResultResponse(e.getStatus().getCode(), e.getStatus().getMessage(), null);
    }

    public static WebhookResultResponse of(CashmallowException e) {
        return new WebhookResultResponse(MallowlinkExceptionType.INTERNAL_SERVER_ERROR.getCode(),
                MallowlinkExceptionType.INTERNAL_SERVER_ERROR.getMessage(),
                e.getMessage());
    }

}
