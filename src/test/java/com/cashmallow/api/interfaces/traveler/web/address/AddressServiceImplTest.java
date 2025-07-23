package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.korean.JusoKoreanItem;
import com.cashmallow.config.EnableDevLocal;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@EnableDevLocal
@Disabled
class AddressServiceImplTest {

    public String addressPart;
    public String address;

    @Autowired
    private AddressKoreanServiceImpl koreanService;

    @Autowired
    private AddressEnglishServiceImpl englishService;

    @Autowired
    private RestTemplate restTemplate;

    private Gson gson;

    // @BeforeAll
    // public void setup() {
    //     address = "테헤란로509";
    //     addressPart = "서울특별시 강남구 테헤란로 509";
    //
    //     gson = new Gson();
    //     // restTemplate = new RestTemplate();
    //     // koreanService = new AddressKoreanServiceImpl(restTemplate);
    //     // englishService = new AddressEnglishServiceImpl(restTemplate);
    // }

    @Test
    public void test() {
        address = "테헤란로509";
        addressPart = "福岡, 市中央区, 大";

        gson = new GsonBuilder().setPrettyPrinting().create();
        // restTemplate = new RestTemplate();
        // koreanService = new AddressKoreanServiceImpl(restTemplate);
        // englishService = new AddressEnglishServiceImpl(restTemplate);

        final List<GoogleAddressResultResponse> searchResultForGlobal = englishService.getSearchResultForGlobal(addressPart);
        log.info(gson.toJson(searchResultForGlobal));
    }

    @Test
    public void 우편번호로_검색이_잘되는지_테스트() {
        addressPart = "169-0073";

        gson = new GsonBuilder().setPrettyPrinting().create();
        // restTemplate = new RestTemplate();
        // koreanService = new AddressKoreanServiceImpl(restTemplate);
        // englishService = new AddressEnglishServiceImpl(restTemplate);

        final List<GoogleAddressResultResponse> searchResultForGlobal = englishService.getSearchResultForGlobal(addressPart);
        log.info(gson.toJson(searchResultForGlobal));
    }

    @Test
    public void 주소값이_정상적으로_잘_넘어오는지_테스트() {
        // when
        List<JusoResponse> list = new ArrayList<>();
        JusoKoreanItem item = new JusoKoreanItem();
        item.setRoadAddrPart1("서울특별시 강남구 테헤란로 509");
        item.setRoadAddr("서울특별시 강남구 테헤란로 509");
        item.setJibunAddr("서울특별시 강남구 역삼동 822-1");
        item.setZipNo("06164");
        list.add(new JusoResponse(item));
        // list.add(new JusoResponse(item));

        // then
        Mockito
                .when(koreanService.getAddress(address))
                .thenReturn(list);

        // given
        List<JusoResponse> responseList = koreanService.getAddress(address);
        assertEquals(1, responseList.size());
        assertEquals("06164", responseList.get(0).getZipCode());
    }

    @Disabled
    @Test
    public void 한국어주소_테스트2() {
        List<JusoResponse> list = koreanService.getAddress(address);
        list.stream().forEach(System.out::println);
    }

    @Disabled
    @Test
    public void 한국어주소_테스트() {
        List<JusoResponse> list = koreanService.getAddress(address);
        list.stream().forEach(System.out::println);
    }

    @Disabled
    @Test
    public void 영어주소_테스트() {
        List<JusoResponse> list = englishService.getAddress(addressPart);
        list.stream().forEach(System.out::println);
    }

    @Disabled
    @Test
    public void 영어_한국어_주소_통합_테스트() {
        List<JusoResponse> responseList = new ArrayList<>();
        koreanService.getAddress(address).forEach(koreanAddress -> englishService.getAddress(koreanAddress.getRoadAddressPart()).forEach(englishAddress -> responseList.add(new JusoResponse(englishAddress, koreanAddress))));
        log.debug(gson.toJson(responseList));
    }

    @Test
    public void 일본여_영문주소변환_테스트() {
        String address = "福岡市中央区天神1丁目8番1号";

        gson = new GsonBuilder().setPrettyPrinting().create();

        List<GoogleAddressResultResponse> searchResultForGlobal = englishService.getSearchResultForGlobal(address);
        if(!searchResultForGlobal.isEmpty()) {
            GoogleAddressResultResponse googleAddressResultResponse = searchResultForGlobal.get(0);
        }
        System.out.println("gson.toJson(searchResultForGlobal) = " + gson.toJson(searchResultForGlobal));
    }

}