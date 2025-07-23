package com.cashmallow.api.interfaces.mallowlink.withdrawal.dto;

import javax.validation.constraints.NotNull;

public record MallowlinkCancelRequest(
        @NotNull String transactionId) {

}
