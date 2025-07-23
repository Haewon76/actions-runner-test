package com.cashmallow.api.interfaces.mallowlink.wlf.dto;

import com.cashmallow.api.infrastructure.aml.dto.OctaWLFRequest;

public record WlfRequest(
        String userId,
        String remittanceId,
        String senderName,
        String receiverName,
        String requestAdminName,
        String senderBirthDate,
        String senderCountryCd,
        String receiverBirthDate,
        String receiverCountryCd
) {
    public static WlfRequest of(OctaWLFRequest req) {
        return new WlfRequest(
                req.getUserId(),
                req.getRemittanceId(),
                req.getSenderName(),
                req.getReceiverName(),
                req.getRequestAdminName(),
                req.getSenderBirthDate(),
                req.getSenderCountryCd(),
                req.getReceiverBirthDate(),
                req.getReceiverCountryCd()
        );
    }
}