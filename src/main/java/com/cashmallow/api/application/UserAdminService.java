package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.user.User;

import java.util.List;

public interface UserAdminService {

    /**
     * get All Admin users
     *
     * @return
     */
    List<User> getAdminUsers();

    String getAdminName(long userId);

    /**
     * get user's auth list
     *
     * @param userId
     * @return
     */
    List<String> getUserAuthListByUserId(Long userId);

}