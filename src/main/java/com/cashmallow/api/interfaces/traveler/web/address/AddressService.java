package com.cashmallow.api.interfaces.traveler.web.address;

import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.api.interfaces.traveler.web.address.dto.JusoResponse;

import java.util.List;

public interface AddressService {

    List<JusoResponse> getAddress(String keyword);

    List<GoogleAddressResultResponse> getSearchResultForGlobal(String address);

    String getUrl(String keyword);

}
