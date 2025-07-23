package com.cashmallow.api.interfaces.statistics.dto;

import com.cashmallow.api.domain.model.statistics.MoneyTransferStatistics;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class MonthlyMoneyTransferDashBoardVO {
    private String currency;
    private List<MoneyTransferStatistics> moneyTransferStatisticsList = new ArrayList<>();
    private MoneyTransferStatistics allMoneyTransferStatistics;
    private MoneyTransferStatistics dailyMoneyTransferStatistics;
    private List<String> labelList = new ArrayList<>();
    private List<BigDecimal> fromAmtList = new ArrayList<>();
    private List<BigDecimal> fromOriAmtList = new ArrayList<>();
    private List<BigDecimal> feeList = new ArrayList<>();
    private List<Integer> cntList = new ArrayList<>();
}
