package com.cashmallow.api.domain.model.system;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface SystemMapper {

    /**
     * DB health check. 'SELECT true;'
     *
     * @return
     */
    boolean isDBHealthy();

    /**
     * Get minimum supported APP version by application id
     *
     * @param applicationId, deviceType
     * @return
     */
    AppVersion getAppVersion(Map<String, String> params);

    JobPlan getJobPlanByJobKey(String jobKey);

    int insertJobPlan(JobPlan jobPlan);
    int insertJobPlanHistory(JobPlanHistory jobPlanHistory);
    int deleteJobPlan(String jobKey);

    List<JobPlan> getJobPlanListByFromCountryCode(String fromCountryCode);
}
