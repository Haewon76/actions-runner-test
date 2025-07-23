package com.cashmallow.api.infrastructure.aml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OctaWLFResponse {

    @JsonProperty("SUCCESS_YN")
    private String SUCCESS_YN;
    @JsonProperty("ERROR_MSG")
    private String ERROR_MSG;
    @JsonProperty("CODE")
    private String CODE;
    @JsonProperty("CODE_MSG")
    private String CODE_MSG;
    @JsonProperty("REAL")
    private String REAL;
}
