package com.cashmallow.api.interfaces.batch.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "batchClient",
        url = "${batch.url}",
        configuration = BatchFeignConfig.class
)
public interface BatchClient {

    @PostMapping("/job")
    String updateJobSchedule();

    @PatchMapping("/job/{jobPlanId}")
    String deleteJobSchedule(@PathVariable long jobPlanId);

}
