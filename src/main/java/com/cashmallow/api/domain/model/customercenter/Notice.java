package com.cashmallow.api.domain.model.customercenter;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for Notice
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Notice {

    private Long id;
    private String beginDate;
    private String endDate;
    private Boolean isPopup;
    private Long modifier;
    private Timestamp modifiedDate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(String beginDate) {
        this.beginDate = beginDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public Boolean getIsPopup() {
        return isPopup;
    }

    public void setIsPopup(Boolean isPopup) {
        this.isPopup = isPopup;
    }

    public Long getModifier() {
        return modifier;
    }

    public void setModifier(Long modifier) {
        this.modifier = modifier;
    }

    public Timestamp getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Timestamp modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
