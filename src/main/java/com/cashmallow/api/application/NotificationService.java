package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.notification.EmailToken;
import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.google.firebase.messaging.FirebaseMessagingException;

import java.util.List;
import java.util.Locale;

public interface NotificationService {

    public final class URLs {
        private URLs() {
            throw new IllegalStateException("Utility class");
        }

        // 사용자 디바이스 정보 리셋 
        public static final String CONFIRM_DEVICE_RESET = "/traveler/users/confirm-device-reset";
        public static final String CONFIRM_DEVICE_RESET_CAPTCHA = "/traveler/users/confirm-device-reset-captcha";

    }

    // Email

    /**
     * Send email for email authorization
     *
     * @param email
     * @param countryCode
     * @return
     * @throws CashmallowException
     */
    String sendEmailToAuth(String email, CountryCode countryCode) throws CashmallowException;

    /**
     * Send email for reset password
     *
     * @param emailTo
     * @param newPassword
     * @param locale
     * @return
     * @throws CashmallowException
     */
    String sendEmailToResetPassword(User user, String emailTo, Locale locale) throws CashmallowException;

    boolean isNotAvailableEmailVerify(Long userId);

    boolean isNotAvailablePassword(EmailTokenVerity verity);

    /**
     * Get the confirmation view(HTML) for bank account confirmation
     *
     * @param isSuccess
     * @param locale
     * @return
     */
    String getBankAccountConfirmView(User user, Locale locale);

    /**
     * Send email for exchange confirmation
     *
     * @param user
     * @param traveler
     * @param exchange
     * @param fromCountry
     * @param toCountry
     * @throws CashmallowException
     */
    void sendEmailConfirmExchange(User user, Traveler traveler, Exchange exchange, Country fromCountry, Country toCountry) throws CashmallowException;

    void sendEmailConfirmRemittance(User user, Traveler traveler, Remittance remittance, Country fromCountry, Country toCountry) throws CashmallowException;

    void sendEmailPrivacyPolicy(User user);

    /**
     * Send email to warn new device
     *
     * @param user
     * @param deviceInfo
     * @param ip
     * @param locale
     * @throws CashmallowException
     */
    void sendEmailToWarnNewDevice(User user, String deviceInfo, String ip) throws CashmallowException;

    /**
     * Send email to reset device information
     *
     * @param user
     * @param deviceInfo
     * @param ip
     */
    void sendEmailToResetDevice(User user, String deviceInfo, String ip) throws CashmallowException;

    /**
     * Get the confirmation view(HTML) for device reset
     *
     * @param isSuccess
     * @return
     * @throws CashmallowException
     */
    String getResetDeviceConfirmView(boolean isSuccess, User user);

    /**
     * Send email to notify dormant user candidates
     *
     * @param userId
     * @param email
     * @throws CashmallowException
     */
    void sendEmailToNotifyDormantUser(long userId, String email) throws CashmallowException;

    // FCM

    /**
     * Add FCM token
     *
     * @param userId
     * @param fcmToken
     * @param devType
     * @throws CashmallowException
     */
    void addFcmToken(long userId, String fcmToken, String devType) throws CashmallowException;

    /**
     * Send FCM asynchronously
     *
     * @param user
     * @param eventCode
     * @param eventValue
     * @param orgId
     */
    void sendFcmNotificationMsgAsync(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId);

    /**
     * Send FCM with custom message asynchronously
     *
     * @param user
     * @param eventCode
     * @param eventValue
     * @param orgId
     * @param message
     */
    void sendFcmNotificationMsgAsync(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, String message);

    void sendFcmNotification(List<User> users, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, String message) throws FirebaseMessagingException;

    /**
     * Add notification table and send with eventCode
     *
     * @param user
     * @param eventCode
     * @param eventValue
     * @param orgId
     * @param fcmtype
     * @return 성공하면 ""이 담긴 Future , 실패하면 에러 메시지가 담긴 Future
     */
    void addFcmNotificationMsg(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId);

    /**
     * Remove unsent FCM
     *
     * @return
     */
    ApiResultVO removeFcmNotification();

    EmailToken getEmailToken(Long userId);

    /**
     * Insert an email token
     *
     * @param emailToken
     * @return
     */
    int addEmailToken(EmailToken emailToken);

    /**
     * Remove an email token
     *
     * @param emailToken
     * @return
     */
    int removeEmailToken(EmailToken emailToken);

    /**
     * Remove all email token by userId
     *
     * @param userId
     * @return
     */
    int removeEmailTokenByUserId(Long userId);

    /**
     * Get verified email password
     *
     * @param token
     * @return
     */
    public EmailTokenVerity getVerifiedEmailPassword(String token);

    public EmailTokenVerity getVerifiedEmailCode(String token);


    /**
     * Token Verity And Password Reset
     *
     * @param token
     * @return
     */
    EmailTokenVerity passwordResetAndVerity(String token);

    void sendEmailReRegisterReceipt(User user) throws CashmallowException;

    // 환전 환불 완료시 이메일 발송
    void sendEmailConfirmNewRefundForExchange(User user, Traveler traveler, NewRefund refund, Country fromCountry, Country toCountry) throws CashmallowException;
    // 환전 환불 완료시 이메일 발송
    void sendEmailConfirmNewRefundForRemittance(User user, Traveler traveler, NewRefund refund, Country fromCountry, Country toCountry, Remittance remittance) throws CashmallowException;

    void sendEmailExpiredWalletBefore7Day(User user) throws CashmallowException;

    void sendEmailExpiredWallet(User user) throws CashmallowException;
}
