package com.cashmallow.api.domain.model.partner;

import com.cashmallow.api.interfaces.mallowlink.agency.dto.AgencyResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class WithdrawalPartnerCashpoint {

    public enum Location {
        /**
         * The stores in a city
         */
        CITY,
        /**
         * The stores in an airport
         */
        AIRPORT
    }

    public enum CashpointType {
        ATM,
        STORE
    }

    private Long id;
    private Long withdrawalPartnerId;
    private String cashpointType;
    private String location;
    private String partnerCashpointId;
    private String partnerCashpointName;
    private String partnerCashpointAddr;
    private Double partnerCashpointLat;
    private Double partnerCashpointLng;
    private String cashOutHours;
    private String about;
    private String iconImagePath;
    private String defaultIconImagePath;
    private Long modifier;
    private Timestamp modifiedDate;


    // todo Deprecated된 메소드들을 APP에서 모델로 사용 중.
    @Deprecated
    public Long getStorekeeperId() {
        return withdrawalPartnerId;
    }

    @Deprecated
    public String getAtmNo() {
        return partnerCashpointId;
    }

    @Deprecated
    public String getAtmName() {
        return partnerCashpointName;
    }

    @Deprecated
    public String getAtmAddr() {
        return partnerCashpointAddr;
    }

    @Deprecated
    public Double getAtmLat() {
        return partnerCashpointLat;
    }

    @Deprecated
    public Double getAtmLng() {
        return partnerCashpointLng;
    }

    public static WithdrawalPartnerCashpoint of(AgencyResponse agencyResponse, Long storekeeperId) {

        WithdrawalPartnerCashpoint data = new WithdrawalPartnerCashpoint();

        data.setPartnerCashpointId(agencyResponse.locationId());
        data.setPartnerCashpointName(agencyResponse.name());
        data.setPartnerCashpointAddr(agencyResponse.address());
        data.setPartnerCashpointLat(Double.valueOf(agencyResponse.latitude()));
        data.setPartnerCashpointLng(Double.valueOf(agencyResponse.longitude()));
        data.setDefaultIconImagePath(agencyResponse.iconPath());
        data.setIconImagePath(agencyResponse.iconPath());
        data.setWithdrawalPartnerId(storekeeperId);

        return data;
    }

}
