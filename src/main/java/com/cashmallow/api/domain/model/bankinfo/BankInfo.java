package com.cashmallow.api.domain.model.bankinfo;


import lombok.Data;

@Data
public class BankInfo {
    private long id;
    private String iso3166;
    private String code;
    private String name;
    private String enable;
    private String iconPath;
    private String bniClearingCode;
    private String swiftCode;
}
