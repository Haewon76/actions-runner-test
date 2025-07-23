package com.cashmallow.api.infrastructure.alarm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Service
public class TowerLampServiceImpl {

    private final RestTemplate restTemplate;

    @Value("${towerLamp.url}")
    private String towerLampUrl;

    enum Color {
        RED("lamp4"),
        YELLOW("lamp3"),
        GREEN("lamp2");

        String param;

        Color(String param) {
            this.param = param;
        }
    }

    public void post(Color color, boolean on) {
        String url = towerLampUrl + "?" + color.param + "=" + (on ? "1" : "0");
        try {
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            log.debug(e.getMessage());
        }
    }

    public void turnOnRed() {
        post(Color.RED, true);
    }

    public void turnOnYellow() {
        post(Color.YELLOW, true);
    }

    public void turnOnGreen() {
        post(Color.GREEN, true);
    }

}
