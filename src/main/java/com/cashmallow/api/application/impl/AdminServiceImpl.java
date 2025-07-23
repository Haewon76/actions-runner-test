package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.cashmallow.api.domain.shared.Const.ROLE_ADMIN;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class AdminServiceImpl implements AdminService {
    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AlarmService alarmService;


    /**
     * Login. Get new token. If new device, send the email to warn or reset.
     *
     * @param loginId
     * @param password
     * @param instanceId
     * @param deviceInfo
     * @param ip
     * @param cls
     * @param langKey
     * @return
     * @throws CashmallowException
     */
    public String loginAdmin(String loginId, String password,
                             String instanceId, String deviceInfo, String ip,
                             Locale locale) throws CashmallowException {

        String method = "loginAdmin()";

        logger.info("{} loginId={}, instanceId={}, deviceInfo={}, ip={}, locale={}",
                method, loginId, instanceId, deviceInfo, ip, locale);

        String refreshToken = userService.loginForRefreshToken(loginId, password, Const.CLS_ADMIN, Const.TRUE);

        Long userId = authService.getUserIdByJsonWebToken(refreshToken);

        User user = userRepositoryService.getUserByUserId(userId);

        // langKey 가 있으면 업데이트 한다.
        if (locale != null) {
            user.setLangKey(locale.getLanguage());
            logger.info("{}: language={}", method, locale.getLanguage());
        }

        // instanId가 없으면 로그인하면 안된다. 에러 처리 
        if (StringUtils.isEmpty(instanceId)) {
            logger.error("{}: instanceId is empty. instanceId={}", method, instanceId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String instanceIdEncryted = securityService.encryptSHA2(instanceId);

        if (StringUtils.isEmpty(user.getInstanceId())) {
            // InstanceId 가 없으면 업데이트하고 리턴  

            user.setInstanceId(instanceIdEncryted);
            int affectedRow = userRepositoryService.updateUser(user);
            if (affectedRow != 1) {
                logger.error("{}: Failed to update User table. affectedRow={}", method, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else if (!user.getInstanceId().equals(instanceIdEncryted)) {
            // InstanceId 가 있는데 Device 불일치 경우

            // Update instanceId
            user.setInstanceId(instanceIdEncryted);
            int affectedRow = userRepositoryService.updateUser(user);

            if (affectedRow == 1) {
                // 경고 메일 발송 (admin user) 
                // notificationService.sendEmailToWarnNewDevice(user, deviceInfo, ip);
            } else {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return refreshToken;

    }

}
