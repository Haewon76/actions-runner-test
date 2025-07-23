package com.cashmallow.api.interfaces;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
public class AuthVO {
    private Long timestamp;
    private String deviceId;

    // additional fields
    // private String userId;
    // private String userId1;
    // private String userId2;
    // private String userId3;
}
