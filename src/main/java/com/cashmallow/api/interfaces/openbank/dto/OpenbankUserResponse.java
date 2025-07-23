package com.cashmallow.api.interfaces.openbank.dto;

import com.cashmallow.api.domain.model.openbank.Openbank;
import com.cashmallow.common.CustomStringUtil;
import lombok.Data;

@Data
public class OpenbankUserResponse {
    private final String signYn;
    private final String signDate;
    private final String bankCodeStd;
    private final String bankName;
    private final String bankIconPath;
    private final String accountNumMasked;
    private final String accountHolderName;
    private final String accountSignYn;
    private final String accountSignDate;
    private final String accountExpireDate;

    public static OpenbankUserResponse of(Openbank openbank, String iconPath) {
        String signYn = openbank.getSignYn();
        String signDate = openbank.getSignDateToString();
        String accountSignYn = openbank.getAccountSignYn();
        String accountSignDate = openbank.getAccountSignDateToString();
        String bankCodeStd = openbank.getBankCodeStd();
        String bankName = openbank.getBankName();
        String accountNumMasked = openbank.getAccountNumMasked();
        String accountHolderName = openbank.getAccountHolderName();
        String accountExpireDate = openbank.getAccountExpireDateToString();

        return new OpenbankUserResponse(signYn, signDate, bankCodeStd, bankName, iconPath, accountNumMasked, accountHolderName, accountSignYn, accountSignDate, accountExpireDate);
    }

    public boolean isDeleted() {
        return "N".equalsIgnoreCase(accountSignYn);
    }

    @Override
    public String toString() {
        return "OpenbankUserResponse{" +
                "signYn='" + signYn + '\'' +
                ", signDate='" + signDate + '\'' +
                ", bankCodeStd='" + bankCodeStd + '\'' +
                ", bankName='" + bankName + '\'' +
                ", bankIconPath='" + bankIconPath + '\'' +
                ", accountNumMasked='" + accountNumMasked + '\'' +
                ", accountHolderName='" + CustomStringUtil.maskingName(accountHolderName) + '\'' +
                ", accountSignYn='" + accountSignYn + '\'' +
                ", accountSignDate='" + accountSignDate + '\'' +
                '}';
    }
}