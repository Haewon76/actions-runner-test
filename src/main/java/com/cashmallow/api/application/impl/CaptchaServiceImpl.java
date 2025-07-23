package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class CaptchaServiceImpl {

    @Value("${google.captcha.url}")
    private String captchaUrl;

    @Value("${google.captcha.secret}")
    private String captchaKey;

    private final RestTemplate restTemplate;
    private final AlarmService alarmService;

    private final ObjectMapper objectMapper;
    private final Gson gsonPretty;

    /**
     * 로봇이면 true
     *
     * @param captcha
     * @return
     */
    public boolean isRobot(String captcha) {

        if (StringUtils.isEmpty(captcha)) {
            alarmService.i("reCaptcha Error", "captcha is empty");
            return true;
        }

        String url = captchaUrl + "?secret=" + captchaKey + "&response=" + captcha;
        log.debug("captcha={}", captcha);

        ResponseEntity<String> response = restTemplate.postForEntity(url, null, String.class);
        if (response.getBody() == null) {
            alarmService.i("reCaptcha Error", "response.getBody() == null");
            log.error("reCaptcha Error:{}", "response.getBody() == null");
            return true;
        }

        log.info("reCaptcha response:{}", gsonPretty.toJson(response.getBody()));

        SiteverifyResponse siteverifyResponse;
        try {
            siteverifyResponse = objectMapper.readValue(response.getBody(), SiteverifyResponse.class);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
            return true;
        }

        if (response.getStatusCode().is2xxSuccessful()) {
            return !siteverifyResponse.isSuccess();

        } else {
            String errorStr = gsonPretty.toJson(response.getBody());
            alarmService.i("reCaptcha Error", errorStr);
            log.error("reCaptcha Error:{}", errorStr);
        }

        return true;
    }

    @Data
    private static class SiteverifyResponse {
        private boolean success;
        private ZonedDateTime challenge_ts;
        private String hostname;
        @JsonProperty("error-codes")
        private Object errorCodes;
    }

}
