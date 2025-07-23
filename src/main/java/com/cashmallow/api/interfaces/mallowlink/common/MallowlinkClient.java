package com.cashmallow.api.interfaces.mallowlink.common;


import com.cashmallow.api.interfaces.mallowlink.common.dto.EchoResponse;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(
        name = "mallowlinkClient",
        url = "${mallowlink.api.url}",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkClient {

    @GetMapping("/health")
    MallowlinkBaseResponse<String> health();

    @PostMapping("/echo")
    <T> MallowlinkBaseResponse<EchoResponse<T>> echo(T request);

    // @PostMapping("/ml/v1/webhooks/SCB")
    // String scbWebhook(@RequestBody SCBInboundRequest request);

}
