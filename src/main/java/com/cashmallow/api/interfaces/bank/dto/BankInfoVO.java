package com.cashmallow.api.interfaces.bank.dto;


import com.cashmallow.api.domain.model.bankinfo.BankInfo;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BankInfoVO {
    private final long bankInfoId;
    private final String iso3166;
    private final String code;
    private final String name;
    private final String iconPath;
    private final String bniClearingCode;

    public BankInfoVO(long bankInfoId, String iso3166, String code, String name, String iconPath, String bniClearingCode) {
        this.bankInfoId = bankInfoId;
        this.iso3166 = iso3166;
        this.code = code;
        this.name = name;
        this.iconPath = iconPath;
        this.bniClearingCode = bniClearingCode;
    }

    public BankInfoVO(BankInfoVO other) {
        this.bankInfoId = other.bankInfoId;
        this.iso3166 = other.iso3166;
        this.code = other.code;
        this.name = other.name;
        this.iconPath = other.iconPath;
        this.bniClearingCode = other.bniClearingCode;
    }

    public static BankInfoVO of(BankInfo bankInfo, String cdnUrl) {
        return new BankInfoVO(
                bankInfo.getId(),
                bankInfo.getIso3166(),
                bankInfo.getCode(),
                bankInfo.getName(),
                cdnUrl + "/static" + bankInfo.getIconPath(),
                bankInfo.getBniClearingCode());
    }
}
