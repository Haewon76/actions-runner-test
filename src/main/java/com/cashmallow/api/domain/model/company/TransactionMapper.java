package com.cashmallow.api.domain.model.company;

import java.util.List;
import java.util.Map;

public interface TransactionMapper {

    /**
     * Find Cashmallow's Transaction List
     *
     * @param params
     * @return
     */
    List<TransactionRecord> getTransactionRecordsList(Map<String, String> params);

    TransactionRecord getTransactionRecord(Map<String, Object> params);

    List<TransactionRecord> getTransactionRecordForPaygateRecord(String paygateRecordId);

    List<TransactionRecord> getTransactionRecordListByIdList(Map<String, Object> params);

    List<TransactionMapping> getTransactionMappingListByPaygateRecordId(String paygateRecordId);

    List<TransactionMapping> getTransactionMappingListByTransactionRecordId(Long transactionRecordId);

    TransactionRecord getTransactionRecordByTransactionRecordId(Long transactionRecordId);

    int countDuplicateTransactionRecord(TransactionRecord transactionRecord);

    int countTransactionMappingByPaygateRecordId(String paygateRecordId);

    int insertTransactionRecord(TransactionRecord transactionRecord);

    int updateTransactionRecord(TransactionRecord transactionRecord);

    int deleteTransactionRecord(TransactionRecord transactionRecord);

    int insertTransactionMapping(TransactionMapping transactionMapping);

    int updateTransactionMapping(TransactionMapping transactionMapping);

    int deleteTransactionMapping(Long transactionRecId);

    int insertRollbackMappingHistory(RollbackMappingHistory rollbackMappingHistory);
}
