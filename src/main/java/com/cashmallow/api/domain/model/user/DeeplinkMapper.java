package com.cashmallow.api.domain.model.user;

import com.cashmallow.api.domain.model.deeplink.DeeplinkDto;
import com.cashmallow.api.domain.model.deeplink.DeeplinkStat;

import java.util.List;

public interface DeeplinkMapper {

    List<DeeplinkStat> getDeeplinkStat(DeeplinkStat dto);

    void addClickEvent(DeeplinkDto dto);

    void addInstallEvent(DeeplinkDto dto);

    void addLogin(DeeplinkDto dto);

    void addRegister(DeeplinkDto dto);
}
