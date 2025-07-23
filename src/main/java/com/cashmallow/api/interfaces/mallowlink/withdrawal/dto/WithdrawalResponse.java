package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record WithdrawalResponse(
        Long cashoutId,
        ConfirmType confirmType,
        List<Credential> credentials,

        String qrCode,
        @JsonIgnore
        ZonedDateTime expireTime
) {

    @JsonProperty
    public String getExpireTime() {
        if (expireTime == null) {
            return null;
        }
        return expireTime.truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    public static WithdrawalResponse of(Long cashoutId,
                                        MallowlinkWithdrawalResponse response) {
        return new WithdrawalResponse(cashoutId, response.confirmType(), response.credentials(), response.qrCode(), response.expireTime());
    }


}
