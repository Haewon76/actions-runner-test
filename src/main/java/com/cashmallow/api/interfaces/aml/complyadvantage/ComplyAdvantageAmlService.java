package com.cashmallow.api.interfaces.aml.complyadvantage;

import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.aml.complyadvantage.client.ComplyadvantageBizClient;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCreateCustomerRequest;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCreateCustomerResponse;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageCustomerCasesResponse;
import com.cashmallow.api.interfaces.aml.complyadvantage.enums.ComplyAdvantageCaseStateCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@SuppressWarnings("unchecked")
public class ComplyAdvantageAmlService {

    @Value("${complyadvantage.configUUID}")
    private String complyAdvantageConfigUUID;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private ComplyadvantageBizClient complyadvantageBizClient;

    public String getComplyAdvantageCustomerId(Traveler traveler, String birthDate) throws CashmallowException {
        String customerId = travelerRepositoryService.getComplyAdvantageCustomerId(traveler.getUserId());

        if (StringUtils.isEmpty(customerId)) {
            return createComplyAdvantageUser(traveler, birthDate);
        }

        return customerId;
    }

    private String createComplyAdvantageUser(Traveler traveler, String birthDate) throws CashmallowException {
        // /workflows/sync/create-and-screen
        // 동기화 생성, Case 조회, Case있으면 경고,

        String customerFirstName = traveler.getEnFirstName();
        String customerLastName = traveler.getEnLastName();

        if (StringUtils.isEmpty(customerLastName) ||
                StringUtils.isEmpty(customerFirstName)) {
            throw new CashmallowException("조회하려는 고객의 이름이 없습니다.");
        }

        ComplyAdvantageCreateCustomerRequest createCustomerRequest = ComplyAdvantageCreateCustomerRequest.of(traveler, complyAdvantageConfigUUID, birthDate);

        Map<String, Object> createResponse = complyadvantageBizClient.createCustomer(createCustomerRequest);

        ComplyAdvantageCreateCustomerResponse customerResponse = convertCreateCustomerResponse(createResponse, traveler.getUserId());

        // DB 저장
        travelerRepositoryService.insertComplyadvantageCustomer(customerResponse);

        return customerResponse.customerId();
    }

    private ComplyAdvantageCreateCustomerResponse convertCreateCustomerResponse(Map<String, Object> createResponse, Long userId) throws CashmallowException {
        //region ComplyAdvantage의 Customer 생성시 Response 예제
        /*
        {
            "workflow_instance_identifier": "0191729d-4e29-7499-a6c8-14fccea464b6",
                "workflow_type": "create-and-screen",
                "steps": [
            "customer-creation",
                    "initial-risk-scoring",
                    "customer-screening",
                    "alerting",
                    "case-creation"
                        ],
            "status": "IN-PROGRESS",
                "step_details": {
            "customer-creation": {
                "status": "COMPLETED",
                        "identifier": "0191729d-4e38-711e-930b-c0776e80fcc3",
                        "step_output": {
                    "customer_identifier": "0191729d-4e9a-74e3-bb85-98e7c9c64c73",
                            "external_identifier": "CA1018461"
                }
            },
            "initial-risk-scoring": {
                "status": "COMPLETED",
                        "identifier": "0191729d-4f0d-723b-b97c-0c035be328ad",
                        "step_output": {
                    "overall_value": 0,
                            "overall_level": "SKIPPED"
                }
            },
            "customer-screening": {
                "status": "COMPLETED",
                        "identifier": "0191729d-5011-70a9-b6b3-a5d11749174f",
                        "step_output": {
                    "screening_result": "HAS_PROFILES"
                }
            },
            "alerting": {
                "status": "IN-PROGRESS",
                        "identifier": null,
                        "step_output": {}
            },
            "case-creation": {
                "status": "NOT-STARTED",
                        "identifier": null,
                        "step_output": {}
            }
        }
        }
         */
        //endregion

        Optional<String> customerId = Optional.of(createResponse).map(data -> (Map<String, Object>) data.get("step_details"))
                .map(stepDetails -> (Map<String, Object>) stepDetails.get("customer-creation"))
                .map(customerCreation -> (Map<String, Object>) customerCreation.get("step_output"))
                .map(stepOutput -> (String) stepOutput.get("customer_identifier"));

        return new ComplyAdvantageCreateCustomerResponse(userId,
                createResponse.get("workflow_instance_identifier").toString(),
                createResponse.get("workflow_type").toString(),
                createResponse.get("status").toString(),
                customerId.orElseThrow(() -> new CashmallowException("ComplyAdvantage에 User를 등록하는데 문제가 생겼습니다.")));
    }

    private ComplyAdvantageCustomerCasesResponse getCustomerCases(String customerId) {
        // 유저의 Case 조회후 처리상태를 확인해서 확인 안한 Risk가 있는지 체크
        //url("https://api.au.mesh.complyadvantage.com/v2/cases?customer.identifier=019177be-c665-7c8e-a14f-f5b4e69e30a2")
        return complyadvantageBizClient.getCustomerCase(customerId);
    }

    public boolean hasRiskCustomer(Long userId) throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        User user = userRepositoryService.getUserByUserId(userId);

        String customerId = getComplyAdvantageCustomerId(traveler, user.getBirthDate());

        ComplyAdvantageCustomerCasesResponse casesResponse = getCustomerCases(customerId);

        if (casesResponse.totalCount() <= 0) {
            return false;
        }

        for (ComplyAdvantageCustomerCasesResponse.ComplyAdvantageCases cases : casesResponse.cases()) {
            if (ComplyAdvantageCaseStateCode.ONBOARDING_POSITIVE_END_STATE.equals(cases.getState()) ||
                    ComplyAdvantageCaseStateCode.ONBOARDING_MANAGEABLE_RISK_POSITIVE_END_STATE.equals(cases.getState()) ||
                    ComplyAdvantageCaseStateCode.MONITORING_NO_RISK_DETECTED_POSITIVE_END_STATE.equals(cases.getState())) {
                return false;
            }
        }

        return true;
    }

}
