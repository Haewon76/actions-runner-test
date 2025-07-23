package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.external.RestClient;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.korean.AddressKoreanVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AddressKoreanServiceImpl extends RestClient<AddressKoreanVo> implements AddressService {

    @Value("${address.korean-key}")
    private String authKey;

    @Autowired
    private AlarmService alarmService;

    public AddressKoreanServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<JusoResponse> getAddress(String keyword) {
        try {
            AddressKoreanVo addressVo = get(getUrl(keyword), AddressKoreanVo.class);
            return addressVo.getResults()
                    .getJuso()
                    .stream()
                    .map(JusoResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            alarmService.i("주소검색오류", keyword + ", " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<GoogleAddressResultResponse> getSearchResultForGlobal(String address) {
        return new ArrayList<>();
    }

    @Override
    public String getUrl(String keyword) {
        return String.format("https://business.juso.go.kr/addrlink/addrLinkApi.do?currentPage=1&countPerPage=20&resultType=json&confmKey=%s&hstryYn=Y&firstSort=road&keyword=%s", authKey, keyword);
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }

/*
{
    "results": {
        "common": {
            "errorMessage": "정상",
            "countPerPage": "10",
            "totalCount": "2",
            "errorCode": "0",
            "currentPage": "1"
        },
        "juso": [{
            "detBdNmList": "",
            "engAddr": "509 Teheran-ro, Gangnam-gu, Seoul",
            "rn": "테헤란로",
            "emdNm": "삼성동",
            "zipNo": "06169",
            "roadAddrPart2": " (삼성동)",
            "emdNo": "02",
            "sggNm": "강남구",
            "jibunAddr": "서울특별시 강남구 삼성동 158-16 엔씨타워 I",
            "siNm": "서울특별시",
            "roadAddrPart1": "서울특별시 강남구 테헤란로 509",
            "bdNm": "엔씨타워  I",
            "admCd": "1168010500",
            "udrtYn": "0",
            "lnbrMnnm": "158",
            "roadAddr": "서울특별시 강남구 테헤란로 509 (삼성동)",
            "lnbrSlno": "16",
            "buldMnnm": "509",
            "bdKdcd": "0",
            "liNm": "",
            "rnMgtSn": "116803122010",
            "mtYn": "0",
            "bdMgtSn": "1168010500101580016000001",
            "buldSlno": "0"
        }, {
            "detBdNmList": "",
            "engAddr": "509-1 Teheran-ro, Gangnam-gu, Seoul",
            "rn": "테헤란로",
            "emdNm": "삼성동",
            "zipNo": "06169",
            "roadAddrPart2": " (삼성동)",
            "emdNo": "02",
            "sggNm": "강남구",
            "jibunAddr": "서울특별시 강남구 삼성동 158-19",
            "siNm": "서울특별시",
            "roadAddrPart1": "서울특별시 강남구 테헤란로 509-1",
            "bdNm": "",
            "admCd": "1168010500",
            "udrtYn": "0",
            "lnbrMnnm": "158",
            "roadAddr": "서울특별시 강남구 테헤란로 509-1 (삼성동)",
            "lnbrSlno": "19",
            "buldMnnm": "509",
            "bdKdcd": "0",
            "liNm": "",
            "rnMgtSn": "116803122010",
            "mtYn": "0",
            "bdMgtSn": "1168010500101580019000001",
            "buldSlno": "1"
        }]
    }
}
*/
}
