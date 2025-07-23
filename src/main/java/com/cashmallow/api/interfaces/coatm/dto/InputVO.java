package com.cashmallow.api.interfaces.coatm.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class InputVO {

    // 쿠콘에서 제휴사에 부여하는 기관코드
    private String rqCompanyCode;

    // 제휴사에서 쿠콘에 부여하는 연동 키
    private String rqCompanyPass;

    // 개인식별번호, SEED ECB 암호화
    private String rqMemberPin;

    // 출금인증번호, SEED ECB 암호화
    private String rqPaymentKey;

    // 거래고유번호
    private String rqWithdrawSeqNo;

    // ATM 단말기번호
    private String rqAtmCode;

    // ATM 출금요청시각
    private String rqAtmWithdrawRequestTime;

    // ATM 출금요청일자
    private String rqAtmWithdrawRequestDate;

    // ATM 출금기관코드
    private String rqOrgCd;

    // 결제토큰
    private String payToken;


    public String getRqCompanyCode() {
        return rqCompanyCode;
    }

    public void setRqCompanyCode(String rqCompanyCode) {
        this.rqCompanyCode = rqCompanyCode;
    }

    public String getRqCompanyPass() {
        return rqCompanyPass;
    }

    public void setRqCompanyPass(String rqCompanyPass) {
        this.rqCompanyPass = rqCompanyPass;
    }

    public String getRqMemberPin() {
        return rqMemberPin;
    }

    public void setRqMemberPin(String rqMemberPin) {
        this.rqMemberPin = rqMemberPin;
    }

    public String getRqPaymentKey() {
        return rqPaymentKey;
    }

    public void setRqPaymentKey(String rqPaymentKey) {
        this.rqPaymentKey = rqPaymentKey;
    }

    public String getRqWithdrawSeqNo() {
        return rqWithdrawSeqNo;
    }

    public void setRqWithdrawSeqNo(String rqWithdrawSeqNo) {
        this.rqWithdrawSeqNo = rqWithdrawSeqNo;
    }

    public String getRqAtmCode() {
        return rqAtmCode;
    }

    public void setRqAtmCode(String rqAtmCode) {
        this.rqAtmCode = rqAtmCode;
    }

    public String getRqAtmWithdrawRequestTime() {
        return rqAtmWithdrawRequestTime;
    }

    public void setRqAtmWithdrawRequestTime(String rqAtmWithdrawRequestTime) {
        this.rqAtmWithdrawRequestTime = rqAtmWithdrawRequestTime;
    }

    public String getRqOrgCd() {
        return rqOrgCd;
    }

    public void setRqOrgCd(String rqOrgCd) {
        this.rqOrgCd = rqOrgCd;
    }

    public String getRqAtmWithdrawRequestDate() {
        return rqAtmWithdrawRequestDate;
    }

    public void setRqAtmWithdrawRequestDate(String rqAtmWithdrawRequestDate) {
        this.rqAtmWithdrawRequestDate = rqAtmWithdrawRequestDate;
    }

    public String getPayToken() {
        return payToken;
    }

    public void setPayToken(String payToken) {
        this.payToken = payToken;
    }

}
