package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.user.dto.UserSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
@Service
public class UserRepositoryService {

    private final UserMapper userMapper;
    private final GlobalQueueService globalQueueService;

    /**
     * Update user info
     *
     * @param user
     * @return
     */
    @Transactional
    public int updateUser(User user) {
        int i = userMapper.updateUser(user);
        globalQueueService.sendLoginInfo(user);
        return i;
    }

    public void updateUserLangKey(Long userId, String langKey) {
        User user = new User();
        user.setId(userId);
        user.setLangKey(langKey);
        userMapper.updateUserLangKey(user);
    }

    public User getUserByUserId(Long userId) {
        return userMapper.getUserByUserId(userId);
    }

    public List<User> getUsersByUserIds(List<Long> userIds) {
        return userMapper.getUsersByUserIds(userIds);
    }

    public User getUserByUserId(Traveler traveler) {
        return userMapper.getUserByUserId(traveler.getUserId());
    }

    public List<User> getHkUsers(Long userId) {
        return userMapper.getHkUsers(userId);
    }

    public boolean isDuplicatedCertificationId(String certificationId) {
        return userMapper.getCertificationCount(certificationId) > 0;
    }

    public User getUserByTravelerId(Long travelerId) {
        return userMapper.getUserByTravelerId(travelerId);
    }

    public String getUserOtp(Long userId) {
        return userMapper.getUserOtp(userId);
    }

    public User getFirstUser() {
        return userMapper.getFirstUser();
    }

    public User getUserByLoginId(String loginId) {
        return userMapper.getUserByLoginId(loginId);
    }

    public UserAgreeTerms getUserAgreeTermsByLoginId(String loginId) {
        return userMapper.getUserAgreeTermsByLoginId(loginId);
    }

    public UserAgreeTerms getUserAgreeTermsByUserId(Long userId) {
        return userMapper.getUserAgreeTermsByUserId(userId);
    }

    public User getUserByEmail(String email) {
        return userMapper.getUserByEmail(email);
    }

    @Transactional
    public int insertOtp(User user) {
        return userMapper.insertOtp(user);
    }

    public List<User> getUsers(UserSearchRequest userSearchRequest) {
        UserSearch userSearch = userSearchRequest.toEntity();
        return userMapper.getUsersByUserSearch(userSearch);
    }

    public User getUserByWithdrawalCashoutId(long cashOutId) {
        return userMapper.getUserByWithdrawalCashoutId(cashOutId);
    }

    public int getUserRoleByUserId(Long userId, String authorityName) {
        return userMapper.getUserRoleByUserId(userId, authorityName);
    }

    @Transactional
    public int clearUserLoginCount(Long userId) {
        return userMapper.clearUserLoginCount(userId);
    }
}
