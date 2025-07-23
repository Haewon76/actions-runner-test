package com.cashmallow.api.interfaces.statistics.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class DailyMoneyTransferStatisticsVO {
    private Map<String, Object> exchange;
    private Map<String, Object> remittance;
    private Map<String, Object> refund;
    private Map<String, Object> all;
    private Map<String, Object> exchangeAndRemittance;
}
