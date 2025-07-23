package com.cashmallow.api.interfaces.traveler.web.address.dto;

import com.cashmallow.api.domain.model.country.enums.Country3;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.GsonBuilder;
import com.google.maps.model.AddressComponent;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/*
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
 */
@Data
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoogleAddressResultResponse {
    private Country3 countryAlpha3;
    private String zipCode;
    private String countryName;
    private String cityName;
    private String districtName;
    private String stateProvinceName;
    private String fullAddress = "";    // 상세 주소를 제외한 주소

    private String lat;
    private String lng;


    public GoogleAddressResultResponse(GeocodingResult geocodingResult) {
        log.debug("GoogleAddressResultResponse: " + new GsonBuilder().setPrettyPrinting().create().toJson(geocodingResult));

        final List<AddressComponent> addressComponents = Arrays.asList(geocodingResult.addressComponents);
        this.fullAddress = addressComponents.stream()
                .filter(addressComponent -> Arrays.stream(addressComponent.types)
                        .noneMatch(t -> StringUtils.containsAny(t.name(), "POSTAL_CODE", "STREET_NUMBER")))
                .map(a -> a.longName)
                .collect(Collectors.joining(", "));

        Collections.reverse(addressComponents);
        for (AddressComponent addressComponent : addressComponents) {
            final boolean isMatchedZipCode = Arrays.stream(addressComponent.types).anyMatch(type -> type.name().equalsIgnoreCase("POSTAL_CODE"));
            if (isMatchedZipCode) {
                this.zipCode = addressComponent.longName;
            } else if (StringUtils.isEmpty(this.countryName) && Arrays.stream(addressComponent.types).anyMatch(type -> type.name().equalsIgnoreCase("COUNTRY"))) {
                this.countryName = addressComponent.longName;
                this.countryAlpha3 = Country3.ofAlpha2(addressComponent.shortName);
            } else if (StringUtils.isEmpty(this.cityName) && Arrays.stream(addressComponent.types).anyMatch(type -> type.name().contains("ADMINISTRATIVE_AREA_LEVEL"))) {
                this.cityName = addressComponent.longName;
            } else if (StringUtils.isEmpty(this.stateProvinceName) && Arrays.stream(addressComponent.types).anyMatch(type ->
                        type.name().contains("LOCALITY") || type.name().contains("SUBLOCALITY") || type.name().contains("NEIGHBORHOOD"))) {
                this.stateProvinceName = addressComponent.longName;
            } else if (StringUtils.isEmpty(this.districtName)
                    && Arrays.stream(addressComponent.types)
                    .anyMatch(type -> type.name().contains("ADMINISTRATIVE_AREA_LEVEL") || type.name().contains("SUBLOCALITY_LEVEL"))) {
                this.districtName = addressComponent.longName;
            }
        }

        // cityName가 없으면 countryName으로 채움.
        if (StringUtils.isEmpty(this.cityName) && StringUtils.isNotBlank(this.countryName)) {
            this.cityName = this.countryName;
        }

        final LatLng location = geocodingResult.geometry.location;
        this.lat = String.valueOf(location.lat);
        this.lng = String.valueOf(location.lng);
    }
}