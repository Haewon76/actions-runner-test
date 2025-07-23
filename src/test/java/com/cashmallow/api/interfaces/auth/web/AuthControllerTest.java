package com.cashmallow.api.interfaces.auth.web;

import com.cashmallow.api.application.UserService;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.application.impl.UserServiceImpl;
import com.cashmallow.api.domain.model.notification.EmailVerityType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.config.EnableDevLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@SpringBootTest
@Slf4j
@Transactional
@EnableDevLocal
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthControllerTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private MessageSource messageSource;

    @Test
    @Disabled
    void 계정_로그인_실패_5회_이후_계정이_블럭되는지_테스트() {
        String email = "tiger003@ruu.kr";
        Locale locale = Locale.getDefault();

        String loginId = email.replaceAll("[^A-Za-z0-9]", "");
        String password = "password";
        String instanceId = "instanceId";
        String deviceInfo = "deviceInfo";
        String ip = "192.168.0.1";
        String deviceType = "A";
        String appbuild = "0";
        String deviceOsVersion = "0";

        for (int i = 0; i < 10; i++) {
            try {
                String refreshToken = travelerService.travelerLogin(loginId, password, instanceId, deviceInfo, ip, locale, deviceType, appbuild, deviceOsVersion, null);
                log.debug(refreshToken);
            } catch (Exception e) {
                final String message = e.getMessage();
                log.error(message);
                if (EmailVerityType.BLOCKED.getMaxFailCount() <= i && UserServiceImpl.USER_LOGIN_BLOCKED.equals(message)) {
                    Assertions.assertEquals(message, UserServiceImpl.USER_LOGIN_BLOCKED);
                } else {
                    Assertions.assertEquals(message, UserServiceImpl.USER_LOGIN_CHECK_ID_PW);
                }
            }
        }
    }

    @Test
    @Disabled
    void 비밀번호_찾기_연속2회_호출불가_1분이후에_가능_테스트() throws CashmallowException {
        String email = "tiger003@ruu.kr";
        Locale locale = Locale.getDefault();
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                userService.passwordResetAndSendEmail(email, locale);
            } else {
                Assertions.assertThrows(CashmallowException.class, () -> userService.passwordResetAndSendEmail(email, locale));
            }
        }
    }

    @Test
    @Disabled
    void 비밀번호_찾기_어드민_계정으로_동작_안하는지_테스트() {
        String email = "jd@cashmallow.com"; //어드민 계정
        Locale locale = Locale.getDefault();

        try {
            userService.passwordResetAndSendEmail(email, locale);
        } catch (Exception e) {
            final String message = e.getMessage();
            log.error(message);
            Assertions.assertEquals(UserServiceImpl.USER_LOGIN_RESET_PWD_NOT_FOUND_EMAIL, message);
        }
    }


    @Test
    @Disabled
    void 비밀번호_변경시_과거_사용된_비밀번호_사용_불가_테스트() throws CashmallowException {
        String email = "tiger003@ruu.kr";
        Locale locale = Locale.getDefault();

        // 비밀번호 찾기 메일 발송
        userService.passwordResetAndSendEmail(email, locale);


        String currentPassword = "ASDLKJSADKJ";
        String newPassword = "SSADSAKJSALDJ";

        String loginId = email.replaceAll("[^A-Za-z0-9]", "");
        final User user = userRepositoryService.getUserByLoginId(loginId);

        userService.changePassword(user.getId(), currentPassword, newPassword);
    }

    @Disabled
    @Test
    void 비밀번호_변1경시_과거_사용된_비밀번호_사용_불가_테스트() throws CashmallowException {
        String email = "tiger003@ruu.kr";
        Locale locale = Locale.getDefault();

        Object[] args = new Object[]{Const.MIN_PWD_LEN};
        final String str = "Your password must be at least {0} characters long, including letters, numbers, and special characters.";
        final String message = messageSource.getMessage(str, args, str, locale);
        System.out.println("message: " + message);
    }
}