package com.cashmallow.api.interfaces.hyphen.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public class FCS {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class request {
        private String bank_cd;
        private String acct_no;

        public request() {
        }

        public request(String bank_cd, String acct_no) {
            this.bank_cd = bank_cd;
            this.acct_no = acct_no;
        }

        public String getBank_cd() {
            return bank_cd;
        }

        public String getAcct_no() {
            return acct_no;
        }

        @Override
        public String toString() {
            return "request{" +
                    "bank_cd='" + bank_cd + '\'' +
                    ", acct_no='" + acct_no + '\'' +
                    '}';
        }
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class response {

        private String name;
        private String reply;
        private String replyMsg;

        public response() {
        }

        public response(String name, String reply, String replyMsg) {
            this.name = name;
            this.reply = reply;
            this.replyMsg = replyMsg;
        }

        public String getName() {
            return name;
        }

        public String getReply() {
            return reply;
        }

        public String getReplyMsg() {
            return replyMsg;
        }

        @Override
        public String toString() {
            return "response{" +
                    "name='" + name + '\'' +
                    ", reply='" + reply + '\'' +
                    ", replyMsg='" + replyMsg + '\'' +
                    '}';
        }
    }
}
