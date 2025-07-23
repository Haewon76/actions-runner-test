package com.cashmallow;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.StopWatch;

@Slf4j
@Disabled
public class StringConcatTest {

    private final Long MAX_VALUE = 500_000L;

    @Test
    public void 스트링빌더에_대한_퍼포먼스를_테스트() {
        StringBuilder sb = new StringBuilder();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < MAX_VALUE; i++) {
            sb.append("a");
        }
        stopWatch.stop();
        System.out.println("StringBuilder: " + stopWatch.getLastTaskTimeMillis());
    }

    @Test
    public void 스트링버퍼에_대한_퍼포먼스를_테스트() {
        StringBuffer sb = new StringBuffer();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < MAX_VALUE; i++) {
            sb.append("a");
        }
        stopWatch.stop();
        System.out.println("StringBuffer: " + stopWatch.getLastTaskTimeMillis());
    }

    @Test
    public void 더하기연산에_대한_퍼포먼스를_테스트() {
        String concat = "";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < MAX_VALUE; i++) {
            concat = concat + "a";
        }
        stopWatch.stop();
        System.out.println("plus(+): " + stopWatch.getLastTaskTimeMillis());
    }

    @Test
    public void 콘켓_연산에_대한_더하기_테스트() {
        String concatString = "";
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        for (int i = 0; i < MAX_VALUE; i++) {
            concatString = concatString.concat("a");
        }
        stopWatch.stop();
        System.out.println("concat: " + stopWatch.getLastTaskTimeMillis());
    }
}