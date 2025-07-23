package com.cashmallow.api.interfaces.mallowlink.agency;


import com.cashmallow.api.interfaces.mallowlink.agency.dto.AgencyResponse;
import com.cashmallow.api.interfaces.mallowlink.agency.dto.MallowlinkAgencyRequest;
import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "mallowlinkAgencyClient",
        url = "${mallowlink.api.url}/v1/agencies",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkAgencyClient {

    /**
     * ATM 위치 가져오기
     *
     * @param request
     * @return
     */
    @PostMapping
    MallowlinkBaseResponse<List<AgencyResponse>> agencies(@RequestBody MallowlinkAgencyRequest request);

}
