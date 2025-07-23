package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import java.util.List;

public record RemittanceWalletResponse(
        List<RemittanceWallet> wallets
) {
    public record RemittanceWallet(
            String walletId,
            String walletName,
            String walletCode,
            String walletNameEng
    ) {
    }
}
