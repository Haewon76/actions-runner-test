package com.cashmallow.api.interfaces.mallowlink.enduser;


import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserRegisterRequest;
import com.cashmallow.api.interfaces.mallowlink.enduser.dto.EndUserUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "mallowlinkEnduserClient",
        url = "${mallowlink.api.url}/v1/user",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkEnduserClient {

    @PostMapping("/register")
    MallowlinkBaseResponse<Void> register(EndUserRegisterRequest request);

    @PostMapping("/update")
    MallowlinkBaseResponse<Void> update(EndUserUpdateRequest request);

}
