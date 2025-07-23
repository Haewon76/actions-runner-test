package com.cashmallow.api.interfaces.aml.octa;

import com.cashmallow.api.application.CountryService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.UserAdminService;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.remittance.RemitReceiverAml;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.AMLException;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.aml.OctaWLFService;
import com.cashmallow.api.infrastructure.aml.dto.OctaWLFRequest;
import com.cashmallow.api.infrastructure.aml.dto.WLFResponse;
import com.cashmallow.common.EnvUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class OctaAmlService {

    @Autowired
    UserRepositoryService userRepositoryService;

    @Autowired
    TravelerRepositoryService travelerRepositoryService;

    @Autowired
    RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    NotificationService notificationService;

    @Autowired
    private UserAdminService userAdminService;

    @Autowired
    private OctaWLFService octaWLFService;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private CountryService countryService;

    /**
     * Search travelers in AML list
     */
    public List<Map<String, Object>> getTravelerAmlList(Long userId) throws CashmallowException {

        List<Map<String, Object>> resultLists = new ArrayList<>();

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        User user = userRepositoryService.getUserByUserId(userId);
        String countryCode = countryService.getCountry(user.getCountry()).getIso3166();
        if (traveler.getCertificationType() == CertificationType.PASSPORT) {
            // 여권국적
            countryCode = Country3.ofAlpha3(traveler.getPassportCountry()).getAlpha2();
        }

        String receiverFirstName = "";
        String receiverLastName = "";
        String receiverCountryCd = "";
        String receiverBirthDate = "";
        Long remittanceId = 0L;

        try {
            resultLists = getAMLSearches(
                    new OctaWLFRequest(
                            traveler.getUserId().toString(),
                            remittanceId.toString(),
                            traveler.getEnFirstName(),
                            traveler.getEnLastName(),
                            receiverFirstName,
                            receiverLastName,
                            requestAdminName(),
                            user.getBirthDate(),
                            countryCode,
                            receiverBirthDate,
                            receiverCountryCd,
                            user.getCountryCode().name()
                    )
            );

            // 매칭되는 데이터가 없는 경우 SearchId에 UserId를 저장한다.
            if (traveler.getAMLSearchId() == null) {
                traveler.setAMLSearchId(userId.toString());
                travelerRepositoryService.updateTraveler(traveler);
            }
        } catch (AMLException e) {
            log.info("AMLException: " + e.getMessage());
            throw new CashmallowException(e.getMessage(), e);
        } catch (CashmallowException e) {
            log.error("AMLException: " + e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }

        return resultLists;
    }

    public List<Map<String, Object>> validateRemittanceAmlList(Remittance remittance) throws CashmallowException {
        String method = "getRemittanceAMLList()";

        List<Map<String, Object>> resultLists = new ArrayList<>();

        Map<String, String> params = new HashMap<String, String>();

        final String receiverFirstName = remittance.getReceiverFirstName();
        final String receiverLastName = remittance.getReceiverLastName();
        Long remittanceId = remittance.getId();

        params.put("receiverFirstName", receiverFirstName);
        params.put("receiverLastName", receiverLastName);
        params.put("birthDate", remittance.getReceiverBirthDate());
        params.put("traverlerId", remittance.getTravelerId().toString());

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(remittance.getTravelerId());
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        String countryCode = countryService.getCountry(user.getCountry()).getIso3166();
        if (traveler.getCertificationType() == CertificationType.PASSPORT) {
            countryCode = Country3.valueOf(traveler.getPassportCountry()).getAlpha2();// 여권국적
        }

        List<RemitReceiverAml> receiverAmlLists = remittanceRepositoryService.getRemitReceiverAml(params);

        try {
            resultLists = getAMLSearches(
                    new OctaWLFRequest(
                            traveler.getUserId().toString(),
                            remittanceId.toString(),
                            traveler.getEnFirstName(),
                            traveler.getEnLastName(),
                            receiverFirstName,
                            receiverLastName,
                            requestAdminName(),
                            user.getBirthDate(),
                            countryCode,
                            remittance.getReceiverBirthDate(),
                            remittance.getReceiverCountry(),
                            user.getCountryCode().name()
                    )
            );

            // AML에 문제가 없다면 처리
            if (CollectionUtils.isEmpty(receiverAmlLists)) {
                RemitReceiverAml remitReceiverAml = new RemitReceiverAml(
                        remittance.getTravelerId(),
                        receiverFirstName,
                        receiverLastName,
                        remittance.getReceiverBirthDate(),
                        remittance.getTravelerId().toString()
                );

                remittanceRepositoryService.insertRemitReceiverAml(remitReceiverAml);
            }
        } catch (AMLException e) {
            log.warn("AMLException: " + e.getMessage());
            throw new CashmallowException(e.getMessage(), e);
        } catch (CashmallowException e) {
            log.warn(e.getMessage());
            throw new CashmallowException(e.getMessage(), e);
        }

        return resultLists;
    }

    // Search ComplyAdvantage AML Searches
    private List<Map<String, Object>> getAMLSearches(OctaWLFRequest request) throws CashmallowException, AMLException {
        String method = "getAMLSearches";

        List<Map<String, Object>> resultLists = new ArrayList<>();

        try {
            // OctaWLFRequest
            final WLFResponse wlfResponse = octaWLFService.execute(request);
            if (!wlfResponse.isNotMatched() && envUtil.isPrd()) {
                log.info("{} : resultMessage = {}", method, wlfResponse.getMessage());
                throw new AMLException(wlfResponse.getMessage());
            }
        } catch (AMLException e) {
            log.info(e.getMessage() + " : AML verification required");
            if (envUtil.isPrd()) {
                throw new AMLException(e.getMessage() + " : AML verification required");
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (envUtil.isPrd()) {
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        }

        return resultLists;
    }

    private String requestAdminName() {
        String workerName = "unknown";
        final String mdcUserId = MDC.get("userId");

        if (StringUtils.isNotBlank(mdcUserId)) {
            workerName = userAdminService.getAdminName(Long.parseLong(mdcUserId));
        }
        return workerName;
    }
}
