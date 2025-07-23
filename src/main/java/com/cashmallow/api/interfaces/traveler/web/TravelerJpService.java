package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.FileService;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.domain.model.traveler.GlobalTravelerCertificationStep;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerRequestSender;
import com.cashmallow.api.domain.model.traveler.enums.ApprovalType;
import com.cashmallow.api.domain.model.traveler.enums.CertificationStep;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.security.SecurityServiceImpl;
import com.cashmallow.api.interfaces.global.GlobalQueueService;
import com.cashmallow.api.interfaces.global.dto.GlobalTravelerCertificationRequest;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerJpRequest;
import com.cashmallow.api.interfaces.traveler.dto.TravelerCertificationJpRequest;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Validated
public class TravelerJpService {

    private final TravelerServiceImpl travelerService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final UserRepositoryService userRepositoryService;
    private final SecurityServiceImpl securityService;
    private final JsonUtil jsonUtil;
    private final AddressEnglishServiceImpl addressEnglishService;
    private final FileService fileService;
    private final GlobalQueueService globalQueueService;
    private final AlarmService alarmService;
    private final MessageSource messageSource;


    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler registerTravelerJpForAuto(long userId, RegisterTravelerJpRequest request, MultipartFile mf) throws CashmallowException {
        // 1. User 생년월일 update
        User user = userRepositoryService.getUserByUserId(userId);
        user.setBirthDate(request.dateOfBirth());
        log.info("updateUser BirthDate userId={}, birthDate={}", userId, user.getBirthDate());

        // RESIDENCE_CARD 인 경우에는 생년월일이 없어서 체크하지 않고 패스하도록 처리
        if (request.certificationType() != CertificationType.RESIDENCE_CARD) {
            int age = CommDateTime.getAge(user.getBirthDate());
            if (age < Const.AGE_CAN_JOIN_JP) {
                log.info("가입 최소 연령 미달자 (만 {} 세 이상). 생년월일: {}", Const.AGE_CAN_JOIN_JP, user.getBirthDate());

                // 제한 나이 출력 메시지 동적으로 추가. 나이 변경 시, AGE_CAN_JOIN_JP 숫자만 바꿔주면 됨.
                Object[] messageArray = new Object[1];
                messageArray[0]  = Const.AGE_CAN_JOIN_JP;
                throw new CashmallowException(messageSource.getMessage("SIGNUP_ERROR_AGE_CONSTRAINT", messageArray, "SIGNUP_ERROR_AGE_CONSTRAINT", LocaleContextHolder.getLocale()));
            }
        }

        userRepositoryService.updateUser(user);

        // 2. traveler 등록
        Traveler existTraveler = travelerRepositoryService.getTravelerByUserId(userId);
        Traveler travelerVo;
        if (existTraveler == null) {
            travelerVo = new Traveler();
            // travelerVo.setSex(Traveler.TravelerSex.MALE);
        } else {
            log.debug("existTraveler={}", existTraveler);
            log.info("existTravelerId={}", existTraveler.getId());
            travelerVo = existTraveler;
        }

        log.debug("travelersRequest={}", jsonUtil.toJson(request));

        // 일본어 주소 영문 주소 변환
        if (StringUtils.isNotBlank(request.address())) {
            travelerVo.setAddress(request.address());
            List<GoogleAddressResultResponse> searchResultForGlobal = addressEnglishService.getSearchResultForGlobal(request.address());
            if (searchResultForGlobal.isEmpty()) {
                log.warn("영문 주소 변환 실패. address={}", request.address());
            } else {
                GoogleAddressResultResponse addressEn = searchResultForGlobal.get(0);
                travelerVo.setAddressSecondary("");
                travelerVo.setAddressCity(addressEn.getCityName());
                travelerVo.setAddressEn(addressEn.getFullAddress());
                String zipCode = addressEn.getZipCode();
                if (StringUtils.isNotBlank(zipCode) && zipCode.length() > 8) {
                    log.warn("구글주소 검색시 우편번호가 잘못됨. zipCode={} address={}", addressEn.getZipCode(), request.address());
                }

                travelerVo.setZipCode(addressEn.getZipCode());
            }
        }

        travelerVo.updateTraveler(request);
        travelerVo.setCertificationOk("W"); // 어스미 승인/거절 까지 대기 상태
        travelerVo.setApprovalType(ApprovalType.NFC);
        log.info("traveler localName = {}, accountName={}", travelerVo.getLocalFirstName() + travelerVo.getLocalLastName(), travelerVo.getAccountName());

        // 3. 신분증 번호 암호화
        travelerVo.setIdentificationNumber(securityService.encryptAES256(travelerVo.getIdentificationNumber().toUpperCase()));
        log.debug("registerTraveler Traveler={}", jsonUtil.toJson(travelerVo));

        // 4. traveler 등록
        travelerService.registerTraveler(userId, travelerVo);

        return travelerService.updateCertificationPhoto(userId, mf, "registerTravelerJp");
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler registerTravelerJpForManual(long userId, @Valid RegisterTravelerJpRequest request) throws CashmallowException {
        // 1. User 생년월일 update
        User user = userRepositoryService.getUserByUserId(userId);
        user.setBirthDate(request.dateOfBirth());
        log.info("updateUser BirthDate userId={}, birthDate={}", userId, user.getBirthDate());

        // RESIDENCE_CARD 인 경우에는 생년월일이 없어서 체크하지 않고 패스하도록 처리
        if (request.certificationType() != CertificationType.RESIDENCE_CARD) {
            int age = CommDateTime.getAge(user.getBirthDate());
            if (age < Const.AGE_CAN_JOIN_JP) {
                log.info("가입 최소 연령 미달자 (만 {} 세 이상). 생년월일: {}", Const.AGE_CAN_JOIN_JP, user.getBirthDate());

                // 제한 나이 출력 메시지 동적으로 추가
                Object[] messageArray = new Object[1];
                messageArray[0]  = Const.AGE_CAN_JOIN_JP;
                throw new CashmallowException(messageSource.getMessage("SIGNUP_ERROR_AGE_CONSTRAINT", messageArray, "SIGNUP_ERROR_AGE_CONSTRAINT", LocaleContextHolder.getLocale()));
            }
        }

        userRepositoryService.updateUser(user);

        // 2. traveler 등록
        Traveler existTraveler = travelerRepositoryService.getTravelerByUserId(userId);
        Traveler travelerVo;
        if (existTraveler == null) {
            travelerVo = new Traveler();
            // travelerVo.setSex(Traveler.TravelerSex.MALE);

            // 2시간 이내에 사진을 올린경우에만 받아줌.
            restoreCertificationStepBeforeTwoHours(userId);
        } else {
            log.debug("existTraveler={}", existTraveler);
            log.info("existTravelerId={}", existTraveler.getId());
            travelerVo = existTraveler;
        }

        log.debug("travelersRequest={}", jsonUtil.toJson(request));

        // 일본어 주소 영문 주소 변환
        if (StringUtils.isNotBlank(request.address())) {
            travelerVo.setAddress(request.address());
            List<GoogleAddressResultResponse> searchResultForGlobal = addressEnglishService.getSearchResultForGlobal(request.address());
            if (searchResultForGlobal.isEmpty()) {
                log.warn("영문 주소 변환 실패. address={}", request.address());
            } else {
                GoogleAddressResultResponse addressEn = searchResultForGlobal.get(0);
                travelerVo.setAddressSecondary("");
                travelerVo.setAddressCity(addressEn.getCityName());
                travelerVo.setAddressEn(addressEn.getFullAddress());
                if (StringUtils.isNotBlank(addressEn.getZipCode()) && addressEn.getZipCode().length() > 8) {
                    log.warn("구글주소 검색시 우편번호가 잘못됨. zipCode={} address={}", addressEn.getZipCode(), request.address());
                }

                travelerVo.setZipCode(addressEn.getZipCode());
            }
        }

        travelerVo.updateTraveler(request);
        travelerVo.setCertificationOk("N");
        travelerVo.setApprovalType(ApprovalType.MANUAL);
        log.info("traveler localName = {}, accountName={}", travelerVo.getLocalFirstName() + travelerVo.getLocalLastName(), travelerVo.getAccountName());

        // 3. 신분증 번호 암호화
        travelerVo.setIdentificationNumber(securityService.encryptAES256(travelerVo.getIdentificationNumber().toUpperCase()));
        travelerVo.setCertificationOkDate(null);
        log.debug("registerTraveler Traveler={}", jsonUtil.toJson(travelerVo));

        // 4. traveler 등록
        Traveler resultTraveler = travelerService.registerTraveler(userId, travelerVo);

        resultTraveler.setIdentificationNumber(securityService.decryptAES256(travelerVo.getIdentificationNumber()));

        TravelerRequestSender globalTraveler = new TravelerRequestSender(user, resultTraveler, resultTraveler.getIdentificationNumber());
        globalQueueService.sendTravelerResult(user, globalTraveler);

        sendSlackManual(travelerVo.getCertificationType(), user);

        return travelerVo;
    }

    private void sendSlackManual(CertificationType certificationType, User user) {
        String slackMessage = String.format(" [%s] 수동인증 승인 신청 \n사용자ID:%s \n국가:%s", certificationType.name(), "JP" + user.getId(), user.getCountry());

        alarmService.aAlert("승인", slackMessage, user);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void updateTravelerCertificationStep(@Valid TravelerCertificationJpRequest certificationJpRequest, MultipartFile photoFile) throws CashmallowException {
        String photoFileName = fileService.upload(photoFile, Const.FILE_SERVER_JP_CERTIFICATION);

        // 최초 신청시 traveler모델이 없기때문에 userId로 받음
        GlobalTravelerCertificationStep globalTravelerCertificationStep = new GlobalTravelerCertificationStep(
                certificationJpRequest.userId(),
                certificationJpRequest.certificationType(),
                certificationJpRequest.certificationStep(),
                photoFileName
        );

        List<GlobalTravelerCertificationStep> activeStepList = travelerRepositoryService.getActiveGlobalTravelerCertificationSteps(certificationJpRequest.userId());

        boolean hasDuplicateType = activeStepList.stream()
                .filter(Objects::nonNull) // null 값 제거
                .map(GlobalTravelerCertificationStep::getCertificationStep) // CertificationStep만 추출
                .anyMatch(step -> step.equals(globalTravelerCertificationStep.getCertificationStep()));

        if (hasDuplicateType) {
            log.warn("updateTravelerCertificationStep() : 같은 Type을 중복요청");
            return;
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(certificationJpRequest.userId());
        User user = userRepositoryService.getUserByUserId(certificationJpRequest.userId());
        boolean needTravelerUpdate = false;

        if (ObjectUtils.isNotEmpty(traveler) && traveler.getCertificationOk().equals("R")) {
            activeStepList.add(globalTravelerCertificationStep);
            boolean hasAllSteps;

            if (globalTravelerCertificationStep.getCertificationType().equals(CertificationType.ID_CARD)) {
                hasAllSteps = activeStepList.stream()
                        .filter(Objects::nonNull)
                        .map(GlobalTravelerCertificationStep::getCertificationStep)
                        .collect(Collectors.toSet()).containsAll(EnumSet.of(CertificationStep.FRONT, CertificationStep.BACK, CertificationStep.SIDE, CertificationStep.IN_HAND));
            } else {
                hasAllSteps = activeStepList.stream()
                        .filter(Objects::nonNull)
                        .map(GlobalTravelerCertificationStep::getCertificationStep)
                        .collect(Collectors.toSet()).containsAll(EnumSet.of(CertificationStep.FRONT, CertificationStep.BACK, CertificationStep.SIDE, CertificationStep.IN_HAND, CertificationStep.EXTRA_DOCUMENT));
            }

            if (hasAllSteps) {
                traveler.setCertificationOk("N");
                needTravelerUpdate = true;
            }
        }

        if (ObjectUtils.isNotEmpty(traveler) && CertificationStep.FRONT.equals(globalTravelerCertificationStep.getCertificationStep())) {
            traveler.setApprovalType(ApprovalType.MANUAL);
            traveler.setCertificationType(certificationJpRequest.certificationType());
            needTravelerUpdate = true;
        }

        if (ObjectUtils.isNotEmpty(traveler) && CertificationStep.ACCOUNT.equals(globalTravelerCertificationStep.getCertificationStep())) {
            traveler.setAccountName(certificationJpRequest.accountLastName() + " " + certificationJpRequest.accountFirstName());
            // ApprovalType이 NFC인 경우를 대비해서 바로 승인 신청으로 추가
            traveler.setCertificationOk("N");
            traveler.setNeedJpAccountRegister("N");
            needTravelerUpdate = true;
        }

        if (needTravelerUpdate) {
            travelerRepositoryService.updateTraveler(traveler);
            TravelerRequestSender globalTraveler = new TravelerRequestSender(user, traveler, securityService.decryptAES256(traveler.getIdentificationNumber()));
            globalQueueService.sendTravelerResult(user, globalTraveler);
        }

        travelerRepositoryService.insertGlobalTravelerCertificationStep(globalTravelerCertificationStep);

        globalQueueService.sendTravelerCertificationStep(globalTravelerCertificationStep);

        if (CertificationStep.FRONT.equals(globalTravelerCertificationStep.getCertificationStep())) {
            globalQueueService.timeoutCertificationStep(certificationJpRequest.userId());
        }
    }

    @Transactional(readOnly = true)
    public List<GlobalTravelerCertificationStep> getActiveCertificationStepList(Long userId) throws CashmallowException {
        return travelerRepositoryService.getActiveGlobalTravelerCertificationSteps(userId);
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void timeoutCertificationStep(Long userId) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        List<GlobalTravelerCertificationStep> globalTravelerCertificationStepList = getActiveCertificationStepList(userId);

        boolean hasAllSteps = globalTravelerCertificationStepList.stream()
                .filter(Objects::nonNull)
                .map(GlobalTravelerCertificationStep::getCertificationStep)
                .collect(Collectors.toSet()).containsAll(EnumSet.of(CertificationStep.FRONT, CertificationStep.BACK, CertificationStep.SIDE, CertificationStep.IN_HAND));

        if (ObjectUtils.isNotEmpty(traveler) && (traveler.getCertificationOk().equals("Y") || hasAllSteps)) {
            return;
        }

        if (globalTravelerCertificationStepList.isEmpty()) {
            return;
        }

        GlobalTravelerCertificationStep firstCertificationStep = globalTravelerCertificationStepList.stream()
                .filter(step -> step.getCertificationStep().equals(CertificationStep.FRONT)).findFirst().orElse(null);

        if (ObjectUtils.isEmpty(firstCertificationStep)) {
            return;
        }

        travelerRepositoryService.timeoutGlobalTravelerCertificationStep(userId);

        globalTravelerCertificationStepList.forEach(step -> {
            step.setActive(false);
            globalQueueService.sendTravelerCertificationStep(step);
        });
    }

    private void restoreCertificationStepBeforeTwoHours(Long userId) throws CashmallowException {
        LocalDateTime minusTwoHours = LocalDateTime.now().minusHours(2);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        Map<String, Object> param = new HashMap<>();
        param.put("userId", userId);
        param.put("createdDate", minusTwoHours.format(formatter));

        List<GlobalTravelerCertificationStep> targetList = travelerRepositoryService.getGlobalTravelerCertificationSteps(param);

        boolean hasAllSteps = targetList.stream()
                .filter(Objects::nonNull)
                .map(GlobalTravelerCertificationStep::getCertificationStep)
                .collect(Collectors.toSet()).containsAll(EnumSet.of(CertificationStep.FRONT, CertificationStep.BACK, CertificationStep.SIDE, CertificationStep.IN_HAND));

        if (!hasAllSteps) {
            // 타임아웃 메세지로 변경할 것
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        List<GlobalTravelerCertificationStep> filteredList = targetList.stream()
                .filter(Objects::nonNull)
                .filter(step -> EnumSet.of(CertificationStep.FRONT, CertificationStep.BACK, CertificationStep.SIDE, CertificationStep.IN_HAND, CertificationStep.EXTRA_DOCUMENT)
                        .contains(step.getCertificationStep())) // 필요한 Enum 값만 필터링
                .collect(Collectors.toMap(
                        GlobalTravelerCertificationStep::getCertificationStep, // Enum 값 기준으로 그룹핑
                        step -> step, // 첫 번째 값 저장
                        (existing, replacement) -> existing, // 중복 시 기존 값 유지 (첫 번째 값 유지)
                        LinkedHashMap::new // 순서 보장
                ))
                .values()
                .stream()
                .toList();

        filteredList.forEach(step -> {
            step.setActive(true);
            travelerRepositoryService.updateGlobalTravelerCertificationStep(step);
            globalQueueService.sendTravelerCertificationStep(step);
        });
    }

    @Transactional
    public void resetCertificationStep(Long travelerId, GlobalTravelerCertificationRequest request) throws CashmallowException {
        try {
            travelerService.verifyIdentityByAdmin(travelerId, request.managerName(), "Z", request.rejectReason(), false);
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
            travelerService.verifyBankAccountByAdmin(traveler, "N", request.rejectReason());

            List<GlobalTravelerCertificationStep> activeStepList = travelerRepositoryService.getActiveGlobalTravelerCertificationSteps(travelerId);
            activeStepList.forEach(step -> {
                step.setActive(false);
                travelerRepositoryService.updateGlobalTravelerCertificationStep(step);
            });
        } catch (CashmallowException e) {
            throw new CashmallowException(e.getMessage());
        }
    }

    @Transactional
    public void deactivateGlobalTravelerCertificationStep(Long stepId) {
        GlobalTravelerCertificationStep globalTravelerCertificationStep = travelerRepositoryService.getGlobalTravelerCertificationStepById(stepId);
        globalTravelerCertificationStep.setActive(false);
        travelerRepositoryService.updateGlobalTravelerCertificationStep(globalTravelerCertificationStep);
    }
}
