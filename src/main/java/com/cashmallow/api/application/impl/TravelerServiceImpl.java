package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.aml.AMLCustomerBase;
import com.cashmallow.api.domain.model.aml.AMLCustomerIndv;
import com.cashmallow.api.domain.model.aml.AMLCustomerIndvCDD;
import com.cashmallow.api.domain.model.cashout.CashOutMapper;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.vo.SystemCouponType;
import com.cashmallow.api.domain.model.coupon.vo.AvailableStatus;
import com.cashmallow.api.domain.model.coupon.vo.CouponIssueUser;
import com.cashmallow.api.domain.model.coupon.vo.UpdateStatusUserCoupon;
import com.cashmallow.api.domain.model.notification.EmailToken;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.traveler.*;
import com.cashmallow.api.domain.model.traveler.Traveler.VerificationType;
import com.cashmallow.api.domain.model.traveler.enums.ApprovalType;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.*;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.DurationDateVO;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.infrastructure.aml.OctaAMLCustomerService;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLCustomerRequest;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.admin.dto.AdminCashOutAskVO;
import com.cashmallow.api.interfaces.admin.dto.AdminCashOutVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.admin.dto.TravelerAskVO;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.api.interfaces.bank.BankServiceImpl;
import com.cashmallow.api.interfaces.coupon.CouponMobileServiceV2;
import com.cashmallow.api.interfaces.coupon.CouponUserService;
import com.cashmallow.api.interfaces.coupon.CouponValidationService;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import com.cashmallow.api.interfaces.mallowlink.enduser.MallowlinkEnduserServiceImpl;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.RemittanceLimitDto;
import com.cashmallow.api.interfaces.traveler.dto.EditTravelerRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelersRequest;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static com.cashmallow.api.domain.shared.Const.*;
import static com.cashmallow.api.domain.shared.MsgCode.*;
import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_PASSWORD_MATCH;
import static com.cashmallow.common.DateUtil.getTimestampToOctaKstFormat;

/**
 * 여행자 신분증 인증, 계좌 인증 관련
 */
