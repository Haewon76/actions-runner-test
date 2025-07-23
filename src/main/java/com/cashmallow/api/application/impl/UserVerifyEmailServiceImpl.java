package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.UserVerifyEmailService;
import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.model.notification.EmailVerityType;
import com.cashmallow.api.domain.model.notification.NotificationMapper;
import com.cashmallow.api.domain.model.user.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.cashmallow.api.domain.model.notification.EmailVerityType.VERIFY;
import static com.cashmallow.common.HashUtil.getMd5Hash;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserVerifyEmailServiceImpl implements UserVerifyEmailService {

    private final UserMapper userMapper;
    private final NotificationMapper notificationMapper;

    @Override
    public void addEmailCertNum(String email, String code) {
        // 이메일 인증 코드 save
        notificationMapper.addVerifiedEmailPassword(new EmailTokenVerity(getMd5Hash(email + code),
                -1L,
                VERIFY.getMaxFailCount(),
                EmailVerityType.findByType(VERIFY.getMaxFailCount())
        ));
    }

    @Override
    public void addVerifiedEmailPassword(EmailTokenVerity emailTokenVerity) {
        notificationMapper.addVerifiedEmailPassword(emailTokenVerity);
    }
}
