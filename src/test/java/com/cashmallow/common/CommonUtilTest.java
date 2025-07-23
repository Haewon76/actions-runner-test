package com.cashmallow.common;

import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.shared.InvalidPasswordException;
import com.cashmallow.api.interfaces.authme.AuthMeProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.cashmallow.common.CommonUtil.*;
import static com.cashmallow.common.HashUtil.HashAlgo.*;
import static com.cashmallow.common.HashUtil.getHash;
import static com.cashmallow.common.HashUtil.getMd5Hash;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
class CommonUtilTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthMeProperties authMeProperties;

    @Test
    void Ip_check() {

        MockHttpServletRequest request = new MockHttpServletRequest();
        final String remoteAddr = CommonUtil.getRemoteAddr(request);
        System.out.println(remoteAddr);
    }

    @Test
    void isValidURL_성공() {
        // given
        String[] urls = new String[]{"https://api.cashmallow.com",
                "http://api.cashmallow.com",
                "http://api.cashmallow.com/images/atm/cashmallow_atm_icon.png"};

        // when
        List<Boolean> result = Arrays.stream(urls).map(CommonUtil::isValidURL).collect(Collectors.toList());

        // then
        assertThat(result).doesNotContain(false);
        log.debug("result:{}", result);
    }

    @Test
    void isValidURL_실패() {
        // given
        String[] urls = new String[]{"api.cashamllow.com",
                "apicashmallow.com",
                "api%%$^&&",
                "api.cashmallow.com/images/atm/cashmallow_atm_icon.png"};

        // when
        List<Boolean> result = Arrays.stream(urls).map(CommonUtil::isValidURL).collect(Collectors.toList());

        // then
        assertThat(result).doesNotContain(true);
        log.debug("result:{}", result);
    }

    @Test
    void 신분증_괄호_오류_리플레이스_테스트() {
        // 신분증 인증시 중국어 2바이트 괄호가 들어가져 있어서 일단 괄호로 강제 변경후 페이게이트에 요청

        // given
        String travelerIdentificationNo = "A12313123（1）";

        // when
        String result = textToNormalize(travelerIdentificationNo);

        // then
        assertThat(result).isEqualTo("A12313123(1)");
    }


    @Test
    public void UUID값을_테스트() {
        final UUID uuid = UUID.randomUUID();
        System.out.println(uuid); //36
        System.out.println(getMd5Hash(uuid.toString() + "asdasdjkashdㅎㄴ감ㄴㅇ머ㅏㅌ임ㅁㅋㅋ"));
    }

    @Test
    public void 랜덤_비밀번호를_테스트합니다() {
        for (int i = 0; i < 100; i++) {
            final String password = generateResetRandomPassword();
            System.out.println(password);
        }
    }

    @Test
    void krPhoneNumber1() throws Exception {
        // given
        User user = new User();
        user.setPhoneCountry("KOR");
        user.setPhoneNumber("+820111231234");

        // when
        String korPhoneNumber = CommonUtil.getKorPhoneNumber(user);

        // then
        assertThat(korPhoneNumber).isEqualTo("0111231234");

    }

    @Test
    void krPhoneNumber2() throws Exception {
        // given
        User user = new User();
        user.setPhoneCountry("KOR");
        user.setPhoneNumber("+82111231234");

        // when
        String korPhoneNumber = CommonUtil.getKorPhoneNumber(user);

        // then
        assertThat(korPhoneNumber).isEqualTo("0111231234");

    }

    @Test
    void krPhoneNumber3() throws Exception {
        // given
        User user = new User();
        user.setPhoneCountry("KOR");
        user.setPhoneNumber("+8201012341234");

        // when
        String korPhoneNumber = CommonUtil.getKorPhoneNumber(user);


        // then
        assertThat(korPhoneNumber).isEqualTo("01012341234");

    }

    @Test
    void krPhoneNumber4() throws Exception {
        // given
        User user = new User();
        user.setPhoneCountry("KOR");
        user.setPhoneNumber("+821012341234");

        // when
        String korPhoneNumber = CommonUtil.getKorPhoneNumber(user);

        // then
        assertThat(korPhoneNumber).isEqualTo("01012341234");
    }


    @Disabled
    @Test
    void time_변환() {
        // given
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        Long currentTimeMill = System.currentTimeMillis();
        // 32400000(9시간)를 더하는건 한국시간이 UTC시간 +9시간인 시간대를 가지기 때문이다.
        BigDecimal koreanTimeMill = BigDecimal.valueOf(currentTimeMill).add(BigDecimal.valueOf(32400000));
        Timestamp koreanTime = new Timestamp(Long.valueOf(koreanTimeMill.toString()));

        // 1시간 전
        BigDecimal beforeOneHour = koreanTimeMill.subtract(BigDecimal.valueOf(1 * 60 * 60 * 1000));
        Timestamp beforeOneHourTime = new Timestamp(Long.valueOf(beforeOneHour.toString()));

        System.out.println("beforeOneHourTime = " + beforeOneHourTime);
        System.out.println("koreanTime = " + koreanTime);

        String legacyToDate = koreanTime.toString().substring(0, 10).replace("-", "");
        String legacyToTime = koreanTime.toString().substring(11, 19).replace(":", "");

        String legacyFromDate = beforeOneHourTime.toString().substring(0, 10).replace("-", "");
        String legacyFromTime = beforeOneHourTime.toString().substring(11, 19).replace(":", "");

        // when
        LocalDateTime toDatetime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        System.out.println("toDatetime = " + toDatetime);
        LocalDateTime fromDateTime = toDatetime.minus(1, ChronoUnit.HOURS);
        System.out.println("fromDateTime = " + fromDateTime);

        String toDate = toDatetime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String toTime = toDatetime.format(DateTimeFormatter.ofPattern("HHmmss"));

        String fromDate = fromDateTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String fromTime = fromDateTime.format(DateTimeFormatter.ofPattern("HHmmss"));

        // then
        assertThat(toDate).isEqualTo(legacyToDate);
        assertThat(toTime).isEqualTo(legacyToTime);
        assertThat(fromDate).isEqualTo(legacyFromDate);
        assertThat(fromTime).isEqualTo(legacyFromTime);
    }

    @Test
    void getMd5HashTest() {
        String qwer23 = getMd5Hash("qwer23");
        assertThat(qwer23).isEqualTo("17BEE976C369408CAE7453D863055B4D");
    }

    @Test
    void localDateTimeTest() throws JsonProcessingException {
        Map<String, Object> map = new HashMap<>();
        LocalDateTime toDatetime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        map.put("date", toDatetime);

        System.out.println("echo = " + objectMapper.writeValueAsString(map));
        // System.out.println("toDatetime = " + JsonStr.toJsonString(toDatetime));
    }

    @Test
    void isValidPassword0() throws InvalidPasswordException {
        // given
        User user = new User();
        user.setLogin("alex01ruukr");
        user.setEmail("alex01@ruu.kr");

        // when
        String pass1 = "1aB@";              // 길이 부족
        String pass2 = "11aa";              // 길이 종류 모두
        String pass3 = "11223344aabbccdd";  // 종류 부족
        String pass4 = "1122aaㅁbb!!@@";    // 허용되지 않은 문자

        String pass11 = "11aaBB@@";
        String pass12 = "+11aaBB123";

        // then
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass1)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("SETTING_ERROR_MIN_PWD_LEN");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass2)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("SETTING_ERROR_MIN_PWD_LEN");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass3)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("SETTING_ERROR_MIN_PWD_LEN");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass4)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_INVALID_CHARACTER");
        CommonUtil.isValidPassword(user, pass11);
        CommonUtil.isValidPassword(user, pass12);
    }

    @Test
    void isValidPassword1() throws InvalidPasswordException {
        // given
        User user = new User();
        user.setLogin("alex01ruukr");
        user.setEmail("alex01@ruu.kr");

        // when
        String pass1 = "1ab@1111";
        String pass2 = "1ab@1234";
        String pass3 = "1ab@abcdef";
        String pass4 = "1ab@qwer";
        String pass5 = "1ab@8765";
        String pass6 = "1ab@14alex01@@";
        String pass7 = "1ab@!@#$";
        String pass8 = "1ab@14alEx01@@";

        // then
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass1)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_NUMBERS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass2)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SEQUENCE_NUMBERS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass3)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SEQUENCE_NUMBERS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass4)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_KEYBOARD_PATTERNS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass5)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SEQUENCE_NUMBERS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass6)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_ID");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass7)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_KEYBOARD_PATTERNS");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass8)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_ID");
    }

    @Test
    void isValidPassword2() throws InvalidPasswordException {
        // given
        User user = new User();
        user.setLogin("alexruukr");
        user.setEmail("alex@ruu.kr");
        user.setBirthDate("20000822");
        user.setPhoneNumber("+8249274593");

        // when
        String pass1 = "1ab@4927";
        String pass2 = "1ab@4593";
        String pass3 = "1ab@0822";
        String pass4 = "1ab@2000";

        // then
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass1)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_PHONE_NUMBER");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass2)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_PHONE_NUMBER");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass3)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_BIRTH_DATE");
        assertThatThrownBy(() -> CommonUtil.isValidPassword(user, pass4)).isInstanceOf(InvalidPasswordException.class).hasMessageContaining("USER_PASSWORD_SAME_BIRTH_DATE");
    }

    @Test
    void 생년월일_포함_여부() {
        // given
        String birthdate = "19900123";

        // when

        // then
        assertThat(CommonUtil.matchString(birthdate, "0012asdf")).isGreaterThanOrEqualTo(4);
        assertThat(CommonUtil.matchString(birthdate, "1990sadf")).isGreaterThanOrEqualTo(4);
        assertThat(CommonUtil.matchString(birthdate, "9900asdf")).isGreaterThanOrEqualTo(4);
        assertThat(CommonUtil.matchString(birthdate, "01234asdf")).isGreaterThanOrEqualTo(4);
        assertThat(CommonUtil.matchString(birthdate, "9001sadf")).isGreaterThanOrEqualTo(4);

    }

    @Disabled
    @Test
    void hash() {
        // given

        // when
        String md5 = getHash(MD5, "20xnljkf0b2lx012ln-df02nbl");
        String sha256 = getHash(SHA256, "20xnljkf0b2lx012ln-df02nbl");
        String sha512 = getHash(SHA512, "20xnljkf0b2lx012ln-df02nbl");

        // then
        assertThat(md5).isEqualTo("ABBA59B04B049DC36C4AFABCFE6FD8A0");
        assertThat(sha256).isEqualTo("2EA1E846E8CB7A0A7A6B4DE9C5C1A3B6F47062E0C97F488AEA465CD8172D6716");
        assertThat(sha512).isEqualTo("25505F532712C2BDFB9D94CC3296E9F706B10D5CB40AC6DD762C9B3808020F0025B7B22C6BAC52599DDFA69F91DBF05138C97AC5E58450D6549C8363F7737462");

    }

    @Test
    public void hmacSha256() {
        //given
        String sig = "2567-741e-4377-9bb5-4d47993b";
        String json = "{\"data\":{\"id\":\"3a13945f-6992-2084-1bdf-6f1d6cee39d8\",\"status\":\"Deleted\",\"source\":\"Agent\"},\"type\":\"IdentityVerification.ChangeState\",\"customerId\":\"ios_1720171265\",\"timestamp\":1720171533264,\"event\":\"JP_ID_CARD\"}";

        String hmacSha256 = CommonUtil.getAuthmeSignature(json, sig);
        System.out.println("hmacSha256 = " + hmacSha256);
    }

    @Test
    public void 국가명_파싱() {
        String str = "HK12312";
        System.out.println("str = " + getCountryByAuthmeId(str));
    }

}