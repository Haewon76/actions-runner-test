package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.user.User;

import javax.validation.constraints.NotNull;

public interface AlarmService {
// testestsetsetsetsetsetestset

    /**
     * 특정 채널에 메시지 발송
     * e: ERROR
     * i: INFO
     * aAlert: ADMIN ALERT
     * sAlert: SYSTEM_ALERT
     * aMsg: ADMIN_MESSAGE
     * aEdd: ADMIN_EDD
     *
     * @param kind
     * @param message
     */
    void e(String kind, @NotNull String message);

    void i(String kind, @NotNull String message);

    void ie(String kind, @NotNull String message);

    boolean aAlert(String kind, @NotNull String message, User user);

    void sAlert(String kind, @NotNull String message);

    void aMsg(String kind, @NotNull String message);

    void aEdd(String kind, @NotNull String message);

}