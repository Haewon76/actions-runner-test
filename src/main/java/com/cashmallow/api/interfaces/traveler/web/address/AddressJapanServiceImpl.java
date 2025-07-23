package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.external.RestClient;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.japan.AddressJapanVo;
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

@Slf4j
@Service
public class AddressJapanServiceImpl extends RestClient<AddressJapanVo> implements AddressService {

    @Autowired
    private AlarmService alarmService;

    @Value("${google.addressApiKey}")
    private String addressApiKey;


    @Autowired
    private Gson gson;

    public AddressJapanServiceImpl(RestTemplate restTemplate) {
        super(restTemplate);
    }

    @Override
    public List<JusoResponse> getAddress(String zipCode) {
        try {
            AddressJapanVo addresssEnglishVo = get(getUrl(zipCode), AddressJapanVo.class);
            // log.error(gson.toJson(addresssEnglishVo));
            if (addresssEnglishVo.isSuccess()) {
                return List.of(new JusoResponse(addresssEnglishVo.data(), zipCode));
            }

            throw new CashmallowException("JP 주소검색오류 발생 : " + zipCode);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            // slack alarm
            alarmService.i("주소검색오류", zipCode + ", " + e.getMessage());
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
        return String.format("https://api.zipaddress.net/?zipcode=%s", keyword);
    }

    @Override
    public HttpHeaders getHeaders() {
        return null;
    }
}
