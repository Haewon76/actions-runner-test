package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class WalletData {
    private final String walletId;
    private final String walletCode;
    private final String walletName;
    private final String walletNameEng;

    public static WalletData of(RemittanceWalletResponse.RemittanceWallet wallet) {
        return new WalletData(wallet.walletId(), wallet.walletCode(), wallet.walletName(), wallet.walletNameEng());
    }
}
