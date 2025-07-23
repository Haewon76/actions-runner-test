package com.cashmallow.api.domain.model.cashout;

import com.cashmallow.api.interfaces.admin.dto.AdminCashOutAskVO;
import com.cashmallow.api.interfaces.admin.dto.AdminCashOutVO;
import com.cashmallow.api.interfaces.scb.model.dto.SCBLog;
import org.apache.ibatis.annotations.Select;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;


public interface CashOutMapper {

    CashOut getCashOut(Long cashOutId);
    CashOut getOPCashOut(Long walletId, Long withdrawalPartnerId);

    List<CashOut> getCashOutByExchangeId(Map<String, Object> params);

    CashOut getCashOutByQrCodeValue(Map<String, Object> params);

    List<CashOut> getCashOutOpListByTravelerId(Long travelerId);

    List<CashOut> getCashOutOpListByWithdrawalPartnerId(Long withdrawalPartnerId);

    /**
     * Find Cashout
     *
     * @param params (travelerId, withdrawalPartnerId, coStatus:"('OP', 'CF', 'PO', 'PF')", cashoutReservedDate(LIKE), flightArrivalDate(LIKE))
     * @return
     */
    List<CashOut> findCashOutList(Map<String, Object> params);

    /**
     * Get cash-out amount statistics by country
     *
     * @param params : country, beginDate("yyyy-MM-dd HH:mm:ss"), endDate("yyyy-MM-dd HH:mm:ss")
     * @return : totalCnt, totalAmt, totalFee, totalTotal,
     * comCnt, comAmt, comFee, comTotal,
     * reqCnt, reqAmt, reqFee, reqTotal,
     * canCnt, canAmt, canFee, canTotal
     */
    Map<String, Object> getCashOutAmountByCountry(Map<String, Object> params);

    /**
     * Get payment amount statistics by country
     *
     * @param params : country, beginDate("yyyy-MM-dd HH:mm:ss"), endDate("yyyy-MM-dd HH:mm:ss")
     * @return : totalCnt, totalAmt, totalFee, totalTotal,
     * comCnt, comAmt, comFee, comTotal,
     * reqCnt, reqAmt, reqFee, reqTotal,
     * canCnt, canAmt, canFee, canTotal
     */
    Map<String, Object> getPaymentAmountByCountry(Map<String, Object> params);

    Map<String, Object> isCalcualtingCashOut(Map<String, Object> params);

    int insertCashOut(CashOut cashOut);

    int updateCashOut(CashOut cashOut);

    int countAdminCashOut(AdminCashOutAskVO pvo);

    List<AdminCashOutVO> findAdminCashOut(AdminCashOutAskVO pvo);

    CashOut getCashoutOpByWalletId(long walletId);

    CashOut getCashoutByCasmTxnId(String CasmTxnId);

    Timestamp getFirstCashoutDateByTravelerId(Long travelerId);

    void insertWithdrawalLog(SCBLog scbLog);

    void updateWithdrawalLog(SCBLog scbLog);

    List<SCBLog> findWithdrawalLogInboundByTransactionId(String TransactionId);

    void updateConnectionConfirm(SCBLog scbLog);

    List<PendingBalance> getPendingBalance(String start, String end);

    List<PendingBalanceDetailVo> getPendingBalanceDetails(String start, String end);

    @Select(" select iso_4217 from country group by iso_4217 ")
    List<String> getISO4217s();

    int insertCashOutAjOtp(CashOutAjOtp cashOutAjOtp);

    CashOutAjOtp getCashOutAjOtpByCashOutId(Long cashOutId);
}
