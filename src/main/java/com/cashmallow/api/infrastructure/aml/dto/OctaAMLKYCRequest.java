package com.cashmallow.api.infrastructure.aml.dto;

import com.cashmallow.api.domain.model.aml.AmlAccountBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranBase;
import com.cashmallow.api.domain.model.aml.AmlAccountTranSendReceipt;
import com.cashmallow.api.domain.model.aml.AmlProdBase;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OctaAMLKYCRequest {

    @JsonProperty("amlAccountBase")
    private AmlAccountBase amlAccountBase;

    @JsonProperty("amlProdBase")
    private AmlProdBase amlProdBase;

    @JsonProperty("amlAccountTranBase")
    private AmlAccountTranBase amlAccountTranBase;

    @JsonProperty("amlAccountTranSendReceipt")
    private AmlAccountTranSendReceipt amlAccountTranSendReceipt;


    public OctaAMLKYCRequest(AmlAccountBase amlAccountBase, AmlProdBase amlProdBase, AmlAccountTranBase amlAccountTranBase, AmlAccountTranSendReceipt amlAccountTranSendReceipt) {
        this.amlAccountBase = amlAccountBase;
        this.amlProdBase = amlProdBase;
        this.amlAccountTranBase = amlAccountTranBase;
        this.amlAccountTranSendReceipt = amlAccountTranSendReceipt;
    }
}
