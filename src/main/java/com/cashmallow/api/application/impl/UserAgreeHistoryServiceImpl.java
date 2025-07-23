package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.UserAgreeHistoryService;
import com.cashmallow.api.domain.model.user.UserAgreeHistory;
import com.cashmallow.api.domain.model.user.UserAgreeHistoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserAgreeHistoryServiceImpl implements UserAgreeHistoryService {

    private final UserAgreeHistoryMapper userAgreeHistoryMapper;

    @Transactional
    @Override
    public int insertUserAgreeHistory(List<UserAgreeHistory> userAgreeHistoryList) {
        return userAgreeHistoryMapper.insertUserAgreeHistory(userAgreeHistoryList);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserAgreeHistory> getMaxVersionUserAgreeHistories(Long userId) {
        return userAgreeHistoryMapper.getMaxVersionUserAgreeHistories(userId);
    }
}
