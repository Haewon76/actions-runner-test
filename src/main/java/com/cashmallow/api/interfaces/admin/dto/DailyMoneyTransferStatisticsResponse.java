package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.interfaces.statistics.dto.DailyMoneyTransferStatisticsVO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DailyMoneyTransferStatisticsResponse {

    private List<DailyMoneyTransferStatisticsVO> result;
    private DailyMoneyTransferStatisticsVO all;
    private List<String> currencyList;
}
