package com.cashmallow.api.domain.model.system;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JobPlanHistory extends JobPlan {
    private Long jobPlanId;
}
