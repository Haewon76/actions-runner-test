package com.cashmallow.api.interfaces.bank;

import com.cashmallow.api.domain.model.bankinfo.BankInfo;
import com.cashmallow.api.domain.model.bankinfo.BankInfoMapper;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.interfaces.bank.dto.BankInfoVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BankServiceImpl {

    @Value("${host.cdn.url}")
    private String cdnUrl;

    private final BankInfoMapper bankInfoMapper;

    private final Map<CountryInfo, List<BankInfoVO>> bankInfoMap = new EnumMap<>(CountryInfo.class);
    private final Map<String, String> bankIconMap = new HashMap<>();
    private final List<CountryInfo> bankCountries = List.of(CountryInfo.KR, CountryInfo.HK, CountryInfo.ID, CountryInfo.MO);

    @PostConstruct
    public void init() {
        for (var bankCountry : bankCountries) {
            List<BankInfo> bankInfos = bankInfoMapper.getBankInfoByIso3166(bankCountry.name());
            bankInfoMap.put(bankCountry, bankInfos.stream().map(bankInfo -> BankInfoVO.of(bankInfo, cdnUrl)).collect(Collectors.toList()));

            for (BankInfo bankInfo : bankInfos) {
                bankIconMap.put(bankInfo.getName(), cdnUrl + "/static" + bankInfo.getIconPath());
            }
        }
    }


    public List<BankInfoVO> getBankInfos(CountryInfo iso3166, BigDecimal amount) {
        if (iso3166.equals(CountryInfo.ID)) {
            if (amount.compareTo(BigDecimal.valueOf(500_000_000)) < 0) {
                return bankInfoMap.get(iso3166).stream().filter(i -> StringUtils.isNotBlank(i.getCode())).collect(Collectors.toList());
            } else {
                return bankInfoMap.get(iso3166).stream().filter(i -> StringUtils.isNotBlank(i.getBniClearingCode())).collect(Collectors.toList());
            }
        }
        return bankInfoMap.get(iso3166);
    }

    public BankInfo getHkBankInfoByName(String bankName) {
        return bankInfoMapper.getBankInfoHkByName(bankName);
    }

    public BankInfo getBankInfoById(long id) {
        return bankInfoMapper.getBankInfoById(id);
    }

    public Map<String, String> getBankIconMap() {
        return bankIconMap;
    }

}
