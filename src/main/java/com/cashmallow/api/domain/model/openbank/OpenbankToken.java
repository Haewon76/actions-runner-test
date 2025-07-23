package com.cashmallow.api.domain.model.openbank;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class OpenbankToken {
    private final Long travelerId;
    private final String accessToken;
    private final String refreshToken;
    private final String userSeqNo;
    private final ZonedDateTime tokenIssueDate;
    private final ZonedDateTime signDate;
}
