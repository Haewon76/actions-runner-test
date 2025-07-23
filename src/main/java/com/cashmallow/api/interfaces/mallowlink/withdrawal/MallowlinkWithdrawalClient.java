package com.cashmallow.api.interfaces.mallowlink.withdrawal;


import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkNotFoundEnduserException;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.MallowlinkCancelRequest;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.MallowlinkQrRequest;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.MallowlinkWithdrawalRequest;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.dto.MallowlinkWithdrawalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "mallowlinkWithdrawalClient",
        url = "${mallowlink.api.url}/v1/withdrawal",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkWithdrawalClient {
    /**
     * 인출 신청
     *
     * @param request
     * @return
     */
    @PostMapping("/request")
    MallowlinkBaseResponse<MallowlinkWithdrawalResponse> withdrawal(@RequestBody MallowlinkWithdrawalRequest request) throws MallowlinkNotFoundEnduserException;

    /**
     * 인출 취소
     *
     * @param request
     * @return
     */
    @PostMapping("/cancel")
    MallowlinkBaseResponse<Void> cancel(@RequestBody MallowlinkCancelRequest request);

    /**
     * QR 전송
     *
     * @param request
     * @return
     */
    @PostMapping("/qr")
    MallowlinkBaseResponse<Void> qr(@RequestBody MallowlinkQrRequest request);

}
