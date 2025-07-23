package com.cashmallow.api.domain.model.company;

import com.cashmallow.api.interfaces.global.dto.Deposit;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PaygateRecord {

    public enum WorkStatus {
        // 입금기준 매핑전, 출금기준 환불 전
        READY,
        // 반환대상
        REPAYMENT,
        // 입금 없음. 출금기준 이체 진행중
        PROCESSING,
        // 입금기준 매핑완료, 출금기준 이체완료
        COMPLETED
    }

    private String id;
    private String country;
    private Long bankAccountId;
    private String iso4217;
    private String depWdrType;
    private BigDecimal amount;

    private String description;
    private String senderName;
    private String senderBank;
    private String senderAccountNo;
    private Timestamp executedDate;
    private Timestamp createdDate;
    private String txnStatus;
    private WorkStatus workStatus;
    private String exchangeTxnId;
    private String depositType;
    private BigDecimal balance;
    private String sourceId;


    public static PaygateRecord of(String countryCode, String currency, Long bankAccountId, Deposit deposit, BigDecimal balance) {
        PaygateRecord paygateRecord = new PaygateRecord();
        paygateRecord.setCountry(countryCode);
        paygateRecord.setBankAccountId(bankAccountId);

        paygateRecord.setId(deposit.depositId());
        paygateRecord.setIso4217(currency);
        paygateRecord.setDepWdrType("DEPOSIT");
        paygateRecord.setAmount(deposit.amount());
        paygateRecord.setDescription(deposit.description());
        paygateRecord.setSenderName(deposit.senderName());
        paygateRecord.setSenderBank(deposit.senderBank());
        paygateRecord.setSenderAccountNo(deposit.senderAccountNo());
        paygateRecord.setExecutedDate(Timestamp.from(deposit.depositTime().toInstant()));
        paygateRecord.setCreatedDate(Timestamp.valueOf(LocalDateTime.now()));
        paygateRecord.setTxnStatus("WAITING");
        paygateRecord.setWorkStatus(WorkStatus.READY);
        paygateRecord.setBalance(balance);

        return paygateRecord;
    }
}
