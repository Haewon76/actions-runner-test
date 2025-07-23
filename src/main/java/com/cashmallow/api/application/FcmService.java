package com.cashmallow.api.application;

import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.infrastructure.fcm.dto.FcmMessageDto;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.List;


public interface FcmService {

    public enum FcmType {
        FCMNOTI, FCMDATA, FCMBOTH
    }

    /**
     * Send FCM to Android devices. Data message.
     *
     * @param token
     * @param title
     * @param body
     * @param eventCode
     * @param eventValue
     * @param option
     */
    void postMsgToFcmV1Android(String token, String title, String body,
                               FcmEventCode eventCode, FcmEventValue eventValue, String option);

    /**
     * Send FCM to iOS devices. Notification message with data.
     *
     * @param token
     * @param title
     * @param body
     * @param eventCode
     * @param eventValue
     * @param option
     */
    void postMsgToFcmV1Ios(String token, String title, String body,
                           FcmEventCode eventCode, FcmEventValue eventValue, String option);

    void sendFcmMessageMulticastAndroid(FcmMessageDto fcmMessageDto, List<String> tokens) throws FirebaseMessagingException;

    void sendFcmMessageMulticastIos(FcmMessageDto fcmMessageDto, List<String> tokens) throws FirebaseMessagingException;
}