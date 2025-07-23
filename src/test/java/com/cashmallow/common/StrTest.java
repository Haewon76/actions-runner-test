package com.cashmallow.common;

import com.cashmallow.api.interfaces.CryptAES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StrTest {

    @Test
    void decode() {
        String token = "94cf16fc-c990-43a2-836e-a43e0159929d";
        String data = "DA2CB205AD090874405B63E39317CB1CD401ECE795BDD6222CA8B1F67A9CD7E92DF239E0EC72DA3A66F1E81020BA7552AC541E6404B9BE00690C93528F753B1A68A9885F16337B94C978DE7D5F4B499B3E07B243C3BB3A307C8B290E19C3F6D8D0883E80";
        String decode = CustomStringUtil.decode(token, data);
        System.out.println("decode = " + decode);
    }

    @Test
    void maskingName() {
        // given
        String name = "AAAABBBBCCCC";

        // when
        String maskedName = CustomStringUtil.maskingName(name);

        // then
        System.out.println("maskedName = " + maskedName);
        assertThat(maskedName).isEqualTo("AAAA****CCCC");
    }

    @Test
    void maskingName_null() {
        // given
        String name = null;

        // when
        String maskedName = CustomStringUtil.maskingName(name);

        // then
        System.out.println("maskedName = " + maskedName);
        assertThat(maskedName).isEqualTo("");
    }

    @Test
    void decode_bearer() throws JsonProcessingException {
        // given
        String token = "Bearer F947F90C3F2E4958A327EF31A14FF408";
        List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        String plainText = new ObjectMapper().writeValueAsString(list);
        String encode = CryptAES.encode(token, plainText);
        System.out.println("encode = " + encode);
        System.out.println("token = " + token);
        // when
        String decode = CustomStringUtil.decode(token, encode);

        // then
        assertThat(decode).isEqualTo(plainText);
    }

    @Test
    void decode_pureToken() {
        // given
        String token = "Bearer F947F90C3F2E4958A327EF31A14FF408";
        String pureToken = "F947F90C3F2E4958A327EF31A14FF408";
        String plainText = "평문1234!@#$";
        String encode = CryptAES.encode(pureToken, plainText);

        // when
        String decode = CustomStringUtil.decode(token, encode);

        // then
        assertThat(decode).isEqualTo(plainText);
    }

    @Test
    void decode_emptyToken() {
        // given
        String token = "";
        String plainText = "평문1234!@#$";
        String encode = CryptAES.encode("0947F90C3F2E4958A327EF31A14FF409", plainText);

        // when
        String decode = CustomStringUtil.decode(token, encode);

        // then
        assertThat(decode).isEqualTo("");
    }

    @Test
    void decode_fail() {
        // given
        String token = "Bearer F947F90C3F2E4958A327EF31A14FF408";
        String plainText = "평문1234!@#$";
        String encode = CryptAES.encode("0947F90C3F2E4958A327EF31A14FF409", plainText);

        // when
        String decode = CustomStringUtil.decode(token, encode);

        // then
        assertThat(decode).isEqualTo(null);
    }

    @Disabled
    @Test
    void countTokenDecode() {
        // given
        String token = "Bearer F947F90C3F2E4958A327EF31A14FF408";
        String pureToken = "F947F90C3F2E4958A327EF31A14FF408";
        String plainText = "평문1234!@#$";

        List<String> tokens = List.of(token, pureToken, pureToken);
        List<String> encodes = List.of(CryptAES.encode(pureToken, plainText), CryptAES.encode(pureToken, plainText), CryptAES.encode(token, plainText));

        SecureRandom secureRandom = new SecureRandom();

        // when
        for (int i = 0; i < 100; i++) {
            CustomStringUtil.decode(tokens.get(secureRandom.nextInt(3)), encodes.get(secureRandom.nextInt(3)));
        }

        // then

    }

    @Test
    void matchMaskedString() {
        // given
        boolean b1 = CustomStringUtil.matchMaskedString("홍*동", "홍길동");
        boolean b2 = CustomStringUtil.matchMaskedString("1234****1234", "1234kkkk1234");
        boolean b3 = CustomStringUtil.matchMaskedString("1234****1234", "1234kkk1234");
        boolean b4 = CustomStringUtil.matchMaskedString(null, "1234kkkk1234");

        // when

        // then
        assertThat(b1).isTrue();
        assertThat(b2).isTrue();
        assertThat(b3).isFalse();
        assertThat(b4).isFalse();
    }

    @Disabled
    @Test
    void tokenNotBearer() {
        // given
        String encoded = "E45E621D2071B9364B5D457115EDC031F57258A2C880E48B2FAC582605CE1CC7B8AD0115B8E8031CE060019161382826F9FA6CCE6D5E6420684377A163447D72E010AF9261A083A7056C709646FA4212706BC393D3F5BAF3964A213AF30EB7AA0E5D0701C3B15C764C36F73D0F1A0C8F739AA21941A92D6DD23C25F58D29760A67BFF755E9E8E2749DBE0180E0FA6431784347B2";
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIyMDIzMDkxNy4xNTgiLCJpYXQiOjE2OTQ5MzA2NzUsImV4cCI6MTY5NDkzMTI3NSwidXNlcl9pZCI6IjQ1Njg1Iiwicm9sZSI6IltcIlJPTEVfQU5PTllNT1VTXCIsIFwiUk9MRV9VU0VSXCJdIiwiaW5zdGFuY2VJZCI6IkU1NjRERTU4N0NENUQ5NjA0QTIzN0RCNEEyNzZGODE5RjlERjU5RDNCNjZDMDFBMTI5Mzc4OTFEQzNDODRBRTc5RDE3OUY2QzRFOTk0NEY5QUU0MjRGMjFGMUVDRUI1MEY4OEVENkQ2M0IxREUyN0JFQ0UzNTM0NTc0MkZERkEyIn0.Y2Z8cGyKmmjdS2fqXU9ixY2cS0GBfplsqf682ze5w4w";
        String decoded = CustomStringUtil.decode(token, encoded);
        System.out.println("decoded = " + decoded);


        // when

        // then

    }

}