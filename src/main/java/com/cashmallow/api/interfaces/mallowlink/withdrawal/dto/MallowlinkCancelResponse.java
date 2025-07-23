package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;


import lombok.Data;

@Data
public final class MallowlinkCancelResponse {
    private final String status;
    private final String code;

    public static MallowlinkCancelResponse ok() {
        return new MallowlinkCancelResponse("SUCCESS", "0000");
    }

}
