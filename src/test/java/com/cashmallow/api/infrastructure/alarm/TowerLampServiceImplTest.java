package com.cashmallow.api.infrastructure.alarm;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled
@SpringBootTest
class TowerLampServiceImplTest {

    @Autowired
    TowerLampServiceImpl towerLampService;

    @Test
    void execute() {
    }

    @Test
    void onRed() {
    }

    @Test
    void onYellow() {
    }

    @Test
    void onGreen() {
        towerLampService.turnOnGreen();
    }
}