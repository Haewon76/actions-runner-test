package com.cashmallow.api.infrastructure.aml.dto;

import com.cashmallow.api.domain.model.aml.AMLCustomerBase;
import com.cashmallow.api.domain.model.aml.AMLCustomerIndv;
import com.cashmallow.api.domain.model.aml.AMLCustomerIndvCDD;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
public class OctaAMLCustomerRequest {

    /**
     * AML_고객_기본
     */
    @JsonProperty("amlCustomerBase")
    private AMLCustomerBase amlCustomerBase;

    /**
     * AML_고객_개인
     */
    @JsonProperty("amlCustomerIndv")
    private AMLCustomerIndv amlCustomerIndv;

    /**
     * AML_고객_개인_CDD
     */
    @JsonProperty("amlCustomerIndvCDD")
    private AMLCustomerIndvCDD amlCustomerIndvCDD;

    public OctaAMLCustomerRequest(AMLCustomerBase amlCustomerBase, AMLCustomerIndv amlCustomerIndv, AMLCustomerIndvCDD amlCustomerIndvCDD) {
        this.amlCustomerBase = amlCustomerBase;
        this.amlCustomerIndv = amlCustomerIndv;
        this.amlCustomerIndvCDD = amlCustomerIndvCDD;
    }
}
