package com.cashmallow.api.domain.model.inactiveuser;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for InactiveStorekeeper
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InactiveWithdrawalPartner {

    private Long id;
    private Long userId;
    private String businessNo;
    private String businessPhoto;
    private String accountNo;
    private String accountName;
    private String shopContactNumber;
    private Timestamp createdDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getBusinessNo() {
        return businessNo;
    }

    public void setBusinessNo(String businessNo) {
        this.businessNo = businessNo;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getShopContactNumber() {
        return shopContactNumber;
    }

    public void setShopContactNumber(String shopContactNumber) {
        this.shopContactNumber = shopContactNumber;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public String getBusinessPhoto() {
        return businessPhoto;
    }

    public void setBusinessPhoto(String businessPhoto) {
        this.businessPhoto = businessPhoto;
    }

}
