package com.cashmallow.api.domain.shared;

import java.math.BigDecimal;

public class Const {

    public static final String SERVER_VER = "v2.7.2";

    // 기본 Encoding
    public static final String DEF_ENCODING = "utf-8";

    // 여행자 환불 계좌 인증 정보
    public static final String CTL_CONFIRM_TRAVELER_ACCOUNT = "/traveler/confirm-account";

    // 가맹점 pay-back 계좌 인증 정보
    public static final String CTL_CONFIRM_STOREKEEPER_ACCOUNT = "/storekeeper/confirm-account";

    // FILE 서버 관련 상수
    public static final String FILE_SEPARATOR = "/";

    public static final String FILE_KIND_PROFILE = "PROFILE";                   // 유저 프로파일 사진 저장소
    public static final String FILE_KIND_PASSPORT = "PASSPORT";                 // 여행자 여권사진 (구)저장소
    public static final String FILE_KIND_CERTIFICATION = "CERTIFICATION";       // 여행자 신분인증 사진 저장소
    public static final String FILE_KIND_AUTHME = "AUTHME";                     // 여행자 AUTHME 사진 저장소
    public static final String FILE_KIND_ADDRESS = "ADDRESS";                   // 여행자 주소 증명서 사진 저장소
    public static final String FILE_KIND_RECEIPT = "RECEIPT";                   // 환전 신청 시 입금영수증 저장소
    public static final String FILE_KIND_RECEIPT_REMIT = "RECEIPT_REMIT";       // 송금 신청 시 입금영수증 저장소
    public static final String FILE_KIND_CMRECEIPT = "CMRECEIPT";               // 환전 완료 시 환전영수증 저장소
    public static final String FILE_KIND_BANKBOOK = "BANKBOOK";                 // 여행자 환불 통장 사본 저장소
    public static final String FILE_KIND_SHOP = "SHOP";                         // 가맹점 상점사진 저장소
    public static final String FILE_KIND_BIZ = "BIZ";                           // 가맹점 사업자등록증 저장소
    public static final String FILE_KIND_TEST = "TEST";                         // 테스트 파일 저장소
    public static final String FILE_KIN_COUPON = "coupon";                      // 쿠폰 파일 저장소
    public static final String FILE_KIN_THUMBNAIL = "thumbnail";                // 썸네일 파일 저장소
    public static final String FILE_KIND_BUNDLE = "BUNDLE";                     // 번들 파일 저장소
    public static final String FILE_KIND_JP_CERTIFICATION = "JP_CERTIFICATION"; // JP 사용자들의 신분인증 사진 저장소

    public static final String FILE_SERVER_PROFILE = FILE_SEPARATOR + FILE_KIND_PROFILE;                                // 유저 프로파일 사진 저장소
    public static final String FILE_SERVER_PASSPORT = FILE_SEPARATOR + FILE_KIND_PASSPORT;                              // 여행자 여권사진 (구)저장소
    public static final String FILE_SERVER_CERTIFICATION = FILE_SEPARATOR + FILE_KIND_CERTIFICATION;                    // 여행자 신분증명 사진 저장소
    public static final String FILE_SERVER_AUTHME = FILE_SEPARATOR + FILE_KIND_AUTHME;                                  // AUTHME 여행자 신분증명 사진 저장소
    public static final String FILE_SERVER_ADDRESS = FILE_SEPARATOR + FILE_KIND_ADDRESS;                                // 여행자 주소 증명서 사진 저장소
    public static final String FILE_SERVER_RECEIPT = FILE_SEPARATOR + FILE_KIND_RECEIPT;                                // 환전 신청 시 입금영수증 저장소
    public static final String FILE_SERVER_RECEIPT_REMIT = FILE_SEPARATOR + FILE_KIND_RECEIPT_REMIT;                    // 송금 신청 시 입금영수증 저장소
    public static final String FILE_SERVER_CMRECEIPT = FILE_SEPARATOR + FILE_KIND_CMRECEIPT;                            // 환전 완료 시 환전영수증 저장소
    public static final String FILE_SERVER_BANKBOOK = FILE_SEPARATOR + FILE_KIND_BANKBOOK;                              // 여행자 환불 통장 사본 저장소
    public static final String FILE_SERVER_SHOP = FILE_SEPARATOR + FILE_KIND_SHOP;                                      // 가맹점 상점사진 저장소
    public static final String FILE_SERVER_BIZ = FILE_SEPARATOR + FILE_KIND_BIZ;                                        // 가맹점 사업자등록증 저장소
    public static final String FILE_SERVER_TEST = FILE_SEPARATOR + FILE_KIND_TEST;                                      // 테스트 파일 저장소
    public static final String FILE_SERVER_COUPON = FILE_SEPARATOR + FILE_KIN_COUPON;                                   // 쿠폰 파일 저장소
    public static final String FILE_SERVER_COUPON_THUMBNAIL = FILE_SERVER_COUPON + FILE_SEPARATOR + FILE_KIN_THUMBNAIL; // 쿠폰 썸네일 파일 저장소
    public static final String FILE_SERVER_BUNDLE = FILE_SEPARATOR + FILE_KIND_BUNDLE;                                  // 번들 파일 저장소
    public static final String FILE_SERVER_JP_CERTIFICATION = FILE_SEPARATOR + FILE_KIND_JP_CERTIFICATION;             // JP 사용자들의 신분인증 사진 저장소

