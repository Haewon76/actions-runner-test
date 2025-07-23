package com.cashmallow.api.domain.model.system;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobPlan {
    private Long id;
    private JobType jobType;
    private String fromCountryCode;
    private String toCountryCode;
    private String jobKey;
    private String cronExpression;
    private Boolean isActive;
    private Boolean isExecuted;
    private Long walletExpiredCalendarDay;

    public JobPlan(String fromCountryCode, String toCountryCode, String jobKey, String cronExpression, Long walletExpiredCalendarDay, JobType jobType) {
        this.fromCountryCode = fromCountryCode;
        this.toCountryCode  = toCountryCode;
        this.jobKey = jobKey;
        this.cronExpression = cronExpression;
        this.walletExpiredCalendarDay = walletExpiredCalendarDay;
        this.isActive = true;
        this.isExecuted = false;
        this.jobType = jobType;
    }
}
