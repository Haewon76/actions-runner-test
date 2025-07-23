package com.cashmallow.api.domain.model.company;

import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsRemittanceRequest;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DbsRemittance {
    public enum ResultStatus {
        REJECT,
        PROCESSING,
        COMPLETED
    }

    private String id;
    private Long userId;
    private String remittanceType;
    private String currency;
    private BigDecimal amount;
    private String receiverName;
    private String receiverAccountNo;
    private String receiverBankName;
    private String receiverHongKongBankCode;
    private String receiverSwiftBankCode;
    private String responseCode;
    private ResultStatus resultStatus;
    private Long managerId;
    private TransactionRecord.RelatedTxnType relatedTxnType;
    private Long relatedTxnId;

    public static DbsRemittance of(DbsRemittanceRequest dbsRemittanceRequest, Long userId, String dbsTxnId, String responseCode,
                                   Long managerId, TransactionRecord.RelatedTxnType relatedTxnType, Long relatedTxnId) {
        DbsRemittance returnValue = new DbsRemittance();

        returnValue.setId(dbsTxnId);
        returnValue.setUserId(userId);
        returnValue.setRemittanceType(dbsRemittanceRequest.getRemittanceType());
        returnValue.setCurrency(dbsRemittanceRequest.getCurrency());
        returnValue.setAmount(dbsRemittanceRequest.getAmount());
        returnValue.setReceiverName(dbsRemittanceRequest.getReceiverName());
        returnValue.setReceiverAccountNo(dbsRemittanceRequest.getReceiverAccountNo());
        returnValue.setReceiverBankName(dbsRemittanceRequest.getReceiverBankName());
        returnValue.setReceiverHongKongBankCode(dbsRemittanceRequest.getReceiverHongKongBankCode());
        returnValue.setReceiverSwiftBankCode(dbsRemittanceRequest.getReceiverSwiftBankCode());
        returnValue.setResponseCode(responseCode);
        returnValue.setManagerId(managerId);
        returnValue.setRelatedTxnType(relatedTxnType);
        returnValue.setRelatedTxnId(relatedTxnId);
        if (dbsRemittanceRequest.getRemittanceType().equals(Const.GPP)) {
            returnValue.setResultStatus(ResultStatus.COMPLETED);
        } else {
            returnValue.setResultStatus(ResultStatus.PROCESSING);
        }

        return returnValue;
    }

}
