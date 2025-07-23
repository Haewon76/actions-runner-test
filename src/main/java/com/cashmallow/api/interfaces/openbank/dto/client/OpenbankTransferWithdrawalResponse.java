package com.cashmallow.api.interfaces.openbank.dto.client;

import com.cashmallow.common.CustomStringUtil;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class OpenbankTransferWithdrawalResponse {
    private final String ApiTranId;
    private final String apiTranDtm;
    private final String rspCode;
    private final String rspMessage;
    private final String dpsBankCodeStd;
    private final String dpsBankCodeSub;
    private final String dpsBankName;
    private final String dpsAccountNumMasked;
    private final String dpsPrintContent;
    private final String dpsAccountHolderName;
    private final String bankTranId;
    private final String bankTranDate;
    private final String bankCodeTran;
    private final String bankRspCode;
    private final String bankRspMessage;
    private final String fintechUseNum;
    private final String accountAlias;
    private final String bankCodeStd;
    private final String bankCodeSub;
    private final String bankName;
    private final String savingsBankName;
    private final String accountNumMasked;
    private final String printContent;
    private final String accountHolderName;
    private final String tranAmt;
    private final String wdLimitRemainAmt;

    @Override
    public String toString() {
        return "OpenbankTransferWithdrawalResponse{" +
                "ApiTranId='" + ApiTranId + '\'' +
                ", apiTranDtm='" + apiTranDtm + '\'' +
                ", rspCode='" + rspCode + '\'' +
                ", rspMessage='" + rspMessage + '\'' +
                ", dpsBankCodeStd='" + dpsBankCodeStd + '\'' +
                ", dpsBankCodeSub='" + dpsBankCodeSub + '\'' +
                ", dpsBankName='" + dpsBankName + '\'' +
                ", dpsAccountNumMasked='" + dpsAccountNumMasked + '\'' +
                ", dpsPrintContent='" + dpsPrintContent + '\'' +
                ", dpsAccountHolderName='" + CustomStringUtil.maskingName(dpsAccountHolderName) + '\'' +
                ", bankTranId='" + bankTranId + '\'' +
                ", bankTranDate='" + bankTranDate + '\'' +
                ", bankCodeTran='" + bankCodeTran + '\'' +
                ", bankRspCode='" + bankRspCode + '\'' +
                ", bankRspMessage='" + bankRspMessage + '\'' +
                ", fintechUseNum='" + fintechUseNum + '\'' +
                ", accountAlias='" + accountAlias + '\'' +
                ", bankCodeStd='" + bankCodeStd + '\'' +
                ", bankCodeSub='" + bankCodeSub + '\'' +
                ", bankName='" + bankName + '\'' +
                ", savingsBankName='" + savingsBankName + '\'' +
                ", accountNumMasked='" + accountNumMasked + '\'' +
                ", printContent='" + printContent + '\'' +
                ", accountHolderName='" + CustomStringUtil.maskingName(accountHolderName) + '\'' +
                ", tranAmt='" + tranAmt + '\'' +
                ", wdLimitRemainAmt='" + wdLimitRemainAmt + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return StringUtils.equals("A0000", this.rspCode);
    }

    public boolean isFail() {
        return !isSuccess();
    }
}
