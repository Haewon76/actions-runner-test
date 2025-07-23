package com.cashmallow.api.interfaces.mallowlink.remittance.enums;

import com.cashmallow.api.domain.model.traveler.enums.CertificationType;

public enum IdCardType {
    ID, PASSPORT;

    public static IdCardType of(CertificationType certificationType) {
        return switch (certificationType) {
            case PASSPORT -> PASSPORT;
            // From JP시 인증 수단이 무엇이든 결국 일본의 my-Number 카드의 번호가 넘어갈 것이기 때문에, ML-API에서는 ID로 통일해서 처리
            case ID_CARD, RESIDENCE_CARD, DRIVER_LICENSE -> ID;
            default -> throw new IllegalStateException("Unexpected value: " + certificationType);
        };
    }
}
