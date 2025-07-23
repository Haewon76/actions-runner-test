package com.cashmallow.api.interfaces.mallowlink.common.dto;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public class MallowlinkException extends RuntimeException {

    private final MallowlinkExceptionType status;

    public MallowlinkException(MallowlinkExceptionType status) {
        super(status.name());
        this.status = status;
    }

    public static MallowlinkException of(MallowlinkExceptionType exceptionType) {
        return new MallowlinkException(exceptionType);
    }
}
