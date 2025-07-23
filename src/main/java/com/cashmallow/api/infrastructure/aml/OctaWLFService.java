package com.cashmallow.api.infrastructure.aml;

import com.cashmallow.api.infrastructure.aml.dto.OctaWLFRequest;
import com.cashmallow.api.infrastructure.aml.dto.WLFResponse;
import com.cashmallow.api.interfaces.mallowlink.wlf.MallowlinkWlfServiceImpl;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class OctaWLFService implements OctaService<OctaWLFRequest, WLFResponse> {

    @Value("${aml.octa.url}")
    private String url;

    private final MallowlinkWlfServiceImpl mallowlinkWlfService;


    @Override
    public WLFResponse execute(OctaWLFRequest request) {
        try {
            return mallowlinkWlfService.request(request);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
            return new WLFResponse(400, e.getMessage());
        }
    }

    @Override
    public String getURL() {
        return String.format("%s/view/AML/common/wlf.jsp", url);
    }

}
