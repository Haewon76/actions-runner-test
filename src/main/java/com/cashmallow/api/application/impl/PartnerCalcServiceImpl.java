package com.cashmallow.api.application.impl;

import com.cashmallow.api.domain.model.withdrawalpartnercalc.WithdrawalPartnerCalc;
import com.cashmallow.api.domain.model.withdrawalpartnercalc.WithdrawalPartnerCalcMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class PartnerCalcServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(PartnerCalcServiceImpl.class);

    @Autowired
    private WithdrawalPartnerCalcMapper withdrawalPartnerCalcMapper;


    //-------------------------------------------------------------------------------
    // 50. 가맹점 정산
    //-------------------------------------------------------------------------------

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public WithdrawalPartnerCalc getWithdrawalPartnerCalc(Long withdrawalPartnerCalcId) {
        return withdrawalPartnerCalcMapper.getWithdrawalPartnerCalc(withdrawalPartnerCalcId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartnerCalc> getWithdrawalPartnerCalcOpListByWithdrawalPartnerId(Long withdrawalPartnerId) {
        return withdrawalPartnerCalcMapper.getWithdrawalPartnerCalcOpListByWithdrawalPartnerId(withdrawalPartnerId);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartnerCalc> getWithdrawalPartnerCalcListByWithdrawalPartnerId(Long withdrawalPartnerId) {
        return withdrawalPartnerCalcMapper.getWithdrawalPartnerCalcListByWithdrawalPartnerId(withdrawalPartnerId);
    }

    @Transactional
    public int updateWithdrawalPartnerCalc(WithdrawalPartnerCalc withdrawalPartnerCalc) {
        return withdrawalPartnerCalcMapper.updateWithdrawalPartnerCalc(withdrawalPartnerCalc);
    }


    /**
     * Get payback amount statistics by country
     *
     * @param params : country
     * @return : totalCnt, totalCashOutAmt, totalFee, totalPaymentAmt totalTotal,
     * comCnt, comCashOutAmt, comFee, comPaymentAmt, comTotal,
     * reqCnt, reqCashOutAmt, reqFee, reqPaymentAmt, reqTotal,
     * canCnt, canCashOutAmt, canFee, canPaymentAmt, canTotal
     */
    public Map<String, Object> getPaybackAmountByCountry(String country) {
        return withdrawalPartnerCalcMapper.getPaybackAmountByCountry(country);
    }
}
