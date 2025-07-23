package com.cashmallow.api.interfaces.scb.facade;

import lombok.Data;

import java.time.LocalDateTime;

@Data
class TimeoutDto {
    private final LocalDateTime dateTime;
    private final String withdrawalRequestNo;
}