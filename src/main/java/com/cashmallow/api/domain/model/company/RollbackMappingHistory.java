package com.cashmallow.api.domain.model.company;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class RollbackMappingHistory {
    private Long id;
    private String relatedTxnType;
    private Long relatedTxnId;
    private String iso4217;
    private BigDecimal amount;
    private String description;
    private Timestamp createdDate;
    private Timestamp updatedDate;
    private Long creator;
    private String fundingStatus;
    private String paygateRecordIdList;

    public RollbackMappingHistory(TransactionRecord transactionRecord, String paygateRecordIdList) {
        this.relatedTxnType = transactionRecord.getRelatedTxnType();
        this.relatedTxnId = transactionRecord.getRelatedTxnId();
        this.iso4217 = transactionRecord.getIso4217();
        this.amount = transactionRecord.getAmount();
        this.description = transactionRecord.getDescription();
        this.createdDate = transactionRecord.getCreatedDate();
        this.updatedDate = transactionRecord.getUpdatedDate();
        this.creator = transactionRecord.getCreator();
        this.fundingStatus = transactionRecord.getFundingStatus();
        this.paygateRecordIdList = paygateRecordIdList;
    }

}
