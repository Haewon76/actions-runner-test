package com.cashmallow.api.interfaces.mallowlink.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@AllArgsConstructor
@Getter
public enum MallowlinkExceptionType {
    // 에러 코드는 4자리로 구성되며,
    // 1번째 자리는 에러 레벨, 2~4번째 자리는 에러 코드로 구성된다.
    // 공통
    SUCCESS("0000", "SUCCESS"),  // 성공시 기본값
    FAIL("1001", "FAIL"),  // 실패시 기본값
    INTERNAL_SERVER_ERROR("1002", "Internal Server Error"),   // 일반적인 에러 발생시
    UNSUPPORTED_API("1003", "Unsupported Operation"), // 지원되지 않는 API
    INVALID_CLIENT("1004", "Invalid Client"),   // Client 값이 일치하지 않는 경우
    INVALID_REQUEST_EXCEPTION("1007", "Invalid Request Exception"),   // 필수 파라미터 정보가 입력되지 않은 경우
    INVALID_PARAMETER_EXCEPTION("1005", "Invalid Parameter Exception"),   // 필수 파라미터 정보가 입력되지 않은 경우
    DEPOSIT_EXCEED_AMOUNT_EXCEPTION("1006", "Exceed Amount Exception"), // 클라이언트 디파짓 잔액 부족

    USER_NOT_FOUND("2001", "User not found"), // 가입된 엔드유저가 없는 경우
    USER_ALREADY_REGISTERED("2002", "User already registered"),   // 가입된 엔드유저가 존재 하는 경우

    TRANSACTION_DUPLICATE("3001", "Duplicate transaction"), // 중복되는 트랙잭션ID
    TRANSACTION_ALREADY_COMPLETED("3002", "Already Completed."),   // 이미 처리 된 상태
    TRANSACTION_ALREADY_CANCELED("3003", "Already Canceled."),   // 이미 취소 된 상태
    TRANSACTION_ALREADY_REVERTED("3004", "Already Reverted."),   // 이미 리버트 처리 된 상태
    TRANSACTION_NOT_FOUND("3005", "Transaction Not Found"),   // 출금 정보를 찾을 수 없습니다
    TRANSACTION_IN_PROGRESS("3006", "In progress.."),   // 진행중인 트랜젝션
    TRANSACTION_USER_EXCEED_AMOUNT("3007", "Daily exchange limit exceeded."), // 엔드유저 인출 가능한도 초과 오류 (QBC 케이스)

    WITHDRAWAL_INVALID_QR("4001", "Invalid QR"), // 잘못된 QR 코드

    REMITTANCE_INVALID_RECEIVER_ACCOUNT("5001", "Invalid Receiver Account"), // 잘못된 수취인 계좌번호

    MAINTENANCE_TIME("6001", "Partner is under maintenance"), // 파트너 유지보수 시간 오류 코드 추가
    WLF_USER("6002", "Please check the watch list"), // Watch List에 등록된 사용자가 검색됨

    PARTNER_SERVER_RESPONSE_ERROR("9999", "Partner Server response Error"),   // 인출 파트너사 장애 상태
    INVALID_ATM("9998", "Invalid ATM Machine"),   // 사용할 수 없는 ATM 기기
    ;

    private final String code;
    private final String message;

    public static MallowlinkExceptionType byCode(String code) {
        return Arrays.stream(MallowlinkExceptionType.values())
                .filter(e -> e.getCode().equals(code))
                .findAny()
                .orElse(MallowlinkExceptionType.INTERNAL_SERVER_ERROR);
    }

}
