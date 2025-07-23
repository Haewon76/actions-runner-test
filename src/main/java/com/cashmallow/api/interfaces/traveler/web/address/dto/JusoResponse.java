package com.cashmallow.api.interfaces.traveler.web.address.dto;

import com.cashmallow.api.interfaces.traveler.web.address.dto.english.JusoEnglishItem;
import com.cashmallow.api.interfaces.traveler.web.address.dto.japan.AddressJapanVo;
import com.cashmallow.api.interfaces.traveler.web.address.dto.korean.JusoKoreanItem;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JusoResponse {
    private String roadAddress;
    private String roadAddressPart;
    private String jibunAddress;
    private String zipCode;
    private String roadAddressEnglish;
    private String jibunAddressEnglish;

    // for Japan
    private String prefecture; // 현
    private String city;
    private String town;

    // for english
    public JusoResponse(JusoEnglishItem item) {
        this.zipCode = item.getZipNo();
        this.roadAddressEnglish = item.getRoadAddr();
        this.jibunAddressEnglish = item.getJibunAddr();
    }

    // for korean
    public JusoResponse(JusoKoreanItem item) {
        this.roadAddressPart = item.getRoadAddrPart1();
        this.roadAddress = item.getRoadAddr();
        this.jibunAddress = item.getJibunAddr();
        this.zipCode = item.getZipNo();
    }

    // for korean and english
    public JusoResponse(JusoResponse english, JusoResponse korean) {
        this.roadAddress = korean.getRoadAddress();
        this.jibunAddress = korean.getJibunAddress();
        this.roadAddressPart = korean.getRoadAddressPart();
        this.zipCode = english.getZipCode();
        this.roadAddressEnglish = english.getRoadAddressEnglish();
        this.jibunAddressEnglish = english.getJibunAddressEnglish();
    }

    public JusoResponse(AddressJapanVo.AddressJapanVoData data, String zipCode) {
        this.prefecture = data.pref();
        this.city = data.city();
        this.town = data.town();
        this.zipCode = zipCode;

    }
}