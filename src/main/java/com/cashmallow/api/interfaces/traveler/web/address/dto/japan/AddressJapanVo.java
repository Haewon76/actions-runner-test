package com.cashmallow.api.interfaces.traveler.web.address.dto.japan;

public record AddressJapanVo(
        int code,
        AddressJapanVoData data

) {

    public boolean isSuccess() {
        return code == 200;
    }

    public record AddressJapanVoData(
            String pref,
            String address,
            String city,
            String town,
            String fullAddress
    ) {
    }
}