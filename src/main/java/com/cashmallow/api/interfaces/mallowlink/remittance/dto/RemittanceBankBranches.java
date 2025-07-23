package com.cashmallow.api.interfaces.mallowlink.remittance.dto;

import lombok.Data;


@Data
public final class RemittanceBankBranches {

    private final String id;
    private final String bankId;
    private final String name;
    private final String code;
    private final String nameEng;
}
