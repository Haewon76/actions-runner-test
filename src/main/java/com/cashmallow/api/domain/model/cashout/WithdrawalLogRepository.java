package com.cashmallow.api.domain.model.cashout;

import com.cashmallow.api.interfaces.scb.model.dto.LogType;
import com.cashmallow.api.interfaces.scb.model.dto.SCBLog;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class WithdrawalLogRepository {

    private final CashOutMapper cashOutMapper;
    private final Gson gson;

    @Transactional
    public void insertWithdrawalLog(String transactionId, LogType requestType, Object object) {
        cashOutMapper.insertWithdrawalLog(
                SCBLog.builder()
                        .transactionId(transactionId)
                        .requestJson(gson.toJson(object))
                        .requestType(requestType)
                        .build()
        );
    }

    @Transactional
    public void insertInboundWithdrawalLog(String transactionId, LogType requestType, Object object, LocalDateTime withdrawalRequestTime) {
        cashOutMapper.insertWithdrawalLog(
                SCBLog.builder()
                        .transactionId(transactionId)
                        .requestJson(gson.toJson(object))
                        .requestType(requestType)
                        .withdrawalRequestTime(withdrawalRequestTime)
                        .build()
        );
    }

    @Transactional
    public void updateWithdrawalLog(String transactionId, int code, String json, LogType requestType) {
        cashOutMapper.updateWithdrawalLog(SCBLog.builder()
                .transactionId(transactionId)
                .code(code)
                .requestType(requestType)
                .responseJson(json)
                .build()
        );
    }

    @Transactional
    public void updateWithdrawalLog(LocalDateTime withdrawalRequestTime, String transactionId, int code, String json, LogType requestType) {
        cashOutMapper.updateWithdrawalLog(SCBLog.builder()
                .transactionId(transactionId)
                .code(code)
                .withdrawalRequestTime(withdrawalRequestTime)
                .requestType(requestType)
                .responseJson(json)
                .build()
        );
    }

    @Transactional
    public void updateConnectionConfirm(String withdrawalRequestNo) {
        cashOutMapper.updateConnectionConfirm(SCBLog.builder()
                .transactionId(withdrawalRequestNo)
                .requestType(LogType.CONFIRM)
                .build()
        );
    }

    public SCBLog getLastInboundSCBLog(String withdrawalRequestNo) {
        List<SCBLog> scbLogs = cashOutMapper.findWithdrawalLogInboundByTransactionId(withdrawalRequestNo);
        if (scbLogs.size() == 0) {
            return null;
        }

        scbLogs.sort((l1, l2) -> l2.getWithdrawalRequestTime().compareTo(l1.getWithdrawalRequestTime()));
        return scbLogs.get(0);
    }
}
