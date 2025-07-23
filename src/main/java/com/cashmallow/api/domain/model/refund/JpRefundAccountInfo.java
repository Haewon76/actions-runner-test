package com.cashmallow.api.domain.model.refund;

import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;
import com.cashmallow.api.interfaces.traveler.dto.JpRefundAccountInfoRequest;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JpRefundAccountInfo {
    private Long id;
    private Long travelerId;
    private String localLastName;
    private String localFirstName;
    @JsonProperty("bankId")
    private Long mlBankId;
    private String bankCode;
    private String bankName;
    private String branchCode;
    private String branchName;
    private JpRefundAccountType accountType;
    private String accountNo;
    private String needReRegister;

    public JpRefundAccountInfo(JpRefundAccountInfoRequest jpRefundAccountInfoRequest, Long travelerId) {
        this.travelerId = travelerId;
        this.localLastName = jpRefundAccountInfoRequest.localLastName();
        this.localFirstName = jpRefundAccountInfoRequest.localFirstName();
        this.mlBankId = jpRefundAccountInfoRequest.bankId();
        this.bankCode = jpRefundAccountInfoRequest.bankCode();
        this.bankName = jpRefundAccountInfoRequest.bankName();
        this.branchCode = jpRefundAccountInfoRequest.branchCode();
        this.branchName = jpRefundAccountInfoRequest.branchName();
        this.accountType = jpRefundAccountInfoRequest.accountType();
        this.accountNo = jpRefundAccountInfoRequest.accountNo();
    }

    public void reRegisterJpRefundAccountInfo(JpRefundAccountInfoRequest jpRefundAccountInfoRequest) {
        this.localLastName = jpRefundAccountInfoRequest.localLastName();
        this.localFirstName = jpRefundAccountInfoRequest.localFirstName();
        this.mlBankId = jpRefundAccountInfoRequest.bankId();
        this.bankCode = jpRefundAccountInfoRequest.bankCode();
        this.bankName = jpRefundAccountInfoRequest.bankName();
        this.branchCode = jpRefundAccountInfoRequest.branchCode();
        this.branchName = jpRefundAccountInfoRequest.branchName();
        this.accountType = jpRefundAccountInfoRequest.accountType();
        this.accountNo = jpRefundAccountInfoRequest.accountNo();
        this.needReRegister = "N";
    }
}
