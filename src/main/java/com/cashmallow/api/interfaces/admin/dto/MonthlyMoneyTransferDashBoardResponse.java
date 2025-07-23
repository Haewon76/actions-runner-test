package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.interfaces.statistics.dto.MonthlyMoneyTransferDashBoardVO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MonthlyMoneyTransferDashBoardResponse {
    private List<MonthlyMoneyTransferDashBoardVO> result;
}
