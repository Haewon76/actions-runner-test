package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.MLWebhookService;
import com.cashmallow.api.interfaces.scb.model.dto.inbound.SCBInboundRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Future;

import static com.cashmallow.api.interfaces.scb.web.SCBController.CORRELATION_ID;
import static com.cashmallow.api.interfaces.scb.web.SCBController.X_API_KEY;

@Service
@Slf4j
@RequiredArgsConstructor
public class MLSCBWebhookService implements MLWebhookService<SCBInboundRequest> {

    private final AsyncTaskExecutor asyncTaskExecutor;
    private final AlarmService alarmService;

    @Value("${mallowlink.api.url}")
    private String mallowlinkApiUrl;


    @Override
    public Future<HttpStatus> send(SCBInboundRequest scbInboundRequest) {
        return asyncTaskExecutor.submit(() -> scbWebhook(scbInboundRequest));
    }

    private HttpStatus scbWebhook(SCBInboundRequest scbInboundRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(X_API_KEY, scbInboundRequest.getApiKey());
        headers.set(CORRELATION_ID, scbInboundRequest.getCorrelationId());

        HttpEntity<SCBInboundRequest> entity = new HttpEntity<>(scbInboundRequest, headers);

        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    String.format("%s/webhooks/SCB", mallowlinkApiUrl),
                    HttpMethod.POST,
                    entity,
                    String.class);
            log.info("response.getStatusCode()={}", response.getStatusCode());
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Request Successful!, requestBody {} ", scbInboundRequest);
            } else {
                log.error("Request Failed!, code: {}, requestBody : {}", response.getStatusCode(), scbInboundRequest);
            }
            return response.getStatusCode();

        } catch (HttpClientErrorException e) {
            alarmService.i("ML proxy scbWebhook Request Failed!", String.format("code: %s, requestBody : %s", e.getStatusCode(), scbInboundRequest));
            log.error("Request Failed!, code: {}, requestBody : {}", e.getStatusCode(), scbInboundRequest);
            return e.getStatusCode();

        } catch (Exception e) {
            alarmService.e("ML proxy scbWebhook Request Failed!", e.getMessage());
            log.error(e.getMessage(), e);
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
