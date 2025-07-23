package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.domain.model.remittance.Remittance;

public record JpData(String bankName) implements RemittanceExtraData {
    public static JpData of(Remittance receiver) {

        return new JpData(
                receiver.getReceiverBankName()
        );
    }
}
