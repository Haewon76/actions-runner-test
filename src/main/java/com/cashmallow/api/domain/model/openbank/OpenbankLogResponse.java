package com.cashmallow.api.domain.model.openbank;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OpenbankLogResponse {
    @NotNull
    private final long id;
    private final String responseCode;
    private final String responseApiTranId;
    private final String responseData;
    private final String responseRspCode;
    private final String responseRspMessage;
}
