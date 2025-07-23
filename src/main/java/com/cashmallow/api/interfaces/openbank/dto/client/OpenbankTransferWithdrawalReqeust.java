package com.cashmallow.api.interfaces.openbank.dto.client;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
public class OpenbankTransferWithdrawalReqeust {
    private final String bankTranId;                // 거래 고유 번호
    private final String cntrAccountType = "N";     // N: 계좌, C: 계정
    private final String cntrAccountNum;            // 약정 계좌
    private final String dpsPrintContent;           // 입금계좌 인자내역
    private final String fintechUseNum;             // 핀테크이용번호
    private final long tranAmt;                   // 거래금액
    private final String tranDtime;                 // 요청일시
    private final String reqClientName;             // 요청고객 성명
    private final String reqClientFintechUseNum;    // 요청고객 핀테크이용번호
    private final String reqClientNum;              // 요청고객 회원번호, travelerId
    private final String transferPurpose = "TR";    // 이체 용도 TR: 송금
    private final String recvClientName;            // 최종수취고객성명
    private final String recvClientBankCode;        // 최종수취고객계좌 개설기관.표준코드
    private final String recvClientAccountNum;      // 최종수취고객계좌번호

    @Override
    public String toString() {
        return "OpenbankTransferWithdrawalReqeust{" +
                "bankTranId='" + bankTranId + '\'' +
                ", cntrAccountType='" + cntrAccountType + '\'' +
                ", cntrAccountNum='" + cntrAccountNum + '\'' +
                ", dpsPrintContent='" + dpsPrintContent + '\'' +
                ", fintechUseNum='" + fintechUseNum + '\'' +
                ", tranAmt=" + tranAmt +
                ", tranDtime='" + tranDtime + '\'' +
                ", reqClientName='" + reqClientName + '\'' +
                ", reqClientFintechUseNum='" + reqClientFintechUseNum + '\'' +
                ", reqClientNum='" + reqClientNum + '\'' +
                ", transferPurpose='" + transferPurpose + '\'' +
                ", recvClientName='" + recvClientName + '\'' +
                ", recvClientBankCode='" + recvClientBankCode + '\'' +
                ", recvClientAccountNum='" + recvClientAccountNum + '\'' +
                '}';
    }
}
