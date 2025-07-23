package com.cashmallow.api.domain.model.user;

import java.util.List;

public interface UserAgreeHistoryMapper {

    int insertUserAgreeHistory(List<UserAgreeHistory> userAgreeHistoryList);

    List<UserAgreeHistory> getUserAgreeHistoriesByUserId(Long userId);

    List<UserAgreeHistory> getMaxVersionUserAgreeHistories(Long userId);

    int deleteUserAgreeHistory(Long id);
}
