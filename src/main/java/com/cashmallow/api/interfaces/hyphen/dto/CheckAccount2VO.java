package com.cashmallow.api.interfaces.hyphen.dto;

public class CheckAccount2VO {
    static public class Request {
        private String oriSeqNo;
        private String inPrintContent;

        public Request() {
        }

        public Request(String oriSeqNo, String inPrintContent) {
            this.oriSeqNo = oriSeqNo;
            this.inPrintContent = inPrintContent;
        }

        public String getOriSeqNo() {
            return oriSeqNo;
        }

        public String getInPrintContent() {
            return inPrintContent;
        }
    }

    static public class Response {
        private String successYn;
        private String error;

        public Response() {
        }

        public String getSuccessYn() {
            return successYn;
        }

        public String getError() {
            return error;
        }

        @Override
        public String toString() {
            return "Response{" +
                    "successYn='" + successYn + '\'' +
                    ", error='" + error + '\'' +
                    '}';
        }
    }
}
