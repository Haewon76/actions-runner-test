package com.cashmallow.api.domain.model.cashout;

import lombok.Data;

@Data
public class PendingBalanceDetailVo {

    private String id;
    private String exchangeId;
    private String travelerWalletId;
    private String fromCountry;
    private String createdDate;
    private String fromCountryKorName;
    private String fromAmount;
    private String toCurrency;
    private String toAmount;
    private String r;
    private String e;
    private String c;

    public PendingBalanceDetailVo() {
        this.id = "순서";
        this.exchangeId = "환전거래ID";
        this.travelerWalletId = "지갑ID";
        this.fromCountry = "From";
        this.toCurrency = "To";
        this.createdDate = "생성일";
        this.fromCountryKorName = "From";
        this.fromAmount = "원금";
        this.toAmount = "환전금";
        this.r = "환불중";
        this.e = "인출중";
        this.c = "환전완료";
    }

    public PendingBalanceDetailVo(int index, PendingBalanceDetailVo pendingBalanceDetailVo) {
        this.id = String.valueOf(index);
        this.exchangeId = pendingBalanceDetailVo.getExchangeId();
        this.travelerWalletId = pendingBalanceDetailVo.getTravelerWalletId();
        this.fromCountry = pendingBalanceDetailVo.getFromCountry();
        this.toCurrency = pendingBalanceDetailVo.getToCurrency();
        this.createdDate = pendingBalanceDetailVo.getCreatedDate();
        this.fromCountryKorName = pendingBalanceDetailVo.getFromCountryKorName();
        this.fromAmount = pendingBalanceDetailVo.getFromAmount();
        this.toAmount = pendingBalanceDetailVo.getToAmount();
        this.r = pendingBalanceDetailVo.getR();
        this.e = pendingBalanceDetailVo.getE();
        this.c = pendingBalanceDetailVo.getC();
    }
}