@Service
public class TravelerServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(TravelerServiceImpl.class);

    public static final String TRAVELER_LOGIN_DEVICE_RESET = "TRAVELER_LOGIN_DEVICE_RESET";

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private AuthService authService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private TravelerMapper travelerMapper;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private CashOutMapper cashOutMapper;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private EasyLoginMapper easyLoginMapper;

    @Autowired
    private EasyLoginHistMapper easyLoginHistMapper;

    @Autowired
    private BankServiceImpl bankService;

    @Autowired
    private CaptchaServiceImpl captchaService;

    @Autowired
    private Gson gsonPretty;

    @Autowired
    private RedisService redisService;

    @Autowired
    private OctaAMLCustomerService octaAMLCustomerService;

    @Autowired
    private MallowlinkEnduserServiceImpl mallowlinkEnduserService;
    @Autowired
    private EnvUtil envUtil;
    @Autowired
    private GlobalQueueService globalQueueService;

    @Autowired
    private AuthMeService authMeService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;
    @Autowired
    private CouponValidationService couponValidationService;
    @Autowired
    private CouponMobileServiceV2 couponMobileServiceV2;
    @Autowired
    private CouponUserService couponUserService;

    private final static int month = 1;

    // -------------------------------------------------------------------------------
    // 20. 여행자
    // -------------------------------------------------------------------------------

    /**
     * Old Login. Get new token. If new device, send the email to warn.
     *
     * @param userName
     * @param password
     * @param instanceId
     * @param deviceInfo
     * @param ip
     * @param cls
     * @return
     * @throws CashmallowException
     * @deprecated 2019-11-06 token changed to Json Web Token
     */
    @Deprecated
    public String travelerLogin(String userName, String password, String instanceId, String deviceInfo, String ip,
                                String cls) throws CashmallowException {
        String method = "travelerLogin()";

        logger.info("{} username={}", method, userName);

        String newToken;

        newToken = userService.login(userName, password, cls);

        Long userId = authService.getUserId(newToken);
        User user = userRepositoryService.getUserByUserId(userId);

        // iOS APP 에서 회원가입 후 바로 로그인할때 instanceId 없이 로그인이 시도되고 있어 일단 instanceId 관련 처리 생략
        // 후 로그인 처리.
        if (StringUtils.isEmpty(instanceId)) {
            logger.error("{}: old version iOS login after SignUp. instanceId={}", method, instanceId);
            return newToken;
        }

        String instanceIdEncryted = securityService.encryptSHA2(instanceId);
        if (StringUtils.isEmpty(user.getInstanceId()) || isIgnoreDeviceInstanceIdUser(user)) {
            // InstanceId 가 없으면 업데이트하고 리턴

            user.setInstanceId(instanceIdEncryted);
            int affectedRow = userRepositoryService.updateUser(user);
            if (affectedRow != 1) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else if (!user.getInstanceId().equals(instanceIdEncryted)) {
            // InstanceId 가 있는데 Device 불일치 경우
            // 이전 버전 사용자는 경고 메일만 발송. (로그인 화면에 에러메시지 처리 기능이 없음)

            // Update instanceId
            user.setInstanceId(instanceIdEncryted);
            int affectedRow = userRepositoryService.updateUser(user);

            if (affectedRow == 1) {
                // 경고 메일 발송
                notificationService.sendEmailToWarnNewDevice(user, deviceInfo, ip);
            } else {
                logger.error("{}: Update user failed!. userId={}, email={}", method, user.getId(), user.getEmail());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return newToken;

    }

    // 기기 instance 체크 예외 처리(임시)
    private boolean isIgnoreDeviceInstanceIdUser(User user) {
        if (envUtil.isDev()) {
            return user.getId() == 991089L;
        }
        return false;
    }


    /**
     * Login. Get new token. If new device, send the email to warn or reset. Do not
     * set @Transactional(rollbackFor = CashmallowException.class). (for
     * account_token insert)
     *
     * @param loginId
     * @param password
     * @param instanceId
     * @param deviceInfo
     * @param ip
     * @param remoteInstanceId
     * @return
     * @throws CashmallowException
     */
    public String travelerLogin(String loginId, String password, String instanceId, String deviceInfo, String ip,
                                Locale locale, String deviceType, String versionCode, String deviceOsVersion, String remoteInstanceId) throws CashmallowException {
        String method = "travelerLogin()";

        logger.info("{} userName={}, instanceId={}, deviceInfo={}, ip={}, locale={}, deviceType={}, versionCode={}, deviceOSVersion={}",
                method, loginId, instanceId, deviceInfo, ip, locale, deviceType, versionCode, deviceOsVersion);

        // 로그인 실패된 경우 login_count를 증가시키고 throw exception
        User user = userService.checkLoginAndPassword(loginId, password);
        Long userId = user.getId();

        // 일반
        // Set last login info
        user.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
        user.setDeviceType(deviceType);
        user.setVersionCode(versionCode);
        user.setDeviceOsVersion(deviceOsVersion);

        // langKey 가 다르면 업데이트 한다.
        if (locale != null && !locale.getLanguage().equals(user.getLangKey())) {
            user.setLangKey(locale.getLanguage());
            logger.info("{}: language={}", method, locale.getLanguage());
        }

        // instanceId가 없으면 로그인하면 안된다. 에러 처리
        if (StringUtils.isEmpty(instanceId)) {
            logger.error("{}: instanceId is empty. instanceId={}", method, instanceId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String instanceIdEncrypted = securityService.encryptSHA2(instanceId);

        String remoteInstanceIdEncrypted = "";
        if (remoteInstanceId != null) {
            remoteInstanceIdEncrypted = securityService.encryptSHA2(remoteInstanceId);
        }

        if (StringUtils.isEmpty(user.getInstanceId())
                || user.getInstanceId().equals(instanceIdEncrypted)
                || StringUtils.equals(user.getInstanceId(), remoteInstanceIdEncrypted)) {
            // 로그인 성공
            // 1. InstanceId가 없는 경우
            // 2. InstanceId가 있고 일치하는 경우.
            // 3. InstanceId가 remoteInstanceId와 일치하는 경우.

            // remoteInstanceIdEncryted가 일치한 경우
            if (StringUtils.equals(user.getInstanceId(), remoteInstanceIdEncrypted)) {
                String msg = "userId:" + userId +
                        "\nremoteInstanceId 사용자 InstanceId로 변경" +
                        "\ninstanceId:" + instanceId +
                        "\nremoteInstanceId:" + remoteInstanceId;
                // alarmService.i( "InstanceId 기기고유번호로 변경", msg);
                logger.info("userId={} remoteInstanceId 사용자 InstanceId로 변경, remoteInstanceIdEncrypted={}, msg={}", userId, remoteInstanceIdEncrypted, msg);
            }

            user.setInstanceId(instanceIdEncrypted);

            // update user for last login time
            int affectedRow = userRepositoryService.updateUser(user);
            if (affectedRow != 1) {
                logger.error("{}: Failed to update user. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            // last lofin fail 초기화
            affectedRow = userRepositoryService.clearUserLoginCount(userId);
            if (affectedRow != 1) {
                logger.error("{}: Failed to clear user login count. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        } else {
            // 로그인 실패
            // InstanceId 가 있는데 불일치 경우

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            if (traveler != null && "Y".equals(traveler.getCertificationOk())) {
                // 여권 승인인 경우 여권정보 재인증 메일 발송 (traveler)
                notificationService.sendEmailToResetDevice(user, deviceInfo, ip);

                // 메일만 보내고 instanceId를 변경하지 않음. 로그인 차단.
                throw new CashmallowException(TRAVELER_LOGIN_DEVICE_RESET, user.getId().toString());

            } else {
                // 여권 승인 안된 경우 경고 메일만 발송하고 로그인 진행.

                // Update instanceId
                user.setInstanceId(instanceIdEncrypted);
                int affectedRow = userRepositoryService.updateUser(user);

                if (affectedRow == 1) {
                    // 경고 메일 발송 (admin user)
                    notificationService.sendEmailToWarnNewDevice(user, deviceInfo, ip);
                } else {
                    logger.error("{}: Failed to update user. userId={}", method, userId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }
        }

        // refreshToken 발급
        String refreshToken = userService.loginForRefreshToken(loginId, password, CLS_TRAVELER, TRUE);

        // 로그인시 간편 로그인 정보가 있다면 refresh token 갱신
        EasyLogin easyLogin = easyLoginMapper.getEasyLoginByUserId(userId);

        if (easyLogin != null) {
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            easyLogin.setRefreshToken(securityService.encryptSHA2(refreshToken));
            easyLogin.setRefreshTime(now);
            easyLogin.setUpdatedAt(now);

            easyLoginMapper.updateEasyLogin(easyLogin);

            try {
                EasyLoginHist easyLoginHistParams = new EasyLoginHist();

                BeanUtils.copyProperties(easyLogin, easyLoginHistParams);
                easyLoginHistParams.setLoginSuccess(Y);
                easyLoginHistParams.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));

                easyLoginHistMapper.insertEasyLoginHist(easyLoginHistParams);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return refreshToken;

    }

    /**
     * 간편 로그인을 위한 refreshToken 생성 로직
     *
     * @param loginId
     * @param password
     * @param locale
     * @return
     * @throws CashmallowException
     */
    @Transactional
    public String travelerLoginForEasyLogin(String loginId, String password, Locale locale, JSONObject body) throws CashmallowException {

        String method = "travelerLoginForEasyLogin()";

        logger.info("{} userName={} ", method, loginId);

        String refreshToken = userService.loginForRefreshToken(loginId, password, CLS_TRAVELER, FALSE);

        Long userId = authService.getUserIdByJsonWebToken(refreshToken);

        User user = userRepositoryService.getUserByUserId(userId);

        String deviceType = null;
        String appbuild = null;
        String deviceOsVersion = null;
        String instanceId = body.getString("instance_id");
        String deviceInfo = body.getString("device_info");
        String ip = body.getString("ip");

        if (body.has("device_type")) {
            deviceType = body.getString("device_type");
        }
        if (body.has("appbuild")) {
            appbuild = body.getString("appbuild");
        }
        if (body.has("device_os_version")) {
            deviceOsVersion = body.getString("device_os_version");
        }

        // Set last login time
        user.setLastLoginTime(Timestamp.valueOf(LocalDateTime.now()));
        // Set deviceType
        user.setDeviceType(deviceType);
        // Set versionCode
        user.setVersionCode(appbuild);
        // Set deviceOSVersion
        user.setDeviceOsVersion(deviceOsVersion);

        // langKey 가 다르면 업데이트 한다.
        if (locale != null && !locale.getLanguage().equals(user.getLangKey())) {
            user.setLangKey(locale.getLanguage());
            logger.info("{}: language={}", method, locale.getLanguage());
        }

        // instanId가 없으면 로그인하면 안된다. 에러 처리
        if (StringUtils.isEmpty(instanceId)) {
            logger.error("{}: instanceId is empty. instanceId={}", method, instanceId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String instanceIdEncryted = securityService.encryptSHA2(instanceId);
        if (StringUtils.isEmpty(user.getInstanceId()) || user.getInstanceId().equals(instanceIdEncryted)) {
            // 로그인 성공
            // InstanceId 가 없으면 업데이트하고 로그인 진행.
            // InstanceId 가 있고 일치하는 경우. 로그인 진행.

            user.setInstanceId(instanceIdEncryted);

            // update user for last login time
            int affectedRow = userRepositoryService.updateUser(user);
            if (affectedRow != 1) {
                logger.error("{}: Failed to update user. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
            // last lofin fail 초기화
            affectedRow = userRepositoryService.clearUserLoginCount(userId);
            if (affectedRow != 1) {
                logger.error("{}: Failed to clear user login count. userId={}", method, userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        } else {
            // 로그인 실패
            // InstanceId 가 있는데 불일치 경우

            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

            if (traveler != null && "Y".equals(traveler.getCertificationOk())) {
                // 여권 승인인 경우 여권정보 재인증 메일 발송 (traveler)
                notificationService.sendEmailToResetDevice(user, deviceInfo, ip);

                // 메일만 보내고 instanceId를 변경하지 않음. 로그인 차단.
                throw new CashmallowException(TRAVELER_LOGIN_DEVICE_RESET);

            } else {
                // 여권 승인 안된 경우 경고 메일만 발송하고 로그인 진행.

                // Update instanceId
                user.setInstanceId(instanceIdEncryted);
                int affectedRow = userRepositoryService.updateUser(user);

                if (affectedRow == 1) {
                    // 경고 메일 발송 (admin user)
                    notificationService.sendEmailToWarnNewDevice(user, deviceInfo, ip);
                } else {
                    logger.error("{}: Failed to update user. userId={}", method, userId);
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }
        }

        return refreshToken;

    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler updateAddressPhotoV3(Traveler traveler, MultipartFile mf) throws CashmallowException {
        String method = "updateAddressPhoto(): ";

        if (traveler == null) {
            logger.error("{} 여행자 주소 승인 신청 중 오류 발생(1)", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String fileName = fileService.upload(mf, FILE_SERVER_ADDRESS);

        traveler.setAddressPhoto(fileName);
        int affectedRow = travelerRepositoryService.updateTraveler(traveler);
        if (affectedRow != 1) {
            logger.error("{} Traveler infomation update failed", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
        return traveler;
    }

    // 기능: 20.2. 신분인증 사진 업데이트
    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler updateCertificationPhoto(Long userId, MultipartFile mf, String method) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler != null) {
            // traveler.setCertificationOk("N");
            traveler.setCertificationOkDate(null);

            if (mf != null) {
                logger.info("addCertificationPhoto(): userId={}, method={}, picture={}", userId, method, mf.getOriginalFilename());
                String fileName = fileService.upload(mf, FILE_SERVER_CERTIFICATION);
                traveler.setCertificationPhoto(fileName);
            } else {
                logger.info("addCertificationPhoto(): userId={}, method={}, picture is null", userId, method);
            }

            int affectedRow = travelerRepositoryService.updateTraveler(traveler);

            if (affectedRow == 1) {
                // Decrypt the passport number.
                traveler.setIdentificationNumber(securityService.decryptAES256(traveler.getIdentificationNumber()));
                return traveler;
            } else {
                logger.error("{} Traveler infomation update failed", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else {
            logger.error("{} 여행자 신분 승인 신청 중 오류 발생(1)", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    // 기능: 20.2. 통장인증 사진 업데이트
    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler updateBankAccount(Traveler traveler, MultipartFile mf) throws CashmallowException {
        String method = "updateBankAccount(): ";
        logger.info(method);

        if (traveler != null) {
            if (mf != null) {
                String fileName = fileService.upload(mf, FILE_SERVER_BANKBOOK);
                traveler.setAccountBankbookPhoto(fileName);
            }
            User user = userRepositoryService.getUserByUserId(traveler.getUserId());
            int affectedRow = travelerRepositoryService.updateTraveler(traveler);

            if (affectedRow == 1) {
                alarmService.aAlert("승인", "통장 인증 신청, 사용자ID:" + traveler.getUserId().toString() + ", 국가:" + user.getCountry() + ", CertificationOk: " + traveler.getCertificationOk(), user);
                return traveler;
            } else {
                logger.error("{} Traveler account infomation update failed", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } else {
            logger.error("{} 여행자 계좌 인증 신청시 오류 발생 :", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    public void insertTravelerVerificationStatus(TravelerVerificationStatusRequest request) {
        travelerMapper.insertTravelerVerificationStatus(request);
    }

    /**
     * 신분증 + 여권 승인시 AML에 고객 정보 전송
     *
     * @param travelerId
     * @param managerName
     */
    private void sendTravelerInfoToAML(long travelerId, String managerName) {
        // null check 안함, 앞 비지니스에서 체크 하는중

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        User user = userRepositoryService.getUserByTravelerId(travelerId);
        CertificationType certificationType = traveler.getCertificationType();
        String rnmNodiv = null;
        Country fromCountry = countryService.getCountry(user.getCountry()); // pattern: 001
        Country3 countryCode = Country3.ofAlpha2(fromCountry.getIso3166()); // pattern: HK

        try {
            if (CertificationType.ID_CARD.equals(certificationType)) { // 신분증
                rnmNodiv = "01";
                countryCode = Country3.ofAlpha2(fromCountry.getIso3166()); // 국적(서비스국가)

            } else if (CertificationType.PASSPORT.equals(certificationType)) { // 여권
                rnmNodiv = "04";
                countryCode = Country3.valueOf(traveler.getPassportCountry()); // 여권국적
            }
        } catch (Exception e) {
            logger.warn("sendTravelerInfoToAML: countryCode is null. travelerId=" + traveler.getId());
        }

        String customerNo = String.valueOf(traveler.getUserId());
        AMLCustomerBase amlCustomerBase = AMLCustomerBase.builder()
                .customerNo(customerNo, user.getCountryCode().name())
                // init BASE
                .customerNm(traveler.getLocalFirstName() + " " + traveler.getLocalLastName())
                .customerEngNm(traveler.getEnFirstName() + " " + traveler.getEnLastName())
                .customerRegDd(getTimestampToOctaKstFormat(user.getCreatedDate()))
                .customerEditDd(getTimestampToOctaKstFormat(Timestamp.from(Instant.now())))

                // init common
                .regUserId(managerName)
                .lastChangeUserId(managerName)
                .build();

        String identificationNumber = securityService.decryptAES256(traveler.getIdentificationNumber());

        // 고객이 주소지 입력에 따른 예외 처리
        String addressCountry = StringUtils.isBlank(traveler.getAddressCountry()) ? fromCountry.getIso3166() : traveler.getAddressCountry();

        AMLCustomerIndv amlCustomerIndv = AMLCustomerIndv.builder()
                .customerNo(customerNo, user.getCountryCode().name())
                // init INDV
                .rnmNoDiv(rnmNodiv)
                .rnmNo(identificationNumber) // 실명번호
                .passportNo(identificationNumber) // 여권번호
                .countryCd(countryCode.getAlpha2())
                .birthDd(user.getBirthDate())
                .sexCd(traveler.getSex()) // 성별 default : 남성
                // .liveYn("N") // default N
                .foreignerDiv("B") // default B: 외국인 NOTE : 한국 서비스 오픈시에 변경 필요
                .liveCountryCd(addressCountry)
                .businessDtlCd(traveler.getJob().getOctaJobCode())
                .kofiuJobDivCd(traveler.getJob())
                // .amlRaChannelCd("03") // default 03: 모바일
                // .largeAmtAssetsYn("N") // default N

                // init common
                .regUserId(managerName)
                .lastChangeUserId(managerName)
                .build();

        AMLCustomerIndvCDD amlCustomerIndvCDD = AMLCustomerIndvCDD.builder()
                .customerNo(customerNo, user.getCountryCode().name())
                // init INDV_CDD
                // .liveYn("N") // default N
                .homeAddrCountryCd(addressCountry)
                // .homeAddrDisplayDiv("KZ") // default KZ: 기타
                // .homePostNo() // 우편번호
                .homeAddr(traveler.getAddress())
                .homeDtlAddr(traveler.getAddressSecondary())
                // .homePhoneCountryCd() // 자택전화번호 국가코드
                // .homePhoneNo() // 자택전화번호
                .cellPhoneNo(user.getPhoneNumber()) // 고객 휴대폰번호
                .emailAddr(user.getEmail()) // 고객 이메일
                // .workNm() // 고객 직장명
                // .deptNm() // 고객 부서명
                // .posiNm() // 직장내 직위명
                // .homepageAddr() // 직장 홈페이지 주소
                // .workAddrCountryCd(countryCode) // 직장 주소 국가코드
                // .workAddrDisplayDiv("KZ") // default KZ: 기타
                // .workPostNo() // 직장 우편번호
                // .workAddr() // 직장 주소
                // .workDtlAddr() // 직장 상세주소
                // .workPhoneCountryCd() // 직장 전화 국가코드
                // .workAreaPhoneNo() // 직장 전화 지역코드
                // .workPhoneNo() // 직장 전화번호
                // .workFaxNo() // 직장 FAX번호
                // NOTE
                //  - 거래목적, 자금출처, 계좌목적은 기본값은 기타로 고정
                //  - 앱 개선 후 사용자가 직접 입력 가능해지면 아래 주석을 풀고 사용
                .tranFundSourceDiv(traveler.getFundSource()) // default A99: 기타
                // .tranFundSourceNm("ETC") // default ETC
                // .tranFundSourceOther("기타") // default 기타
                .accountNewPurposeCd(traveler.getFundPurpose()) // default A99: 기타
                // .accountNewPurposeNm("기타") // default 기타
                // .accountNewPurposeOther("기타") // default 기타

                // init common
                .regUserId(managerName)
                .lastChangeUserId(managerName)
                .build();

        OctaAMLCustomerRequest octaAMLCustomerRequest = new OctaAMLCustomerRequest(amlCustomerBase, amlCustomerIndv, amlCustomerIndvCDD);
        octaAMLCustomerService.execute(octaAMLCustomerRequest);
    }

    private boolean isDuplicatedCertificationId(String certificationId) {
        return userRepositoryService.isDuplicatedCertificationId(certificationId);
    }

    private boolean isInspectDuplicateCertificationNumber(Long userId, Traveler traveler) {

        User user = userRepositoryService.getUserByUserId(userId);

        TravelerAskVO tpaVO = new TravelerAskVO();
        tpaVO.setPage(0);
        tpaVO.setSize(2);
        tpaVO.setStart_row(0);
        tpaVO.setIdentification_number(traveler.getIdentificationNumber());
        tpaVO.setCountry(user.getCountry());
        tpaVO.setCertification_ok("Y");

        if (CertificationType.PASSPORT == traveler.getCertificationType()) {
            tpaVO.setPassport_country(traveler.getPassportCountry());
        }

        List<Map<String, Object>> tpaiMaps = travelerMapper.getTravelerCertificationInfo(tpaVO);

        ObjectMapper mapper = new ObjectMapper();

        for (Map<String, Object> tpaiMap : tpaiMaps) {
            // TravelerPassportAndAccountInfoVO tpaiVO = mapper.convertValue(tpaiMap, TravelerPassportAndAccountInfoVO.class);
            if (!userId.equals((Long) tpaiMap.get("user_id"))) {
                return true;
            }
        }

        return false;
    }

    // 기능: 21.1. 여행자 등록(최초 인증, 혹은 재인증)
    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler registerTraveler(Long userId, Traveler traveler) throws CashmallowException {

        String method = "registerTraveler()";

        traveler.setUserId(userId);
        traveler.setCreator(userId);

        // 성별값이 없는 경우 기본 남자로 설정
        if (traveler.getSex() == null) {
            traveler.setSex(Traveler.TravelerSex.MALE);
        }

        //		if(InspectBanCountry(traveler)) {//위험국가로 지정된 손님인지 검사함.
        //			logger.error("{}: Traveler Passport Country is BanCountry. userId={}, PassportCountry={}", method, userId, traveler.getPassportCountry());
        //
        //			throw new CashmallowException("We are not accepted for the selected passport country.");
        //		}

        User user = userRepositoryService.getUserByUserId(userId);
        // 여권번호 중복체크 dev에서는 무시되도록 추가
        if (isDuplicatedCertificationId(traveler.getIdentificationNumber()) && isInspectDuplicateCertificationNumber(userId, traveler)) {
            String errorMessage;

            switch (traveler.getCertificationType()) {
                case PASSPORT -> errorMessage = TRAVELER_DUPLICATE_PASSPORT;
                case DRIVER_LICENSE, ID_CARD, RESIDENCE_CARD, SPECIAL_RESIDENT_CERTIFICATE -> errorMessage = TRAVELER_DUPLICATE_IDCARD;
                default -> errorMessage = INTERNAL_SERVER_ERROR;
            }

            logger.error("Error Traveler certification number. userId={}, identificationNumber={}, ErrorMessage={}", userId, traveler.getIdentificationNumber(), errorMessage);
            if (envUtil.isPrd()) {
                // 개발에서는 중복으로 테스트 하기 때문에 throw하지 않고 운영환경에서만 오류 발생하도록 처리
                throw new CashmallowException(errorMessage);
            }
        }

        logger.info("traveler certification type debug, travelerId: {}, certificationType: {}", traveler.getId(), traveler.getCertificationType());

        int affectedRow = traveler.getId() == null ? travelerRepositoryService.insertTraveler(traveler) : travelerRepositoryService.updateTraveler(traveler);
        if (affectedRow != 1) {
            logger.error("{}: Failed to certify Traveler info. userId={}", method, userId);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler resultTraveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (isNotEqualsNameUserAndTraveler(user, resultTraveler)) {
            user.setFirstName(resultTraveler.getLocalFirstName());
            user.setLastName(resultTraveler.getLocalLastName());

            affectedRow = userRepositoryService.updateUser(user);
            if (affectedRow != 1) {
                logger.error("{}: Failed to update user name. userId={}", "registerTraveler()", userId);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        if (user.getCountryCode() == CountryCode.HK && "Y".equalsIgnoreCase(traveler.getAccountOk())) {
            // 홍콩 통장 승인시에는 5분뒤 authme 상태 체크하지 않음
        } else if(ApprovalType.NFC.equals(traveler.getApprovalType())) {
            // 인증타입이 NFC일때만 5분뒤 authme 상태 체크
            logger.info("checkAuthmeStatus registerTraveler: userId={}, travelerId={}, certificationType={}", userId, traveler.getId(), traveler.getCertificationType());
            authMeService.checkAuthmeStatus(user, traveler);
            globalQueueService.certificationExpirationUpdateAfter7Days(resultTraveler);
        }

        return resultTraveler;
    }

    private boolean isNotEqualsNameUserAndTraveler(User user, Traveler resultTraveler) {
        if (StringUtils.isBlank(resultTraveler.getLocalFirstName()) || StringUtils.isBlank(resultTraveler.getLocalLastName())
                || StringUtils.isBlank(user.getFirstName()) || StringUtils.isBlank(user.getLastName())) {
            return false;
        }

        return !user.getFirstName().equals(resultTraveler.getLocalFirstName())
                || !user.getLastName().equals(resultTraveler.getLocalLastName());
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler registerTravelerV4(Long userId, TravelersRequest request, Locale locale) throws CashmallowException {
        // authme에서 즉시 전달 받지 못하는 경우 null 로 전달 될 수 있음
        String dateOfBirth = request.getDateOfBirth();

        logger.info("updateUser BirthDate userId={}, birthDate={}", userId, dateOfBirth);

        if (StringUtils.isNotBlank(dateOfBirth)) {
            int age = CommDateTime.getAge(dateOfBirth);
            if (age < AGE_CAN_JOIN) {
                logger.info("가입 최소 연령 미달자 (만 {} 세 이상). 생년월일: {}", AGE_CAN_JOIN, dateOfBirth);

                // 제한 나이 출력 메시지 동적으로 추가. 나이 변경 시, AGE_CAN_JOIN 숫자만 바꿔주면 됨.
                Object[] messageArray = new Object[1];
                messageArray[0]  = Const.AGE_CAN_JOIN;
                throw new CashmallowException(messageSource.getMessage("SIGNUP_ERROR_AGE_CONSTRAINT", messageArray, "SIGNUP_ERROR_AGE_CONSTRAINT", locale));
            }
        }

        if (request.getJob().equals(Job.UNKNOWN)) {
            throw new CashmallowException("TRAVELER_INVALID_JOB");
        }

        User user = userRepositoryService.getUserByUserId(userId);
        user.setBirthDate(dateOfBirth);
        userRepositoryService.updateUser(user);

        // traveler 등록
        Traveler existTraveler = travelerRepositoryService.getTravelerByUserId(userId);
        Traveler travelerVo;
        if (existTraveler == null) {
            travelerVo = new Traveler();
        } else {
            // 이미 진행중인 건이 있는데 중복으로 요청하는 경우(앱 플로우상 발생해서는 안됨)
            if ("W".equals(existTraveler.getCertificationOk())) {
                logger.warn("이미 진행중인 승인 요청이 있습니다. userId={}, type={}", userId, existTraveler.getCertificationType().name());
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            travelerVo = existTraveler;
        }

        travelerVo.updateTraveler(request);

        // 신분증 번호 암호화
        String identificationNumber = travelerVo.getIdentificationNumber();
        if (StringUtils.isNotBlank(identificationNumber)) {
            travelerVo.setIdentificationNumber(securityService.encryptAES256(identificationNumber.toUpperCase()));
        } else {
            travelerVo.setCertificationPhoto("null");
        }

        Traveler traveler = registerTraveler(userId, travelerVo);

        // 사진등록 플로우에서 하던 작업 진행
        traveler.setCertificationPhoto(travelerVo.getCertificationPhoto());
        traveler.setCertificationOkDate(null);
        traveler.setCertificationOk("W"); // 어스미 승인/거절 까지 대기 상태
        traveler.setAccountOk("N");

        // from JP 인 경우 계좌 인증이 없어서 임의로 값을 넣어둠. (JP 인 경우만 해당)
        // (앱 플로우 최대한 수정 없이 하기 위함)
        if (CountryCode.JP == user.getCountryCode()) {
            traveler.setAccountBankbookPhoto("null");
            traveler.setAccountOk("Y");
            traveler.setAccountOkDate(Timestamp.valueOf(LocalDateTime.now().plusYears(100)));
        }

        travelerRepositoryService.updateTraveler(traveler);

        return traveler;
    }

    public boolean isDeviceResetCodeValid(String tokenId, String code, String fcmToken, Locale locale, String deviceType) throws CashmallowException {
        String deviceResetKey = securityService.decryptAES256(tokenId);
        logger.info("deviceResetKey={}", deviceResetKey);
        if (StringUtils.isNotBlank(deviceResetKey) && StringUtils.isNotBlank(code)) {
            final String[] deviceParams = deviceResetKey.split("_");
            Long userId = Long.parseLong(deviceParams[0]);
            String emailToken = deviceParams[1];
            final ResetDevice resetDeviceEmailToken = getResetDeviceEmailToken(userId);
            if (resetDeviceEmailToken != null
                    && resetDeviceEmailToken.isValidTokenIdAndCode(securityService, emailToken, code)
            ) {

                // 앱 푸시 토큰 업데이트
                // (리셋한 기기로 알람 가도록 처리하기 위함)
                if (StringUtils.isNotBlank(fcmToken) && StringUtils.isNotBlank(deviceType)) {
                    notificationService.addFcmToken(userId, fcmToken, deviceType);
                }

                // 푸시 알람시 langKey기반으로 발송하기에 langKey 업데이트 진행
                userRepositoryService.updateUserLangKey(userId, locale.getLanguage());

                // 기기 리셋 하는 로직 추가
                confirmDeviceResetByTraveler(userId, emailToken, locale, null, true);
                return true;
            }
        }

        return false;
    }

    // 신규 디바이스 이용을 위한 여행자 여권 정보 리셋.
    @Transactional(rollbackFor = CashmallowException.class)
    public String confirmDeviceResetByTraveler(long userId, String accountToken, Locale locale, String captcha, boolean ignoreCaptchaValidation)
            throws CashmallowException {

        String method = "confirmNewDeviceResetByTraveler()";

        logger.info("{}: userId={}, accountToken={}", method, userId, accountToken);

        // 1. login id 에 대한 User data를 읽는다.
        User user = userRepositoryService.getUserByUserId(userId);
        String successView = notificationService.getResetDeviceConfirmView(true, user);
        String failureView = notificationService.getResetDeviceConfirmView(false, user);

        if (captchaService.isRobot(captcha) && !ignoreCaptchaValidation) {
            logger.error("{}: Can not find user", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, failureView);
        }

        if (user == null) {
            logger.error("{}: Can not find user", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, failureView);
        }

        if (isNotValidToken(userId, accountToken)) {
            logger.warn("{}: Invalid parameters. userId={}, accountToken={}", method, userId, accountToken);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, failureView);
        }

        // 여권 정보 조회
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(user.getId());

        if ("N".equals(traveler.getCertificationOk())) {
            // To prevent FailureView from returning when a user clicks on a URL link
            // multiple times
            return successView;
        }

        logger.info("Reset 기존 instance_id => {}", user.getInstanceId());

        // Reset Instance ID
        user.setInstanceId(null);
        user.setLastModifiedDate(Timestamp.valueOf(LocalDateTime.now()));
        int affectedRow = userRepositoryService.updateUser(user);
        if (affectedRow != 1) {
            logger.error("{}: 사용자 정보를 업데이트 할 수 없습니다.(Instance ID 초기화)", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR, failureView);
        }

        // 본인 인증 승인 상태 변경 및 알림 (본인 인증 취소)
        String message = messageSource.getMessage("TRAVELER_LOGIN_NEW_DEVICE", null, "Identification information is not registered.", locale);
        traveler = verifyIdentity(traveler.getId(), "R", message);
        setVerifyBankAccount(traveler, "N", message);

        if (CountryCode.JP.equals(user.getCountryCode())) {
            TravelerRequestSender globalTraveler = new TravelerRequestSender(user, traveler, securityService.decryptAES256(traveler.getIdentificationNumber()));
            globalQueueService.sendTravelerResult(user, globalTraveler);
        }

        // 마지막에 신분증 파일 제거
        // fileService.deleteObejctOnlyOne(FILE_SERVER_CERTIFICATION, certificationPhotoTemp);
        return successView;

    }

    /**
     * 토큰 유효기간 10분 체크, 마지막 신청한 토큰이 맞는지 체크
     *
     * @param userId
     * @param accountToken
     * @return true: 올바르지 않은 토큰
     */
    private boolean isNotValidToken(long userId, String accountToken) {
        EmailToken emailToken = notificationService.getEmailToken(userId);
        if (emailToken == null) {
            return true;
        }

        LocalDateTime tokenCreatedTime = emailToken.getCreatedDate().toLocalDateTime();

        return tokenCreatedTime.plus(10, ChronoUnit.MINUTES).isBefore(LocalDateTime.now())
                || !StringUtils.equals(emailToken.getAccountToken(), accountToken);
    }

    public ResetDevice getResetDeviceEmailToken(long userId) {

        final EmailToken emailToken = notificationService.getEmailToken(userId);
        if (emailToken == null) {
            return null;
        }

        ResetDevice resetDevice = new ResetDevice(
                emailToken.getCreatedDate().toLocalDateTime().plus(10, ChronoUnit.MINUTES).toInstant(ZoneOffset.UTC).toEpochMilli(),
                securityService.encryptAES256(emailToken.getUserId() + "_" + emailToken.getAccountToken()),
                emailToken.getCode()
        );
        return resetDevice;
    }

    /**
     * 여권/신분증/통장인증에 대한 만료일 체크
     *
     * @param resultMap
     * @param locale
     * @throws CashmallowException
     */
    private void updateCertificationAndAccountExpiredDate(Map<String, Object> resultMap, Locale locale) throws CashmallowException {
        if (resultMap == null) {
            return;
        }

        // 여권일 경우만 여권의 만료일 체크
        verifyPassportExpiredDate(resultMap, locale);
        verifyIdentityBefore1Year(resultMap, locale);
        verifyBankAccountBefore1Year(resultMap, locale);
    }

    private void verifyPassportExpiredDate(Map<String, Object> resultMap, Locale locale) throws CashmallowException {
        String certificationOk = resultMap.get("certification_ok") == null ? "N" : resultMap.get("certification_ok").toString();
        // 인증 유형이 여권이고 인증 상태가 Y일때 만료일 체크
        if ("Y".equalsIgnoreCase(certificationOk) && CertificationType.PASSPORT.equals(CertificationType.valueOf(resultMap.get("certification_type").toString()))) {
            // 현재 일자 기준으로 만료여부만 체크함.
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date expDate = null;
            try {
                expDate = sdf.parse(resultMap.get("passport_exp_date").toString());
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
                return;
            }

            // 여권만료일이 지났으면 캔슬
            if (expDate.before(new Date())) {
                String message = messageSource.getMessage("FCM_AU_AI_PASSPORT_EXPIRED", null, "", locale);
                verifyIdentityBefore1Year((Long) resultMap.get("id"), message);
                // result data 변경
                resultMap.put("certification_ok", "R");
            }
        }
    }

    private void verifyBankAccountBefore1Year(Map<String, Object> resultMap, Locale locale) throws CashmallowException {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        // 신분증 인증완료 날짜기준 358일 동안 유효함
        String account_ok = resultMap.get("account_ok") == null ? "N" : resultMap.get("account_ok").toString();
        Object certificationOkDate = resultMap.get("certification_ok_date");
        Object accountOkDate = resultMap.get("account_ok_date");

        if (!"Y".equalsIgnoreCase(account_ok)) {
            return;
        }

        if (certificationOkDate == null || accountOkDate == null) {
            return;
        }

        // 현재 신분증 또는 계좌인증 만료일 중 먼저 도래하는 날짜로 만료일으로 변경 설정
        long minTimestamp = Math.min((Long) certificationOkDate, (Long) accountOkDate);

        if ("Y".equalsIgnoreCase(account_ok)) {
            cal.setTimeInMillis(minTimestamp);
            cal.add(Calendar.DATE, 358);
            Date expireDate = cal.getTime();

            // 358일 이후 강제로 인증 만료 처리
            if (now.after(expireDate)) {
                String message = messageSource.getMessage("TRAVELER_BANK_ACCOUNT_VERIFICATION_EXPIRED", null, "", locale);
                verifyBankAccountBefore1Year((long) resultMap.get("id"), message);
                // result data 만료 처리
                resultMap.put("account_ok", "R");
            }
        }
    }

    private void verifyIdentityBefore1Year(Map<String, Object> resultMap, Locale locale) throws CashmallowException {
        Date now = new Date();
        Calendar cal = Calendar.getInstance();
        // 본인인증일(certification_ok)에 대해서 1년이 지났는지 확인한다.
        String certificationOk = resultMap.get("certification_ok") == null ? "N" : resultMap.get("certification_ok").toString();
        if ("Y".equalsIgnoreCase(certificationOk) && resultMap.get("certification_ok_date") != null) {
            Long dateTimestamp = (Long) resultMap.get("certification_ok_date");
            cal.setTimeInMillis(dateTimestamp);
            // 1년 기준 365일 만료 이후 강제로 인증 만료 처리
            cal.add(Calendar.DATE, 358);
            // cal.add(Calendar.DATE, 15);
            Date oneYearAgo = cal.getTime();

            // 1. 계좌/신분증 1년 지났는지 확인후 R으로 변경
            // 2. 계좌/신분증/여권 사진 삭제
            if (now.after(oneYearAgo)) {
                // 신분증 인증 만료 처리
                String message = messageSource.getMessage("TRAVELER_IDENTIFICATION_VERIFICATION_EXPIRED", null, "", locale);
                verifyIdentityBefore1Year((Long) resultMap.get("id"), message);
                // result data 변경
                resultMap.put("certification_ok", "R");
            }
        }
    }

    /**
     * 여행자 상세 정보 조회
     *
     * @param userId
     * @param locale
     * @return
     * @throws CashmallowException
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getTravelerMapByUserId(Long userId, Locale locale) throws CashmallowException {

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        if (traveler == null) {
            return null;
        }

        traveler.decryptData(securityService);

        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        Map<String, Object> result = mapper.convertValue(traveler, new TypeReference<Map<String, Object>>() {
        });

        // todo progress 추가
        // 쓰레기값 넣어야함
        result.put("certificationProgress", traveler.isCertificationProgress());
        result.put("bankAccountProgress", traveler.isBankAccountProgress());

        // 은행 아이콘
        result.put("bankIconPath", bankService.getBankIconMap().getOrDefault(traveler.getBankName(), ""));

        // 1번이라도 신분증 및 여권 인증 했는지 flag
        result.put("travelerVerifiedMoreThanOnce", travelerRepositoryService.isTravelerVerifiedMoreThanOnce(traveler.getId()));
        // 생년월일 추가 (@엔써니님 요청)
        User user = userRepositoryService.getUserByTravelerId(traveler.getId());
        result.put("birthDate", user.getBirthDate());
        result.put("certification_ok", traveler.isPendingAuthme() ? "N" : traveler.getCertificationOk());

        // 보류 사유
        Map<String, Object> travelerVerificationStatus = travelerMapper.getLastTravelerVerificationStatus(traveler.getId());
        if (travelerVerificationStatus != null) {
            result.put("verification_hold_reason", travelerVerificationStatus.getOrDefault("verification_hold_reason", "").toString());
        }

        // 여권/신분증/통장인증에 대한 만료일 체크
        updateCertificationAndAccountExpiredDate(result, locale);

        return result;
    }

    /**
     * 여행자의 신분증 인증 상태 변경.
     *
     * @param travelerId
     * @param certificationOk ("Y", "N", "R")
     * @param message
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler verifyIdentity(Long travelerId, String certificationOk, String message) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        setVerifyIdentity(traveler, certificationOk, message);
        sendVerifiedNotificationToSlack("여행자", certificationOk, traveler.getUserId(), message, false);
        return traveler;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void verifyIdentityByAdmin(Long travelerId, String managerName, String certificationOk, String message, boolean needJpAccount) throws CashmallowException {
        sendVerificationInfo(travelerId, certificationOk, message, needJpAccount);

        // 신분증 or 여권 인증 완료 후
        if ("Y".equalsIgnoreCase(certificationOk)) {
            // AML에 내역 전송
            sendTravelerInfoToAML(travelerId, managerName);

            // 히스토리 기록
            travelerRepositoryService.addTravelerVerificationHistory(travelerId, managerName);
        }
    }


    private void sendVerificationInfo(long travelerId, String certificationOk, String message, boolean needJpAccount) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        if (needJpAccount) {
            traveler.setNeedJpAccountRegister("Y");
        }

        if ("Y".equalsIgnoreCase(certificationOk)) {
            // mallowlink 가입
            User user = userRepositoryService.getUserByTravelerId(travelerId);

            List<UpdateStatusUserCoupon> userCouponList = new ArrayList<>();
            // 2025.03.25 추가: 본인인증 완료 후, Welcome 쿠폰 사용 이력 있으면 바로 회수 처리 (유저에게 알림 보내지 않음)
            boolean isUsedWelcome = couponValidationService.hasUsedSystemCoupon(user.getId(), SystemCouponType.welcome, traveler.getIdentificationNumber(), month);
            if (isUsedWelcome) {
                // 유저가 발급받은 가입 쿠폰 중에 AVAILABLE 상태인 것만 가져옴
                List<CouponIssueUser> couponIssueUserList = couponUserService.getUserCouponListByUserIdAndLikeCouponCode(user.getId(), SystemCouponType.welcome.getCode())
                                                            .stream().filter(f -> f.getAvailableStatus().equals(AvailableStatus.AVAILABLE.name()))
                                                            .toList();
                if (!couponIssueUserList.isEmpty()) {
                    // 일본 가입쿠폰 회수 데이터 전송을 위한 데이터 적재
                    if (CountryCode.JP.getCode().equals(user.getCountry())) {
                        List<UpdateStatusUserCoupon> updateStatusUserCoupons = couponIssueUserList.stream().map(UpdateStatusUserCoupon::ofRevoke).toList();
                        userCouponList.addAll(updateStatusUserCoupons);
                    }

                    List<Long> toUpdateCouponUserIds = couponIssueUserList.stream().map(CouponIssueUser::getId).toList();
                    int updated = couponMobileServiceV2.updateListCouponStatus(toUpdateCouponUserIds, AvailableStatus.REVOKED);
                    if (updated < 1) {
                        logger.error("sendVerificationInfo() 가입 쿠폰 REVOKED 업데이트에 실패했습니다: userId={}, toUpdateCouponUserIds={}", user.getId(), toUpdateCouponUserIds);
                        throw new CashmallowException(COUPON_CANNOT_UPDATE_REVOKED);
                    }
                }
            }
            // 2025.04.25 추가: 본인인증 완료 후, ThankYouMyFriend, 등폰 등록 이력 있거나 거래 내역있으면 바로 회수 처리 (유저에게 알림 보내지 않음)
            boolean isRegisteredInvite = couponValidationService.hasRegisteredCoupon(user.getId(), SystemCouponType.thankYouMyFriend.getCode(), traveler.getIdentificationNumber());
            boolean isInactiveTransactionInvite = couponValidationService.hasInactiveTransactionHistory(traveler.getIdentificationNumber());
            if (isRegisteredInvite || isInactiveTransactionInvite) {
                // 유저가 발급받은 가입 쿠폰 중에 AVAILABLE 상태인 것만 가져옴
                List<CouponIssueUser> couponIssueUserList = couponUserService.getUserCouponListByUserIdAndLikeCouponCode(user.getId(), SystemCouponType.thankYouMyFriend.getCode())
                                                            .stream().filter(f -> f.getAvailableStatus().equals(AvailableStatus.AVAILABLE.name()))
                                                            .toList();
                if (!couponIssueUserList.isEmpty()) {
                    // 일본 초대쿠폰 회수 데이터 전송을 위한 데이터 적재
                    if (CountryCode.JP.getCode().equals(user.getCountry())) {
                        List<UpdateStatusUserCoupon> updateStatusUserCoupons = couponIssueUserList.stream().map(UpdateStatusUserCoupon::ofRevoke).toList();
                        userCouponList.addAll(updateStatusUserCoupons);
                    }

                    List<Long> toUpdateCouponUserIds = couponIssueUserList.stream().map(CouponIssueUser::getId).toList();
                    int updated = couponMobileServiceV2.updateListCouponStatus(toUpdateCouponUserIds, AvailableStatus.REVOKED);
                    if (updated < 1) {
                        logger.error("sendVerificationInfo() 초대 쿠폰 REVOKED 업데이트에 실패했습니다: userId={}, toUpdateCouponUserIds={}", user.getId(), toUpdateCouponUserIds);
                        throw new CashmallowException(COUPON_CANNOT_UPDATE_REVOKED);
                    }
                }
            }
            // 2025.04.25 추가: 본인인증 완료 후, Influence 쿠폰 등록 이력 있거나 거래 내역있으면 바로 회수 처리 (유저에게 알림 보내지 않음)
            boolean isRegisteredInfluence = couponValidationService.hasRegisteredCoupon(user.getId(), SystemCouponType.influencer.getCode(), traveler.getIdentificationNumber());
            boolean isInactiveTransactionInfluence = couponValidationService.hasInactiveTransactionHistory(traveler.getIdentificationNumber());
            if (isRegisteredInfluence || isInactiveTransactionInfluence) {
                // 유저가 발급받은 가입 쿠폰 중에 AVAILABLE 상태인 것만 가져옴
                List<CouponIssueUser> couponIssueUserList = couponUserService.getUserCouponListByUserIdAndLikeCouponCode(user.getId(), SystemCouponType.influencer.getAbbreviation())
                                                            .stream().filter(f -> f.getAvailableStatus().equals(AvailableStatus.AVAILABLE.name()))
                                                            .toList();
                if (!couponIssueUserList.isEmpty()) {
                    // 일본 인플루언서 쿠폰 회수 데이터 전송을 위한 데이터 적재
                    if (CountryCode.JP.getCode().equals(user.getCountry())) {
                        List<UpdateStatusUserCoupon> updateStatusUserCoupons = couponIssueUserList.stream().map(UpdateStatusUserCoupon::ofRevoke).toList();
                        userCouponList.addAll(updateStatusUserCoupons);
                    }

                    List<Long> toUpdateCouponUserIds = couponIssueUserList.stream().map(CouponIssueUser::getId).toList();
                    int updated = couponMobileServiceV2.updateListCouponStatus(toUpdateCouponUserIds, AvailableStatus.REVOKED);
                    if (updated < 1) {
                        logger.error("sendVerificationInfo() 인플루언서 쿠폰 REVOKED 업데이트에 실패했습니다: userId={}, toUpdateCouponUserIds={}", user.getId(), toUpdateCouponUserIds);
                        throw new CashmallowException(COUPON_CANNOT_UPDATE_REVOKED);
                    }
                }
            }

            // 일본 쿠폰 회수 데이터 전송
            // 일본 서버에는 RabbitMQ 로만 요청 보내는 것이 가능함
            if (CountryCode.JP.getCode().equals(user.getCountry())) {
                globalQueueService.setUpdateStatusUserCoupon(userCouponList, AvailableStatus.REVOKED.name());
            }

            try {
                mallowlinkEnduserService.register(user, traveler);
                sendVerityAndNotification(certificationOk, message, traveler);
            } catch (MallowlinkException e) {
                if (e.getStatus().equals(MallowlinkExceptionType.USER_ALREADY_REGISTERED)) {
                    mallowlinkEnduserService.update(user, traveler);
                    sendVerityAndNotification(certificationOk, message, traveler);
                } else {
                    logger.error("Mallowlink 가입 실패:{}", e.getMessage(), e);
                    throw new CashmallowException(e.getMessage(), e);
                }
            }
        } else {
            sendVerityAndNotification(certificationOk, message, traveler);
        }
    }

    private void sendVerityAndNotification(String certificationOk, String message, Traveler traveler) throws CashmallowException {
        setVerifyIdentity(traveler, certificationOk, message);
        sendVerifiedNotificationToSlack("여행자", certificationOk, traveler.getUserId(), message, true);
    }


    /**
     * 여행자의 신분증 인증 만료 변경.
     *
     * @param travelerId
     * @param message
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void verifyIdentityBefore1Year(long travelerId, String message) throws CashmallowException {
        // 여권인증일 1년 지났는지 확인후 R으로 변경
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        User user = userRepositoryService.getUserByTravelerId(travelerId);
        setVerifyIdentity(traveler, "R", message);
        sendKycNotificationToSlack("여행자", traveler.getUserId());

        if (CountryCode.JP.equals(user.getCountryCode())) {
            TravelerRequestSender globalTraveler = new TravelerRequestSender(user, traveler, securityService.decryptAES256(traveler.getIdentificationNumber()));
            globalQueueService.sendTravelerResult(user, globalTraveler);
        }
    }

    private void setVerifyIdentity(Traveler traveler, String certificationOk, String message) throws CashmallowException {
        String method = "verifyIdentity()";
        logger.info("{}: travelerId={}, certificaionOk={}, message={}", method, traveler.getId(), certificationOk, message);

        String certOk = CustomStringUtil.trim(certificationOk).toUpperCase();
        String backupAddressPhoto = traveler.getAddressPhoto();
        String backupCertificationPhoto = traveler.getCertificationPhoto();

        try {
            if (traveler == null) {
                throw new CashmallowException("여행자 정보 또는 사용자의 국가 정보를 찾을 수 없습니다.");
            }

            traveler.setCertificationOk(certOk);
            traveler.setCertificationOkDate(Timestamp.valueOf(LocalDateTime.now()));
            traveler.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));

            // 사용자가 신청할때와 승인자가 승인할때 둘다 체크함.
            if (envUtil.isPrd() && certOk.equals("Y") &&
                    Boolean.TRUE.equals(isInspectDuplicateCertificationNumber(traveler.getUserId(), traveler))) {
                throw new CashmallowException("여행자 인증을 할 수 없습니다. 이미 여권 인증 완료된 여행자인지 확인하십시오.");
            }

            int affectedRow = travelerRepositoryService.updateTraveler(traveler);

            if (envUtil.isPrd() && affectedRow != 1) {
                throw new CashmallowException("여행자 인증을 할 수 없습니다. 이미 여권 인증 완료된 여행자인지 확인하십시오.");
            }

            // 주소 이력 저장
            insertTravelerVerificationStatus(
                    new TravelerVerificationStatusRequest(
                            traveler.getId(),
                            VerificationType.ADDRESS_PHOTO.name(),
                            certificationOk,
                            message,
                            backupAddressPhoto,
                            MDC.get("userId")
                    )
            );

            // 여권 또는 신분증 이력 저장
            insertTravelerVerificationStatus(
                    new TravelerVerificationStatusRequest(
                            traveler.getId(),
                            VerificationType.CERTIFICATION.name(),
                            certificationOk,
                            message,
                            backupCertificationPhoto,
                            MDC.get("userId")
                    )
            );

            int ynr = CustomStringUtil.ynrToNo(certOk);
            // Y : 0
            // N : 1
            // R : 2

            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            // 여권 인증 알림
            Long orgId = (long) ynr;
            if ("Y".equals(traveler.getNeedJpAccountRegister())) {
                // 같은 보류라도 일본에서 계좌사진 추가요청시 AC로 내림. 현재 구분 방법이 없어서 임시로 보류 메세지로 구분중
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.AU, FcmEventValue.AC, orgId, message);
            } else {
                notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.AU, FcmEventValue.AI, orgId, message);
            }
        } catch (Exception e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    /**
     * 여행자의 계좌/여권 인증 상태 슬랙으로 알람
     *
     * @param reasonMsg
     * @throws CashmallowException
     */
    private void sendVerifiedNotificationToSlack(String type, String certOk, Long userId, String reasonMsg, boolean admin) {
        String prefix = admin ? "[ADMIN]" : "";

        String msg = "";
        String kind = "";
        switch (certOk) {
            case "Y":
                kind = "승인";
                msg = "등록 처리 승인";
                break;
            case "R":
                kind = "보류";
                msg = "요청 처리 완료";
                break;
            case "Z":
                kind = "초기화";
                msg = "요청 처리 완료";
                break;
            case "N":
                msg = "등록 처리 요청";
                kind = "요청";
                break;
            default:
                logger.error("certOk={}", certOk);
        }
        String slackMsg = String.format("%s %s %s, 사용자ID:%s, 처리값:%s", prefix, type, msg, userId, certOk);
        if (StringUtils.isNotBlank(reasonMsg)) {
            slackMsg += "\n 사유(고객에 전달):" + reasonMsg;
        }
        alarmService.aAlert(kind, slackMsg, userRepositoryService.getUserByUserId(userId));
    }

    private void sendKycNotificationToSlack(String type, Long userId) {
        alarmService.aAlert("보류", String.format("%s KYC 만료 처리 완료, 사용자ID:%s, 처리값:R", type, userId), userRepositoryService.getUserByUserId(userId));
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void verifyBankAccountByAdmin(Traveler traveler, String accountOk, String message) throws CashmallowException {
        // use admin
        setVerifyBankAccount(traveler, accountOk, message);
        sendVerifiedNotificationToSlack("통장", accountOk, traveler.getUserId(), message, true);
    }

    /**
     * 여행자의 은행계좌 인증 만료 변경.
     *
     * @param travelerId
     * @param message
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void verifyBankAccountBefore1Year(Long travelerId, String message) throws CashmallowException {
        // accountOk : R
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        traveler.setAccountBankbookPhoto(null); // 사진 데이터 삭제
        setVerifyBankAccount(traveler, "R", message);

        sendKycNotificationToSlack("통장", traveler.getUserId());
    }

    private void setVerifyBankAccount(Traveler traveler, String accountOk, String message) throws CashmallowException {
        traveler.setUpdatedDate(Timestamp.valueOf(LocalDateTime.now()));
        traveler.setAccountOkDate(Timestamp.valueOf(LocalDateTime.now()));

        traveler.setAccountOk(accountOk);
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());

        if ("Y".equalsIgnoreCase(accountOk)) {
            registerTraveler(traveler.getUserId(), traveler);
        } else if ("N".equalsIgnoreCase(accountOk) || "R".equalsIgnoreCase(accountOk)) {
            travelerRepositoryService.updateTraveler(traveler);
        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        insertTravelerVerificationStatus(
                new TravelerVerificationStatusRequest(
                        traveler.getId(),
                        VerificationType.BANKACCOUNT.name(),
                        accountOk,
                        message,
                        traveler.getAccountBankbookPhoto(),
                        MDC.get("userId")
                )
        );

        int ynr = CustomStringUtil.ynrToNo(accountOk);
        Long orgId = Long.valueOf(ynr);

        // HK 고객인 경우 계좌 인증 알림 (JP는 계좌 인증이 없음)
        if (user.getCountryCode() == CountryCode.HK) {
            notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.AU, FcmEventValue.AT, orgId, message);
        }
    }

    public void validateVerification(Traveler traveler) throws CashmallowException {
        if (traveler == null) {
            throw new CashmallowException("EXCHANGE_NOT_REGISTERED_IDENTIFICATION");
        }

        if (!"Y".equals(traveler.getCertificationOk())) {
            throw new CashmallowException("EXCHANGE_NOT_APPROVED_IDENTIFICATION");
        }

        if (!"Y".equals(traveler.getAccountOk())) {
            throw new CashmallowException("EXCHANGE_NOT_APPROVED_BANK_ACCOUNT");
        }

        /* Check traveler passport expire date. */
        if (traveler.getCertificationType().equals(CertificationType.PASSPORT)) {
            try {
                // 현재 일자 기준으로 만료여부만 체크함.
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date expDate = sdf.parse(traveler.getPassportExpDate());

                Calendar limitDate = Calendar.getInstance();

                if (expDate.before(limitDate.getTime())) {

                    User user = userRepositoryService.getUserByUserId(traveler.getUserId());
                    String language = user.getLangKey().substring(0, 2);
                    Locale locale = new Locale(language);

                    String messageCode = "FCM_AU_AI_PASSPORT_EXPIRED";
                    String fcmMessage = messageSource.getMessage(messageCode, null, "", locale);
                    verifyIdentity(traveler.getId(), "N", fcmMessage);

                    if (CountryCode.JP.equals(user.getCountryCode())) {
                        TravelerRequestSender globalTraveler = new TravelerRequestSender(user, traveler, securityService.decryptAES256(traveler.getIdentificationNumber()));
                        globalQueueService.sendTravelerResult(user, globalTraveler);
                    }

                    String message = String.format("여행자 여권 만료, 사용자ID:%s, 처리값:%s", traveler.getUserId(), "N");
                    alarmService.aAlert("승인", message, user);

                    throw new CashmallowException("EXCHANGE_EXPIRED_PASSPORT");
                }
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    // 기능: 여행자의 정보를 수정한다.
    @Transactional(rollbackFor = CashmallowException.class)
    public void updateTravelerInfo(Long managerId, Traveler traveler, User user) throws CashmallowException {

        List<String> auths = userAdminService.getUserAuthListByUserId(managerId);
        if (auths.contains(ROLE_ASSIMAN)) {
            int rows = 0;

            // long userId = userParam.getId();
            // User user = userService.getUserByUserId(userId);
            // user.setCountry(userParam.getCountry());
            // user.setFirstName(userParam.getFirstName());
            // user.setLastName(userParam.getLastName());
            // user.setBirthDate(userParam.getBirthDate());

            rows = userRepositoryService.updateUser(user);

            if (rows != 1) {
                throw new CashmallowException("User Update Failure! (" + rows + " rows updated)");
            }

            rows = travelerRepositoryService.updateTraveler(traveler);

            if (rows != 1) {
                throw new CashmallowException("Traveler Update Failure! (" + rows + " rows updated)");
            }

        } else {
            throw new CashmallowException(MSG_NEED_AUTH);
        }
    }


    // 기능: 여행자 본인인증 정보 조회
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO getCertificationInfo(Long managerId, TravelerAskVO vo) throws CashmallowException {
        SearchResultVO result = null;
        String method = "getCertificationInfo():";

        List<String> auths = userAdminService.getUserAuthListByUserId(managerId);
        if (auths.contains(ROLE_ASSIMAN)) {
            logger.info("{} authService.containsRole(token, Const.ROLE_ASSIMAN) is TRUE", method);

            int page = vo.getPage() != null ? vo.getPage() : DEF_PAGE_NO;
            int size = vo.getSize() != null ? vo.getSize() : DEF_PAGE_SIZE;
            vo.setPage(page);
            vo.setSize(size);
            vo.setStart_row(size * page);
            vo.setIdentification_number(securityService.encryptAES256(vo.getIdentification_number()));

            result = new SearchResultVO(page, size, vo.getSort());

            int totalCount = travelerMapper.countTravelerCertificationInfo(vo);
            List<Map<String, Object>> vos = travelerMapper.getTravelerCertificationInfo(vo);

            List<Object> newVos = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Map<String, Object> tMap : vos) {
                String idNo = securityService.decryptAES256((String) tMap.get("identification_number"));
                tMap.put("identification_number", idNo);
                tMap.put("phone_number", securityService.decryptAES256((String) tMap.get("phone_number")));
                tMap.put("first_name", securityService.decryptAES256((String) tMap.get("first_name")));
                tMap.put("last_name", securityService.decryptAES256((String) tMap.get("last_name")));
                tMap.put("email", securityService.decryptAES256((String) tMap.get("email")));
                tMap.put("en_first_name", securityService.decryptAES256((String) tMap.get("en_first_name")));
                tMap.put("en_last_name", securityService.decryptAES256((String) tMap.get("en_last_name")));
                tMap.put("account_no", securityService.decryptAES256((String) tMap.get("account_no")));
                tMap.put("account_name", securityService.decryptAES256((String) tMap.get("account_name")));
                tMap.put("address", securityService.decryptAES256((String) tMap.get("address")));
                tMap.put("address_secondary", securityService.decryptAES256((String) tMap.get("address_secondary")));
                tMap.put("gender", (String) tMap.get("gender"));


                if (ObjectUtils.isNotEmpty(tMap.get("passport_exp_date"))) {
                    tMap.put("passport_exp_date", sdf.format(tMap.get("passport_exp_date")));
                }
                if (ObjectUtils.isNotEmpty(tMap.get("passport_issue_date"))) {
                    tMap.put("passport_issue_date", sdf.format(tMap.get("passport_issue_date")));
                }
                if (ObjectUtils.isNotEmpty(tMap.get("job"))) {
                    tMap.put("job", Job.valueOf((String) tMap.get("job")).getTitle(LocaleContextHolder.getLocale()));
                }
                newVos.add(tMap);
            }
            logger.debug("{}: vos={}", method, vos);

            result.setResult(newVos, totalCount, page);

        } else {
            logger.info("{} authService.containsRole(token, Const.ROLE_ASSIMAN) is FALSE", method);
            throw new CashmallowException(MSG_NEED_AUTH);
        }

        return result;
    }

    // 기능: 여행자 계좌 정보 조회
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public SearchResultVO getAccountInfo(Long managerId, TravelerAskVO vo) throws CashmallowException {
        SearchResultVO result = null;
        String method = "getAccountInfo():";

        List<String> auths = userAdminService.getUserAuthListByUserId(managerId);
        if (auths.contains(ROLE_ASSIMAN)) {
            logger.info("{} authService.containsRole(token, Const.ROLE_ASSIMAN) is TRUE", method);

            int page = vo.getPage() != null ? vo.getPage() : DEF_PAGE_NO;
            int size = vo.getSize() != null ? vo.getSize() : DEF_PAGE_SIZE;
            vo.setPage(page);
            vo.setSize(size);
            vo.setStart_row(size * page);
            vo.setIdentification_number(securityService.encryptAES256(vo.getIdentification_number()));

            result = new SearchResultVO(page, size, vo.getSort());

            int totalCount = travelerMapper.countTravelerAccountInfo(vo);
            List<Map<String, Object>> vos = travelerMapper.getTravelerAccountInfo(vo);

            List<Object> newVos = new ArrayList<>();
            for (Map<String, Object> tMap : vos) {
                tMap.put("first_name", securityService.decryptAES256((String) tMap.get("first_name")));
                tMap.put("last_name", securityService.decryptAES256((String) tMap.get("last_name")));
                tMap.put("email", securityService.decryptAES256((String) tMap.get("email")));
                tMap.put("en_first_name", securityService.decryptAES256((String) tMap.get("en_first_name")));
                tMap.put("en_last_name", securityService.decryptAES256((String) tMap.get("en_last_name")));
                tMap.put("account_no", securityService.decryptAES256((String) tMap.get("account_no")));
                tMap.put("account_name", securityService.decryptAES256((String) tMap.get("account_name")));

                tMap.put("address", securityService.decryptAES256((String) tMap.get("address")));
                tMap.put("address_secondary", securityService.decryptAES256((String) tMap.get("address_secondary")));
                newVos.add(tMap);
            }
            logger.debug("{}: vos={}", method, vos);

            result.setResult(newVos, totalCount, page);
        } else {
            logger.info("{} authService.containsRole(token, Const.ROLE_ASSIMAN) is FALSE", method);
            throw new CashmallowException(MSG_NEED_AUTH);
        }

        return result;
    }


    /**
     * Get confirm count by country
     *
     * @param country
     * @return : total, Y, N, R
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Map<String, Object> getConfirmCntByCountry(String country) {
        return travelerMapper.getConfirmCntByCountry(country);
    }


    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public BigDecimal getSumEMoneyByCountry(String country) {
        return travelerMapper.getSumEMoney(country);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public BigDecimal getSumRMoneyByCountry(String country) {
        return travelerMapper.getSumRMoney(country);
    }


    // -------------------------------------------------------------------------------
    // 12. 여행자 인출
    // -------------------------------------------------------------------------------


    // 기능: 12.9. 관리자용 인출 정보 조회
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ApiResultVO findAdminCashOut(Long managerId, AdminCashOutAskVO pvo) {
        ApiResultVO result = new ApiResultVO(CODE_INVALID_USER_ID);

        String method = "findAdminCashOut(): ";
        String error = "";
        Object obj = null;
        logger.info("{}: pvo={}", method, (pvo != null ? pvo.toString() : pvo));

        try {
            int startRow = pvo.getStart_row();

            int size = pvo.getSize() != null ? pvo.getSize() : DEF_PAGE_SIZE;
            pvo.setSize(size);

            int page = (startRow + size) / size - 1;

            SearchResultVO searchResult = new SearchResultVO(page, size, pvo.getSort());

            int totalCount = cashOutMapper.countAdminCashOut(pvo);
            List<AdminCashOutVO> vos = cashOutMapper.findAdminCashOut(pvo);

            searchResult.setResult(List.copyOf(vos), totalCount, page);

            obj = searchResult;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            error = "INTERNAL_SERVER_ERROR";
            result.setFailInfo(error);
            return result;
        }

        result.setSuccessInfo(obj);

        return result;
    }

    // -------------------------------------------------------------------------------
    // 27. 가맹점 평점
    // -------------------------------------------------------------------------------

    int getCountNewTravelers(String startDate, String endDate) {
        DurationDateVO durationDateVO = new DurationDateVO(startDate, endDate);
        return travelerMapper.getCountNewTravelers(durationDateVO);
    }


    @Transactional
    public void editTravelerInfo(long userId, EditTravelerRequest editTravelerRequest) throws CashmallowException {

        if (editTravelerRequest.getJob().equals(Job.UNKNOWN)) {
            throw new CashmallowException(TRAVELER_INVALID_JOB);
        }

        boolean match = redisService.isMatch(REDIS_KEY_PASSWORD_MATCH, String.valueOf(userId), editTravelerRequest.getOtp());
        if (!match) {
            logger.error("OTP가 일치하지 않음. userId:{}", userId);
            throw new CashmallowException(STATUS_TRAVELER_INFO_MODIFY_FAIL);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        user.setPhoneNumber(editTravelerRequest.getPhoneNumber());
        user.setPhoneCountry(editTravelerRequest.getPhoneCountry().name());
        traveler.setJob(editTravelerRequest.getJob());

        String address = editTravelerRequest.getAddress();
        if (StringUtils.isNotBlank(address)) {
            traveler.setAddress(address);
        }

        String addressSecondary = editTravelerRequest.getAddressSecondary();
        if (StringUtils.isNotBlank(addressSecondary)) {
            traveler.setAddressSecondary(addressSecondary);
        }

        int i1 = userRepositoryService.updateUser(user);
        int i2 = travelerRepositoryService.updateTraveler(traveler);
    }

    public List<GlobalTravelerWalletBalance> getTravelerBalance(Long travelerId) {
        return travelerMapper.getTravelerBalance(travelerId);
    }

    public void warningExpireWalletBefore7Day(Long walletId) throws CashmallowException {
        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(walletId);
        User user = userRepositoryService.getUserByTravelerId(travelerWallet.getTravelerId());

        notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.WL, FcmEventValue.BF, null);
        notificationService.sendEmailExpiredWalletBefore7Day(user);
    }

    // NPR 송금 BANK, WALLET, CASH_PICKUP 제한금액
    // TODO: 추후 기획 완료 시, 테이블에서 조회함. WALLET 이 먼저 나가므로 추가함.
    public String remittanceAmountLimitCheckByPartnerSub(String currency, Remittance.RemittanceType remittanceType, BigDecimal toAmt, Locale locale) {
        Object[] messageArray = new Object[3];
        messageArray[0] = currency;

        // 각 송금 유형별 한도 설정을 Map 으로 관리
        Map<Remittance.RemittanceType, RemittanceLimitDto> limitMap = Map.of(
                Remittance.RemittanceType.BANK, new RemittanceLimitDto(currency, "REMITTANCE_TYPE_BANK", "1,500,000", new BigDecimal("1500000")),
                Remittance.RemittanceType.WALLET, new RemittanceLimitDto(currency, "REMITTANCE_TYPE_WALLET", "50,000", new BigDecimal("50000")),
                Remittance.RemittanceType.CASH_PICKUP, new RemittanceLimitDto(currency, "REMITTANCE_TYPE_BANK", "999,999", new BigDecimal("999999"))
        );

        // 해당 송금 유형의 한도 정보 가져오기
        RemittanceLimitDto limitInfo = limitMap.get(remittanceType);
        if (limitInfo == null) {
            return "Invalid remittance type"; // 정의되지 않은 송금 유형인 경우
        }

        // 한도 초과 체크
        if (toAmt.compareTo(limitInfo.limit()) > 0) {
            messageArray[1] = messageSource.getMessage(limitInfo.messageKey(), null, "", locale);
            messageArray[2] = limitInfo.formattedLimit();

            String defaultMessage = messageArray[1] + " 송금은 최대 " + messageArray[2] + " " + messageArray[0] + " 까지 가능합니다.";
            return messageSource.getMessage(REMITTANCE_EXCEEDED_LIMIT, messageArray, defaultMessage, locale);
        }

        return null;
    }

}