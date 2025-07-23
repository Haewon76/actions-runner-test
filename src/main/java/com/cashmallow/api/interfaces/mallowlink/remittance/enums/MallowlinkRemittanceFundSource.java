package com.cashmallow.api.interfaces.mallowlink.remittance.enums;

import lombok.AllArgsConstructor;

import java.util.Arrays;

@AllArgsConstructor
public enum MallowlinkRemittanceFundSource {
    SALARY("WORKINCOME"), // 근로/연금/퇴직 소득
    BUSINESS_EARNINGS("BUSINESSINCOME"), // 사업/부동산 소득
    INHERITANCE("GIFTINCOME"), // 상속/증여
    REAL_ESTATE_TRANSFER_INCOME("OWNPROPERTYDISPOAL"), // 본인소유 재산처분 대금
    FINANCIAL_INCOME("INTERESTINCOME"); // 이자, 배당 소득

    private final String cashamllowFundSource;

    public static MallowlinkRemittanceFundSource of(String cashamllowFundSource) {
        return Arrays.stream(MallowlinkRemittanceFundSource.values())
                .filter(e -> e.cashamllowFundSource.equals(cashamllowFundSource))
                .findAny()
                .orElseThrow(IllegalArgumentException::new);
    }
}