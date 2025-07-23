package com.cashmallow.api.infrastructure.aml.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;

/**
 * The OctaAMLCustomerResponse class represents the response received from the Octa AML Customer API.
 * It contains the result and message fields.
 */
@Getter
public class OctaAMLCustomerResponse {
    @JsonProperty("result")
    private String result;
    @JsonProperty("message")
    private String message;

}
