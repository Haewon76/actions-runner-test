package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.model.Job;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.common.JsonStr;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static com.cashmallow.api.domain.model.remittance.enums.RemittanceFundSource.INTERESTINCOME;
import static com.cashmallow.api.domain.model.remittance.enums.RemittancePurpose.LIVINGEXPENSES;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TravelersRequestTest {

    // private static final ObjectMapper mapper = JsonStr.mapper;
    @Autowired
    ObjectMapper mapper;

    @Test
    void toTravelerRequestIdCard() throws Exception {
        // given
        String body = "{\n" +
                "  \"certificationType\":\"ID_CARD\",\n" +
                "  \"enLastName\":\"enLast\",\n" +
                "  \"enFirstName\":\"enFirst\",\n" +
                "  \"identificationNumber\":\"123456\",\n" +
                "  \"dateOfBirth\":\"19991230\",\n" +
                "  \"job\":\"FINANCIAL_INDUSTRY\",\n" +
                "  \"sex\":\"FEMALE\",\n" +
                "  \"fundPurpose\":\"LIVINGEXPENSES\",\n" +
                "  \"fundSource\":\"INTERESTINCOME\"\n" +
                "}";


        // when
        TravelersRequest travelersRequest = mapper.readValue(body, TravelersRequest.class);
        // System.out.println("travelersRequest = " + travelersRequest);
        // System.out.println("travelersRequest.getClass() = " + travelersRequest.getClass());

        System.out.println("JsonStr.toJson(travelersRequest) = " + JsonStr.toJson(travelersRequest));

        // then
        TravelersHkIdCardDto travelersHkIdCardDto = new TravelersHkIdCardDto(
                "enLast",
                "enFirst",
                "123456",
                "19991230",
                Job.FINANCIAL_INDUSTRY,
                null,
                Traveler.TravelerSex.FEMALE,
                LIVINGEXPENSES.name(),
                INTERESTINCOME.name()
        );
        assertThat(travelersRequest).isInstanceOf(TravelersHkIdCardDto.class);
        assertThat(travelersRequest).isEqualTo(travelersHkIdCardDto);

    }

    @Test
    void toTravelerRequestPassport() throws Exception {
        // given
        String body = "{\n" +
                "  \"certificationType\":\"PASSPORT\",\n" +
                "  \"enLastName\":\"enLast\",\n" +
                "  \"enFirstName\":\"enFirst\",\n" +
                "  \"localLastName\":\"localLast\",\n" +
                "  \"localFirstName\":\"localFirst\",\n" +
                "  \"identificationNumber\":\"123456\",\n" +
                "  \"dateOfBirth\":\"19991230\",\n" +
                "  \"passportIssueDate\":\"19991230\",\n" +
                "  \"passportExpDate\":\"20001230\",\n" +
                "  \"passportCountry\":\"KOR\",\n" +
                "  \"job\":\"FINANCIAL_INDUSTRY\",\n" +
                "  \"sex\":\"FEMALE\"\n" +
                "}";

        TravelersRequest travelersRequest = mapper.readValue(body, TravelersRequest.class);
        // System.out.println("travelersRequest = " + travelersRequest);

        // System.out.println("travelersRequest.getClass() = " + travelersRequest.getClass());
        // System.out.println("JsonStr.toJson(travelersRequest) = " + JsonStr.toJson(travelersRequest));

        TravelersHkPassportDto hkPassportDto = (TravelersHkPassportDto) travelersRequest;
        // System.out.println("hkPassportDto.getPassportExpDate() = " + hkPassportDto.getPassportExpDate());

        // when

        // then
        TravelersHkPassportDto travelersHkPassportDto = new TravelersHkPassportDto("enLast",
                "enFirst",
                "123456",
                "19991230",
                Job.FINANCIAL_INDUSTRY,
                null,
                "localLast",
                "localFirst",
                "19991230", "20001230", Country3.KOR,
                Traveler.TravelerSex.FEMALE,
                null,
                null);

        assertThat(travelersRequest).isInstanceOf(TravelersHkPassportDto.class);
        assertThat(travelersRequest).isEqualTo(travelersHkPassportDto);
    }

}