    public static final String COUPON_BASE_URL = "coupon"; // 쿠폰 BASE URL
    public static final String COUPON_MOBILE_BASE_URL = "mobile"; // 쿠폰 BASE URL

    //--------------------------------------------
    // 날짜/시간 관련 상수
    //--------------------------------------------
    public static final String DATE_TIME_YYYYMMDDHHMMSS = "yyyy-MM-dd HH:mm:ss";

    //--------------------------------------------
    // 권한 관련 상수
    //--------------------------------------------

    // 권한 정의
    public static final String ROLE_ADMIN = "ROLE_ADMIN";            // 개발자. admin web 시스템에 접속하는 IP 등록이 가능한 사용자 권한을 의미한다.
    public static final String ROLE_SYSTEM = "ROLE_SYSTEM";          // 시스템 --> 미사용 
    public static final String ROLE_SUPERMAN = "ROLE_SUPERMAN";      // 총괄관리자 05.17 사용 추가 3) Paygate권한 추가 - 1)수수료율 변경 권한 체크 2)특정 권한 사용자 등록 권한. 3) 시스템 쿠폰 관리 1,2기능은 사용 안하고 있음.
    public static final String ROLE_MANAGER = "ROLE_MANAGER";        // 정관리자 --> 미사용 
    public static final String ROLE_ASSIMAN = "ROLE_ASSIMAN";        // 부관리자
    public static final String ROLE_USER = "ROLE_USER";              // 일반사용자
    public static final String ROLE_ANONYMOUS = "ROLE_ANONYMOUS";    // 익명사용자

