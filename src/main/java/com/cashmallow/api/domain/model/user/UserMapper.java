package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.model.authme.Authme;
import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.shared.DurationDateVO;
import com.cashmallow.api.interfaces.user.dto.CountNewUsersAndTravelersByCountryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface UserMapper {

    // Read user 

    /**
     * Get user if user's login and password are correct.
     *
     * @param params (login, password, cls)
     * @return
     */
    User checkUserPwd(Map<String, Object> params);

    User checkUserWithoutPassword(Map<String, Object> params);

    /**
     * Get user by userId.
     *
     * @param userId
     * @return
     */
    User getUserByUserId(Long userId);
    User getUserByWithdrawalTransactionId(String transactionId);
    User getUserByWithdrawalCashoutId(Long cashoutId);

    List<User> getHkUsers(Long userId);

    @Select("select COUNT(1) from traveler where certification_ok = 'Y' and identification_number = #{certificationId} ")
    int getCertificationCount(String certificationId);

    User getUserByTravelerId(Long travelerId);

    String getUserOtp(Long userId);

    User getFirstUser();

    /**
     * Get user by loginId.
     *
     * @param userId
     * @return
     */
    User getUserByLoginId(String loginId);

    /**
     * Get user by email.
     *
     * @param email
     * @return
     */
    User getUserByEmail(String email);

    /**
     * Get Admin user List
     *
     * @return
     */
    List<User> getAdminUsers();

    /**
     * Search users for travelerInfo
     *
     * @param params
     * @return
     */
    List<Map<String, String>> getUsers(String keyword);

    /**
     * Search users
     *
     * @param params
     * @return
     */
    List<User> searchUsers(String keyword);

    /**
     * Get user count by country
     *
     * @param params
     * @return
     */
    Map<String, Object> getUserCntByCountry(String country);

    // Write user 

    /**
     * Insert user
     *
     * @param user
     * @return
     */
    int insertUser(User user);

    int insertOtp(User user);

    // about authme
    void insertAuthmeWebhookLog(Authme authme);
    void updateAuthmeTravelerSynced(String customerId);
    @Select("""
                    select received_at
                    from authme
                    where user_id = #{customerId}
                    and traveler_synced_at is null
                    order by id desc
                    limit 1
            """)
    LocalDateTime getAuthmeWebhookReceivedAt(String customerId);

    String selectOtp(@Param("userId") Long userId);

    /**
     * Update user
     *
     * @param user
     * @return
     */
    int updateUser(User user);

    void updateUserLangKey(User user);

    /**
     * 로그인 비밀번호 불 일치인 경우,
     * 로그인 실패시 카운트 값을 1씩 증가 시킨다
     */
    int updateUserLoginCount(EmailTokenVerity emailTokenVerity);

    int updateUserPassword(EmailTokenVerity emailTokenVerity);

    // 로그인 성공한 경우 LoginCount를 초기화 한다
    int clearUserLoginCount(Long userId);

    int cancelTermsAgree(Long userId);

    // Read user_authority

    /**
     * Get user_authority list
     *
     * @param userId
     * @return
     */
    List<String> getUserAuthListByUserId(Long userId);

    // Write user_authority

    /**
     * Insert user_authority
     *
     * @param userAuthority
     * @return
     */
    int insertUserAuthority(UserAuthority userAuthority);


    // Write IP

    /**
     * Update IP to allow Admin system usage.
     *
     * @param params
     * @return
     */
    int updateAdminIpInfo(Map<String, Object> params);


    // Write login_hist

    /**
     * Insert login_hist. login and logout history.
     *
     * @param params
     * @return
     */
    int addLogInOutHist(Map<String, Object> params);

    List<Long> getOrderByDescNewUserIds(int topCount);

    UserAgreeTerms getUserAgreeTermsByLoginId(String loginId);

    UserAgreeTerms getUserAgreeTermsByUserId(Long userId);

    int getCountNewUsers(DurationDateVO date);

    List<CountNewUsersAndTravelersByCountryVO> getCountNewUsersAndTravelersByCountry(DurationDateVO date);

    List<User> getUsersLimit(Map<String, String> params);


    int insertUserUnMaskedLog(UserUnmaskedLog userUnmaskedLog);

    int updateUserInfo(User user);

    List<User> getUsersByUserSearch(UserSearch userSearch);

    @Select(" select count(IF(limited = 'N', 0, 1)) from user_edd where user_id = #{userId} ")
    boolean isEddUser(Long userId);

    List<User> getUsersByUserIds(@Param("userIds") List<Long> userIds);

    int getUserRoleByUserId(@Param("userId") Long userId, @Param("authorityName") String authorityName);
}
