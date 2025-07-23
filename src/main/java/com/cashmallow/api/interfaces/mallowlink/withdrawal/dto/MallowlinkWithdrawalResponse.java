package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;

import java.time.ZonedDateTime;
import java.util.List;

public record MallowlinkWithdrawalResponse(
        int agencyId,
        ConfirmType confirmType,
        List<Credential> credentials,

        String qrCode,
        ZonedDateTime expireTime
) {

    public static MallowlinkWithdrawalResponse of(int agencyId, MallowlinkWithdrawalResponse res) {
        return new MallowlinkWithdrawalResponse(agencyId, res.confirmType, res.credentials, res.qrCode, res.expireTime);
    }
}
