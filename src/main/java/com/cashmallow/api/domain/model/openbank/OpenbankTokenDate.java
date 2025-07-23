package com.cashmallow.api.domain.model.openbank;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class OpenbankTokenDate {
    private final ZonedDateTime startDate;
    private final ZonedDateTime endDate;
}
