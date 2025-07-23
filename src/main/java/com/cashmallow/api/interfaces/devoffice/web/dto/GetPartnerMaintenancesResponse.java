package com.cashmallow.api.interfaces.devoffice.web.dto;

import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerMaintenance;
import lombok.Data;

import java.util.List;

@Data
public class GetPartnerMaintenancesResponse {

    private final String kindOfStorekeeper;
    private final String shopName;
    private final List<WithdrawalPartnerMaintenance> partnerMaintenances;

    public static GetPartnerMaintenancesResponse of(WithdrawalPartner withdrawalPartner, List<WithdrawalPartnerMaintenance> partnerMaintenances) {
        return new GetPartnerMaintenancesResponse(
                withdrawalPartner.getKindOfStorekeeper(),
                withdrawalPartner.getShopName(),
                partnerMaintenances);
    }
}
