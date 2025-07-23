package com.cashmallow.api.domain.model.notification;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class FcmToken {

    private Long userId;

    private String fcmToken;

    private String devType;

    private LocalDateTime updatedDate;

    @Builder
    public FcmToken(Long userId, String fcmToken, String devType, LocalDateTime updatedDate) {
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.devType = devType;
        this.updatedDate = updatedDate;
    }
}
