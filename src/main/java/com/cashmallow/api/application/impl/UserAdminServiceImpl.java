package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.UserAdminService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

    private final UserMapper userMapper;

    private final Map<Long, String> adminNameList = new HashMap<>();

    @PostConstruct
    private void init() {
        List<User> adminUsers = getAdminUsers();
        for (var user : adminUsers) {
            final Long userId = user.getId();
            if (StringUtils.isNotBlank(user.getEmail())) {
                adminNameList.put(userId, user.getEmail().split("@")[0]);
            }
        }
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<User> getAdminUsers() {
        return userMapper.getAdminUsers();
    }

    @Override
    public String getAdminName(long userId) {
        return adminNameList.getOrDefault(userId, String.valueOf(userId));
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<String> getUserAuthListByUserId(Long userId) {
        return userMapper.getUserAuthListByUserId(userId);
    }

}
