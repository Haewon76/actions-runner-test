package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import lombok.Data;

@Data
public final class RemittanceResponse {
    private final MallowlinkRemittanceStatus status;
    private final String code;
}
