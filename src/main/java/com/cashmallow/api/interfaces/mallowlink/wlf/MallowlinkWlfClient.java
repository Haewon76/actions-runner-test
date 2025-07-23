package com.cashmallow.api.interfaces.mallowlink.wlf;


import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.wlf.dto.WlfRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "mallowlinkWlfClient",
        url = "${mallowlink.api.url}/v1/wlf",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkWlfClient {

    @PostMapping("/request")
    MallowlinkBaseResponse<Void> request(WlfRequest request);

}
