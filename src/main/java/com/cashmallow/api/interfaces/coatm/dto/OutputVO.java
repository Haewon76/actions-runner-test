package com.cashmallow.api.interfaces.coatm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class OutputVO {

    // 응답코드
    private String rsResultCode;

    // 응답메시지
    private String rsResultMsg;

    // 출금액
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String rsAmount;

    // 출금수수료
    @JsonIgnoreProperties(ignoreUnknown = true)
    private String rsFee;

    // 개인식별번호, SEED ECB 암호화
    private String rsMemberPin;

    // 출금인증번호, SEED ECB 암호화
    private String rsPaymentKey;

    // 거래고유번호
    private String rsWithdrawSeqNo;

    // ATM 단말기번호
    private String rsAtmCode;

    // ATM 인출요청시간
    private String rsAtmWithdrawRequestTime;

    // ATM 출금요청일자
    private String rsAtmWithdrawRequestDate;

    // 결제토큰
    private String payToken;

    // ATM 출금기관코드
    private String rsOrgCD;


    public String getRsResultCode() {
        return rsResultCode;
    }

    public void setRsResultCode(String rsResultCode) {
        this.rsResultCode = rsResultCode;
    }

    public String getRsResultMsg() {
        return rsResultMsg;
    }

    public void setRsResultMsg(String rsResultMsg) {
        this.rsResultMsg = rsResultMsg;
    }

    public String getRsAmount() {
        return rsAmount;
    }

    public void setRsAmount(String rsAmount) {
        this.rsAmount = rsAmount;
    }

    public String getRsFee() {
        return rsFee;
    }

    public void setRsFee(String rsFee) {
        this.rsFee = rsFee;
    }

    public String getRsMemberPin() {
        return rsMemberPin;
    }

    public void setRsMemberPin(String rsMemberPin) {
        this.rsMemberPin = rsMemberPin;
    }

    public String getRsPaymentKey() {
        return rsPaymentKey;
    }

    public void setRsPaymentKey(String rsPaymentKey) {
        this.rsPaymentKey = rsPaymentKey;
    }

    public String getRsWithdrawSeqNo() {
        return rsWithdrawSeqNo;
    }

    public void setRsWithdrawSeqNo(String rsWithdrawSeqNo) {
        this.rsWithdrawSeqNo = rsWithdrawSeqNo;
    }

    public String getRsAtmCode() {
        return rsAtmCode;
    }

    public void setRsAtmCode(String rsAtmCode) {
        this.rsAtmCode = rsAtmCode;
    }

    public String getRsAtmWithdrawRequestTime() {
        return rsAtmWithdrawRequestTime;
    }

    public void setRsAtmWithdrawRequestTime(String rsAtmWithdrawRequestTime) {
        this.rsAtmWithdrawRequestTime = rsAtmWithdrawRequestTime;
    }

    public String getRsAtmWithdrawRequestDate() {
        return rsAtmWithdrawRequestDate;
    }

    public void setRsAtmWithdrawRequestDate(String rsAtmWithdrawRequestDate) {
        this.rsAtmWithdrawRequestDate = rsAtmWithdrawRequestDate;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

    public String getRsOrgCD() {
        return rsOrgCD;
    }

    public void setRsOrgCD(String rsOrgCD) {
        this.rsOrgCD = rsOrgCD;
    }

}
