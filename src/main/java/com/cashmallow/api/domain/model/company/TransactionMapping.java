package com.cashmallow.api.domain.model.company;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionMapping {
    private Long transactionRecId;
    private String paygateRecId;
    private Timestamp createdDate;
    private Long creator;
    private Long travelerId;

    private String newPaygateRecId;


    public Long getTravelerId() {
        return travelerId;
    }

    public void setTravelerId(Long travelerId) {
        this.travelerId = travelerId;
    }

    public Long getTransactionRecId() {
        return transactionRecId;
    }

    public void setTransactionRecId(Long transactionRecId) {
        this.transactionRecId = transactionRecId;
    }

    public String getPaygateRecId() {
        return paygateRecId;
    }

    public void setPaygateRecId(String paygateRecId) {
        this.paygateRecId = paygateRecId;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Long getCreator() {
        return creator;
    }

    public void setCreator(Long creator) {
        this.creator = creator;
    }

    public String getNewPaygateRecId() {
        return newPaygateRecId;
    }

    public void setNewPaygateRecId(String newPaygateRecId) {
        this.newPaygateRecId = newPaygateRecId;
    }
}
