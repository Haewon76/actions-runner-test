package com.cashmallow.api.interfaces.coupon;

import com.cashmallow.api.domain.model.coupon.CouponIssueV2Mapper;
import com.cashmallow.api.domain.model.coupon.CouponJobPlanMapper;
import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.cashmallow.api.domain.model.system.JobPlan;
import com.cashmallow.api.domain.model.system.JobPlanHistory;
import com.cashmallow.api.domain.model.system.JobType;
import com.cashmallow.api.domain.model.system.SystemMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.batch.client.BatchClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.cashmallow.api.domain.shared.Const.CODE_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponJobPlanServiceImpl implements CouponJobPlanService {

    private final SystemMapper systemMapper;
    private final BatchClient batchClient;

    private final CouponJobPlanMapper couponJobPlanMapper;

    // Job Plan 오류 나더라도 Insert 롤백되면 안 되므로 트랜잭션 걸지 않음
    @Override
    public void insertJobPlan(String fromCountryCode, String jobKey, String cronExpression) throws CashmallowException {
        try {

            // Job Plan 에 적재
            //  - toCountryCode 는 중요하지 않으므로 fromCountryCode 와 동일하게 넣어준다
            JobPlan jobPlan = new JobPlan(fromCountryCode, fromCountryCode, jobKey, cronExpression, 0L, JobType.COUPON);
            systemMapper.insertJobPlan(jobPlan);
            insertJobPlanHistory(jobPlan);

            batchClient.updateJobSchedule();
        } catch (Exception e) {
            log.error("Error: "+ SendType.RESERVATION.name()+" Coupon 발급 오류={}", e.getMessage(), e);
            throw new CashmallowException("Error: "+SendType.RESERVATION.name()+" Coupon 발급 오류", CODE_SERVER_ERROR);
        }
    }

    private void insertJobPlanHistory(JobPlan jobPlan) {
        JobPlanHistory jobPlanHistory = new JobPlanHistory();
        BeanUtils.copyProperties(jobPlan, jobPlanHistory);
        jobPlanHistory.setJobPlanId(jobPlan.getId());
        systemMapper.insertJobPlanHistory(jobPlanHistory);
    }

    @Override
    public Long updateSuccessJobPlan(List<String> jobKeyList) {
        return couponJobPlanMapper.updateSuccessJobPlan(jobKeyList);
    }

    @Override
    public Long updateJobKeyCouponIssue(Long couponIssueId, String jobKey) {
        return couponJobPlanMapper.updateJobKeyCouponIssue(couponIssueId, jobKey);
    }

    @Override
    public String getCronExpression(LocalDateTime time) {
        return String.format("%d %d %d %d %d ? %d", time.getSecond(), time.getMinute(), time.getHour(), time.getDayOfMonth(), time.getMonthValue(), time.getYear());
    }

    @Override
    public String getJobKey(String jobKeyFormat, String iso3166, Long couponIssueId) {
        return jobKeyFormat.formatted(iso3166, iso3166, couponIssueId);
    }
}
