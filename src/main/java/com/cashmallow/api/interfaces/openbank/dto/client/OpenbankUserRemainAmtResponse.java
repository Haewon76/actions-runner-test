package com.cashmallow.api.interfaces.openbank.dto.client;

import lombok.Data;

@Data
public class OpenbankUserRemainAmtResponse {
    private final String api_tran_id;
    private final String api_tran_dtm;
    private final String rsp_code;
    private final String rsp_message;
    private final Long day_wd_limit_amt;    // 일간 출금한도금액(사용자 단위)
    private final Long day_wd_amt;          // 당일 출금이체 금액(누적)
    private final Long wd_limit_remain_amt; // 출금한도 잔여금액(일간출금한도금액–당일출금이체금액)
}
