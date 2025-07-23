package com.cashmallow.api.domain.model.company;

import com.cashmallow.api.interfaces.admin.dto.BankAccountVO;

import java.util.List;
import java.util.Map;

public interface CompanyMapper {

    /**
     * Count for findBankAccount
     *
     * @param params
     * @return
     */
    Integer countBankAccount(Map<String, Object> params);

    /**
     * Find Cashmallow's Bank Account list
     *
     * @param params
     * @return
     */
    List<BankAccount> findBankAccount(Map<String, Object> params);


    /**
     * 국가와 은행코드로 계좌 목록 조회
     *
     * @param countryCode
     * @param bankCode
     * @return
     */
    List<BankAccount> findBankAccountByCountryCodeAndBankCode(String countryCode, String bankCode);

    /**
     * Get Cashmallow's bank account information by bankAccountId
     *
     * @param bankAccountId
     * @return
     */
    BankAccount getBankAccountByBankAccountId(Integer bankAccountId);

    int getCompanyIdByCountry(Map<String, Object> params);

    int putBankAccount(BankAccountVO pvo);

    int insertRepaymentHistory(RepaymentHistory repaymentHistory);
}
