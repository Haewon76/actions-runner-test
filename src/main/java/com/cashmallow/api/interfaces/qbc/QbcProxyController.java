package com.cashmallow.api.interfaces.qbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/devoffice/qbc")
public class QbcProxyController {

    private final RestTemplate restTemplate;

    @Value("${sevenbank.api.atmListUrlNav}")
    private String atmListUrlNavitime;

    @GetMapping("/atms")
    public ResponseEntity<String> atm(HttpServletRequest request) throws URISyntaxException {

        String queryString = request.getQueryString();
        log.debug("queryString:{}", queryString);

        ResponseEntity<String> stringResponseEntity = proxyRequest(queryString, null, HttpMethod.GET, request);
        log.debug("stringResponseEntity.getStatusCode():{}", stringResponseEntity.getStatusCode());
        log.debug("stringResponseEntity.getBody():{}", stringResponseEntity.getBody());

        return stringResponseEntity;
    }

    private ResponseEntity<String> proxyRequest(String queryString,
                                                String body,
                                                HttpMethod method,
                                                HttpServletRequest request) throws URISyntaxException {

        String targetUrl = atmListUrlNavitime;
        if (queryString != null) {
            targetUrl += "?" + queryString;
        }
        log.debug("targetUrl:{}", targetUrl);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            headers.add(headerName, headerValue);
        }
        log.debug("headers:{}", headers);

        // Create HttpEntity
        HttpEntity<String> httpEntity = null;
        if (StringUtils.isNotBlank(body)) {
            httpEntity = new HttpEntity<>(body, headers);
            log.debug("body:{}", body);
        }

        // Send request and get response
        return restTemplate.exchange(new URI(targetUrl), method, null, String.class);
    }
}
