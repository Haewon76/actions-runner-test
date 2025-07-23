package com.cashmallow.api.domain.model.deeplink;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DeeplinkStat {

    private String clickedCount;
    private String installedCount;
    private String loginedCount;
    private String registeredCount;

    private String startDate;
    private String utmMedium;
    private String endDate;

}
