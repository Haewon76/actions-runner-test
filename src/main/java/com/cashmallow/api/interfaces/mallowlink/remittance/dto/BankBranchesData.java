package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class BankBranchesData {
    private final String branchesId;
    private final String bankId;
    private final String branchesCode;
    private final String branchesName;
    private final String branchesNameEng;


    public static BankBranchesData of(RemittanceBankBranches branches) {
        return BankBranchesData.builder()
                .branchesId(branches.getId())
                .bankId(branches.getBankId())
                .branchesCode(branches.getCode())
                .branchesName(branches.getName())
                .branchesNameEng(branches.getNameEng())
                .build();
    }
}
