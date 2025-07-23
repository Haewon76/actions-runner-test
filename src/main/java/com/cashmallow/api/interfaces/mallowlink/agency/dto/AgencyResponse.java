package com.cashmallow.api.interfaces.mallowlink.agency.dto;

public record AgencyResponse(
        String name,
        String km,
        String address,
        String latitude,
        String longitude,
        String locationId,
        String iconPath
) {

}
