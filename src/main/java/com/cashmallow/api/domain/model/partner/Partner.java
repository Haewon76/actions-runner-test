package com.cashmallow.api.domain.model.partner;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Partner {

    public enum TypeOfSupport {
        /**
         * The stores in a city
         */
        W,
        /**
         * The stores in an airport
         */
        F
    }

    private Long id;
    private String typeOfSupport;
    private String partnerName;
    private String businessNo;
    private String officeAddress;
    private String contractEmail;
    private String contractCallNumber;
    private String activated;
    private Timestamp termOfContractStart;
    private Timestamp termOfContractEnd;
    private Timestamp createdDate;
    private Timestamp updatedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeOfSupport() {
        return typeOfSupport;
    }

    public void setTypeOfSupport(String typeOfSupport) {
        this.typeOfSupport = typeOfSupport;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public String getOfficeAddress() {
        return officeAddress;
    }

    public void setOfficeAddress(String officeAddress) {
        this.officeAddress = officeAddress;
    }

    public String getContractEmail() {
        return contractEmail;
    }

    public void setContractEmail(String contractEmail) {
        this.contractEmail = contractEmail;
    }

    public String getContractCallNumber() {
        return contractCallNumber;
    }

    public void setContractCallNumber(String contractCallNumber) {
        this.contractCallNumber = contractCallNumber;
    }

    public String getActivated() {
        return activated;
    }

    public void setActivated(String activated) {
        this.activated = activated;
    }

    public Timestamp getTermOfContractStart() {
        return termOfContractStart;
    }

    public void setTermOfContractStart(Timestamp termOfContractStart) {
        this.termOfContractStart = termOfContractStart;
    }

    public Timestamp getTermOfContractEnd() {
        return termOfContractEnd;
    }

    public void setTermOfContractEnd(Timestamp termOfContractEnd) {
        this.termOfContractEnd = termOfContractEnd;
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

}
