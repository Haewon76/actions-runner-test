package com.cashmallow.api.infrastructure.aml;

import com.cashmallow.api.infrastructure.aml.dto.OctaAMLCustomerRequest;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLCustomerResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class OctaAMLCustomerService implements OctaService<OctaAMLCustomerRequest, ResponseEntity<OctaAMLCustomerResponse>> {

    @Value("${aml.octa.url}")
    private String url;

    private final RestTemplate restTemplate;


    @Override
    public ResponseEntity<OctaAMLCustomerResponse> execute(OctaAMLCustomerRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.set("X-API-KEY", "2f74b80b-f3d9-41ea-8436-0920a1ca2e14"); // NOTE : PRD, DEV 동일키 사용
        HttpEntity<OctaAMLCustomerRequest> requestEntity = new HttpEntity<>(request, headers);


        ResponseEntity<OctaAMLCustomerResponse> response = restTemplate.exchange(getURL(), HttpMethod.POST, requestEntity, OctaAMLCustomerResponse.class);
        log.debug("response {}", response);
        return response;
    }

    @Override
    public String getURL() {
        return String.format("%s/view/CASHMFT/set_approve_traveler.jsp", url);
    }

}
