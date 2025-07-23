package com.cashmallow.api.domain.model.remittance;

import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkRemittanceStatus;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class RemittanceMallowlink {
    private Long remitId;
    private String transactionId;
    private String endUserId;
    private MallowlinkRemittanceStatus status;
    private ZonedDateTime createAt;
    private ZonedDateTime updateAt;

    public static RemittanceMallowlink of(Remittance remittance, String transactionId) {
        RemittanceMallowlink remittanceMallowlink = new RemittanceMallowlink();
        remittanceMallowlink.setRemitId(remittance.getId());
        remittanceMallowlink.setTransactionId(transactionId);
        remittanceMallowlink.setEndUserId(remittance.getTravelerId().toString());
        remittanceMallowlink.setStatus(MallowlinkRemittanceStatus.REQUEST);

        return remittanceMallowlink;
    }
}
