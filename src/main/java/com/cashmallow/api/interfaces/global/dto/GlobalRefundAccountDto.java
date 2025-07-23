package com.cashmallow.api.interfaces.global.dto;

import com.cashmallow.api.domain.model.refund.JpRefundAccountInfo;
import com.cashmallow.api.interfaces.global.enums.JpRefundAccountType;

public record GlobalRefundAccountDto(Long cmRefundId,
                                     Long cmRefundAccountId,
                                     Long travelerId,
                                     String localLastName,
                                     String localFirstName,
                                     Long mlBankId,
                                     String bankCode,
                                     String bankName,
                                     String branchCode,
                                     String branchName,
                                     JpRefundAccountType accountType,
                                     String accountNo
) {
    public GlobalRefundAccountDto(Long cmRefundId, JpRefundAccountInfo jpRefundAccountInfo) {
        this(
                cmRefundId,
                jpRefundAccountInfo.getId(),
                jpRefundAccountInfo.getTravelerId(),
                jpRefundAccountInfo.getLocalLastName(),
                jpRefundAccountInfo.getLocalFirstName(),
                jpRefundAccountInfo.getMlBankId(),
                jpRefundAccountInfo.getBankCode(),
                jpRefundAccountInfo.getBankName(),
                jpRefundAccountInfo.getBranchCode(),
                jpRefundAccountInfo.getBranchName(),
                jpRefundAccountInfo.getAccountType(),
                jpRefundAccountInfo.getAccountNo()
        );
    }
}
