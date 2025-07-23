package com.cashmallow.api.domain.model.traveler;

import java.util.List;

public interface TravelerWalletMapper {

    void insertBackupWallet(Long walletId);

    void insertRestoreWallet(Long walletId);

    void deleteBackupWallet(Long walletId);

    TravelerWallet getBackupWallet(Long walletId);

    List<TravelerWallet> getRelatedWalletsByWalletId(Long walletId);

    void insertBackupRelatedWalletsByWalletId(Long walletId);

    void insertRestoreRelatedWalletsByWalletId(Long walletId);

    void deleteBackupRelatedWalletsByWalletId(Long walletId);

    void deleteOtherRelatedWallets(Long walletId);

    /**
     * @return
     */
    List<AllWallet> getAllWallets(String fromCd);

    List<TravelerWallet> getUnpaidListForGlobal(String fromCountryCode);
}
