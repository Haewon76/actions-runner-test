package com.cashmallow.api.interfaces.sevenbank.dto;

import lombok.Data;

@Data
public class BalanceResponse<T> {

    private String result;
    private String resultCode;
    private String resultMsg;
    private T resultData;

}