    // 사용자별 부여 권한
    public static final String[] ROLE_STRS_ADMIN = new String[]{ROLE_ADMIN, ROLE_SYSTEM, ROLE_SUPERMAN, ROLE_MANAGER, ROLE_ASSIMAN, ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_SYSTEM = new String[]{ROLE_SYSTEM, ROLE_SUPERMAN, ROLE_MANAGER, ROLE_ASSIMAN, ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_SUPERMAN = new String[]{ROLE_SUPERMAN, ROLE_MANAGER, ROLE_ASSIMAN, ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_MANAGER = new String[]{ROLE_MANAGER, ROLE_ASSIMAN, ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_ASSIMAN = new String[]{ROLE_ASSIMAN, ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_USER = new String[]{ROLE_USER, ROLE_ANONYMOUS};
    public static final String[] ROLE_STRS_ANONYMOUS = new String[]{ROLE_ANONYMOUS};


    //--------------------------------------------
    // 암호 관련 상수
    //--------------------------------------------

    // 최대 암호 길이
    public static final int MIN_PWD_LEN = 8;
    public static final int MAX_PWD_LEN = 64;
    public static final int MIN_PWD_KIND = 8;


    //--------------------------------------------
    // Paging 관련 상수
    //--------------------------------------------
    public static final int DEF_PAGE_NO = 0;
    public static final int DEF_PAGE_SIZE = 10;


    //--------------------------------------------
    // 거리 관련 상수
    //--------------------------------------------
    public static final int DEF_DISTANCE_KM = 5;                // 5 Kilo-meter


    //--------------------------------------------
    // 사용자 관련 상수
    //--------------------------------------------

    // 등록 안 된 사용자 ID.
    public static final long NO_USER_ID = -1;

    // 등록 안 된 company ID.
    public static final long NO_COMPANY_ID = -1;

    // 사용자 type
    public static final String CLS_NA = "";                          // 어디에도 속하지 않는 경우. 오류 비교을 위해 선언함.
    public static final String CLS_RECOMMENDER = "R";                // 추천인(여행자 또는 가맹점으로 전환하지 않은 사용자)
    public static final String CLS_TRAVELER = "T";                   // 여행자
    public static final String CLS_STOREKEEPER = "S";                // 가맹점
    public static final String CLS_ADMIN = "A";                      // Admin user

    //--------------------------------------------
    // 가맹점 관련 상수
    //--------------------------------------------

    // 가맹점 인출 가능액 추가/초기화
    public static final Object CASH_OUT_BALANCE_TYPE_ADD = "A";     // 추가
    public static final Object CASH_OUT_BALANCE_TYPE_RESET = "R";   // 초기화


    //--------------------------------------------
    // Mapping 관련 상수
    //--------------------------------------------

    public static final BigDecimal UNDEFINED = new BigDecimal(-1);


    //--------------------------------------------
    // QR-ODE 관련 상수
    //--------------------------------------------

    public static final int SIZE_OF_UUID = 32;
    public static final int SIZE_OF_QRCODE = SIZE_OF_UUID * 3;


    //--------------------------------------------
    // 오류 관련 상수
    //--------------------------------------------

    // WEB API 응답코드
    public static final int RESPONSE_CODE_OK = 200; // 정상
    public static final int RESPONSE_CODE_ERROR = 400; // 오류

    public static final String CODE_SUCCESS = "200";
    public static final String STATUS_SUCCESS = "Success";
    public static final String MSG_SUCCESS = "";

    // 400 error : request has a bad syntax. 서버에서는 어떤 메소드를 호출 조차 할 수 없는 상태. 
    public static final String CODE_FAILURE = "400";
    public static final String STATUS_FAILURE = "Failure";

    public static final String CODE_LOGIN_FAILURE = "401";
    public static final String STATUS_LOGIN_FAILURE = "Bad Request";
    public static final String MSG_LOGIN_FAILURE = "로긴 실패. 아이디, 암호 등이 일치히지 않습니다.";

    public static final String CODE_INVALID_PARAMS = "402";
    public static final String STATUS_INVALID_PARAMS = "Invalid Parameters";
    public static final String MSG_INVALID_PARAMS = "파라미터가 올바르지 않습니다.";

    public static final String CODE_INVALID_TOKEN = "403";
    public static final String STATUS_INVALID_TOKEN = "Invalid token";
    public static final String MSG_INVALID_TOKEN = "Invalid token";

    public static final String CODE_INVALID_USER_ID = "404";
    public static final String STATUS_INVALID_USER_ID = "Invalid user ID.";
    public static final String MSG_INVALID_USER_ID = "할당되지 않은 사용자 ID.입니다.";

    public static final String CODE_NEED_AUTH = "405";
    public static final String STATUS_NEED_AUTH = "Bad auth.";
    public static final String MSG_NEED_AUTH = "권한이 부족합니다.";

    public static final String CODE_TEST_MODE = "406";
    public static final String STATUS_TEST_MODE = "real mode";
    public static final String MSG_TEST_MODE = "이 기능은 테스트 모드에서만 사용가능 합니다.";

    public static final String CODE_SERVER_ERROR = "500";
    public static final String STATUS_SERVER_ERROR = "서버 오류";


    public static final String CODE_CAN_USE_EMAIL = CODE_SUCCESS;
    public static final String STATUS_CAN_USE_EMAIL = CODE_SUCCESS;
    public static final String MSG_CAN_USE_EMAIL = "사용할 수 있는 E-Mail 주소입니다.";

    public static final String CODE_ALREADY_USED_EMAIL = CODE_FAILURE;
    public static final String STATUS_ALREADY_USED_EMAIL = "Bad Request";
    public static final String MSG_ALREADY_USED_EMAIL = "이미 사용 중인 E-Mail 주소입니다.";

    public static final String CODE_EMPTY_EMAIL = CODE_FAILURE;
    public static final String STATUS_EMPTY_EMAIL = "Bad Request";
    public static final String MSG_EMPTY_EMAIL = "E-Mail 주소를 입력하십시오.";

    public static final String DATE_CALCULATION_STANDARD_DYNAMIC = "dynamic";

    public static final String SHOW_THE_TERMS = "Show the terms";

    public static final String STATUS_CHANGED_EXCHANGE_RATE = "CHANGED_EXCHANGE_RATE";

    public static final String REFUND_CHANGED_EXCHANGE_RATE = "REFUND_CHANGED_EXCHANGE_RATE";


    // 간편 로그인 상수
    public static final String STATUS_EXPIRE_TOKEN = "Expire token";
    public static final String STATUS_LOGIN_PASSWORD_5_COUNT_FAIL = "Password 5 Count Fail";
    public static final String STATUS_LOGIN_PIN_CODE_NOT_MATCH = "Pin code does not match";
    public static final String STATUS_LOGIN_NO_TOKEN_INFO = "No token information";

    public static final String STATUS_LOGIN_PIN_CODE_NOT_MATCH_MAX = "Pin code does not max match";

    // 에러코드
    public static final String OPENBANK_NOT_REGISTERED = "Not Registered";
    public static final String OPENBANK_DELETED = "Deleted";

    public static final String STATUS_INVALID_PASSWORD = "INVALID PASSWORD";

    public static final String INVALID_COUNTRY_CODE = "Invalid Country Code";
    public static final String INVALID_DISCOUNT_TYPE_CODE = "Invalid Discount Type Code";

    public static final String STATUS_TRAVELER_INFO_MODIFY_FAIL = "TRAVELER_INFO_MODIFY_FAIL";

    public static final String ALREADY_COMPLETE = "ALREADY_COMPLETE";


    //--------------------------------------------
    // 기타 상수
    //--------------------------------------------

    // 간편 로그인 Expire Days
    public static final int EXPIRE_DAYS = 365;

    public static final int NO_RECORD_ID = -1;

    // 등급을 잘못 입력한 경우 기본 값.
    public static final int NO_GRADE = -1;

    public static final int NO_DATA = -1;


    // field separator 문자열
    public static final String FIELD_SEPARATOR = "'|`";


    // 여권 번호 최소/최대 자리수
    public static final int MIN_PASSPORT_NO_LEN = 7;
    public static final int MAX_PASSPORT_NO_LEN = 9;

    // 가입 최소 연령(만 16세)
    /** 제한 연령 변경 시, 여기서 숫자만 바꿔주면 됨 */
    public static final int AGE_CAN_JOIN = 16; // 홍콩, 한국
    public static final int AGE_CAN_JOIN_JP = 18; // 일본은 만 18세부터 가입 가능


    // true인 경우 FCM에게 메시지 즉시 전송함.
    public static final boolean SEND_MSG_TO_FCT_IMMEDIATELY = true;

    // Long의 기본 값.
    public static final Long DEF_LONG_VALUE = null;

    // Integer의 기본 값.
    public static final Long DEF_INTEGER_VALUE = null;

    // first name, last name의 최소 글자 수
    public static final int MIN_NAME_SIZE = 1;
    public static final int MIN_NAME_SIZE2 = (MIN_NAME_SIZE * 2);

    // 추천인 사용 여부 (2017.04.11 처음 도입)
    public static final boolean USE_RECOMMENDER = false;


    // ref_value 범위 지정.
    public static final int BEGIN_REF_VALUE = 100000;
    public static final int END_REF_VALUE = 999999;
    public static final int REF_VALUE_RANGE = (END_REF_VALUE - BEGIN_REF_VALUE + 1);

    // DB 환전 id 관련 JSON 키값 저장 
    public static final String EXCHANGE_IDS = "exchange_ids";

    // 일, 월, 연간 인출 제한 Status
    // 일, 월, 년 한도에서 제한이 걸림(출금 자체가 불가)
    public static final String MONEY_WITHDRAWAL_LIMIT_STATUS = "Money withdrawal limit";
    // 일, 월, 년 한도에서 제한이 걸림(출금 가능)
    public static final String MONEY_WITHDRAWAL_EXCESS_STATUS = "Money withdrawal excess";
    // 최소, 최대 환전금액 초과 케이스(최대 환전가능한 금액을 받아서 세팅)
    public static final String MONEY_EXCHANGE_LIMIT_STATUS = "Money exchange limit";

    public static final String WALLET_LIMIT_STATUS = "Wallet limit";

    // USER EDD LIMITED
    public static final String USER_EDD_LIMITED_Y = "Y";
    public static final String USER_EDD_LIMIT = "User EDD limit";

    // agreement terms
    public static final String IS_TERMS_AGREE = "Y";

    public static final String N = "N";
    public static final String Y = "Y";

    public static final boolean TRUE = true;
    public static final boolean FALSE = false;

    public static final String SESSION_TIMEOUT_10_MIN = "360000"; // 6분

    // 로그인 실패 횟수
    public static final Integer LOGIN_PASSWORD_MISMATCHES_5 = 5;

    public static final String STATUS_UNREGISTERED_ACCOUNT = "Unregistered account";

    public static final String INSTANCE_ID_HAS_CHANGED = "Instance id has changed";

    public static final String INSTANCE_ID_IS_EMPTY = "Instance id is empty";

    public static final String ACCESS_DENIED = "접근 권한이 없습니다.";

    // 송금 관련 상수
    public static final String REMITTANCE_TYPE_BANK = "REMITTANCE_TYPE_BANK";
    public static final String REMITTANCE_TYPE_WALLET = "REMITTANCE_TYPE_WALLET";
    public static final String REMITTANCE_TYPE_CASH_PICKUP = "REMITTANCE_TYPE_CASH_PICKUP";
    public static final String REMITTANCE_EXCEEDED_LIMIT = "REMITTANCE_EXCEEDED_LIMIT"; // 송금 한도금액 초과

    //쿠폰 관련 상수
    public static final String NO_INPUT_COUPON = "NO_INPUT_COUPON";
    public static final String INVALID_COUPON = "INVALID_COUPON";
    public static final String NO_COUPONS_AVAILABLE = "NO_COUPONS_AVAILABLE";
    public static final String USING_INFLUENCER_COUPON = "USING_INFLUENCER_COUPON";
    public static final String FRIEND_COUPON_ALREADY_REGISTERED = "FRIEND_COUPON_ALREADY_REGISTERED";
    public static final String THIS_COUPON_CANNOT_BE_REGISTERED = "THIS_COUPON_CANNOT_BE_REGISTERED";
    public static final String NOT_FOUND_INVITE_CODE = "NOT_FOUND_INVITE_CODE";
    public static final String CANNOT_ADD_INVITE_COUPON = "CANNOT_ADD_INVITE_COUPON";
    public static final String NOT_REGISTRATION_PERIOD = "NOT_REGISTRATION_PERIOD";

    public static final String WELCOME_COUPON_NAME = "WELCOME_COUPON_NAME";
    public static final String WELCOME_COUPON_DESCRIPTION = "WELCOME_COUPON_DESCRIPTION";
    public static final String BIRTHDAY_COUPON_NAME = "BIRTHDAY_COUPON_NAME";
    public static final String BIRTHDAY_COUPON_DESCRIPTION = "BIRTHDAY_COUPON_DESCRIPTION";
    public static final String THANK_YOU_MY_FRIEND_COUPON_NAME = "THANK_YOU_MY_FRIEND_COUPON_NAME";
    public static final String THANK_YOU_MY_FRIEND_COUPON_DESCRIPTION = "THANK_YOU_MY_FRIEND_COUPON_DESCRIPTION";
    public static final String THANK_YOU_TOO_COUPON_NAME = "THANK_YOU_TOO_COUPON_NAME";
    public static final String THANK_YOU_TOO_COUPON_DESCRIPTION = "THANK_YOU_TOO_COUPON_DESCRIPTION";

    public static final String NOT_EXISTS_COUPON_USER = "쿠폰이 존재하지 않습니다.";
    public static final String COUPON_CANNOT_RESTORE = "쿠폰 원복 오류가 발생하였습니다.";
    public static final String COUPON_CANNOT_UPDATE_EXPIRED = "쿠폰 만료 처리에 실패했습니다.";
    public static final String COUPON_CANNOT_UPDATE_REVOKED = "쿠폰 회수 처리에 실패했습니다.";
    public static final String COUPON_ALREADY_REGISTERED = "Coupon already registered";
    public static final String LINK_TYPE_ALREADY_REGISTERED = "링크 타입이 이미 존재합니다.";

    public static final String ACT = "ACT"; // DBS 송금 종류 중 ACT, DBS내부 송금
    public static final String GPP = "GPP"; // DBS 송금 종류 중 GPP(FPS), 실시간 송금

    public static final String DEEPLINK_PROMOTION_TEXT = "DEEPLINK_PROMOTION_TEXT";

    public static String getAuthFilePath(Long travelerId) {
        return FILE_SERVER_AUTHME + "/" + travelerId;
    }
}
