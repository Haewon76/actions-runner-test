package com.cashmallow.api.domain.model.withdrawalpartnercalc;

import java.util.List;
import java.util.Map;

public interface WithdrawalPartnerCalcMapper {

    WithdrawalPartnerCalc getWithdrawalPartnerCalc(Long WithdrawalPartnerCalcId);

    List<WithdrawalPartnerCalc> getWithdrawalPartnerCalcListByWithdrawalPartnerId(Long WithdrawalPartnerId);

    List<WithdrawalPartnerCalc> getWithdrawalPartnerCalcOpListByWithdrawalPartnerId(Long WithdrawalPartnerId);

    List<Object> getWithdrawalPartnerFeeCalcInFeeCalc(Map<String, String> params);

    List<Object> getWithdrawalPartnerCalcP2Info(Map<String, String> params);

    /**
     * Get payback amount statistics by country
     *
     * @param params : country
     * @return : totalCnt, totalCashOutAmt, totalFee, totalPaymentAmt totalTotal,
     * comCnt, comCashOutAmt, comFee, comPaymentAmt, comTotal,
     * reqCnt, reqCashOutAmt, reqFee, reqPaymentAmt, reqTotal,
     * canCnt, canCashOutAmt, canFee, canPaymentAmt, canTotal
     */
    Map<String, Object> getPaybackAmountByCountry(String country);

    int updateWithdrawalPartnerCalc(WithdrawalPartnerCalc withdrawalPartnerCalc);

}
