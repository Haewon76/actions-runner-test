package com.cashmallow.api.interfaces.scb.service;

import com.cashmallow.api.infrastructure.redis.RedisServiceImpl;
import com.cashmallow.api.interfaces.scb.model.dto.InboundMessage;
import org.apache.tomcat.jni.Time;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.AsyncTaskExecutor;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisServiceTest {

    @Autowired
    private RedisPubService pubService;
    @Autowired
    private RedisSubService subService;
    @Autowired
    private RedisServiceImpl redisService;

    @Autowired
    private AsyncTaskExecutor asyncTaskExecutor;

    @DisplayName("레디스의 PUB/SUB이 정상동작하는지 테스트")
    @Test
    @Disabled
    public void TEST_레디스_PUB_SUB() throws InterruptedException {

        // given - precondition or setup
        String withdrawalRequestNo = "2023020802166";

        // 성공 케이스 전송 - success
        InboundMessage success = InboundMessage.builder()
                .code(200)
                .withdrawalRequestNo(withdrawalRequestNo).build();

        // 실패 케이스 전송 - error
        InboundMessage error = InboundMessage.builder()
                .code(400)
                .withdrawalRequestNo(withdrawalRequestNo).build();

        // SSE연결 이벤트 전송 - connectedSSE
        InboundMessage connectedSSE = InboundMessage.builder()
                .code(333)
                .withdrawalRequestNo(withdrawalRequestNo).build();

        // when - action or the behaviour that we are going test
        pubService.sendMessage(success);
        pubService.sendMessage(error);
        pubService.sendMessage(connectedSSE);

        AtomicReference<InboundMessage> sMessage = new AtomicReference<>();
        AtomicReference<InboundMessage> eMessage = new AtomicReference<>();
        AtomicReference<InboundMessage> sseMessage = new AtomicReference<>();

        subService.setRedisMessageListener(message -> {
            if (success.equals(message)) {
                sMessage.set(message);
            }
            if (error.equals(message)) {
                eMessage.set(message);
            }
            if (connectedSSE.equals(message)) {
                sseMessage.set(message);
            }
        });

        // 딜레이를 고려하여, 메세지가 다 들어 올때 까지 3초간 대기한다
        while (sMessage.get() == null || eMessage.get() == null || sseMessage.get() == null) {
            Time.sec(1);
        }

        // then - verify the output
        // 정상적인 데이터가 들어왔는지 검증하는 과정

        assertThat(sMessage.get().code()).isEqualTo(success.code());
        assertThat(sMessage.get().withdrawalRequestNo()).isEqualTo(success.withdrawalRequestNo());

        assertThat(eMessage.get().code()).isEqualTo(error.code());
        assertThat(eMessage.get().withdrawalRequestNo()).isEqualTo(error.withdrawalRequestNo());

        assertThat(sseMessage.get().code()).isEqualTo(connectedSSE.code());
        assertThat(sseMessage.get().withdrawalRequestNo()).isEqualTo(connectedSSE.withdrawalRequestNo());
    }

    @DisplayName("레디스의 SET/GET 테스트를 진행")
    @Test
    public void 레디스의_SET_GET_테스트를_진행() {

        // given - precondition or setup
        String key = "Hello";
        String value = "JD";

        // when - action or the behaviour that we are going test
        redisService.put(key, value);
        String result = redisService.get(key);

        // then - verify the output
        assertThat(result).isEqualTo(value);
        redisService.remove(key);
    }

    @Test
    void 결제요청번호_증가_동시성_테스트() throws InterruptedException {
        String key = "TEST_" + UUID.randomUUID();

        CountDownLatch countDownLatch = new CountDownLatch(10);
        for (int i = 1; i <= 10; i++) {
            asyncTaskExecutor.execute(() -> {
                redisService.increaseAndGetCount(key, 1L);
                countDownLatch.countDown();
            });
        }

        countDownLatch.await();
        Long count = redisService.increaseAndGetCount(key, 1L);
        redisService.setTimeout(key, 1);
        assertThat(count).isEqualTo(11);
    }
}
