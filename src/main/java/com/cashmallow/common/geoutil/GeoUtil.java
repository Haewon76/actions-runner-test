package com.cashmallow.common.geoutil;

import com.cashmallow.api.domain.model.geo.GeoLocation;
import net.renfei.ip2location.IP2Location;
import net.renfei.ip2location.IPResult;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;


public interface GeoUtil {

    IP2Location loc = new IP2Location();

    void init() throws IOException;

    default String getMyLocation(String address) {
        final GeoLocation myCountryCode = getMyCountryCode(address);
        String location = "Unknown Location";
        if (myCountryCode != null && !"-".equals(myCountryCode.getCity()) && StringUtils.isNotBlank(myCountryCode.getCountryLong())) {
            if (myCountryCode.getCity().equals(myCountryCode.getRegion())) {
                location = myCountryCode.getRegion() + ", " + myCountryCode.getCountryLong();
            } else {
                location = myCountryCode.getCity() + ", " + myCountryCode.getRegion() + ", " + myCountryCode.getCountryLong();
            }
        }

        return location;
    }

    default GeoLocation getMyCountryCode(String address) {
        String errorMessage = "";
        try {
            String ip = getIpAddress(address);
            IPResult rec = loc.IPQuery(ip);
            if ("OK".equals(rec.getStatus())) {
                return new GeoLocation(ip, rec);
            } else if ("EMPTY_IP_ADDRESS".equals(rec.getStatus())) {
                errorMessage = "IP address cannot be blank.";
            } else if ("INVALID_IP_ADDRESS".equals(rec.getStatus())) {
                errorMessage = "Invalid IP address.";
            } else if ("MISSING_FILE".equals(rec.getStatus())) {
                errorMessage = "Invalid database path.";
            } else if ("IPV6_NOT_SUPPORTED".equals(rec.getStatus())) {
                errorMessage = "This BIN does not contain IPv6 data.";
            } else {
                errorMessage = "Unknown error." + rec.getStatus();
            }
        } catch (Exception e) {
            errorMessage = "Exception error." + e.getMessage();
        } finally {
            // loc.Close();
        }

        return new GeoLocation(address, errorMessage);
    }

    default String getIpAddress(String address) {
        String ip = address;
        if (address.indexOf("/") != -1) {
            ip = address.trim().substring(0, address.indexOf("/"));
        }
        return ip;
    }
}