package com.cashmallow.api.infrastructure.fcm;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.FcmService;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.fcm.dto.FcmMessageDto;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.KeyUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.messaging.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FcmServiceImpl implements FcmService {
    private static final Logger logger = LoggerFactory.getLogger(FcmServiceImpl.class);

    // FCM Legacy HTTP
    private static final String FCM_SERVER_KEY = "AAAAX8TpnZk:APA91bFJp5xXu0mVENZVCjr0_P86TdCivEd2EkrI4iTr8GaZz8KKCIYb11UQg78Fnv9884giYXek06IGDRbJMu4SJrcgYLeJIDX9NXfsdY4PIgug893p0Xze_6bCWUkWyimOPLIeH4ut";
    private static final String URL_FCM = "https://fcm.googleapis.com/fcm/send";

    // FCM HTTP v1
    @Value("${firebase.cloudMessaging.key}")
    private String fcmKey;
    @Value("${firebase.cloudMessaging.url}")
    private String urlFcmV1;

    private static final String MESSAGING_SCOPE = "https://www.googleapis.com/auth/firebase.messaging";
    private static final String[] SCOPES = {MESSAGING_SCOPE};

    private static final int MULTICAST_MAX_SIZE = 500;

    private final AlarmService alarmService;

    private final AsyncTaskExecutor asyncTaskExecutor;

    private final FirebaseMessaging firebaseMessaging;

    @Deprecated
    @Getter
    @Setter
    public static class FcmData {
        private String title;
        private String message;
        private FcmEventCode eventCode;
        private FcmEventValue eventValue;
        private Long orgId;
        private Timestamp sendTime;

        public FcmData(String title, String message, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId,
                       Timestamp sendTime) {
            this.title = title;
            this.message = message;
            this.eventCode = eventCode;
            this.eventValue = eventValue;
            this.orgId = orgId;
            this.sendTime = CommDateTime.getCurrentDateTime();
            if (sendTime != null) {
                this.sendTime = sendTime;
            }
        }
    }

    @Deprecated
    @Getter
    @Setter
    public static class FcmNotification {
        private String title;
        private String titleLocKey;
        private String body;
        private String bodyLocKey;
        private String sound;
        private int badge;
        private Timestamp sendTime;

        public FcmNotification(String title, String body, Timestamp sendTime) {
            this.title = title;
            this.body = body;

            this.sound = "default";
            this.badge = 0;

            if (sendTime == null) {
                this.sendTime = CommDateTime.getCurrentDateTime();
            } else {
                this.sendTime = sendTime;
            }
        }
    }

    @Deprecated
    @Getter
    @Setter
    public static class FcmBody {
        private FcmData data;
        private FcmNotification notification;
        private String token;

        public FcmBody(String token, String title, String message, FcmEventCode eventCode, FcmEventValue eventValue,
                       Long orgId, FcmType fcmtype) {

            this.token = token;

            Timestamp sendTime = CommDateTime.getCurrentDateTime();

            this.data = null;
            if (fcmtype == FcmType.FCMBOTH || fcmtype == FcmType.FCMDATA) {
                this.data = new FcmData(title, message, eventCode, eventValue, orgId, sendTime);
            }

            this.notification = null;
            if (fcmtype == FcmType.FCMBOTH || fcmtype == FcmType.FCMNOTI) {
                this.notification = new FcmNotification(title, message, sendTime);
                this.notification.titleLocKey = "TITLE_LOC_KEY_" + eventCode + "_" + eventValue;

                // org_id 에 의미가 있는 경우
                String orgIdStr = "";
                if (FcmEventCode.AU == eventCode) {
                    orgIdStr = "_" + orgId;
                }

                this.notification.bodyLocKey = "BODY_LOC_KEY_" + eventCode + "_" + eventValue + orgIdStr;

            }
        }
    }

    public String getFCMAccessToken() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(KeyUtil.getJSONKey(fcmKey).getBytes()))
                    .createScoped(Arrays.asList(SCOPES));
            credentials.refreshIfExpired();
            return credentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return "";
    }

    /**
     * Build FCM message body. without 'option' property
     *
     * @deprecated 2021-07-28
     */
    @Deprecated
    private JSONObject buildMessageBody(String token, String title, String message, FcmEventCode eventCode, FcmEventValue eventValue) {

        JSONObject eventData = new JSONObject();
        eventData.put("event_code", eventCode);
        eventData.put("event_value", eventValue);

        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", message);

        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("data", eventData);
        jsonMessage.put("notification", notification);
        jsonMessage.put("token", token);

        JSONObject jsonResult = new JSONObject();
        jsonResult.put("message", jsonMessage);

        return jsonResult;
    }

    /**
     * Send FCM for Android devices
     */
    public void postMsgToFcmV1Android(String token, String title, String body,
                                      FcmEventCode eventCode, FcmEventValue eventValue, String option) {

        if (StringUtils.isEmpty(body)) {
            body = "Notification message!";
        }

        JSONObject eventData = new JSONObject();
        eventData.put("title", title);
        eventData.put("body", body);

        eventData.put("event_code", eventCode);
        eventData.put("event_value", eventValue);
        eventData.put("option", option);

        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("data", eventData);
        jsonMessage.put("token", token);

        JSONObject androidConfig = new JSONObject();
        androidConfig.put("priority", "high");
        jsonMessage.put("android", androidConfig);

        JSONObject jsonBody = new JSONObject();
        jsonBody.put("message", jsonMessage);

        postMsgToFcmV1(token, jsonBody);
    }

    /**
     * Send FCM for iOS devices.
     */
    public void postMsgToFcmV1Ios(String token, String title, String body,
                                  FcmEventCode eventCode, FcmEventValue eventValue, String option) {

        if (StringUtils.isEmpty(body)) {
            body = "Notification message!";
        }

        // Data
        JSONObject data = new JSONObject();
        data.put("event_code", eventCode);
        data.put("event_value", eventValue);
        data.put("option", option);

        // Notification
        JSONObject notification = new JSONObject();
        notification.put("title", title);
        notification.put("body", body);

        // APNS
        // To receive FCM in background
        JSONObject aps = new JSONObject();
        aps.put("content-available", 1);
        aps.put("sound", "default");

        JSONObject payload = new JSONObject();
        payload.put("aps", aps);

        JSONObject apns = new JSONObject();
        apns.put("payload", payload);

        // Message
        JSONObject message = new JSONObject();
        message.put("data", data);
        message.put("notification", notification);
        message.put("token", token);
        message.put("apns", apns);

        JSONObject fcmBody = new JSONObject();
        fcmBody.put("message", message);

        postMsgToFcmV1(token, fcmBody);
    }

    /**
     * FCM을 발송하고 결과 리턴.
     *
     * @param token
     * @param jsonBody
     */
    private void postMsgToFcmV1(String token, JSONObject jsonBody) {
        final String method = "postMsgToFcmV1()";

        final String keyString = jsonBody.keySet().stream().map(key -> "key: " + jsonBody.get(key)).collect(Collectors.joining("\n"));

        if (StringUtils.isEmpty(token)) {
            alarmService.i("푸시 오류", "토큰이 비어있음\n" + keyString);
            logger.info("FCM Error token empty", keyString);
            return;
        }

        HttpPost httpPost = new HttpPost(urlFcmV1);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + getFCMAccessToken());

        StringEntity strEntity = new StringEntity(jsonBody.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(strEntity);

        asyncTaskExecutor.execute(() -> {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

                    logger.info("{}: statusLine={}", method, response.getStatusLine());

                    // API서버로부터 받은 JSON 문자열 데이터
                    org.apache.http.HttpEntity entity = response.getEntity();
                    String fcmResult = EntityUtils.toString(entity);
                    logger.info("{}: [FCM 결과] {}", method, fcmResult);
                    EntityUtils.consume(entity);
                }

            } catch (Exception e) {
                logger.error("FCM Error - " + keyString + " -- " + e.getMessage(), e);
                alarmService.i("푸시 오류", "전송중 오류 발생\nkeys: " + keyString + "\nmessage: " + e.getMessage());
            }
        });
    }

    /**
     * Send FCM. without 'option' property
     *
     * @deprecated 2021-07-28
     */
    @Deprecated
    public void postMsgToFcmV1(String token, String title, String message, FcmEventCode eventCode, FcmEventValue eventValue) {
        Future<String> future = null;

        final String method = "postMsgToFcmV1()";

        if (StringUtils.isEmpty(token)) {
            return;
        }

        if (StringUtils.isEmpty(title)) {
            title = "Cashmallow";
        }

        if (StringUtils.isEmpty(message)) {
            message = "Notification message!";
        }

        HttpPost httpPost = new HttpPost(urlFcmV1);
        httpPost.setHeader("Content-Type", "application/json");
        httpPost.setHeader("Authorization", "Bearer " + getFCMAccessToken());

        JSONObject jsonBody = buildMessageBody(token, title, message, eventCode, eventValue);

        StringEntity strEntity = new StringEntity(jsonBody.toString(), ContentType.APPLICATION_JSON);
        httpPost.setEntity(strEntity);

        future = asyncTaskExecutor.submit(() -> {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                try (CloseableHttpResponse response = httpclient.execute(httpPost)) {

                    logger.info("{}: statusLine={}", method, response.getStatusLine());

                    // API서버로부터 받은 JSON 문자열 데이터
                    org.apache.http.HttpEntity entity = response.getEntity();
                    String fcmResult = EntityUtils.toString(entity);
                    logger.info("{}: [FCM 결과] {}", method, fcmResult);

                    Map<String, Object> map = JsonStr.toHashMap(fcmResult);
                    Integer success = (Integer) map.get("success");
                    Integer failure = (Integer) map.get("failure");
                    Integer canonicalIds = (Integer) map.get("canonical_ids");

                    Object results = map.get("results");

                    logger.info("{}: success={}, failure={}, canonicalIds={}, results={}", method, success, failure, canonicalIds, results);

                    EntityUtils.consume(entity);
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return e.getMessage();
            }
            return "";
        });
    }

    /**
     * Send FCM. Legacy API
     *
     * @deprecated 2021-07-28
     */
    @Deprecated
    public void postMsgToFcm(String token, String title, String message,
                             FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, FcmType fcmtype) {

        Future<String> future = null;

        final String method = "postMsgToFcmServer()";

        if (StringUtils.isEmpty(token)) {
            return;
        }

        if (StringUtils.isEmpty(title)) {
            title = "Cashmallow";
        }

        if (StringUtils.isEmpty(message)) {
            message = "Notification message!";
        }

        try {
            FcmBody fcmBody = new FcmBody(token, title, message, eventCode, eventValue, orgId, fcmtype);

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
            String json = mapper.writeValueAsString(fcmBody);

            if (fcmtype == FcmType.FCMNOTI) {
                json = json.replace("\"data\" : null,", "");
                json = json.replace("\"title\" : \"none\",", "");
                json = json.replace("\"body\" : \"none\",", "");
            } else if (fcmtype == FcmType.FCMDATA) {
                json = json.replace("\"notification\" : null,", "");
            }

            HttpPost httpPost = new HttpPost(URL_FCM);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "key=" + FCM_SERVER_KEY);

            logger.info("{}: fcmtype={}, json={}", method, fcmtype, json);

            StringEntity jsonParam = new StringEntity(json, Const.DEF_ENCODING);
            httpPost.setEntity(jsonParam);

            // 외부 시스템에 전송 요청은 가능하면 쓰레드를 이용해서 보내야 시스템 대기 상태를 피할 수 있다.
            future = asyncTaskExecutor.submit(() -> {
                try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                    try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                        logger.info("{}: statusLine={}", method, response.getStatusLine());

                        // API서버로부터 받은 JSON 문자열 데이터
                        org.apache.http.HttpEntity entity = response.getEntity();
                        String fcmResult = EntityUtils.toString(entity);
                        logger.info("{}: [FCM 결과] {}", method, fcmResult);

                        Map<String, Object> map = JsonStr.toHashMap(fcmResult);
                        Integer success = (Integer) map.get("success");
                        Integer failure = (Integer) map.get("failure");
                        Integer canonicalIds = (Integer) map.get("canonical_ids");

                        Object results = map.get("results");

                        logger.info("{}: success={}, failure={}, canonicalIds={}, results={}", method, success, failure, canonicalIds, results);

                        EntityUtils.consume(entity);

                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return e.getMessage();
                }
                return "";

            });

            //            executorService.shutdown();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void sendFcmMessageMulticastAndroid(FcmMessageDto fcmMessageDto, List<String> tokens) throws FirebaseMessagingException {
        Map<String, String> message = new HashMap<>();
        message.put("title", fcmMessageDto.getTitle());
        message.put("body", fcmMessageDto.getBody());
        message.put("event_code", fcmMessageDto.getEventCode().name());
        message.put("event_value", fcmMessageDto.getEventValue().name());

        for (int i = 0; i < tokens.size(); i += MULTICAST_MAX_SIZE) {
            List<String> sendTokenList = tokens.subList(i, Math.min(tokens.size(), i + MULTICAST_MAX_SIZE));
            MulticastMessage fcmMessage = MulticastMessage.builder()
                    .putAllData(message)
                    .addAllTokens(sendTokenList)
                    .build();

            logger.info(i+" sendFcmMessageMulticastAndroid fcmMessage={}", fcmMessage.toString());
            firebaseMessaging.sendEachForMulticast(fcmMessage);
        }
    }

    @Override
    public void sendFcmMessageMulticastIos(FcmMessageDto fcmMessageDto, List<String> tokens) throws FirebaseMessagingException {
        ApnsConfig aps = getApnsConfig();
        Notification notification = getNotification(fcmMessageDto);

        for (int i = 0; i < tokens.size(); i += MULTICAST_MAX_SIZE) {
            List<String> sendTokenList = tokens.subList(i, Math.min(tokens.size(), i + MULTICAST_MAX_SIZE));
            MulticastMessage fcmMessage = MulticastMessage.builder()
                    .setApnsConfig(aps)
                    .setNotification(notification)
                    .putData("event_code", fcmMessageDto.getEventCode().name())
                    .putData("event_value", fcmMessageDto.getEventValue().name())
                    .addAllTokens(sendTokenList)
                    .build();
            firebaseMessaging.sendEachForMulticast(fcmMessage);
        }
    }

    private Notification getNotification(FcmMessageDto fcmMessageDto) {
        return Notification.builder()
                .setTitle(fcmMessageDto.getTitle())
                .setBody(fcmMessageDto.getBody())
                .build();
    }

    private ApnsConfig getApnsConfig() {
        return ApnsConfig.builder()
                .setAps(Aps.builder()
                        .setContentAvailable(true)
                        .setSound("default")
                        .build())
                .build();
    }
}
