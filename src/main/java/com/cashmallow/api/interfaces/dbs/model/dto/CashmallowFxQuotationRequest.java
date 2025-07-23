package com.cashmallow.api.interfaces.dbs.model.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@NoArgsConstructor
public class CashmallowFxQuotationRequest {

    private String transactionId; // 스스로 생성하자(중복 방지를 위해 해당일자에 1개의 트랜젝션 생성)
    private String exchangeTarget = "BUY"; // BUY
    private String endUserId; // admin user id

    // private String currencyPair;  // USDHKD - currency pair - from admin
    private String fromCurrency;  // USD - from admin
    private String toCurrency;  // USD - from admin
    private BigDecimal targetAmount; // 10000 - from admin
    private String toAccount; //  - 입금계정 from admin
    private String fromAccount; //  - 출금계정 from admin

    public String getTransactionId() {
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
        return String.format("%s%s%s", df.format(new Date()), fromCurrency, toCurrency);
    }
}
