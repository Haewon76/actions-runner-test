package com.cashmallow.api.domain.model.country.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.ZoneId;
import java.util.Arrays;

import static com.cashmallow.api.domain.shared.Const.INVALID_COUNTRY_CODE;

/**
 * DB의 country 테이블과 일치하게 유지 해야 함.
 */
@AllArgsConstructor
@Getter
public enum CountryCode {
    HK("001", "HKD", "Hong Kong", "홍콩", ZoneId.of("Asia/Hong_Kong")),
    TW("002", "TWD", "Taiwan", "대만", ZoneId.of("Asia/Taipei")),
    KR("003", "KRW", "South Korea", "한국", ZoneId.of("Asia/Seoul")),
    JP("004", "JPY", "Japan", "일본", ZoneId.of("Asia/Tokyo")),
    ID("005", "IDR", "Indonesia", "인도네시아", ZoneId.of("Asia/Jakarta")),
    MY("006", "MYR", "Malaysia", "말레이시아", ZoneId.of("Asia/Kuala_Lumpur")),
    VN("007", "VND", "Vietnam", "베트남", ZoneId.of("Asia/Ho_Chi_Minh")),
    PH("008", "PHP", "Philippines", "필리핀", ZoneId.of("Asia/Manila")),
    SG("009", "SGD", "Singapore", "싱가포르", ZoneId.of("Asia/Singapore")),
    TH("010", "THB", "Thailand", "태국", ZoneId.of("Asia/Bangkok")),
    MO("011", "MOP", "Macao", "마카오", ZoneId.of("Asia/Macau")),
    US("012", "USD", "United States", "미국", ZoneId.of("America/New_York")),
    GB("013", "GBP", "United Kingdom", "영국", ZoneId.of("Europe/London")),
    ES("014", "EUR", "Spain", "스페인", ZoneId.of("Europe/Madrid")),
    CA("015", "CAD", "Canada", "캐나다", ZoneId.of("Canada/Eastern")),
    AU("016", "AUD", "Australia", "호주", ZoneId.of("Australia/Sydney")),
    IE("017", "EUR", "Ireland", "아일랜드", ZoneId.of("Europe/Dublin")),
    DK("018", "EUR", "Denmark", "덴마크", ZoneId.of("Europe/Copenhagen")),
    CY("019", "EUR", "Cyprus", "키프로스", ZoneId.of("Asia/Nicosia")),
    EE("020", "EUR", "Estonia", "에스토니아", ZoneId.of("Europe/Tallinn")),
    FI("021", "EUR", "Finland", "핀란드", ZoneId.of("Europe/Helsinki")),
    PT("022", "EUR", "Portugal", "포르투갈", ZoneId.of("Europe/Lisbon")),
    LU("023", "EUR", "Luxembourg", "룩셈부르크", ZoneId.of("Europe/Luxembourg")),
    BE("024", "EUR", "Belgium", "벨기에", ZoneId.of("Europe/Brussels")),
    BG("025", "EUR", "Bulgaria", "불가리아", ZoneId.of("Europe/Sofia")),
    MC("026", "EUR", "Monaco", "모나코", ZoneId.of("Europe/Monaco")),
    SE("027", "EUR", "Sweden", "스웨덴", ZoneId.of("Europe/Stockholm")),
    PL("028", "EUR", "Poland", "폴란드", ZoneId.of("Europe/Warsaw")),
    AT("029", "EUR", "Austria", "오스트리아", ZoneId.of("Europe/Vienna")),
    IT("030", "EUR", "Italy", "이탈리아", ZoneId.of("Europe/Rome")),
    NL("031", "EUR", "Netherlands", "네덜란드", ZoneId.of("Europe/Amsterdam")),
    LT("032", "EUR", "Lithuania", "리투아니아", ZoneId.of("Europe/Vilnius")),
    FR("033", "EUR", "France", "프랑스", ZoneId.of("Europe/Paris")),
    DE("034", "EUR", "Germany", "독일", ZoneId.of("Europe/Berlin")),
    LV("035", "EUR", "Latvia", "라트비아", ZoneId.of("Europe/Riga")),
    MT("036", "EUR", "Malta", "몰타", ZoneId.of("Europe/Malta")),
    CN("037", "CNY", "China", "중국", ZoneId.of("Asia/Shanghai")),
    PK("038", "PKR", "Pakistan", "파키스탄", ZoneId.of("Asia/Karachi")),
    IN("039", "INR", "India", "인도", ZoneId.of("Asia/Kolkata")),
    BD("040", "BDT", "Bangladesh", "방글라데시", ZoneId.of("Asia/Dhaka")),
    LK("041", "LKR", "Sri Lanka", "스리랑카", ZoneId.of("Asia/Colombo")),
    MN("042", "MNT", "Mongolia", "몽골", ZoneId.of("Asia/Ulaanbaatar")),
    ZA("043", "ZAR", "South Africa", "남아프리카 공화국", ZoneId.of("Africa/Johannesburg")),
    NP("044", "NPR", "Nepal", "네팔", ZoneId.of("Asia/Kathmandu"));



    private String code;
    private String currency;
    private String name;
    private String korName;
    private ZoneId zoneId;

    /**
     * 캐시멜로 국가 코드를 countryCode로 변환 "001" -> HK
     *
     * @param code
     * @return
     */
    public static CountryCode of(String code) {
        return Arrays.stream(CountryCode.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(INVALID_COUNTRY_CODE));
    }

    public static CountryCode fromCurrency(String currency) {
        return Arrays.stream(CountryCode.values())
                .filter(c -> c.getCurrency().equals(currency))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(INVALID_COUNTRY_CODE));
    }

    public static CountryCode ofZoneId(String code) {
        return Arrays.stream(CountryCode.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Country Code"));
    }

    public static CountryCode fromIso3166(String iso3166) {
        return Arrays.stream(CountryCode.values())
                .filter(c -> c.name().equals(iso3166))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(iso3166+" : Invalid Country Enum Name"));
    }
}
