package com.cashmallow.api.domain.model.exchange;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ExchangeStatus {

    private long exchangeId;
    private String exchangeStatus;
    private String message;

    public ExchangeStatus(Exchange exchange) {
        this.exchangeId = exchange.getId();
        this.exchangeStatus = StringUtils.isEmpty(exchange.getExStatus()) ? "OP" : exchange.getExStatus();
        this.message = exchange.getMessage();
    }
}
