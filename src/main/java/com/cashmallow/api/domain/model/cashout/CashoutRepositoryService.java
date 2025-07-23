package com.cashmallow.api.domain.model.cashout;

import com.cashmallow.api.application.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class CashoutRepositoryService {

    private final CashOutMapper cashOutMapper;

    private final SecurityService securityService;

    public CashOut getCashOut(Long cashOutId) {
        return cashOutMapper.getCashOut(cashOutId);
    }

    public CashOut getOPCashOut(Long walletId, Long withdrawalPartnerId) {
        return cashOutMapper.getOPCashOut(walletId, withdrawalPartnerId);
    }

    public List<CashOut> getCashOutByExchangeId(String exchangeId, Long travelerId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("exchangeId", exchangeId);
        params.put("travelerId", travelerId);

        return cashOutMapper.getCashOutByExchangeId(params);
    }

    @Transactional
    public int updateCashOut(CashOut cashOut) {
        return cashOutMapper.updateCashOut(cashOut);
    }

    public CashOut getCashOutByQrCodeValue(String country, String coStatus, String qrCodeValue) {
        Map<String, Object> params = new HashMap<>();
        params.put("country", country);
        params.put("coStatus", coStatus);
        params.put("qrCodeValue", qrCodeValue);

        return cashOutMapper.getCashOutByQrCodeValue(params);
    }

    public CashOut getCashOutLastOpByWalletId(long walletId) {
        return cashOutMapper.getCashoutOpByWalletId(walletId);
    }

    public Optional<CashOut> getCashOutByCasmTxnId(String transactionId) {
        return Optional.ofNullable(cashOutMapper.getCashoutByCasmTxnId(transactionId));
    }

    public boolean isCmCustomer(Long travelerId) {
        Timestamp firstCashoutDateByTravelerId = cashOutMapper.getFirstCashoutDateByTravelerId(travelerId);
        // 1. 거래를 1번도 안한경우
        if (firstCashoutDateByTravelerId == null) {
            return true;
        }

        // 2. 2024-12-24 일 이후 최초 거래인 경우 true를 return
        ZonedDateTime kstDateTime = firstCashoutDateByTravelerId.toInstant().atZone(ZoneId.of("Asia/Seoul"));
        ZonedDateTime kstThreshold = ZonedDateTime.of(2024, 12, 24, 0, 0, 0, 0, ZoneId.of("Asia/Seoul"));
        return kstDateTime.isAfter(kstThreshold); // 2024-12-24 이후 거래면 true 반환
    }

    @Transactional
    public int insertCashOutAjOtp(CashOutAjOtp cashOutAjOtp) {
        return cashOutMapper.insertCashOutAjOtp(cashOutAjOtp);
    }

    public CashOutAjOtp getCashOutAjOtpByCashOutId(Long cashOutId) {
        return cashOutMapper.getCashOutAjOtpByCashOutId(cashOutId);
    }

}
