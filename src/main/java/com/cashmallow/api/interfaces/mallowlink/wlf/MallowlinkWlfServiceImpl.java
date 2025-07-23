package com.cashmallow.api.interfaces.mallowlink.wlf;

import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.aml.dto.OctaWLFRequest;
import com.cashmallow.api.infrastructure.aml.dto.WLFResponse;
import com.cashmallow.api.interfaces.mallowlink.wlf.dto.WlfRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class MallowlinkWlfServiceImpl {

    private final MallowlinkWlfClient mallowlinkWlfClient;

    public WLFResponse request(OctaWLFRequest request) throws CashmallowException {
        try {
            final WlfRequest wlfRequest = WlfRequest.of(request);
            mallowlinkWlfClient.request(wlfRequest);
            return new WLFResponse(200, "SUCCESS");
        } catch (Exception e) {
            log.warn("{}", e.getMessage());
            return new WLFResponse(400, e.getMessage());
        }
    }

}
