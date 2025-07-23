package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DbsBalanceResponse {
    private List<BalanceData> balanceList;

    @Data
    public static class BalanceData {
        private String currency;
        private BigDecimal balance;
    }
}
