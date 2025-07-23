package com.cashmallow.api.interfaces.openbank.dto.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class OpenbankCancelAccountResponse {
    private String apiTranId;         // 거래고유번호
    private String apiTranDtm;        // 거래일시(ms)
    private String rspCode;            // 응답코드
    private String rspMessage;         // 응답메시지
    private String bankTranId;        // 거래고유번호(참가은행)
    private String bankTranDate;      // 거래일자(참가은행)
    private String bankCodeTran;      // 참가은행.표준코드
    private String bankRspCode;       // 응답코드(참가은행)
    private String bankRspMessage;    // 응답메시지(참가은행)

    @Override
    public String toString() {
        return "OpenbankCancelAccountRes{" +
                "apiTranId='" + apiTranId + '\'' +
                ", apiTranDtm='" + apiTranDtm + '\'' +
                ", rspCode='" + rspCode + '\'' +
                ", rspMessage='" + rspMessage + '\'' +
                ", bankTranId='" + bankTranId + '\'' +
                ", bankTranDate='" + bankTranDate + '\'' +
                ", bankCodeTran='" + bankCodeTran + '\'' +
                ", bankRspCode='" + bankRspCode + '\'' +
                ", bankRspMessage='" + bankRspMessage + '\'' +
                '}';
    }

    public boolean isSuccess() {
        return StringUtils.equals("A0000", this.rspCode);
    }

    public boolean isFail() {
        return !isSuccess();
    }

}
