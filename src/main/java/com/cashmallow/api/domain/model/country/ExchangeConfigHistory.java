package com.cashmallow.api.domain.model.country;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
public class ExchangeConfigHistory extends ExchangeConfig {
    private Long exchangeConfigId;
    private Timestamp createdDate;
    private Long userId;
    private String ip;
}
