package com.cashmallow.api.domain.model.openbank;

import com.cashmallow.common.CustomStringUtil;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Openbank {

    private Long travelerId;
    private String accessToken;
    private String refreshToken;
    private ZonedDateTime tokenIssueDate;
    private ZonedDateTime signDate;
    private String signYn;
    private String userSeqNo;
    private String userCi;
    private String userName;
    private String fintechUseNum;
    private String bankCodeStd;
    private String bankName;
    private String accountNumMasked;
    private String accountHolderName;
    private String accountSignYn;
    private ZonedDateTime accountSignDate;
    private ZonedDateTime createdDate;
    private ZonedDateTime updatedDate;

    public boolean isSigned() {
        return StringUtils.equals(signYn, "Y");
    }

    public String getAccountSignDateToString() {
        try {
            return accountSignDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    public String getAccountExpireDateToString() {
        try {
            ZonedDateTime expireDate = accountSignDate.plus(1, ChronoUnit.YEARS).minus(1, ChronoUnit.DAYS);
            return expireDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }


    public String getSignDateToString() {
        try {
            return signDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String toString() {
        return "Openbank{" +
                "travelerId=" + travelerId +
                ", tokenIssueDate=" + tokenIssueDate +
                ", signDate=" + signDate +
                ", signYn='" + signYn + '\'' +
                ", userSeqNo='" + userSeqNo + '\'' +
                ", userName='" + CustomStringUtil.maskingName(userName) + '\'' +
                ", fintechUseNum='" + fintechUseNum + '\'' +
                ", bankCodeStd='" + bankCodeStd + '\'' +
                ", bankName='" + bankName + '\'' +
                ", accountNumMasked='" + accountNumMasked + '\'' +
                ", accountHolderName='" + CustomStringUtil.maskingName(accountHolderName) + '\'' +
                ", accountSignYn='" + accountSignYn + '\'' +
                ", accountSignDate=" + accountSignDate +
                ", createdDate=" + createdDate +
                ", updatedDate=" + updatedDate +
                '}';
    }
}
