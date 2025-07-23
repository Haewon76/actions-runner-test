package com.cashmallow.api.interfaces.traveler.dto;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Data
@Slf4j
public class ExchangeCalculateResponse {

    public static final int CODE_ERROR = 400;
    public static final int CODE_SUCCESS = 200;
    public static final String CODE_ERROR_MESSAGE = "환전 계산 결과를 구할 수 없습니다.";

    private ExchangeCalculate data;
    private int code;
    private String message;

    public ExchangeCalculateResponse() {
        this.code = CODE_ERROR;
        this.message = "FAIL";
    }

    public ExchangeCalculateResponse toResponse(BigDecimal toMoney) {
        final ExchangeCalculateResponse exchangeCalculate = new ExchangeCalculateResponse();

        try {
            exchangeCalculate.setData(new ExchangeCalculate(toMoney));
            exchangeCalculate.setCode(CODE_SUCCESS);
            exchangeCalculate.setMessage("SUCCESS");
            return exchangeCalculate;
        } catch (Exception e) {
            log.error("ExchangeCalculateResponse error: {}", e.getMessage(), e);
        }

        return exchangeCalculate;
    }
}
