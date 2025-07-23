package com.cashmallow.api.interfaces;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class ConvertTest {

    @Test
    void strToIntDef() {
        String s = "3.5";
        Long l = Convert.strToLongDef(s, 10L);
        log.info("l={}", l);
    }

    @Test
    void strToLongDef() {
        String s = "3.5";
        Integer i = Convert.strToIntDef(s, 0);
        log.info("i={}", i);
    }
}