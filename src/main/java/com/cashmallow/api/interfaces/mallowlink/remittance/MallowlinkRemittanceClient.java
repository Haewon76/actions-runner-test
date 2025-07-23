package com.cashmallow.api.interfaces.mallowlink.remittance;


import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkNotFoundEnduserException;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.RemittanceRequestV3;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.RemittanceResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "mallowlinkRemittanceClient",
        url = "${mallowlink.api.url}/v3/remittance",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkRemittanceClient {

    // 송금 등록 및 실행
    @PostMapping("/request")
    MallowlinkBaseResponse<RemittanceResponse> request(@RequestBody RemittanceRequestV3 request) throws MallowlinkNotFoundEnduserException;

}
