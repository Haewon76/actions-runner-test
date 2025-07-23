package com.cashmallow.api.infrastructure.aml;

import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCRequest;
import com.cashmallow.api.infrastructure.aml.dto.OctaAMLKYCResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Service
@Slf4j
@RequiredArgsConstructor
public class OctaAMLKYCService implements OctaService<OctaAMLKYCRequest, ResponseEntity<OctaAMLKYCResponse>> {

    @Value("${aml.octa.url}")
    private String url;

    private final RestTemplate restTemplate;


    @Override
    public ResponseEntity<OctaAMLKYCResponse> execute(OctaAMLKYCRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.set("X-API-KEY", "3093f27b-e6ac-4fb4-bc46-ba13d08ea513"); // PRD, DEV 동일키 사용
        HttpEntity<OctaAMLKYCRequest> requestEntity = new HttpEntity<>(request, headers);


        ResponseEntity<OctaAMLKYCResponse> response = restTemplate.exchange(getURL(), HttpMethod.POST, requestEntity, OctaAMLKYCResponse.class);
        log.debug("response {}", response);
        return response;
    }

    @Override
    public String getURL() {
        return String.format("%s/view/CASHMFT/set_complate_remittance.jsp", url);
    }

}
