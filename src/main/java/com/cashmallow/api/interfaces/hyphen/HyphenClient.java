package com.cashmallow.api.interfaces.hyphen;

import com.cashmallow.api.interfaces.hyphen.dto.CheckAccount1VO;
import com.cashmallow.api.interfaces.hyphen.dto.CheckAccount2VO;
import com.cashmallow.api.interfaces.hyphen.dto.FCS;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;

@Service
public class HyphenClient {

    @Autowired
    private HyphenConst hyphenConst;

    private String hyphenUrl;
    private String hyphenId;
    private String hKey;
    private WebClient webClient;

    @PostConstruct
    private void setHyphenConst() {
        hyphenUrl = hyphenConst.getURL();
        hyphenId = hyphenConst.getUserId();
        hKey = hyphenConst.getHKey();
        webClient = WebClient.builder()
                .baseUrl(hyphenUrl)
                .defaultHeader("user-id", hyphenId)
                .defaultHeader("Hkey", hKey)
                // .defaultHeader("hyphen-gustation", "Y")  // 테스트 API
                .build();
    }

    public FCS.response postFCS(FCS.request request) {
        FCS.response response = webClient
                .post()
                .uri("/hb0081000398")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(FCS.response.class)
                .block();

        return response;
    }


    @Getter
    @RequiredArgsConstructor
    private class CheckAccount1Request {
        private final String inBankCode;
        private final String inAccount;
        public final String msgType = "2";
        public final String compName;
    }

    public CheckAccount1VO.Response checkAccount1(CheckAccount1VO.Request request) {

        CheckAccount1Request checkAccount1Request = new CheckAccount1Request(request.getInBankCode(), request.getInAccount(), hyphenConst.getCompanyName());

        CheckAccount1VO.Response response = webClient
                .post()
                .uri("/hb0081000378")
                .bodyValue(checkAccount1Request)
                .retrieve()
                .bodyToMono(CheckAccount1VO.Response.class)
                .block();

        /*
         * response
         * { "replyCode": "0000",
         *  "successYn": "Y",
         *  "sign": " ",
         *  "svcCharge":
         *  "000000000",
         *  "tradeTime": "132202",
         *  "oriSeqNo": "000084",
         *  "inPrintContent": "빠른마루",
         *  "tr_date": "2022-10-04" }
         *
         * Fail response
         * {
         *  "replyCode": "0012",
         *  "successYn": "N",
         *  "sign": "",
         *  "balance": "",
         *  "svcCharge": "",
         *  "tradeTime": ""
         * }
         */

        return response;
    }

    public CheckAccount2VO.Response checkAccount2(CheckAccount2VO.Request request) {

        CheckAccount2VO.Request checkAccount2Request = new CheckAccount2VO.Request(request.getOriSeqNo(), hyphenConst.getCompanyName() + request.getInPrintContent());

        CheckAccount2VO.Response response = webClient
                .post()
                .uri("/hb0081000379")
                .bodyValue(checkAccount2Request)
                .retrieve()
                .bodyToMono(CheckAccount2VO.Response.class)
                .block();

        return response;
    }

}
