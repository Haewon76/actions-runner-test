package com.cashmallow.api.domain.model.remittance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RemittanceFundSource {
    WORKINCOME("WORKINCOME", "給料、年金、退職所得"), // 근로/연금/퇴직 소득
    BUSINESSINCOME("BUSINESSINCOME", "事業、収益"), // 사업/부동산 소득
    GIFTINCOME("GIFTINCOME", "相続、贈与"), // 상속/증여
    OWNPROPERTYDISPOAL("OWNPROPERTYDISPOAL", "自己所有財産処分代金"), // 본인소유 재산처분 대금
    INTERESTINCOME("INTERESTINCOME", "利子、配当所得"); // 이자, 배당 소득

    private String enDecription;
    private String jpDecription;
}