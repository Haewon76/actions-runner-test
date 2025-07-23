package com.cashmallow.api.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

// 기능 : login 사용자의 id, 권한 정보 등을 보관하기위한 class.
@ToString
@Getter
@AllArgsConstructor
public class UserAuth {

    private final long userId;
    private final String instanceId;
    private final List<String> auths;
    private final String serviceCountry;

}
//캐싱 testestsetsetsetset3333333333