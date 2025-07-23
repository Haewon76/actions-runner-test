package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.notification.EmailTokenVerity;
import com.cashmallow.api.domain.model.notification.EmailVerityType;
import com.cashmallow.api.domain.model.terms.TermsHistory;
import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.domain.model.user.*;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.DurationDateVO;
import com.cashmallow.api.domain.shared.InvalidPasswordException;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.terms.TermsHistoryService;
import com.cashmallow.api.interfaces.traveler.dto.TermsHistoryVO;
import com.cashmallow.api.interfaces.user.dto.CountNewUsersAndTravelersByCountryVO;
import com.cashmallow.common.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_PASSWORD_MATCH;
import static com.cashmallow.common.CommonUtil.getRandomToken;
import static com.cashmallow.common.HashUtil.getMd5Hash;
import static com.cashmallow.common.RandomUtil.ALPHA_NUMERIC;
import static com.cashmallow.common.RandomUtil.generateRandomString;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    public static final String USER_LOGIN_CHECK_ID_PW = "USER_LOGIN_CHECK_ID_PW";
    public static final String USER_LOGIN_BLOCKED = "USER_LOGIN_BLOCKED";
    private static final String SETTING_ERROR_CURRENT_PWD_NOT_MATCH = "SETTING_ERROR_CURRENT_PWD_NOT_MATCH";
    private static final String USER_PASSWORD_DUPLICATED = "USER_PASSWORD_DUPLICATED";
    public static final String USER_LOGIN_RESET_PWD_NOT_FOUND_EMAIL = "USER_LOGIN_RESET_PWD_NOT_FOUND_EMAIL";
    public static final String USER_LOGIN_RESET_PWD_NOT_AVAILABLE = "USER_LOGIN_RESET_PWD_NOT_AVAILABLE";
    private static final String SIGNUP_ERROR_AGE_CONSTRAINT = "SIGNUP_ERROR_AGE_CONSTRAINT";
    private static final String ROLLBACK_FOR_TEST = "ROLLBACK_FOR_TEST";

    private static final String REQUIRED_TERMS_UNCHECKED = "REQUIRED_TERMS_UNCHECKED";

    private Random random = new SecureRandom();

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private AuthService authService;

    @Autowired
    private FileService fileService;

    @Autowired
    private UserVerifyEmailService userVerifyEmailService;

    @Autowired
    private UserAgreeHistoryService userAgreeHistoryService;

    @Autowired
    private TermsHistoryService termsHistoryService;

    @Autowired
    private EasyLoginHistMapper easyLoginHistMapper;

    @Autowired
    private EasyLoginMapper easyLoginMapper;

    @Autowired
    private UserAdminServiceImpl userAdminService;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private CountryService countryService;
    @Autowired
    private CouponMobileServiceV2 couponMobileServiceV2;
    @Autowired
    private GlobalQueueService globalQueueService;

    /**
     * 주어진 userId를 가진 사용자가 지정된 역할을 가지고 있는지를 확인합니다.
     *
     * @param userId    역할을 확인할 사용자의 ID입니다.
     * @param roleCodes 확인할 역할의 코드들입니다.
     * @return 사용자가 지정된 역할을 가지고 있으면 true, 그렇지 않으면 false를 반환합니다.
     */
    @Override
    public boolean isVerifyRole(Long userId, String... roleCodes) {
        // userId null 체크
        if (userId == null || roleCodes == null) {
            return false;
        }

        List<String> auths = userAdminService.getUserAuthListByUserId(userId);

        // 모든 권한이 없는지 체크
        if (auths.isEmpty()) {
            return false;
        }

        // roleCodes의 권한을 가지고 있는지 체크 - A, B, C 중 하나라도 가지고 있으면 true
        boolean hasRole = auths.stream()
                // .peek(item -> System.out.println("Role Name: " + item))  //
                .anyMatch(role -> Arrays.asList(roleCodes).contains(role));
        log.debug("userId : {}, hasRole : {}, roleCodes : {}, auths : {}", userId, hasRole, roleCodes, auths);
        return hasRole;
    }

    /**
     * adminName을 반환, 없으면 userId를 그대로 반환.
     *
     * @return
     */
    @Override
    public void validatesEmailCertNum(String email, String code, Locale locale) throws CashmallowException {
        // 이메일 인증 코드 check
        final EmailTokenVerity emailTokenVerity = notificationService.getVerifiedEmailCode(getMd5Hash(email + code));
        if (emailTokenVerity == null) {
            throw new CashmallowException("MAIL_SUBJECT_EMAIL_AUTH_FAIL");
        }
    }

    // 기능: 10.1.1. ~ 10.1.3. 로긴 처리. DAO로 부터 사용자 권한 정보를 구하여 OAuto에 등록한다.
    @Override
    public String login(String userName, String password, String cls) throws CashmallowException {
        String method = "login()";
        logger.info("{} username={}", method, userName);

        userName = CustomStringUtil.trim(userName);
        password = CustomStringUtil.trim(password);
        password = CustomStringUtil.subStr(password, 0, Const.MAX_PWD_LEN);

        // 1. 사용자 아이디와 암호가 일치하는 ID.를 구한다. 일치하는 자료가 없을 경우 null을 응답하므로 결과를 Object 형태로 받는다.
        HashMap<String, Object> params = new HashMap<>();
        params.put("loginId", userName);
        params.put("password", securityService.encryptSHA2(password));
        params.put("cls", cls);
        User user = userMapper.checkUserPwd(params);

        // 2. id가 존재하면 id에 대한 권한 정보를 구한다.
        Long userId = user.getId();
        logger.info("{}: id = {}", method, userId);

        List<String> authList = userMapper.getUserAuthListByUserId(userId);
        if (authList == null || authList.isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();

        // 3. 권한 정보가 존재하면 사용자 정보에 login, id, 권한 등을 설정한다.
        long travelerId = Const.NO_USER_ID;
        long storekeeperId = Const.NO_USER_ID;

        AccessToken accessToken = new AccessToken();
        accessToken.setLogin(userName);
        accessToken.setAccessTime(CommDateTime.getCurrentDateTime());
        accessToken.setUserId(userId);
        accessToken.setTravelerId(travelerId);
        accessToken.setStorekeeperId(storekeeperId);

        try {
            String auths = mapper.writeValueAsString(authList);
            accessToken.setAuths(auths);
        } catch (JsonProcessingException e) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String token = authService.add(accessToken);
        logger.info("{}: login() 결과: userName={}", method, userName);

        long managerId = authService.getUserId(token);

        if (managerId != Const.NO_USER_ID && accessToken.getAuths().indexOf(Const.ROLE_ASSIMAN) >= 0) {
            // admin user login 경우 history 기록함.
            params = new HashMap<>();
            params.put("user_id", managerId);
            params.put("log_type", "I"); // I : login, O : logout

            int affectedRow = userMapper.addLogInOutHist(params);

            // admin user login 경우 history 남기는데 실패하면 실패한 결과를 리턴. 
            if (affectedRow != 1) {
                logger.error("Insert login_hist failed!");
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        }

        return token;

    }

    /**
     * id, password 검증 성공시 user리턴
     *
     * @param loginId
     * @param password
     * @return
     * @throws CashmallowException
     */
    public User checkLoginAndPassword(String loginId, String password) throws CashmallowException {
        Map<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("cls", Const.CLS_TRAVELER);

        User user = userMapper.checkUserWithoutPassword(params);

        if (user == null || !user.isActivated() || user.isNotMatchedPassword(securityService.encryptSHA2(password))) {
            if (user != null) {
                final EmailTokenVerity emailTokenVerity = new EmailTokenVerity(user.getId(), user.getPasswordHash());
                // 계정이 블럭 되어있다면, 임의의 비밀번호로 변경한다
                if (EmailVerityType.BLOCKED == user.getEmailVerityType()) {
                    emailTokenVerity.setPassword(securityService.encryptSHA2(getRandomToken()));
                    // 간편 로그인 정보도 삭제한다.
                    deleteEasyLogin(user.getId());
                }
                userMapper.updateUserLoginCount(emailTokenVerity);

                // 계정이 블록되었는지 체크한다.
                if (EmailVerityType.BLOCKED == user.getEmailVerityType()) {
                    throw new CashmallowException(USER_LOGIN_BLOCKED);
                }
            }

            throw new CashmallowException(USER_LOGIN_CHECK_ID_PW);
        }

        return user;
    }

    /**
     * 간편 로그인 정보 삭제
     *
     * @param userId
     */
    public void deleteEasyLogin(Long userId) {
        try {
            EasyLoginHist easyLoginHistParams = new EasyLoginHist();
            easyLoginMapper.deleteEasyLoginByUserId(userId);
            authService.deleteRefreshToken(userId);

            final Timestamp now = Timestamp.valueOf(LocalDateTime.now());
            EasyLogin easyLoginParams = EasyLogin.builder()
                    .refreshToken("BLOCKED USER")
                    .userId(userId)
                    .pinCodeHash(securityService.encryptSHA2("BLOCKED USER"))
                    .refreshTime(now)
                    .failCount(0)
                    .createdAt(now)
                    .build();

            BeanUtils.copyProperties(easyLoginParams, easyLoginHistParams);
            easyLoginHistParams.setLoginSuccess(Const.N);
            easyLoginHistParams.setCreatedAt(now);

            easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 간편 로그인으로 인해 boolean encrypted 추가, 간편로그인의 경우 password를 db에서 가져 와서 바로 refreshToken은 만들어 준다.
     *
     * @param loginId
     * @param password
     * @param cls
     * @param encrypted
     * @return
     * @throws CashmallowException
     */
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public String loginForRefreshToken(String loginId, String password, String cls, boolean encrypted) throws CashmallowException {
        String method = "loginForRefreshToken()";
        logger.info("{}: username={}", method, loginId);

        loginId = CustomStringUtil.trim(loginId);
        password = CustomStringUtil.trim(password);

        if (encrypted) {
            password = CustomStringUtil.subStr(password, 0, Const.MAX_PWD_LEN);
        }

        // 1. 사용자 아이디와 암호가 일치하는 ID.를 구한다. 일치하는 자료가 없을 경우 null을 응답하므로 결과를 Object 형태로 받는다.
        HashMap<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);

        if (encrypted) {
            params.put("password", securityService.encryptSHA2(password));
        } else {
            params.put("password", password);
        }

        params.put("cls", cls);
        User user = userMapper.checkUserPwd(params);

        // wrong password
        if (user == null) {
            throw new CashmallowException(USER_LOGIN_CHECK_ID_PW);
        }

        // 2. id가 존재하면 id에 대한 권한 정보를 구한다.
        Long userId = user.getId();
        logger.info("{}: id = {}", method, userId);

        List<String> authList = userMapper.getUserAuthListByUserId(userId);
        if (authList == null || authList.isEmpty()) {
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();

        // 3. 권한 정보를 가져온다.
        String auths = null;
        try {
            auths = mapper.writeValueAsString(authList);
        } catch (JsonProcessingException e) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String refreshToken = authService.issueRefreshToken(loginId, userId, auths);

        logger.info("{}: login() 결과: userName={}", method, loginId);

        if (authList.contains(Const.ROLE_ASSIMAN)) {
            // admin user login 경우 history 기록함.
            params = new HashMap<>();
            params.put("user_id", userId);
            // I : login, O : logout
            params.put("log_type", "I");

            int affectedRow = userMapper.addLogInOutHist(params);

            // admin user login 경우 history 남기는데 실패하면 실패한 결과를 리턴.
            if (affectedRow != 1) {
                logger.error("Insert login_hist failed!");
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return refreshToken;
    }

    /**
     * Login. Get new token. If new device, send the email to warn
     *
     * @param userName
     * @param password
     * @param instanceId
     * @param deviceInfo
     * @param ip
     * @param cls
     * @return
     * @throws CashmallowException
     */
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public String login(String userName, String password, String instanceId, String deviceInfo, String ip, String cls) throws CashmallowException {
        String method = "login()";

        logger.info("{} username={}", method, userName);

        String newToken = login(userName, password, cls);

        Long userId = authService.getUserId(newToken);
        User user = userRepositoryService.getUserByUserId(userId);

        // instanId(admin의 경우 IP)를 비교해서 체크 
        if (!StringUtils.isEmpty(instanceId)) {
            String instanceIdEncryted = securityService.encryptSHA2(instanceId);

            if (!StringUtils.isEmpty(user.getInstanceId()) && !user.getInstanceId().equals(instanceIdEncryted)) {
                // Device 불일치 경우 처리 

                // 경고 메일 발송 (admin user) 
                notificationService.sendEmailToWarnNewDevice(user, deviceInfo, ip);

                // Update instanceId 
                user.setInstanceId(instanceIdEncryted);
                userRepositoryService.updateUser(user);

            }
        }

        return newToken;
    }

    // 기능: 11.1. 사용자 회원 가입(user 등록)
    @Transactional(rollbackFor = CashmallowException.class)
    public Long registerUserV3(User user, String password, String recommenderEmail,
                             List<TermsType> termsTypeList) throws CashmallowException {

        String[] auths = CustomStringUtil.getAuthStr(Const.ROLE_USER);

        String method = "registerUserV3()";

        logger.debug("{}: user={}", method, user);

        // 패스워드 검증
        CommonUtil.isValidPassword(user, password);

        if (StringUtils.isEmpty(user.getLastName())) {
            user.setLastName("");
        }
        if (StringUtils.isEmpty(user.getFirstName())) {
            user.setFirstName(user.getEmail());
        }

        // encrypt password
        String passwordHash = securityService.encryptSHA2(password);
        user.setPasswordHash(passwordHash);
        user.setInstanceId(securityService.encryptSHA2(user.getInstanceId()));

        User userByLoginId = userMapper.getUserByLoginId(user.getLogin());
        if (userByLoginId != null) {
            throw new CashmallowException(USER_LOGIN_CHECK_ID_PW);
        }
        userMapper.insertUser(user);

        // result of selectedKey tag of MyBatis
        long userId = user.getId();

        // register userAuthority
        registerUsersAuths(userId, auths);

        List<UserAgreeHistory> insertAgreeList = new ArrayList<>();

        Set<TermsType> termsTypeSet = new HashSet<>(termsTypeList);
        List<TermsHistory> recentVersionHistories = termsHistoryService
                .getRecentVersionHistories(user.getCountry(), true);
        for (TermsHistory recentVersionHistory : recentVersionHistories) {
            if (recentVersionHistory.getRequired() && !termsTypeSet.contains(recentVersionHistory.getType())) {
                throw new CashmallowException(REQUIRED_TERMS_UNCHECKED);
            }
            boolean agreed = termsTypeSet.contains(recentVersionHistory.getType());
            insertAgreeList.add(getUserAgreeHistory(userId, recentVersionHistory, agreed));
        }

        userAgreeHistoryService.insertUserAgreeHistory(insertAgreeList);

        // 회원 가입 쿠폰 증정
        try {
            // countryCode(캐시멜로 관리 코드): 001, 004
            String countryCode = user.getCountry();
            Country country = countryService.getCountry(countryCode);
            // iso3166 코드: HK, JP
            String iso3166 = country.getIso3166();
            ZoneId zoneId = CountryCode.fromIso3166(iso3166).getZoneId();

            couponMobileServiceV2.issueMobileCouponsV3(SystemCouponType.welcome.getCode(), iso3166, country.getIso4217(), userId, zoneId, null, null);

        } catch (Exception e) {
            log.error("User Welcome Coupon Issue Fail : " + userId);
        }

        globalQueueService.sendUserRegister(user);

        return userId;
    }

    // 기능: 11.1. 사용자 회원 가입(user 등록)
    @Transactional(rollbackFor = CashmallowException.class)
    public User updateTerms(Long userId) {
        User user = userRepositoryService.getUserByUserId(userId);

        List<UserAgreeHistory> insertAgreeList = new ArrayList<>();

        List<TermsHistory> recentVersionHistories = termsHistoryService
                .getRecentVersionHistories(user.getCountry(), true);

        Map<TermsType, Integer> userAgreeHistoryMap = userAgreeHistoryService.getMaxVersionUserAgreeHistories(userId).stream()
                .collect(Collectors.toMap(UserAgreeHistory::getTermsType, UserAgreeHistory::getVersion));

        for (TermsHistory recentVersionHistory : recentVersionHistories) {
            //유저 동의 이력이 존재하고 최신 버전의 약관과 버전이 같은 경우 무시함
            if (userAgreeHistoryMap.containsKey(recentVersionHistory.getType()) &&
                    userAgreeHistoryMap.get(recentVersionHistory.getType()).compareTo(recentVersionHistory.getVersion()) == 0) {
                continue;
            }
            insertAgreeList.add(getUserAgreeHistory(userId, recentVersionHistory, true));
        }

        if (!insertAgreeList.isEmpty()) {
            userAgreeHistoryService.insertUserAgreeHistory(insertAgreeList);
        }

        user.setAgreeTerms("Y");
        user.setAgreePrivacy("Y");

        userRepositoryService.updateUser(user);

        return user;
    }

    @Transactional
    @Override
    public void cancelTermsAgree(Long userId) {
        userMapper.cancelTermsAgree(userId);
    }

    /**
     * 사용자의 권한들을 등록한다.
     *
     * @param userId
     * @param auths
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void registerUsersAuths(long userId, String[] auths) throws CashmallowException {
        String method = "registerUsersAuths()";

        if (auths == null || auths.length < 1) {
            logger.error("{}: userAuthority parameter is empty.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        for (String auth : auths) {
            if (auth != null && !auth.isEmpty()) {
                userMapper.insertUserAuthority(new UserAuthority(userId, auth));
            }
        }
    }

    // 기능: 10.1.4. ~ 10.1.5. 사용자의 암호를 변경한다.
    @Override
    @Transactional
    public ApiResultVO changePassword(String token, String currentPassword, String newPassword) {
        ApiResultVO result = new ApiResultVO(Const.CODE_INVALID_TOKEN);
        String method = "changePassword()";

        if (token == null) {
            return result;
        }

        long userId = authService.getUserId(token);

        if (userId == Const.NO_USER_ID) {
            return result;
        }

        String error = "";
        logger.info(method);

        currentPassword = CustomStringUtil.trim(currentPassword);
        currentPassword = CustomStringUtil.subStr(currentPassword, 0, Const.MAX_PWD_LEN);

        newPassword = CustomStringUtil.trim(newPassword);
        newPassword = CustomStringUtil.subStr(newPassword, 0, Const.MAX_PWD_LEN);

        try {
            User user = userRepositoryService.getUserByUserId(userId);
            CommonUtil.isValidPassword(user, newPassword);

            String currentEncryptPassword = securityService.encryptSHA2(currentPassword);
            if (user.getPasswordHash().equalsIgnoreCase(currentEncryptPassword)) {

                String newEncryptPassword = securityService.encryptSHA2(newPassword);

                isNotAvailablePassword(userId, newEncryptPassword); // 2024.06 웹 취약점 대응(안랩)

                user.setPasswordHash(newEncryptPassword);
                int affectedRow = userRepositoryService.updateUser(user);

                if (affectedRow == 0) {
                    result.setFailInfo(INTERNAL_SERVER_ERROR);
                    return result;
                }
            } else {
                logger.error("{} 사용자 암호가 일치하지 않습니다.", method);
                result.setFailInfo(SETTING_ERROR_CURRENT_PWD_NOT_MATCH);
                return result;
            }
        } catch (InvalidPasswordException e) {
            logger.info(e.getMessage());
            Locale locale = LocaleContextHolder.getLocale();
            String message = messageSource.getMessage(e.getMessage(), null, locale);
            result.setFailInfo(message);
            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setFailInfo(INTERNAL_SERVER_ERROR);
            return result;
        }

        result.setSuccessInfo();

        return result;
    }


    // 기능: 10.1.4. ~ 10.1.5. 사용자의 암호를 변경한다.
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public void changePassword(long userId, String currentPassword, String newPassword) throws CashmallowException {
        String method = "changePassword()";

        String error = "";
        logger.info(method);

        currentPassword = CustomStringUtil.trim(currentPassword);
        currentPassword = CustomStringUtil.subStr(currentPassword, 0, Const.MAX_PWD_LEN);

        newPassword = CustomStringUtil.trim(newPassword);
        newPassword = CustomStringUtil.subStr(newPassword, 0, Const.MAX_PWD_LEN);

        User user = userRepositoryService.getUserByUserId(userId);
        CommonUtil.isValidPassword(user, newPassword);

        String currentEncryptPassword = securityService.encryptSHA2(currentPassword);

        if (currentEncryptPassword != null && currentEncryptPassword.equalsIgnoreCase(user.getPasswordHash())) {
            String newEncryptPassword = securityService.encryptSHA2(newPassword);
            user.setPasswordHash(newEncryptPassword);

            isNotAvailablePassword(userId, newEncryptPassword); // 2024.06 웹 취약점 대응(안랩)
            int affectedRow = userRepositoryService.updateUser(user);

            if (affectedRow != 1) {
                logger.warn("{}: Failed to update user.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else {
            logger.warn("{}: User's current password does not match.", method);
            throw new CashmallowException(SETTING_ERROR_CURRENT_PWD_NOT_MATCH);
        }
    }

    /**
     * 사용자의 비밀번호 재사용 여부를 체크한다.
     * @param userId
     * @param newEncryptPassword
     * @throws CashmallowException
     */
    private void isNotAvailablePassword(long userId, String newEncryptPassword) throws CashmallowException {
        // 비밀번호 중복 체크 (과거에 사용했던 비밀번호 사용 불가)
        if (notificationService.isNotAvailablePassword(new EmailTokenVerity(userId, newEncryptPassword))) {
            throw new InvalidPasswordException(USER_PASSWORD_DUPLICATED);
        }

        // 초기화한 비밀번호 저장
        userVerifyEmailService.addVerifiedEmailPassword(new EmailTokenVerity(getRandomToken(), userId, newEncryptPassword, EmailVerityType.RESET));
    }


    // 기능: 10.3.2. profile 사진 업데이트
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public String uploadProfilePhoto(long userId, MultipartFile file) throws CashmallowException {
        String method = "updateProfilePhoto()";

        String fileName = null;

        logger.info("{}: update profile photo. userId={}", method, userId);

        User user = userMapper.getUserByUserId(userId);
        if (user == null) {
            logger.error("{}: cannot find user by userId. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        fileName = fileService.upload(file, Const.FILE_SERVER_PROFILE);

        user.setProfilePhoto(fileName);
        user.setProfilePhotoUrl(envUtil.getCdnUrl(), fileName);
        int rows = userRepositoryService.updateUser(user);
        if (rows != 1) {
            logger.error("{}: failed to update user info. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return user.getProfilePhotoUrl();
    }


    /* (non-Javadoc)
     * @see com.cashmallow.api.application.impl.AdminV2Service#searchUsers(java.util.HashMap)
     */
    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<User> searchUsers(String keyword) throws CashmallowException {
        logger.info("searchUsers() : keyword={}", keyword);

        if (StringUtils.isEmpty(keyword)) {
            throw new CashmallowException("At least one search keyword is required.");
        }

        List<User> usersResult = userMapper.searchUsers(keyword);

        usersResult.stream()
                .filter(user -> StringUtils.isNotEmpty(user.getEnFirstName()) && StringUtils.isNotEmpty(user.getEnLastName()))
                .forEach(user -> {
                    user.setFirstName(user.getEnFirstName());
                    user.setLastName(user.getEnLastName());
                });

        return usersResult;
    }

    // 기능: email 계정의 password 를 임의로 리셋하여 메일로 발송한다.
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public String passwordResetAndSendEmail(String email, Locale locale) throws CashmallowException {

        String method = "passwordResetAndSendEmail()";
        logger.info("{}: email={}, languageType={}", method, email, locale.getLanguage());

        String loginId = email.replaceAll("[^A-Za-z0-9]", "");
        User user = userRepositoryService.getUserByLoginId(loginId);

        // 여행자 계정만 비밀번호를 재설정할 수 있다.
        if (user == null || user.getPasswordHash() == null || user.isNotTraveler()) {
            throw new CashmallowException(USER_LOGIN_RESET_PWD_NOT_FOUND_EMAIL);
        }

        // 1분이내의 비밀번호 요청이 있는지 체크
        if (notificationService.isNotAvailableEmailVerify(user.getId())) {
            throw new CashmallowException(USER_LOGIN_RESET_PWD_NOT_AVAILABLE);
        }

        notificationService.sendEmailToResetPassword(user, email, locale);

        return email;
    }


    // 기능: email 계정의 password 를 임의로 리셋하여 메일로 발송한다.
    public String passwordResetAndSendEmailForAdmin(String email) throws CashmallowException {
        String loginId = email.replaceAll("[^A-Za-z0-9]", "");
        User user = userRepositoryService.getUserByLoginId(loginId);
        return notificationService.sendEmailToResetPassword(user, email, Locale.getDefault());
    }

    @Override
    public EmailTokenVerity passwordResetAndVerity(String token) {
        return notificationService.passwordResetAndVerity(token);
    }

    @Override
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getUserCntByCountry(String country) {
        return userMapper.getUserCntByCountry(country);
    }


    /**
     * 사용자의 사용 언어 코드
     *
     * @param userID
     * @return ko zh en ja fr vi th de
     */
    @Override
    public String getLangKey(Long userID) {
        User userInfo = userRepositoryService.getUserByUserId(userID);
        return userInfo.getLangKey();

    }

    @Override
    public void addSCBMessage(Model model, Locale locale) {
        // 인출 요청
        String SCB_WITHDRAWAL_ATM_URL = messageSource.getMessage("SCB_WITHDRAWAL_ATM_URL", null, locale);
        String SCB_WITHDRAWAL_CDM_URL = messageSource.getMessage("SCB_WITHDRAWAL_CDM_URL", null, locale);
        String SCB_WITHDRAWAL_REQUEST_TITLE = messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_TITLE", null, locale);
        String SCB_WITHDRAWAL_REQUEST_DESCRIPTION = messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION", null, locale);
        String SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK = messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK", null, locale);
        String SCB_WITHDRAWAL_REQUEST_BUTTON = messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_BUTTON", null, locale);
        String SCB_WITHDRAWAL_REQUEST_ERROR = messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_ERROR", null, locale);
        List<String> SCB_WITHDRAWAL_REQUEST_DESCRIPTION_ATMS = Arrays.asList(messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_ATMS", null, locale).split("♥"));
        List<String> SCB_WITHDRAWAL_REQUEST_DESCRIPTION_CDMS = Arrays.asList(messageSource.getMessage("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_CDMS", null, locale).split("♥"));

        // 인출 인바운드
        String SCB_WITHDRAWAL_INBOUND_TITLE = messageSource.getMessage("SCB_WITHDRAWAL_INBOUND_TITLE", null, locale);
        String SCB_WITHDRAWAL_INBOUND_DESCRIPTION = messageSource.getMessage("SCB_WITHDRAWAL_INBOUND_DESCRIPTION", null, locale);
        String SCB_WITHDRAWAL_INBOUND_COUNTDOWN_PRE = messageSource.getMessage("SCB_WITHDRAWAL_INBOUND_COUNTDOWN_PRE", null, locale);
        String SCB_WITHDRAWAL_INBOUND_COUNTDOWN_POST = messageSource.getMessage("SCB_WITHDRAWAL_INBOUND_COUNTDOWN_POST", null, locale);
        String SCB_WITHDRAWAL_INBOUND_COUNTDOWN = messageSource.getMessage("SCB_WITHDRAWAL_INBOUND_COUNTDOWN", null, locale);

        model.addAttribute("SCB_WITHDRAWAL_ATM_URL", SCB_WITHDRAWAL_ATM_URL);
        model.addAttribute("SCB_WITHDRAWAL_CDM_URL", SCB_WITHDRAWAL_CDM_URL);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_ERROR", SCB_WITHDRAWAL_REQUEST_ERROR);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_TITLE", SCB_WITHDRAWAL_REQUEST_TITLE);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_DESCRIPTION", SCB_WITHDRAWAL_REQUEST_DESCRIPTION);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK", SCB_WITHDRAWAL_REQUEST_DESCRIPTION_LINK);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_BUTTON", SCB_WITHDRAWAL_REQUEST_BUTTON);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_ATMS", SCB_WITHDRAWAL_REQUEST_DESCRIPTION_ATMS);
        model.addAttribute("SCB_WITHDRAWAL_REQUEST_DESCRIPTION_CDMS", SCB_WITHDRAWAL_REQUEST_DESCRIPTION_CDMS);
        model.addAttribute("SCB_WITHDRAWAL_INBOUND_TITLE", SCB_WITHDRAWAL_INBOUND_TITLE);
        model.addAttribute("SCB_WITHDRAWAL_INBOUND_DESCRIPTION", SCB_WITHDRAWAL_INBOUND_DESCRIPTION);
        model.addAttribute("SCB_WITHDRAWAL_INBOUND_COUNTDOWN_PRE", SCB_WITHDRAWAL_INBOUND_COUNTDOWN_PRE);
        model.addAttribute("SCB_WITHDRAWAL_INBOUND_COUNTDOWN_POST", SCB_WITHDRAWAL_INBOUND_COUNTDOWN_POST);
        model.addAttribute("SCB_WITHDRAWAL_INBOUND_COUNTDOWN", SCB_WITHDRAWAL_INBOUND_COUNTDOWN);
    }

    @Override
    public List<Long> getOrderByDescNewUserIds(int limitCount) {
        return userMapper.getOrderByDescNewUserIds(limitCount);
    }

    @Override
    public int getCountNewUsers(String startDate, String endDate) {
        DurationDateVO durationDateVO = new DurationDateVO(startDate, endDate);
        return userMapper.getCountNewUsers(durationDateVO);
    }

    @Override
    public List<CountNewUsersAndTravelersByCountryVO> getCountNewUsersAndTravelersByCountry(String startDate, String endDate) {
        DurationDateVO durationDateVO = new DurationDateVO(startDate, endDate);
        return userMapper.getCountNewUsersAndTravelersByCountry(durationDateVO);
    }

    @Override
    public int insertUserUnMaskedLog(UserUnmaskedLog userUnmaskedLog) {
        return userMapper.insertUserUnMaskedLog(userUnmaskedLog);
    }

    @Transactional(readOnly = true)
    @Override
    public List<TermsHistoryVO> getUnreadTermsList(Long userId, String countryCode, Locale locale) {
        List<TermsHistory> recentVersionHistories = termsHistoryService.getRecentVersionHistories(countryCode, true)
                .stream().sorted(Comparator.comparingInt(o -> o.getType().ordinal()))
                .toList();

        Map<TermsType, Integer> maxVersionUserAgreeHistoryMap = userAgreeHistoryService.getMaxVersionUserAgreeHistories(userId)
                .stream().collect(Collectors.toMap(UserAgreeHistory::getTermsType, UserAgreeHistory::getVersion));

        List<TermsHistory> unreadTermsHistories = new ArrayList<>();
        for (TermsHistory termsHistory : recentVersionHistories) {
            //유저가 본 최신 약관과 다를경우 추가
            if (!maxVersionUserAgreeHistoryMap.containsKey(termsHistory.getType()) ||
                    maxVersionUserAgreeHistoryMap.get(termsHistory.getType()).compareTo(termsHistory.getVersion()) != 0) {
                unreadTermsHistories.add(termsHistory);
            }
        }

        return unreadTermsHistories.stream().map(termsHistory -> new TermsHistoryVO(termsHistory.getType(),
                messageSource.getMessage(termsHistory.getType().getViewTitle(countryCode), null, null, locale),
                envUtil.getStaticUrl().concat(termsHistory.getPath()),
                termsHistory.getVersion(),
                termsHistory.getRequired(),
                DateUtil.getTimestampToLocalDate(countryCode, termsHistory.getStartedAt()))
        ).toList();
    }

    @Override
    @Transactional
    public int updateUserInfo(User user) {
        return userMapper.updateUserInfo(user);
    }

    private UserAgreeHistory getUserAgreeHistory(Long userId, TermsHistory recentVersionHistory, boolean agreed) {
        return UserAgreeHistory.builder()
                .userId(userId)
                .version(recentVersionHistory.getVersion())
                .termsType(recentVersionHistory.getType())
                .agreed(agreed)
                .build();
    }

    public Map<String, String> matchPassword(long userId, String loginId, String password) throws CashmallowException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("loginId", loginId);
        params.put("password", securityService.encryptSHA2(password));
        params.put("cls", Const.CLS_TRAVELER);
        User user = userMapper.checkUserPwd(params);

        if (user == null || userId != user.getId()) {
            throw new CashmallowException(USER_LOGIN_CHECK_ID_PW);
        }

        Map<String, String> resultMap = new HashMap<>();
        String randomString = generateRandomString(ALPHA_NUMERIC, 32);
        String matchKey = REDIS_KEY_PASSWORD_MATCH + user.getId();
        redisService.put(matchKey, randomString, 10, TimeUnit.MINUTES);
        resultMap.put("otp", randomString);

        return resultMap;
    }

}
