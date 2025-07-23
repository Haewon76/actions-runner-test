package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;

import java.util.List;

@Data
public final class RemittanceBankResponse {

    private List<RemittanceBank> banks;

}
