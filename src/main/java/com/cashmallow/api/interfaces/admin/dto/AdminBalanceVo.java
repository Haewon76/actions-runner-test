package com.cashmallow.api.interfaces.admin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminBalanceVo {
    public AdminBalanceVo() {
        this.HKD = new AdminBalance("HKD", BigDecimal.ZERO);
        this.USD = new AdminBalance("USD", BigDecimal.ZERO);
        this.KRW = new AdminBalance("KRW", BigDecimal.ZERO);
    }

    private AdminBalance HKD;
    private AdminBalance USD;
    private AdminBalance KRW;

    @Data
    public static class AdminBalance {
        public AdminBalance(String currency, BigDecimal amount) {
            this.currency = currency;
            this.amount = amount;
        }

        private String currency;
        private BigDecimal amount;
    }
}
