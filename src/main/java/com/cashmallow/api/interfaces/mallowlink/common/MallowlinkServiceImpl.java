package com.cashmallow.api.interfaces.mallowlink.common;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkException;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkExceptionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_MALLOWLINK_TRANSACTION;
import static com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig.ML_CLIENT_ID;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkServiceImpl {

    private final MallowlinkClient mallowlinkClient;
    private final RedisService redisService;

    private final MallowlinkProperties properties;
    private final RestTemplate restTemplate;

    public boolean isHealth() {
        String health = mallowlinkClient.health().getData();
        return StringUtils.equals("OK", health);
    }

    /**
     * mallowlink용 clientTransactionId 생성
     *
     * @return
     */
    @NotNull
    public String increaseAndGetTransactionId() {
        String now = LocalDate.now(CountryCode.KR.getZoneId()).format(DateTimeFormatter.ofPattern("yyMMdd"));
        String key = REDIS_KEY_MALLOWLINK_TRANSACTION + now;

        Long count = redisService.increaseAndGetCount(key, 1L);
        redisService.setTimeout(key, 60 * 60 * 24);

        return String.format("CM%s%08d", now, count);
    }

    public long getScbCountIncreaseAndGet() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ML_CLIENT_ID, properties.getClientId());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> request = new HttpEntity<>(null, headers);

        ResponseEntity<String> exchange = restTemplate.exchange(properties.getUrl() + "/cm/scbCounter/increaseAndGet",
                HttpMethod.POST,
                request,
                String.class);

        if (!exchange.getStatusCode().is2xxSuccessful()) {
            throw new MallowlinkException(MallowlinkExceptionType.INTERNAL_SERVER_ERROR);
        }

        log.debug("exchange.getBody()={}", exchange.getBody());

        return Long.parseLong(exchange.getBody().replaceAll("\"", ""));
    }

}
