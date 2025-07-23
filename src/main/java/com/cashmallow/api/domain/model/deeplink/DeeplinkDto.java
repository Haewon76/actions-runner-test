package com.cashmallow.api.domain.model.deeplink;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeeplinkDto {
    private String utmMedium;
    private String utmSource;
    private String userAgent;
    private String osType;
    private String thumbnail;


    private String trackingId; // utm_campaign
    private String uuid;

    private String ip;
    private Long userId;

}
