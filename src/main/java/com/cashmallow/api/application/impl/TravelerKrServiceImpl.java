package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.hyphen.HyphenServiceImpl;
import com.cashmallow.api.interfaces.hyphen.dto.CertificationReqVO;
import com.cashmallow.api.interfaces.traveler.dto.RegisterTravelerKrRequest;
import com.cashmallow.common.CommDateTime;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TravelerKrServiceImpl {

    private final TravelerServiceImpl travelerService;
    private final TravelerRepositoryService travelerRepositoryService;

    private final UserRepositoryService userRepositoryService;

    private final HyphenServiceImpl hyphenService;

    private final SecurityService securityService;

    private final AlarmService alarmService;

    private final EnvUtil envUtil;

    private final Gson gsonPretty;

    private final MessageSource messageSource;

    /**
     * 한국 여행자 등록 v3
     * user 생일 업데이트, traveler 생성, 신분증 사진 등록
     *
     * @param userId
     * @param travelersRequest
     * @param certificationPicture
     * @return
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Traveler registerTravelerKrV3(long userId, RegisterTravelerKrRequest registerRequest) throws CashmallowException {
        // User 생년월일 update
        String brithDate = getBrithDate(registerRequest.getIdentificationNumber());
        log.info("updateUser BirthDate userId={}, birthDate={}", userId, brithDate);
        User user = userRepositoryService.getUserByUserId(userId);
        user.setBirthDate(brithDate);

        int age = CommDateTime.getAge(user.getBirthDate());
        if (age < Const.AGE_CAN_JOIN) {
            log.info("가입 최소 연령 미달자 (만 {} 세 이상). 생년월일: {}", Const.AGE_CAN_JOIN, user.getBirthDate());

            // 제한 나이 출력 메시지 동적으로 추가. 나이 변경 시, AGE_CAN_JOIN 숫자만 바꿔주면 됨.
            Object[] messageArray = new Object[1];
            messageArray[0]  = Const.AGE_CAN_JOIN;
            throw new CashmallowException(messageSource.getMessage("SIGNUP_ERROR_AGE_CONSTRAINT", messageArray, "SIGNUP_ERROR_AGE_CONSTRAINT", LocaleContextHolder.getLocale()));
        }

        if (registerRequest.getJob().equals(Job.UNKNOWN)) {
            throw new CashmallowException("TRAVELER_INVALID_JOB");
        }

        userRepositoryService.updateUser(user);

        // hyphen 신분증 검증
        CertificationReqVO certificationReqVO = new CertificationReqVO(
                registerRequest.getCertificationType(),
                registerRequest.getLocalName(),
                registerRequest.getIdentificationNumber(),
                registerRequest.getIssueDate(),
                brithDate,
                registerRequest.getLicenseNo(),
                registerRequest.getSerialNo());

        if (envUtil.isPrd()) {
            boolean certification = hyphenService.certification(certificationReqVO);

            if (!certification) {
                throw new CashmallowException("INCORRECT_INFORMATION");

            }
        }

        // traveler 등록
        Traveler existTraveler = travelerRepositoryService.getTravelerByUserId(userId);
        Traveler travelerVo;
        if (existTraveler == null) {
            travelerVo = new Traveler();
        } else {
            log.debug("existTraveler={}", existTraveler);
            log.info("existTravelerId={}", existTraveler.getId());
            travelerVo = existTraveler;
        }

        log.info("travelersRequest={}", JsonStr.toJson(registerRequest));
        travelerVo.updateTraveler(registerRequest);

        // 신분증 번호 암호화
        travelerVo.setIdentificationNumber(securityService.encryptAES256(travelerVo.getIdentificationNumber().toUpperCase()));
        log.info("registerTraveler Traveler={}", gsonPretty.toJson(travelerVo));

        // 본인 인증 신청
        Traveler traveler = travelerService.registerTraveler(userId, travelerVo);

        // 알림 발송
        String slackMessage = traveler.getCertificationType().name() +
                " 등록 신청" + " 사용자ID:" + userId +
                ", 국가:" + user.getCountry();
        alarmService.aAlert("승인", slackMessage, user);

        traveler.setAccountOk("N");
        traveler.setCertificationOk("W"); // 어스미 승인/거절 까지 대기 상태
        travelerRepositoryService.updateTraveler(traveler);
        return traveler;
    }

    /**
     * 주민등록번호에서 생년월일 가져오기
     *
     * @param juminNo
     * @return
     */
    private String getBrithDate(String juminNo) {
        if (juminNo.length() != 13) {
            throw new RuntimeException("Invalid Jumin Number Length");
        }

        String birthDay = juminNo.substring(0, 6);
        String sexSign = juminNo.substring(6, 7);
        log.debug("sexSign:{}", sexSign);

        String preYear = "";
        if (sexSign.equals("1") || sexSign.equals("2")) {
            preYear = "19";
        } else if (sexSign.equals("3") || sexSign.equals("4")) {
            preYear = "20";
        } else {
            throw new RuntimeException("Invalid Sex Sign");
        }
        return preYear + birthDay;
    }

}
