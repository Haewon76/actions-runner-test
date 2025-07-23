package com.cashmallow.api.domain.model.customercenter;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

/**
 * Domain model for Notice Content
 *
 * @author swshin
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoticeContent {

    private Long id;
    private String languageType;
    private String title;
    private String content;
    private Long modifier;
    private Timestamp modifiedDate;

    private String beginDate;
    private String endDate;
    private Boolean isPopup;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLanguageType() {
        return languageType;
    }

    public void setLanguageType(String languageType) {
        this.languageType = languageType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

}
