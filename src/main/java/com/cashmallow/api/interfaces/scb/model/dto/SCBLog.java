package com.cashmallow.api.interfaces.scb.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SCBLog {
    private String transactionId;
    private String requestJson;
    private String responseJson;
    private LogType requestType;
    private int code;
    private LocalDateTime withdrawalRequestTime;

}
