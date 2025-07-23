package com.cashmallow.api.infrastructure.aml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OctaAMLKYCResponse {
    private String result;
    private String message;
}
