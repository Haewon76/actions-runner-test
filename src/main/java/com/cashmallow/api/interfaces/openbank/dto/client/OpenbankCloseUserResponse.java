package com.cashmallow.api.interfaces.openbank.dto.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class OpenbankCloseUserResponse {
    private final String apiTranId;
    private final String apiTranDtm;
    private final String rspCode;
    private final String rspMessage;

    public boolean isSuccess() {
        return StringUtils.equals("A0000", this.rspCode);
    }

    public boolean isFail() {
        return !isSuccess();
    }
}
