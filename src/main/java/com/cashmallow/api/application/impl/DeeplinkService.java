package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.coupon.entity.Coupon;
import com.cashmallow.api.domain.model.deeplink.DeeplinkDto;
import com.cashmallow.api.domain.model.deeplink.DeeplinkStat;
import com.cashmallow.api.domain.model.user.DeeplinkMapper;
import com.cashmallow.common.CommonUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.cashmallow.api.domain.shared.Const.DEEPLINK_PROMOTION_TEXT;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeeplinkService {

    private final DeeplinkMapper deeplinkMapper;
    private final Gson gson;
    private final MessageSource messageSource;

    @Value("${deeplink.url}")
    private String deeplinkUrl;

    /**
     * 다이나믹 링크를 생성하고, DB에 click event insert 처리
     *
     * @param dto
     * @param request
     * @return
     */
    public String click(DeeplinkDto dto, HttpServletRequest request) throws UnsupportedEncodingException {
        dto
                .setUuid(UUID.randomUUID().toString())
                .setIp(CommonUtil.getRemoteAddr(request))
                .setUserAgent(CommonUtil.getUserAgent(request))
                .setOsType(CommonUtil.getOsType(request));
        addClickEvent(dto);

        Locale locale = request.getLocale();
        String text = messageSource.getMessage(DEEPLINK_PROMOTION_TEXT, null, "No ATM Fees Abroad, \nSwap Currencies in Seconds!", locale);
        if (text == null) {
            text = "No ATM Fees Abroad, \nSwap Currencies in Seconds!";
        }
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);

        return deeplinkUrl + encoded + String.format("&si=%s&utm_campaign=%s&utm_medium=%s&utm_source=%s", dto.getThumbnail(), dto.getUuid(), dto.getUtmMedium(), dto.getUtmSource());
    }

    /**
     * DB에 click event insert 처리
     *
     * @param dto
     */
    private void addClickEvent(DeeplinkDto dto) {
        deeplinkMapper.addClickEvent(dto);
    }

    /**
     * 앱 설치 시 event insert 처리
     *
     * @param json
     * @param request
     */
    public void install(String json, HttpServletRequest request) {
        /**
         {
         "uuid": "bdbe6c0d-8a8e-4005-97e9-035336c59609",
         }
         **/
        DeeplinkDto dto = gson.fromJson(json, DeeplinkDto.class)
                .setIp(CommonUtil.getRemoteAddr(request))
                .setUserAgent(CommonUtil.getUserAgent(request))
                .setOsType(CommonUtil.getOsType(request));
        deeplinkMapper.addInstallEvent(dto);
    }

    /**
     * 로그인시 event insert 처리
     *
     * @param deeplink
     * @param request
     */
    public void login(DeeplinkDto deeplink, HttpServletRequest request) {
        /**
         {
         "uuid": "123123123",
         }
         **/
        deeplink.setIp(CommonUtil.getRemoteAddr(request))
                .setUserAgent(CommonUtil.getUserAgent(request))
                .setOsType(CommonUtil.getOsType(request));

        deeplinkMapper.addLogin(deeplink);
    }

    /**
     * 가입시 event insert 처리
     *
     * @param deeplink
     * @param request
     */
    public void register(DeeplinkDto deeplink, HttpServletRequest request) {
        /**
         {
         "uuid": "123123123",
         }
         **/
        deeplink.setIp(CommonUtil.getRemoteAddr(request))
                .setUserAgent(CommonUtil.getUserAgent(request))
                .setOsType(CommonUtil.getOsType(request));

        deeplinkMapper.addRegister(deeplink);
    }

    public List<DeeplinkStat> getDeeplinkStat(String startDate, String endDate, HttpServletRequest request) {
        return deeplinkMapper.getDeeplinkStat(
                new DeeplinkStat()
                        .setStartDate(startDate)
                        .setEndDate(endDate));
    }
}
