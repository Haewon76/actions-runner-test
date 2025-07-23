package com.cashmallow.api.domain.model.remittance.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RemittanceRelationship {
    PARENTS("REMITTANCE_RELATIONSHIP_PARENT", "Parents"), // 부모
    CHILD("REMITTANCE_RELATIONSHIP_CHILD", "Child"), // 자식
    SIBLING("REMITTANCE_RELATIONSHIP_SIBLING", "Sibling/Brother/Sister"), // 형제
    SPOUSE("REMITTANCE_RELATIONSHIP_SPOUSE", "Spouse"), // 배우자
    EX_SPOUSE("REMITTANCE_RELATIONSHIP_EX_SPOUSE", "Ex-Spouse"), // 전-배우자
    RELATIVE("REMITTANCE_RELATIONSHIP_RELATIVE", "Relative/Uncle/Auntie/Cousin"), // 친척
    FRIENDS("REMITTANCE_RELATIONSHIP_FRIENDS", "Friends"), // 친구
    SELF("REMITTANCE_RELATIONSHIP_SELF", "Self"), // 본인
    GRANDPARENTS("REMITTANCE_RELATIONSHIP_GRANDPARENTS", "Grandparents"), // 조부모
    ACQUAINTANCE("REMITTANCE_RELATIONSHIP_ACQUAINTANCE", "Acquaintance"), // 지인
    CLIENT("REMITTANCE_RELATIONSHIP_CLIENT", "Client/Customer"), // 거래처
    REPRESENTATIVE("REMITTANCE_RELATIONSHIP_REPRESENTATIVE", "Representative"), // 대표자
    SUBSIDIARY_COMPANY("REMITTANCE_RELATIONSHIP_SUBSIDIARY_COMPANY", "Subsidiary Company"), // 자회사
    BRANCH("REMITTANCE_RELATIONSHIP_BRANCH", "Branch"), // 지점
    CREDITOR("REMITTANCE_RELATIONSHIP_CREDITOR", "Creditor"), // 채권자
    DEBTORS("REMITTANCE_RELATIONSHIP_DEBTORS", "Debtors"), // 채무자
    COLLEAGUE("REMITTANCE_RELATIONSHIP_COLLEAGUE", "Colleague"), // 동료
    EMPLOYEE("REMITTANCE_RELATIONSHIP_EMPLOYEE", "Employee"), // 종업원
    FIANCE("REMITTANCE_RELATIONSHIP_FIANCE", "Fiance"), // 약혼자
    NON_RELATED("REMITTANCE_RELATIONSHIP_NON_RELATED", "Non Related"); // 관계없음

    private String messageCode;
    private String defaultMessage;
}