package com.cashmallow.api.interfaces.mallowlink.remittance;


import com.cashmallow.api.interfaces.mallowlink.common.MallowlinkClientConfig;
import com.cashmallow.api.interfaces.mallowlink.common.dto.MallowlinkBaseResponse;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "mallowlinkRemittanceBankClient",
        url = "${mallowlink.api.url}/v3/remittance",
        configuration = MallowlinkClientConfig.class
)
public interface MallowlinkRemittanceBankClient {

    // 은행 리스트 조회
    @PostMapping("/bank")
    MallowlinkBaseResponse<RemittanceBankResponse> getBank(@RequestBody RemittanceBankRequest request);

    @PostMapping("/bank/branches")
    MallowlinkBaseResponse<RemittanceBankBranchesResponse> getBankBranches(@RequestBody RemittanceBankBranchesRequest request);

    @PostMapping("/wallet")
    MallowlinkBaseResponse<RemittanceWalletResponse> getWallet(@RequestBody RemittanceWalletRequest request);

}
