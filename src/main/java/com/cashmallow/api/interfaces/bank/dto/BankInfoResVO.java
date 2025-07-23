package com.cashmallow.api.interfaces.bank.dto;

import lombok.Data;

import java.util.List;

@Data
public class BankInfoResVO {
    private final List<BankInfoVO> banks;
}
