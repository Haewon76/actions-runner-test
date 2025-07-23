package com.cashmallow.api.interfaces.dbs.model.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DbsDepositRecordRequest {
    @NotEmpty
    private String transactionId;
    @NotEmpty
    private String currency;
    @NotNull
    private BigDecimal amount;
    private String senderAccountNo;
    private String senderName;
    private String depositType;
    @NotNull
    private LocalDateTime executedDate;

    @Override
    public String toString() {
        return "DbsDepositRecordRequest{" +
                "transactionId='" + transactionId + '\'' +
                ", currency='" + currency + '\'' +
                ", amount=" + amount +
                ", senderAccountNo='*" + '\'' +
                ", senderName='*" + '\'' +
                ", depositType='" + depositType + '\'' +
                ", executedDate=" + executedDate +
                '}';
    }
}
