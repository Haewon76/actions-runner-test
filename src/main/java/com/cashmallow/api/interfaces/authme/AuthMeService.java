package com.cashmallow.api.interfaces.authme;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.FileService;
import com.cashmallow.api.domain.model.authme.Authme;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.traveler.*;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.infrastructure.security.SecurityServiceImpl;
import com.cashmallow.api.interfaces.authme.dto.*;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import static com.cashmallow.api.config.RabbitConfig.AUTHME_TIMEOUT_ROUTING_KEY;
import static com.cashmallow.api.config.RabbitConfig.AUTHME_TIMEOUT_TOPIC;
import static com.cashmallow.api.domain.shared.Const.FILE_KIND_CERTIFICATION;
import static com.cashmallow.api.domain.shared.Const.getAuthFilePath;
import static com.cashmallow.common.CommonUtil.*;
import static com.cashmallow.common.DateUtil.isDate;
import static com.cashmallow.common.TiffToImageConverter.convertToJpg;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthMeService {

    private final AuthMeAdminClient authMeAdminClient;
    private final AuthMeApiClient authMeApiClient;
    private final AuthMeProperties authMeProperties;
    private final SecurityServiceImpl securityService;
    private final FileService fileService;
    private final JsonUtil jsonUtil;
    private final TravelerMapper travelerMapper;
    private final AlarmService alarmService;
    private final UserMapper userMapper;
    private final RabbitTemplate rabbitTemplate;
    private final GlobalQueueService globalQueueService;
    private final AddressEnglishServiceImpl addressEnglishService;
    private final EnvUtil envUtil;
    private final TravelerRepositoryService travelerRepositoryService;
    private final MessageSource messageSource;

    @Value("${host.file.path.home}")
    private String hostFilePathHome;

    public AuthMeTokenResponseDto getAdminToken(CountryCode countryCode) {
        return AuthMeTokenResponseDto.toAdminResponse(
                null,
                authMeAdminClient.getToken(
                        authMeProperties.grantType(),
                        "AuthmeServices",
                        getClientId(countryCode, authMeProperties),
                        getClientSecret(countryCode, authMeProperties)
                ));
    }

    public AuthMeTokenResponseDto getApiToken(String clientId,
                                              CountryCode countryCode,
                                              CertificationType certificationType) {
        return AuthMeTokenResponseDto.toApiResponse(
                clientId,
                authMeAdminClient.getToken(
                        authMeProperties.grantType(),
                        authMeProperties.scope()
                                .replace("{userId}", clientId)
                                .replace("{eventName}", getAuthmeEventName(countryCode, certificationType)),
                        getClientId(countryCode, authMeProperties),
                        getClientSecret(countryCode, authMeProperties)
                ));
    }

    public void insertAuthmeWebhookLog(AuthMeCustomerWebhookResponse authMeWebhook, String webhookJson) {
        Authme authme = new Authme(
                authMeWebhook.customerId(),
                authMeWebhook.data().status(),
                authMeWebhook.event(),
                securityService.encryptAES256(webhookJson)
        );
        userMapper.insertAuthmeWebhookLog(authme);
    }

    /**
     * 인증 요청 후 10분이 지난 후에 authme에 현재 상태를 체크 진행
     *
     * @param customerId
     */
    public void checkTimeoutAndUpdateStatus(String customerId) {
        MDC.put(AuthInterceptor.SERVICE_COUNTRY, getCountryByAuthmeId(customerId));
        try {
            checkTimeoutAndUpdateStatus(customerId, false);
        } catch (Exception e) {
            log.error("authmeUpdateStatus() Delay : message={}", e.getMessage(), e);
        }
        MDC.remove(AuthInterceptor.SERVICE_COUNTRY);
    }

    public void checkTimeoutAndUpdateStatusManual(String customerId) {
        MDC.put(AuthInterceptor.SERVICE_COUNTRY, getCountryByAuthmeId(customerId));
        try {
            checkTimeoutAndUpdateStatus(customerId, true);
        } catch (Exception e) {
            log.error("authmeUpdateStatus() Manual : message={}", e.getMessage(), e);
        }
        MDC.remove(AuthInterceptor.SERVICE_COUNTRY);
    }

    private void checkTimeoutAndUpdateStatus(String customerId, boolean manual) throws Exception {
        String method = "checkTimeoutAndUpdateStatus()";
        Long userId = Long.parseLong(customerId.substring(2));

        User user = userMapper.getUserByUserId(userId);
        if (user == null) {
            log.warn("[Authme Timeout] user 정보가 등록되지 않았습니다. userId={}", customerId);
            return;
        }

        Traveler traveler = travelerMapper.getTravelerByUserId(userId);
        if (traveler == null) {
            log.warn("[Authme Timeout] traveler 정보가 등록되지 않았습니다. userId={}", customerId);
            return;
        }

        // 이미 인증 완료된 경우 처리하지 않음
        if ("Y".equalsIgnoreCase(traveler.getCertificationOk())) {
            if (manual) {
                String slackMessage = """
                            [%s] Authme 수동업데이트 실패
                        
                            이미 승인 상태 이므로 수동 업데이트 할 수 없습니다.
                            사용자ID: %s
                        """.formatted(traveler.getCertificationType().name(),
                        user.getCustomerId());

                alarmService.aAlert("승인", slackMessage, user);
            } else {
                log.warn("이미 승인 상태 이므로 업데이트 할 수 없습니다. customerId={}, status={}", customerId, traveler.getCertificationOk());
            }
            return;
        }

        try {
            AuthMeTokenResponseDto tokenResponse = getAdminToken(user.getCountryCode());
            String accessToken = tokenResponse.accessToken();

            AuthMeCustomerEventResponse customerEvent = getCustomerEvent(accessToken, customerId, user.getCountryCode(), traveler.getCertificationType());
            log.info("{} : status={}, code={}, message={}", method, customerEvent.status(), customerEvent.code(), customerEvent.message());
            if (customerEvent.isApproved()) {
                AuthmeEvent authmeEvent = getAuthmeImages(customerEvent, user, traveler, customerId, tokenResponse.accessToken());
                Traveler newTraveler = updateTravelerAppproved(user, traveler, authmeEvent, manual);
                if (newTraveler != null) {
                    List<TravelerImage> authmeImages = authmeEvent.travelerImages();
                    if (!"null".equalsIgnoreCase(traveler.getCertificationPhoto()) && StringUtils.isNotBlank(traveler.getCertificationPhoto())) {
                        try {
                            File certificationPhotoFile = fileService.download(FILE_KIND_CERTIFICATION, traveler.getCertificationPhoto());
                            fileService.uploadNoEncrypt(certificationPhotoFile, getAuthFilePath(traveler.getId()));
                            authmeImages.add(TravelerImage.from(traveler.getId(), traveler.getCertificationPhoto(), traveler.getCertificationType().name()));
                        } catch (Exception e) {
                            log.error("Authme certificationPhotoFile download error : {}", e.getMessage(), e);
                        }
                    }
                    saveAndSendTravelerImagesToGlobal(user, authmeImages);
                }
            } else {
                updateTravelerRejected(user, traveler, customerEvent.status(), manual);
            }
            // }
        } catch (Exception e) {
            updateTravelerRejected(user, traveler, "Error", manual);
            log.error("[Authme Timeout] 상태 업데이트 실패 (Reject 상태로 전환합니다). userId={}", customerId, e);
        }
    }

    private boolean isDifferentCertifiedUser(Traveler traveler) {
        Long travelerId = traveler.getId();
        String identificationNumber = securityService.decryptAES256(traveler.getIdentificationNumber());

        try {
            final TravelerVerificationHistory travelerVerificationHistory = travelerRepositoryService.getTravelerFirstVerification(travelerId);
            // 1번이라도 신분증 및 여권 인증 했다면 인증번호가 기존과 동일한지 체크 하고
            // 기존인증받은 정보와 다른 경우 Reject 처리
            if (travelerVerificationHistory != null) {
                Traveler oldTraveler = jsonUtil.fromJson(securityService.decryptAES256(travelerVerificationHistory.travelerJson()), Traveler.class);
                if (oldTraveler != null && StringUtils.isNotBlank(oldTraveler.getIdentificationNumber()) && StringUtils.isNotBlank(identificationNumber)) {
                    if (!isSameCertification(oldTraveler.getIdentificationNumber(), identificationNumber)) {
                        String message = """
                                [Reject] 신분증 정보가 일치하지 않습니다.
                                userId: %s
                                old IdentificationNumber: %s
                                new IdentificationNumber: %s
                                """.formatted(travelerId.toString(), oldTraveler.getIdentificationNumber(), identificationNumber);
                        alarmService.i("[신분증정보불일치]", message);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error("신분증 인증 정보 변경시 오류 발생 : {}", e.getMessage(), e);
        }

        return false;
    }

    private Traveler updateTravelerAppproved(User user,
                                             Traveler t,
                                             AuthmeEvent authmeEvent,
                                             boolean manual) {
        String method = "updateTravelerAppproved()";
        Traveler traveler = switch (user.getCountryCode()) {
            case HK -> updateTravelerApprovedHK(t, authmeEvent, manual);
            case JP -> updateTravelerApprovedJP(t, authmeEvent, manual);
            default -> throw new RuntimeException("지원하지 않는 국가입니다.");
        };

        if (isDifferentCertifiedUser(traveler)) {
            String statusMessage = messageSource.getMessage("FCM_AU_AI_2_TITLE", null, "", new Locale(user.getLangKey()));
            updateTravelerRejected(user, traveler, statusMessage, manual);
            return null;
        }

        AuthMeCustomerResponseDocumentDetails details = authmeEvent.customerEvent().getDocument().details();

        // 승인 전단계 처리
        traveler.setCertificationOk("N");
        traveler.setCertificationOkDate(null);

        // traveler certificationPhoto 값이 없는 경우 임의의 null 값 생성
        if (StringUtils.isBlank(traveler.getCertificationPhoto())) {
            traveler.setCertificationPhoto("null"); // 'null' string
        }

        // 영문 주소 업데이트
        final String address = details.address();
        if (StringUtils.isNotBlank(address)) {
            List<GoogleAddressResultResponse> searchResultForGlobal = addressEnglishService.getSearchResultForGlobal(address);
            if (searchResultForGlobal.isEmpty()) {
                traveler.setAddress(address);
                log.warn("Authme address 영문 주소 변환 실패. address={}", address);
            } else {
                GoogleAddressResultResponse addressEn = searchResultForGlobal.get(0);
                traveler.setAddressSecondary("");
                traveler.setAddressCity(addressEn.getCityName());
                traveler.setAddress(address);
                traveler.setAddressEn(addressEn.getFullAddress());
                String zipCode = addressEn.getZipCode();
                if (StringUtils.isNotBlank(zipCode) && zipCode.length() > 8) {
                    log.warn("구글주소 검색시 우편번호가 잘못됨. zipCode={}, address={}", zipCode, address);
                } else { // null 허용, 8자 이상 방어
                    traveler.setZipCode(zipCode);
                }
            }
        }

        // 생년월일 업데이트
        String dateOfBirth = details.dateOfBirth();
        if (StringUtils.isNotBlank(dateOfBirth)) {
            user.setBirthDate(dateOfBirth);
            user.setFirstName(traveler.getLocalFirstName());
            user.setLastName(traveler.getLocalLastName());
            userMapper.updateUser(user);
        }

        // EDD에 등록된 유저인지 체크
        traveler.setEddUser(userMapper.isEddUser(traveler.getUserId()));

        TravelerRequestSender travelerRequestSender = sendGlobalService(user, traveler, authmeEvent);
        traveler.setFaceRate(travelerRequestSender.getFaceRate());
        traveler.setImageIntegrity(travelerRequestSender.getImageIntegrity());
        traveler.setVisualAuthenticity(travelerRequestSender.getVisualAuthenticity());

        log.info("{} : travelerPassportIssueDate={}", method, traveler.getPassportIssueDate());
        // traveler 업데이트
        travelerMapper.updateTraveler(traveler);

        // 어드민 자동인증 플로우 처리(추후 필요시 추가)
        // commonService.verifyIdentity(traveler.getId(), "Authme", "Y", null);

        String slackMessage = """
                    [%s] Authme [%s]승인 완료(%s)
                    사용자ID: %s
                    국가: %s
                """.formatted(traveler.getCertificationType().name(),
                manual ? "수동" : "자동",
                authmeEvent.customerEvent().status(),
                user.getCustomerId(),
                user.getCountry());
        alarmService.aAlert("승인", slackMessage, user);

        travelerMapper.insertTravelerVerificationStatus(
                new TravelerVerificationStatusRequest(
                        traveler.getId(),
                        Traveler.VerificationType.CERTIFICATION.name(),
                        traveler.getCertificationOk(),
                        "Approval " + authmeEvent.customerEvent().status(),
                        traveler.getCertificationPhoto(),
                        MDC.get("userId")
                )
        );

        return traveler;
    }

    private Traveler updateTravelerApprovedJP(Traveler traveler, AuthmeEvent authmeEvent, boolean manual) {
        AuthMeCustomerResponseDocumentDetails data = authmeEvent.customerEvent().getDocument().details();

        switch (traveler.getCertificationType()) {
            case ID_CARD -> {
                // traveler.setPassportExpDate(data.expiryDate()); // NULL 로 들어옴 (추후 Authme에서 넣어줄 예정)
                // traveler.setSex(data.getGender());
                // traveler.setIdentificationNumber(securityService.encryptAES256(data.getIdNumber()));
                // traveler.setCertificationPhoto(data.getPhoto());
            }
            case DRIVER_LICENSE -> {
                // NULL 로 들어옴 기본은 '남'으로 넣고 (추후 앱애서 받아야함)
                // traveler.setSex(data.getGender() == null ? (traveler.getSex() == null ? Traveler.TravelerSex.MALE : traveler.getSex()) : data.getGender());
                // traveler.setPassportIssueDate(data.dateOfIssue());
                // traveler.setPassportExpDate(data.expiryDate());
                // traveler.setEnFirstName(data.getFirstName());
                // traveler.setEnLastName(data.getLastName());
            }
            case RESIDENCE_CARD -> {
                traveler.setSex(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getGender()).filter(Objects::nonNull).findFirst().orElse(Traveler.TravelerSex.MALE) :
                        data.getGender()
                );
                traveler.setPassportIssueDate(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().dateOfIssue()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.dateOfIssue()
                );
                traveler.setPassportExpDate(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().expiryDate()).filter(StringUtils::isNotEmpty).findFirst().orElse(null) :
                        data.expiryDate()
                );
                traveler.setPassportCountry(traveler.getPassportCountry());
                traveler.setEnFirstName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getFirstName()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.getFirstName()
                );
                traveler.setEnLastName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getLastName()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.getLastName()
                );
            }
        }

        traveler.setAccountName(traveler.getLocalLastName() + " " + traveler.getLocalFirstName());
        return traveler;
    }

    private Traveler updateTravelerApprovedHK(Traveler traveler, AuthmeEvent authmeEvent, boolean manual) {
        AuthMeCustomerResponseDocumentDetails data = authmeEvent.customerEvent().getDocument().details();


        switch (traveler.getCertificationType()) {
            case ID_CARD -> {
                traveler.setPassportIssueDate(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().dateOfIssue()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.dateOfIssue()
                );

                // traveler.getPassportIssueDate()가 yyyy-MM-dd 형식 인지 체크 한 후 해당 형식이 아닌경우 null 처리
                if (!isDate(traveler.getPassportIssueDate())) {
                    traveler.setPassportIssueDate(null);
                }

                traveler.setSex(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getGender()).filter(Objects::nonNull).findFirst().orElse(Traveler.TravelerSex.MALE) :
                        data.getGender()
                );
                traveler.setIdentificationNumber(manual ?
                        securityService.encryptAES256(authmeEvent.customerEvent().documents().stream().map(m -> m.details().getIdNumber()).filter(Objects::nonNull).findFirst().orElse("")) :
                        securityService.encryptAES256(data.getIdNumber())
                );
                traveler.setEnFirstName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getFirstName()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.getFirstName()
                );
                traveler.setEnLastName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getLastName()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.getLastName()
                );
            }
            case PASSPORT -> {
                // traveler.setCertificationType(Traveler.CertificationType.PASSPORT);
                traveler.setSex(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().getGender()).filter(Objects::nonNull).findFirst().orElse(Traveler.TravelerSex.MALE) :
                        data.getGender()
                );
                traveler.setPassportExpDate(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().expiryDate()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.expiryDate()
                );
                traveler.setPassportCountry(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().nationality()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.nationality()
                );
                traveler.setIdentificationNumber(manual ?
                        securityService.encryptAES256(authmeEvent.customerEvent().documents().stream().map(m -> m.details().documentNumber()).filter(Objects::nonNull).findFirst().orElse("")) :
                        securityService.encryptAES256(data.documentNumber())
                );
                traveler.setEnFirstName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().givenName()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.givenName()
                );
                traveler.setEnLastName(manual ?
                        authmeEvent.customerEvent().documents().stream().map(m -> m.details().surname()).filter(Objects::nonNull).findFirst().orElse("") :
                        data.surname()
                );
            }
        }

        // 로컬이름이 없는 경우 영문이름으로 대체
        if (StringUtils.isBlank(traveler.getLocalFirstName()) || StringUtils.isBlank(traveler.getLocalLastName())) {
            traveler.setLocalFirstName(traveler.getEnFirstName());
            traveler.setLocalLastName(traveler.getEnLastName());
        }

        traveler.setAccountName(traveler.getEnLastName() + " " + traveler.getEnFirstName());

        return traveler;
    }

    // Reject 상태로 변경
    private void updateTravelerRejected(User user, Traveler traveler, String status, boolean manual) {
        String method = "updateTravelerRejected()";
        String slackMessage = """
                    [%s] Authme [%s]승인 실패(%s)
                    사용자ID: %s
                    국가: %s
                """.formatted(traveler.getCertificationType().name(),
                manual ? "수동" : "자동",
                status,
                user.getCustomerId(),
                user.getCountry());
        alarmService.aAlert("승인", slackMessage, user);

        log.info("{} : traveler issueDate={}", method, traveler.getPassportIssueDate());

        traveler.setCertificationOk("R");
        traveler.setCertificationOkDate(null);
        travelerMapper.updateTraveler(traveler);
        sendGlobalService(user, traveler, null);

        travelerMapper.insertTravelerVerificationStatus(
                new TravelerVerificationStatusRequest(
                        traveler.getId(),
                        Traveler.VerificationType.CERTIFICATION.name(),
                        traveler.getCertificationOk(),
                        status,
                        traveler.getCertificationPhoto(),
                        MDC.get("userId")
                )
        );
    }

    // Authme에 저장된 이미지 다운 받아서 스토리지에 업로드
    public AuthmeEvent getAuthmeImages(AuthMeCustomerEventResponse customerEvent,
                                       User user,
                                       Traveler traveler,
                                       String customerId,
                                       String accessToken) {
        List<TravelerImage> travelerImages = new ArrayList<>();
        // 고객정보의 미디어 정보 업로드
        getCustomerEventMedia(accessToken, customerId, user.getCountryCode(), traveler.getCertificationType()).items()
                .stream()
                .filter(m -> StringUtils.isNoneEmpty(m.mediaId()))
                .forEach(f -> {
                    String fileName = f.mediaId();
                    AuthMeCustomerEventMediaImageResponse customerEventMediaImage = getCustomerEventMediaImage(accessToken, customerId, f.mediaId(), user.getCountryCode(), traveler.getCertificationType());
                    String base64Image = customerEventMediaImage.content();
                    if (StringUtils.isNotBlank(base64Image)) {
                        try {
                            String fileServerDir = hostFilePathHome + getAuthFilePath(traveler.getId());
                            File base64ToJPGFile = getBase64ToJPGFile(fileServerDir, fileName, convertToJpg(base64Image));
                            fileService.upload(base64ToJPGFile, getAuthFilePath(traveler.getId()));
                            travelerImages.add(TravelerImage.from(traveler.getId(), fileName, f.getType()));
                        } catch (Exception e) {
                            log.error("AuthMeCustomerEventMediaImageResponse error : {}", e.getMessage(), e);
                        }
                    }
                });
        return new AuthmeEvent(customerEvent, travelerImages);
    }

    public void saveAndSendTravelerImagesToGlobal(User user, List<TravelerImage> authmeImages) {
        // certification 전송.
        for (TravelerImage image : authmeImages) {
            travelerMapper.addTravelerImage(image);
            globalQueueService.sendTravelerCertification(user, image);
        }
    }

    private TravelerRequestSender sendGlobalService(User user,
                                                    Traveler traveler,
                                                    @Nullable AuthmeEvent authmeEvent) {
        TravelerRequestSender globalTraveler = new TravelerRequestSender(user, traveler, authmeEvent, securityService.decryptAES256(traveler.getIdentificationNumber()));
        globalQueueService.sendTravelerResult(user, globalTraveler);
        return globalTraveler;
    }

    AuthMeCustomerEventResponse getCustomerEvent(String accessToken, String travelerId, CountryCode countryCode, CertificationType certificationType) {
        try {
            String customerEvent = authMeApiClient.getCustomerEvent(accessToken, travelerId, getAuthmeEventName(countryCode, certificationType));
            String jsonLog = Arrays.stream(customerEvent.split("document")).findFirst().toString();
            log.debug(jsonLog);
            return jsonUtil.fromJson(customerEvent, AuthMeCustomerEventResponse.class);
        } catch (Exception e) {
            log.error("AuthMeService.getCustomerEvent error: " + e.getMessage() + "travelerId: " + travelerId + ", countryCode: " + countryCode + ", certificationType: " + certificationType + ", accessToken: " + accessToken);
            return AuthMeCustomerEventResponse.rejected();
        }
    }

    AuthMeCustomerEventMediaResponse getCustomerEventMedia(String accessToken, String travelerId, CountryCode countryCode, CertificationType certificationType) {
        String json = authMeApiClient.getCustomerEventMedia(accessToken, travelerId, getAuthmeEventName(countryCode, certificationType));
        return jsonUtil.fromJson(json, AuthMeCustomerEventMediaResponse.class);
    }

    AuthMeCustomerEventMediaImageResponse getCustomerEventMediaImage(String accessToken, String travelerId, String mediaId, CountryCode countryCode, CertificationType certificationType) {
        String json = authMeApiClient.getCustomerEventMediaImage(accessToken, travelerId, mediaId, getAuthmeEventName(countryCode, certificationType));
        return jsonUtil.fromJson(json, AuthMeCustomerEventMediaImageResponse.class);
    }

    public void checkAuthmeStatus(User user, Traveler traveler) {
        if (traveler != null) {
            int delayTimes = 5 * 60 * 1000; // 5분 이후에 결과값 체크
            rabbitTemplate.convertAndSend(AUTHME_TIMEOUT_TOPIC,
                    AUTHME_TIMEOUT_ROUTING_KEY,
                    user.getCustomerId(),
                    message -> {
                        message.getMessageProperties().setHeader("x-delay", delayTimes);
                        log.info("rabbitTemplate: send message");
                        return message;
                    });
            log.info("delayedSend: send message");
        }
    }
}
