package com.cashmallow.api.domain.model.remittance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.cloud.Timestamp;

import java.math.BigDecimal;

/**
 * Domain model for Remittance
 *
 * @author bongseok
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemittanceSentbe {

    private String sentbeRefId;
    private Long remitId;
    private String receiverBankCode;
    private String quotationId;
    private BigDecimal exchangeRate;
    private String exchangeSource;
    private String exchangeDestination;
    private BigDecimal calculatedFeeAmount;
    private String calculatedFeeCurrency;
    private BigDecimal sourceAmount;
    private String sourceCurrency;
    private BigDecimal destinationAmount;
    private String destinationCurrency;
    private String sentbeRemitId;
    private String status;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public String getSentbeRefId() {
        return sentbeRefId;
    }

    public void setSentbeRefId(String sentbeRefId) {
        this.sentbeRefId = sentbeRefId;
    }

    public Long getRemitId() {
        return remitId;
    }

    public void setRemitId(Long remitId) {
        this.remitId = remitId;
    }

    public String getReceiverBankCode() {
        return receiverBankCode;
    }

    public void setReceiverBankCode(String receiverBankCode) {
        this.receiverBankCode = receiverBankCode;
    }

    public String getQuotationId() {
        return quotationId;
    }

    public void setQuotationId(String quotationId) {
        this.quotationId = quotationId;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(BigDecimal exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getExchangeSource() {
        return exchangeSource;
    }

    public void setExchangeSource(String exchangeSource) {
        this.exchangeSource = exchangeSource;
    }

    public String getExchangeDestination() {
        return exchangeDestination;
    }

    public void setExchangeDestination(String exchangeDestination) {
        this.exchangeDestination = exchangeDestination;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(BigDecimal sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public BigDecimal getDestinationAmount() {
        return destinationAmount;
    }

    public void setDestinationAmount(BigDecimal destinationAmount) {
        this.destinationAmount = destinationAmount;
    }

    public String getDestinationCurrency() {
        return destinationCurrency;
    }

    public void setDestinationCurrency(String destinationCurrency) {
        this.destinationCurrency = destinationCurrency;
    }

    public String getSentbeRemitId() {
        return sentbeRemitId;
    }

    public void setSentbeRemitId(String sentbeRemitId) {
        this.sentbeRemitId = sentbeRemitId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Timestamp updatedDate) {
        this.updatedDate = updatedDate;
    }

    public BigDecimal getCalculatedFeeAmount() {
        return calculatedFeeAmount;
    }

    public void setCalculatedFeeAmount(BigDecimal calculatedFeeAmount) {
        this.calculatedFeeAmount = calculatedFeeAmount;
    }

    public String getCalculatedFeeCurrency() {
        return calculatedFeeCurrency;
    }

    public void setCalculatedFeeCurrency(String calculatedFeeCurrency) {
        this.calculatedFeeCurrency = calculatedFeeCurrency;
    }

}
