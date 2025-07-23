package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
@Data
public class CountryStatsDto {

    private final String toCd;
    private final BigDecimal totalAmount;       // 거래액 합계(환전+송금)
    private final BigDecimal overDeposits;      // 과입금
    private final BigDecimal remitFinish;       // 송금 완료액
    private final BigDecimal remitDoing;        // 송금 대기액
    private final BigDecimal cashout;           // 인출 완료액
    private final BigDecimal pendingWallets;    // 지갑 잔액
    private final BigDecimal refundRemit;       // 송금 환불액
    private final BigDecimal refundEx;          // 환전 환불액

}
