package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserUnmaskedLog;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.api.interfaces.user.dto.CountNewUsersAndTravelersByCountryVO;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface UserService {


    /**
     * search user
     *
     * @param keyword : ID or email
     * @return
     * @throws CashmallowException
     */
    List<User> searchUsers(String keyword) throws CashmallowException;

    /**
     * User Login
     *
     * @param userName
     * @param password
     * @param cls
     * @return
     * @throws CashmallowException
     */
    String login(String userName, String password, String cls) throws CashmallowException;

    /**
     * User Login for refresh token
     *
     * @param loginId
     * @param password
     * @param cls
     * @return refresh token
     * @throws CashmallowException
     */
    String loginForRefreshToken(String loginId, String password, String cls, boolean encrypted) throws CashmallowException;

    User checkLoginAndPassword(String loginId, String password) throws CashmallowException;

    /**
     * Login. Get new token. If new device, send the email to warn
     *
     * @param userName
     * @param password
     * @param instanceId
     * @param deviceInfo
     * @param ip
     * @param cls
     * @return
     * @throws CashmallowException
     */
    String login(String userName, String password, String instanceId, String deviceInfo, String ip, String cls) throws CashmallowException;

    /**
     * Register User V3
     *
     * @param user
     * @param password
     * @param recommenderEmail
     * @return Long
     * @throws CashmallowException
     */
    Long registerUserV3(User user, String password, String recommenderEmail, List<TermsType> termsTypeList) throws CashmallowException;

    /**
     * User change own password
     *
     * @param token
     * @param currentPassword
     * @param newPassword
     * @return
     */
    ApiResultVO changePassword(String token, String currentPassword, String newPassword);

    /**
     * Update terms and privacy
     *
     * @param
     * @param
     * @return
     */
    User updateTerms(Long userId) throws CashmallowException;

    void cancelTermsAgree(Long userId);

    /**
     * Change user's password
     *
     * @param userId
     * @param currentPassword
     * @param newPassword
     * @throws CashmallowException
     */
    void changePassword(long userId, String currentPassword, String newPassword) throws CashmallowException;

    /**
     * Update user profile photo
     *
     * @param userId
     * @param file
     * @return
     * @throws CashmallowException
     */
    String uploadProfilePhoto(long userId, MultipartFile file) throws CashmallowException;

    /**
     * Reset own password and send the temporary password with e-mail.
     *
     * @param email
     * @param locale
     * @return
     */
    String passwordResetAndSendEmail(String email, Locale locale) throws CashmallowException;

    EmailTokenVerity passwordResetAndVerity(String token);

    /**
     * Get user count by country
     *
     * @param country
     * @return user, traveler, storekeeper, adminUser count
     */
    Map<String, Object> getUserCntByCountry(String country);

    String getLangKey(Long userID);

    void addSCBMessage(Model model, Locale locale);

    List<Long> getOrderByDescNewUserIds(int topCount);

    int getCountNewUsers(String startDate, String endDate);

    List<CountNewUsersAndTravelersByCountryVO> getCountNewUsersAndTravelersByCountry(String startDate, String endDate);

    int insertUserUnMaskedLog(UserUnmaskedLog userUnmaskedLog);

    List<TermsHistoryVO> getUnreadTermsList(Long userId, String countryCode, Locale locale);

    int updateUserInfo(User user);

    void validatesEmailCertNum(String email, String code, Locale locale) throws CashmallowException;

    Map<String, String> matchPassword(long userId, String loginId, String password) throws CashmallowException;

}