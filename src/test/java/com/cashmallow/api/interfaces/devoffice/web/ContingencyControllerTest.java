package com.cashmallow.api.interfaces.devoffice.web;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.mallowlink.controller.dto.DepositEmailRequest;
import com.cashmallow.common.JsonUtil;
import javacryption.aes.AesCtr;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl.COMMON_ENC;
import static com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl.FIXED_BITS;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Slf4j
@Disabled
class ContingencyControllerTest {
    private String paygateAesKey = "qOalrS7xn**";
    private String paygateMemberGUID = "5MfVT**";
    private String travelerMemberId = "F23pb**"; //paygateMemberId
    private String paygateUrl = "https://v5.paygate.net";
    private String paygateTxnId = "T**";

    MockMvc mockMvc;

    @Autowired
    private ContingencyController contingencyController;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private JsonUtil jsonUtil;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(contingencyController).build();
    }

    @Disabled
    @Test
    void makePaygateMappingConfirm() throws UnsupportedEncodingException {
        String httpType = "POST";
        String eachEncodedByParameter = "reqMemGuid=" + paygateMemberGUID + "&_method=" + httpType
                + "&tid=" + paygateTxnId + "&orgMemGuid=" + travelerMemberId + "&beneficiaryMemGuid=" + travelerMemberId;
        log.info("eachParameter : {}", eachEncodedByParameter);
        String cipherEncoded = encryptParameter(eachEncodedByParameter);
        String url = paygateUrl + "/v5/obd/partnerConfirmation" + "?reqMemGuid=" + paygateMemberGUID + "&_method=" + httpType + "&encReq=" + cipherEncoded;
        log.info("url : {}", url);
    }

    public String encryptParameter(String eachEncodedByParameter) throws UnsupportedEncodingException {
        // Encrypt your result String (parameter set)
        String cipher = AesCtr.encrypt(eachEncodedByParameter, paygateAesKey, FIXED_BITS);

        // URLEncode the whole encrypted String, and send it as the "encReq" parameter
        String cipherEncoded = URLEncoder.encode(cipher, COMMON_ENC);

        return cipherEncoded;
    }

    @Test
    void login() throws Exception {
        ApiResultVO alexhk02temlnet = contingencyController.login("alexhk01temlnet", "alex111@");
        System.out.println("alexhk02temlnet = " + jsonUtil.toJsonPretty(alexhk02temlnet));
    }


/*
// 20230418173532
// https://v5.paygate.net/v5/obd/partnerConfirmation?reqMemGuid=5MfVTbcxo5oSgV6Et4DC83&_method=POST&encReq=vv6ZqR5WPmRGvwpSPVZKp5c9GrubexqfxpP2OwVm7fKEojHhdbo8Jh6q%2BJQstcVDbmxm9nRkBWjG%2BtyydSermMm1BgYktKyDHMGo9pFIb%2FY%2Fwrc%2B3%2FRzE4P9D91yjLA0Lnzpfwr9mdAzqbgDSGOOpafWcfFM7qSjDyNg3Het5RmoHLGyEcwD4v6BzzZCmg%3D%3D

{
  "data": {
    "tid": "t301f83",
    "trnsctnFilterTp": "OBD_PAYIN_REQUIRED_CONFIRM",
    "comment": "SUCCESS",
    "verifySt": "VERIFIED",
    "trnsctnSt": "OBD_VA_PAYIN_PROCESSING"
  },
  "_debug": null,
  "status": "SUCCESS"
}
*/
}