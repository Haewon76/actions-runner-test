package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class RemittanceMallowlinkStatus {
    private Long id;
    private Long remitId;
    private String transactionId;
    private MallowlinkRemittanceStatus status;
    private ZonedDateTime createAt;

    public static RemittanceMallowlinkStatus of(RemittanceMallowlink remittanceMallowlink) {
        RemittanceMallowlinkStatus remittanceMallowlinkStatus = new RemittanceMallowlinkStatus();
        remittanceMallowlinkStatus.setRemitId(remittanceMallowlink.getRemitId());
        remittanceMallowlinkStatus.setTransactionId(remittanceMallowlink.getTransactionId());
        remittanceMallowlinkStatus.setStatus(remittanceMallowlink.getStatus());
        return remittanceMallowlinkStatus;
    }

}
