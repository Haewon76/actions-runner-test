package com.cashmallow.api.infrastructure.fcm.dto;

import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FcmMessageDto {

    private String title;

    private String body;

    private FcmEventCode eventCode;

    private FcmEventValue eventValue;

    public static FcmMessageDto of(String title, String body, FcmEventCode eventCode, FcmEventValue eventValue) {
        FcmMessageDto fcmMessageDto = new FcmMessageDto();
        fcmMessageDto.setTitle(title);
        fcmMessageDto.setBody(body);
        fcmMessageDto.setEventCode(eventCode);
        fcmMessageDto.setEventValue(eventValue);
        return fcmMessageDto;
    }
}
