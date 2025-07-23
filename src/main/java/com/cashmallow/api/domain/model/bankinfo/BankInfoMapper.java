package com.cashmallow.api.domain.model.bankinfo;

import java.util.List;

public interface BankInfoMapper {

    List<BankInfo> getBankInfoByIso3166(String iso3166);

    BankInfo getBankInfoKrByCode(String bankCode);

    BankInfo getBankInfoHkByName(String bankName);
    BankInfo getBankInfoById(long id);
}
