package com.cashmallow.api.interfaces.hyphen.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class CheckAccount1VO {

    @Getter
    @ToString
    static public class Request {
        private final String inBankCode;
        private final String inAccount;

        public Request(String inBankCode, String inAccount) {
            this.inBankCode = inBankCode;
            this.inAccount = inAccount;
        }
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    static public class Response {
        private String replyCode;
        private String successYn;
        private String sign;
        private String tradeTime;
        private String inPrintContent;
        private String svcCharge;
        private String oriSeqNo;
        private String tr_date;
    }
}
