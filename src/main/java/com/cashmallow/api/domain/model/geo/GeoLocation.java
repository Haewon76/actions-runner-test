package com.cashmallow.api.domain.model.geo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import net.renfei.ip2location.IPResult;
import org.apache.commons.lang3.StringUtils;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeoLocation {
    private String errorMessage;
    private String ip;
    private String countryShort;
    private String countryLong;
    private String region;
    private String city;
    private Double latitude;
    private Double longitude;
    private String zipcode;
    private String timezone;

    public GeoLocation(String ipAddress, IPResult rec) {
        this.ip = ipAddress;
        if (rec != null) {
            this.countryShort = rec.getCountryShort();
            this.countryLong = rec.getCountryLong();
            this.region = rec.getRegion();
            this.city = rec.getCity();
            this.latitude = Double.parseDouble(String.valueOf(rec.getLatitude()));
            this.longitude = Double.parseDouble(String.valueOf(rec.getLongitude()));
            this.zipcode = rec.getZipCode();
            this.timezone = rec.getTimeZone();
        }
    }

    public GeoLocation(String ipAddress, String errorMessage) {
        this.ip = ipAddress;
        this.errorMessage = errorMessage;
    }

    public String getTimezoneToZoneId() {
        if (StringUtils.isEmpty(timezone) || "-".equalsIgnoreCase(timezone)) {
            return "-00:00";
        }
        return timezone;
    }
}
