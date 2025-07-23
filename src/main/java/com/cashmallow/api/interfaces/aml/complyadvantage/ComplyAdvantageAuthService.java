package com.cashmallow.api.interfaces.aml.complyadvantage;

import com.cashmallow.api.infrastructure.redis.RedisServiceImpl;
import com.cashmallow.api.interfaces.aml.complyadvantage.client.ComplyadvantageAuthClient;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageTokenRequest;
import com.cashmallow.api.interfaces.aml.complyadvantage.dto.ComplyAdvantageTokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_COMPLYADVANTAGE_TOKEN;

@Slf4j
@Service
public class ComplyAdvantageAuthService {

    @Value("${complyadvantage.userId}")
    private String complyAdvantageUserId;
    @Value("${complyadvantage.password}")
    private String complyAdvantagePassword;
    @Value("${complyadvantage.realm}")
    private String complyAdvantageRealm;

    @Autowired
    private RedisServiceImpl redisService;
    @Autowired
    private ComplyadvantageAuthClient complyadvantageAuthClient;

    public String getAccessToken() {
        String accessToken = redisService.get(REDIS_KEY_COMPLYADVANTAGE_TOKEN);

        if (StringUtils.isEmpty(accessToken)) {
            // /token, 토큰만료시간 24H
            // 인증시 필요정보, username(email), password, realm
            ComplyAdvantageTokenRequest complyAdvantageTokenRequest = new ComplyAdvantageTokenRequest(complyAdvantageUserId,
                    complyAdvantagePassword, complyAdvantageRealm);
            ComplyAdvantageTokenResponse result = complyadvantageAuthClient.getAccessToken(complyAdvantageTokenRequest);
            redisService.put(REDIS_KEY_COMPLYADVANTAGE_TOKEN, result.accessToken(), 23, TimeUnit.HOURS);
            return result.accessToken();
        }

        return accessToken;
    }

}
