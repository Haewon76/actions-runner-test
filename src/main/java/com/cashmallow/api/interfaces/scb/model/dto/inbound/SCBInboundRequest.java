package com.cashmallow.api.interfaces.scb.model.dto.inbound;

import com.cashmallow.common.CommDateTime;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
{
    "func": "ConfirmWithdrawal",
    "args": {
        "receptionId": "CASM0000012345",
        "TransactionId": "2022112500001",
        "result": "done",
        "datetime": "2022-06-28T17:38:00+07.00"
    }
}
*/
@Data
public class SCBInboundRequest {
    private String func;
    private SCBInboundRequestArg args;
    private String correlationId;
    private String apiKey;

    public LocalDateTime getWithdrawalRequestTime() {
        return CommDateTime.localDateTimeToUTCDateTime(args.getDatetime());
    }

    public int getConfirmCode() {
        return "ConfirmWithdrawal".equalsIgnoreCase(func) ? 200 : 404;
    }

    public String getValidation() {
        List<String> errorFields = new ArrayList<>();
        if (StringUtils.isEmpty(func)) {
            errorFields.add("func");
        }
        if (args == null) {
            errorFields.add("args");
        } else {
            //            if(StringUtils.isEmpty(args.getDatetime())) {
            //                errorFields.add("datetime");
            //            }
            //            if(StringUtils.isEmpty(args.getReceptionId())) {
            //                errorFields.add("receptionId");
            //            }
            if (StringUtils.isEmpty(args.getTransactionId())) {
                errorFields.add("TransactionId");
            }
            if (StringUtils.isEmpty(args.getReceptionId())) {
                errorFields.add("receptionId");
            }
        }

        return errorFields.stream().collect(Collectors.joining(","));
    }

}
