package com.cashmallow.api.interfaces.traveler.web;

import com.cashmallow.api.domain.model.terms.TermsType;
import com.cashmallow.api.interfaces.traveler.dto.TermsAndPrivacyRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.util.List;

class ApiControllerTest {

    Gson gson = new Gson();

    @Test
    void termsAndPrivacy() {
        // given
        String json = "{\"termsTypeList\":[\"TERMS\",\"PRIVACY\",\"TERMS_OPENBANK\"],\"timestamp\":1694268817,\"deviceId\":\"afcb75cef6fe2061\"}";

        // Type type = new TypeToken<Map<String, List<TermsType>>>() {
        // }.getType();

        TermsAndPrivacyRequest map = gson.fromJson(json, TermsAndPrivacyRequest.class);
        // List<TermsType> termsTypeList = (List<TermsType>) map.get("termsTypeList");
        List<TermsType> termsTypeList = map.getTermsTypeList();

        System.out.println("map = " + map);
        System.out.println("termsTypeList = " + termsTypeList);


        // when

        // then

    }

}