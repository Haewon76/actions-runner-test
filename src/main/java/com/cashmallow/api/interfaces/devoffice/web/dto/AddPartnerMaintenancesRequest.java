package com.cashmallow.api.interfaces.devoffice.web.dto;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AddPartnerMaintenancesRequest {
    private final String kindOfStorekeeper;
    private final ZonedDateTime startAt;
    private final ZonedDateTime endAt;
}
