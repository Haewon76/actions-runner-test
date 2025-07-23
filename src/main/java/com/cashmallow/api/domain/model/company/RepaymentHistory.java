package com.cashmallow.api.domain.model.company;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RepaymentHistory {

    private Long id;
    private String currency;
    private BigDecimal amount;
    private BigDecimal fee;
    private Long travelerId;
    private Long managerId;

    public static RepaymentHistory of(String currency, BigDecimal amount, BigDecimal fee, Long travelerId, Long managerId) {
        RepaymentHistory returnValue = new RepaymentHistory();

        returnValue.setCurrency(currency);
        returnValue.setAmount(amount);
        returnValue.setFee(fee);
        returnValue.setTravelerId(travelerId);
        returnValue.setManagerId(managerId);

        return returnValue;
    }
}
