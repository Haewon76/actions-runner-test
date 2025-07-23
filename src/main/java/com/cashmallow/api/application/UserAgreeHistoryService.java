package com.cashmallow.api.application;

import com.cashmallow.api.domain.model.user.UserAgreeHistory;

import java.util.List;

public interface UserAgreeHistoryService {
    int insertUserAgreeHistory(List<UserAgreeHistory> userAgreeHistoryList);

    List<UserAgreeHistory> getMaxVersionUserAgreeHistories(Long userId);
}
