package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.external.RestClient;
import com.cashmallow.api.infrastructure.alarm.SlackChannel;
import com.cashmallow.api.interfaces.traveler.web.address.dto.AddresssEnglishVo;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;
import com.google.gson.Gson;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
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
public class AddressEnglishServiceImpl extends RestClient<AddresssEnglishVo> implements AddressService {

    @Value("${address.english-key}")
    private String authKey;

    @Value("${google.addressApiKey}")
    private String addressApiKey;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private Gson gson;

    public AddressEnglishServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<JusoResponse> getAddress(String keyword) {
        try {
            AddresssEnglishVo addresssEnglishVo = get(getUrl(keyword), AddresssEnglishVo.class);
            log.error(gson.toJson(addresssEnglishVo));
            return addresssEnglishVo.getResults()
                    .getJuso()
                    .stream()
                    .map(JusoResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // slack alarm
            alarmService.i("주소검색오류", keyword + ", " + e.getMessage());
        }
        return new ArrayList<>();
    }

    @Override
    public List<GoogleAddressResultResponse> getSearchResultForGlobal(String address) {
        List<GoogleAddressResultResponse> result = new ArrayList<>();

        final GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(addressApiKey)
                .build();

        try {
            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();
            for (GeocodingResult geocodingResult : results) {
                try {
                    result.add(new GoogleAddressResultResponse(geocodingResult));
                } catch (Exception e) {
                    log.error("GoogleAddressResultResponse: " + e.getMessage(), e);
                    alarmService.i("Google 주소검색오류", address + ", " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("getSearchResultForGlobal: " + e.getMessage(), e);
        } finally {
            geoApiContext.shutdown();
        }
        return result;
    }

    @Override
    public String getUrl(String keyword) {
        return String.format("https://business.juso.go.kr/addrlink/addrEngApi.do?currentPage=1&countPerPage=10&resultType=json&confmKey=%s&hstryYn=Y&firstSort=road&keyword=%s", authKey, keyword);
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
            "errorCode": "0",
            "totalCount": "2",
            "currentPage": "1"
        },
        "juso": [{
            "siNm": "Seoul",
            "lnbrMnnm": "158",
            "bdKdcd": "0",
            "jibunAddr": "158-16 Samseong-dong, Gangnam-gu, Seoul",
            "buldSlno": "0",
            "zipNo": "06169",
            "admCd": "1168010500",
            "roadAddr": "509 Teheran-ro, Gangnam-gu, Seoul",
            "liNm": "",
            "mtYn": "0",
            "rnMgtSn": "116803122010",
            "korAddr": "서울특별시 강남구 테헤란로 509",
            "sggNm": "Gangnam-gu",
            "buldMnnm": "509",
            "emdNm": "Samseong-dong",
            "lnbrSlno": "16",
            "udrtYn": "0",
            "rn": "Teheran-ro"
        }, {
            "siNm": "Seoul",
            "lnbrMnnm": "158",
            "bdKdcd": "0",
            "jibunAddr": "158-19 Samseong-dong, Gangnam-gu, Seoul",
            "buldSlno": "1",
            "zipNo": "06169",
            "admCd": "1168010500",
            "roadAddr": "509-1 Teheran-ro, Gangnam-gu, Seoul",
            "liNm": "",
            "mtYn": "0",
            "rnMgtSn": "116803122010",
            "korAddr": "서울특별시 강남구 테헤란로 509-1",
            "sggNm": "Gangnam-gu",
            "buldMnnm": "509",
            "emdNm": "Samseong-dong",
            "lnbrSlno": "19",
            "udrtYn": "0",
            "rn": "Teheran-ro"
        }]
    }
}
*/
}
