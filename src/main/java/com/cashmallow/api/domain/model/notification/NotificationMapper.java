package com.cashmallow.api.domain.model.notification;

import java.util.List;
import java.util.Map;

public interface NotificationMapper {

    // Table: emailInfo
    Map<String, Object> getEmailInfo(String type);

    // Table: email_template
    Map<String, Object> getEmailTemplate(Map<String, Object> params);

    Map<String, Object> getEmailTemplateComplete(Map<String, Object> params);

    EmailTokenVerity getVerifiedEmailPassword(String token);

    EmailTokenVerity getVerifiedEmailCode(String token);

    boolean isAvailableEmailVerify(Long userId);

    boolean isNotAvailablePassword(EmailTokenVerity verity);

    void updateVerifiedEmailPassword(EmailTokenVerity token);

    void addVerifiedEmailPassword(EmailTokenVerity emailTokenVerity);

    // Table: fcm_token
    Map<String, String> getFcmTokenByUserId(Long userId);

    int addFcmToken(Map<String, Object> params);

    // Table: fcm_notification
    int addFcmNotificationMsg(Map<String, Object> params);

    int removeFcmNotification();

    List<Map<String, Object>> getFcmNotification(Map<String, Object> params);

    // Table: account_token
    EmailToken getEmailToken(Long userId);

    int insertEmailToken(EmailToken emailToken);

    /**
     * Delete an email token
     *
     * @param emailToken
     * @return
     */
    int deleteEmailToken(EmailToken emailToken);

    /**
     * Delete all token by userId
     *
     * @param userId
     * @return
     */
    int deleteEmailTokenByUserId(Long userId);

    List<FcmToken> getFcmTokensByUserIds(List<Long> userIds);
}
