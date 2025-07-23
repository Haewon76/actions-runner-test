package com.cashmallow.api.interfaces.authme;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.authme.dto.AuthMeCustomerEventMediaImageResponse;
import com.cashmallow.api.interfaces.authme.dto.AuthMeCustomerEventMediaResponse;
import com.cashmallow.api.interfaces.authme.dto.AuthMeCustomerEventResponse;
import com.cashmallow.api.interfaces.authme.dto.AuthMeTokenResponseDto;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.stream.Stream;

import static com.cashmallow.api.domain.shared.Const.getAuthFilePath;
import static com.cashmallow.common.CommonUtil.getBase64ToJPGFile;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@EnableDevLocal
class AuthMeServiceTest {

    @Autowired
    AuthMeService authMeService;

    @Autowired
    JsonUtil jsonUtil;

    @Value("${host.file.path.home}")
    private String hostFilePathHome;

    @Test
    void 토큰을_받아오는지_테스트() {
        CountryCode co = CountryCode.HK;
        long travelerId = 123L;
        String authmeTravelerId = co.name() + travelerId;
        final AuthMeTokenResponseDto token = authMeService.getAdminToken(co);

        assertNotNull(token.accessToken());
        assertNotNull(token.expiredAt());

        System.out.println("token = " + token);


        // 토큰 디코딩
        // DecodedJWT jwt = JWT.decode(token.accessToken());
        // String payload = jwt.getPayload();
        // byte[] decodedBytes = Base64.getUrlDecoder().decode(payload);
        // String decodedPayload = new String(decodedBytes, StandardCharsets.UTF_8);
        //
        // System.out.println(getJsonStringToPretty(decodedPayload));
    }

    @Test
    public void JP_테스트명() {
        try {
            CountryCode co = CountryCode.HK;
            CertificationType certificationType = CertificationType.ID_CARD;
            String customerId = "JP984833";

            AuthMeTokenResponseDto tokenResponseDto = authMeService.getAdminToken(co);
            String accessToken = tokenResponseDto.accessToken();
            Stream.of(customerId).forEach(travelerId -> {
                AuthMeCustomerEventResponse customer = authMeService.getCustomerEvent(accessToken, travelerId, CountryCode.JP, certificationType);
                System.out.println("customer = " + jsonUtil.toJsonPretty(customer));
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void HK_테스트명() {
        // try {
        CountryCode co = CountryCode.HK;
        CertificationType certificationType = CertificationType.ID_CARD;
        String customerId = "HK1019974";
        AuthMeTokenResponseDto tokenResponseDto = authMeService.getAdminToken(co);
        String accessToken = tokenResponseDto.accessToken();

        Stream.of(customerId).forEach(travelerId -> {
            AuthMeCustomerEventResponse customer = authMeService.getCustomerEvent(accessToken, customerId, CountryCode.HK, certificationType);
            System.out.println("customer = " + jsonUtil.toJsonPretty(customer));
        });
        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

    @Test
    public void SHA256_HMAC_TEST() {

    }

    @Test
    public void BASE64_TO_JPG() {
        try {
            CountryCode co = CountryCode.HK;
            String travelerId = "JP1018307";
            CertificationType certificationType = CertificationType.RESIDENCE_CARD;
            CountryCode countryCode = CountryCode.valueOf(travelerId.substring(0, 2));

            AuthMeTokenResponseDto tokenResponseDto = authMeService.getAdminToken(co);
            AuthMeCustomerEventMediaResponse customerEventMedia = authMeService.getCustomerEventMedia(tokenResponseDto.accessToken(), travelerId, countryCode, certificationType);
            jsonUtil.toJsonPretty(customerEventMedia);
            customerEventMedia.items()
                    .stream()
                    .filter(m -> StringUtils.isNoneEmpty(m.mediaId()))
                    .forEach(f -> {
                        System.out.println("f = " + f);
                        String mediaId = f.mediaId();
                        AuthMeCustomerEventMediaImageResponse customerEventMediaImage = authMeService.getCustomerEventMediaImage(tokenResponseDto.accessToken(), travelerId, mediaId, countryCode, certificationType);
                        if (StringUtils.isNotBlank(customerEventMediaImage.content())) {
                            String fileServerDir = hostFilePathHome + getAuthFilePath(Long.parseLong(travelerId.substring(2)));
                            File base64ToJPGFile = getBase64ToJPGFile(fileServerDir, mediaId, customerEventMediaImage.content());

                            System.out.println("base64ToJPGFile = " + base64ToJPGFile.getAbsolutePath());
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void GET_AUTH_ME_USER_INFO() throws CashmallowException {
        String travelerId = "JP997713"; //HK986414 - passport
        // authMeService.updateAuthCustomerData(travelerId, false, null);
        // log.info("authCustomerData = {}", jsonUtil.toJsonPretty(authCustomerData));
    }

    @Test
    public void 성_이름테스트() {
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("CH LOK YAU")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("CHAI, LOK YAU")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("TANAKA TARO")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("KIM JD JUNG")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("kim jd jung nam")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("KIM JD")));
        // System.out.println("test = " + jsonUtil.toJsonPretty(getName("BOM")));
    }

    @Test
    public void 토큰가져오기() {
        CertificationType certificationType = CertificationType.RESIDENCE_CARD;
        CountryCode countryCode = CountryCode.JP;
        Long userId = 984833L;
        AuthMeTokenResponseDto tokenAdmin = authMeService.getAdminToken(countryCode);
        System.out.println("tokenAdmin = " + tokenAdmin);
        AuthMeTokenResponseDto tokenApi = authMeService.getApiToken(countryCode.name() + userId, countryCode, certificationType);
        System.out.println("tokenApi = " + tokenApi);
    }

    @Test
    public void Authme_상태업데이트() {
        String customerId = "JP1022163";
        authMeService.checkTimeoutAndUpdateStatus(customerId);
    }

    @Test
    public void 테스트명() {
        String base64String = """
                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
                """;

        try {
            // Base64 디코딩
            byte[] decodedBytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64String);
            String decodedString = new String(decodedBytes, "UTF-8");
            System.out.println("디코딩 결과: " + decodedString);
        } catch (Exception e) {
            System.out.println("디코딩 에러: " + e.getMessage());
            e.printStackTrace();
        }
    }
}