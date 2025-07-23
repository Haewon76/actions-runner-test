package com.cashmallow.api.interfaces.scb.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CashoutResponse {
    Long cashoutId;
    Integer code;
    String errorMessage;
}

