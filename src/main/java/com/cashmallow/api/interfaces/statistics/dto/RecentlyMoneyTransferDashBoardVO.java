package com.cashmallow.api.interfaces.statistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RecentlyMoneyTransferDashBoardVO {
    private String label;
    private BigDecimal fromAmt;
    private BigDecimal fromOriAmt;
    private BigDecimal fee;
}
