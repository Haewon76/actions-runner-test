package com.cashmallow.api.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Map;

@Configuration
@Slf4j
public class ExecutorConfig {

    @Bean
    @Primary
    public AsyncTaskExecutor addTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(10);
        taskExecutor.setTaskDecorator(runnable -> {
            final Map<String, String> copyOfContextMap = MDC.getCopyOfContextMap();
            return () -> {
                if (copyOfContextMap != null) {
                    MDC.setContextMap(copyOfContextMap);
                }
                runnable.run();
            };
        });

        return taskExecutor;
    }
}
