package com.cashmallow.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EnvUtil {

    @Value("${spring.profiles.active}")
    private String profilesActive;

    public boolean isPrd() {
        return "prd".equalsIgnoreCase(profilesActive);
    }

    public boolean isApplicationRunning() {
        final String runner_name = System.getProperty("RUNNER_NAME");
        return StringUtils.isEmpty(runner_name);
    }

    /**
     * dev AND dev-local
     *
     * @return
     */
    public boolean isDev() {
        return !isPrd();
    }

    public String getEnv() {
        return profilesActive;
    }

    @Getter
    @Value("${host.cdn.url}")
    private String cdnUrl;

    public String getStaticUrl() {
        return cdnUrl + "/static";
    }

    public @NotNull String getCouponThumbnailUrl(String thumbnail) {
        return cdnUrl + "/coupon/thumbnail/" + thumbnail;
    }
}
