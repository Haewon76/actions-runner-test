package com.cashmallow.api.interfaces.hyphen.dto;


import lombok.Data;

@Data
public class BankCertificationInfo {
    private final BankAccountInfoVo bankAccountInfoVo;
    private final CheckAccount1VO.Response response;
}
