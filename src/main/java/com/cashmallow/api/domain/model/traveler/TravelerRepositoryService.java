package com.cashmallow.api.domain.model.traveler;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCreateCustomerResponse;
import com.cashmallow.api.interfaces.authme.dto.TravelerImage;
import com.cashmallow.common.JsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class TravelerRepositoryService {

    private final TravelerMapper travelerMapper;

    private final UserRepositoryService userRepositoryService;

    private final JsonUtil jsonUtil;

    private final SecurityService securityService;


    // 기능: 21.2. userId의 여행자 정보 읽기
    public Traveler getTravelerByUserId(Long userId) {
        return travelerMapper.getTravelerByUserId(userId);
    }

    public List<TravelerVerificationStatusResponse> getTravelerVerificationStatuses(Long travelerId) {
        return travelerMapper.getTravelerVerificationStatuses(travelerId);
    }

    public List<TravelerImage> getTravelerImages(Long travelerId) {
        return travelerMapper.getTravelerImages(travelerId);
    }

    // travelerId의 여행자 정보 읽기
    public Traveler getTravelerByTravelerId(Long travelerId) {
        return travelerMapper.getTravelerByTravelerId(travelerId);
    }


    public String getNameByTravelerId(long travelerId) {
        Map<String, String> nameByTravelerId = travelerMapper.getNameByTravelerId(travelerId);

        String isFamilyNameAfterFirstName = nameByTravelerId.get("is_family_name_after_first_name");
        String localFirstName = nameByTravelerId.get("local_first_name");
        String localLastName = nameByTravelerId.get("local_last_name");

        return "Y".equals(isFamilyNameAfterFirstName) ? localFirstName + localLastName : localLastName + localFirstName;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public int insertTraveler(Traveler traveler) throws CashmallowException {

        int affectedRow = travelerMapper.insertTraveler(traveler);

        if (affectedRow >= 1) {
            log.info("여행자 가입 결과: {}", affectedRow);

            User user = userRepositoryService.getUserByUserId(traveler.getUserId());

            if (!StringUtils.isEmpty(traveler.getLocalFirstName()) && !StringUtils.isEmpty(traveler.getLocalLastName())) {
                user.setFirstName(traveler.getLocalFirstName());
                user.setLastName(traveler.getLocalLastName());

                affectedRow = userRepositoryService.updateUser(user);
                if (affectedRow != 1) {
                    log.error("{} User table update failed. userId={}", "insertTraveler", traveler.getUserId());
                    throw new CashmallowException(INTERNAL_SERVER_ERROR);
                }
            }
        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return affectedRow;
    }

    // 기능: 여행자의 정보를 수정한다.
    // TODO : inspectDuplicateCertificationNumber같은 공통적으로 데이터를 다 체크해야 하는 부분은 updateTraveler의 서비스에서 다 체크하고 그외에선 제거해버려야한다.
    // 그러나 지금 변경하려면 받는 parameter중에 user_id가 포함되어야 함으로 우선 급한 것 부터 변경하고 지나감.
    @Transactional
    public int updateTraveler(Traveler traveler) throws CashmallowException {
        return travelerMapper.updateTraveler(traveler);
    }

    public void addTravelerVerificationHistory(Long travelerId, String managerName) {
        Traveler traveler = getTravelerByTravelerId(travelerId);
        traveler.decryptData(securityService);

        User user = userRepositoryService.getUserByTravelerId(travelerId);

        // 신분증 or 여권 인증 완료 후 히스토리 기록
        TravelerVerificationHistory travelerVerificationHistory = new TravelerVerificationHistory(
                traveler.getId().toString(),
                securityService.encryptAES256(jsonUtil.toJson(traveler)),
                securityService.encryptAES256(jsonUtil.toJson(user)),
                managerName
        );

        travelerMapper.addTravelerVerificationHistory(travelerVerificationHistory);
    }

    public TravelerVerificationHistory getTravelerFirstVerification(Long travelerId) {
        return travelerMapper.getTravelerFirstVerification(travelerId);
    }

    public boolean isTravelerVerifiedMoreThanOnce(Long travelerId) {
        return getTravelerFirstVerification(travelerId) != null;
    }

    public void insertComplyadvantageCustomer(ComplyAdvantageCreateCustomerResponse customerResponse) {
        travelerMapper.insertComplyadvantageCustomer(customerResponse);
    }

    public String getComplyAdvantageCustomerId(Long userId) {
        return travelerMapper.getComplyadvantageCustomerId(userId);
    }

    public TravelerImage getTravelerImage(Long travelerId) {
        return travelerMapper.getTravelerImage(travelerId);
    }

    public List<Traveler> getVerifiedTravelListById() {
        return travelerMapper.getVerifiedTravelerById();
    }

    public List<Traveler> getVerifiedTravelListByPassport() {
        return travelerMapper.getVerifiedTravelerByPassport();
    }

    public void insertGlobalTravelerCertificationStep(GlobalTravelerCertificationStep travelerCertificationStep) {
        travelerMapper.insertGlobalTravelerCertificationStep(travelerCertificationStep);
    }

    public int updateGlobalTravelerCertificationStep(GlobalTravelerCertificationStep travelerCertificationStep) {
        return travelerMapper.updateGlobalTravelerCertificationStep(travelerCertificationStep);
    }

    public List<GlobalTravelerCertificationStep> getActiveGlobalTravelerCertificationSteps(Long userId) {
        return travelerMapper.getActiveGlobalTravelerCertificationSteps(userId);
    }

    public int timeoutGlobalTravelerCertificationStep(Long userId) {
        return travelerMapper.timeoutGlobalTravelerCertificationStep(userId);
    }

    public List<GlobalTravelerCertificationStep> getGlobalTravelerCertificationSteps(Map<String, Object> params) {
        return travelerMapper.getGlobalTravelerCertificationSteps(params);
    }

    public GlobalTravelerCertificationStep getGlobalTravelerCertificationStepById(Long id) {
        return travelerMapper.getGlobalTravelerCertificationStepById(id);
    }

    public List<Traveler> getTravelersByUserIds(List<Long> userIds) {
        return travelerMapper.getTravelersByUserIds(userIds);
    }
}
