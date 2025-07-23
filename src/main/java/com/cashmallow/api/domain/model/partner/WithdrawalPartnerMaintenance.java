package com.cashmallow.api.domain.model.partner;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class WithdrawalPartnerMaintenance {
    private long id;
    private long withdrawalPartnerId;
    private ZonedDateTime startAt;
    private ZonedDateTime endAt;
    private ZonedDateTime createAt;
}
