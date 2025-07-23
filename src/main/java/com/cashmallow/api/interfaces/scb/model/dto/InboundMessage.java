package com.cashmallow.api.interfaces.scb.model.dto;

import lombok.Builder;

@Builder
public record InboundMessage(
        int code,
        String withdrawalRequestNo) {

    public static InboundMessage success(String clientTransactionId) {
        return new InboundMessage(200, clientTransactionId);
    }

    public static InboundMessage fail(String clientTransactionId) {
        return new InboundMessage(404, clientTransactionId);
    }
}
