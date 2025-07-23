package com.cashmallow.api.interfaces.traveler.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Data
public class ExchangeCalculate {

    public ExchangeCalculate(BigDecimal toMoney) {
        DecimalFormat df = new DecimalFormat("#.####");
        this.toMoney = df.format(toMoney);
    }

    private String toMoney;

}
