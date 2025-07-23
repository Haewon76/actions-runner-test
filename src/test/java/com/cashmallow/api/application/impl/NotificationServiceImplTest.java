package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.exchange.ExchangeRepositoryService;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.remittance.RemittanceRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.common.JsonUtil;
import com.cashmallow.config.EnableDevLocal;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@SpringBootTest
@Transactional
@EnableDevLocal
@Slf4j
class NotificationServiceImplTest {
    @Autowired
    private NotificationServiceImpl notificationService;

    @Autowired
    private JsonUtil jsonUtil;

    @Autowired
    AlarmService alarmService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Autowired
    private CountryServiceImpl countryService;

    @Autowired
    private ExchangeRepositoryService exchangeRepositoryService;

    @Autowired
    private RemittanceRepositoryService remittanceRepositoryService;

    @Autowired
    private AuthMeService authMeService;

    @Test
    @Disabled
    public void 기기인증_초기화_이메일_테스트_진행() {
        final String resetDeviceConfirmView = notificationService.getResetDeviceConfirmView(false, null);
        log.info("resetDeviceConfirmView: {}", resetDeviceConfirmView);
    }

    @Test
    @Disabled
    public void 환불_명세서_이메일전송_테스트() throws CashmallowException {
        User user = userRepositoryService.getUserByUserId(993046L);
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(993046L);
        NewRefund refund = refundRepositoryService.getNewRefundById(15175L);
        Country fromCountry = countryService.getCountry(refund.getFromCd());
        Country toCountry = countryService.getCountry(refund.getToCd());

        notificationService.sendEmailConfirmNewRefundForExchange(user, traveler, refund, fromCountry, toCountry);
    }

    @Test
    @Disabled
    public void 환전_완료_명세서_발송_테스트() throws CashmallowException {
        User user = userRepositoryService.getUserByUserId(993046L);
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(993046L);
        Exchange exchange = exchangeRepositoryService.getExchangeByExchangeId(39261L);
        Country fromCountry = countryService.getCountry(exchange.getFromCd());
        Country toCountry = countryService.getCountry(exchange.getToCd());

        notificationService.sendEmailConfirmExchange(user, traveler, exchange, fromCountry, toCountry);
    }

    @Test
    @Disabled
    public void 송금_완료_명세서_발송_테스트() throws CashmallowException {
        User user = userRepositoryService.getUserByUserId(993275L);
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(993275L);
        Remittance remittance = remittanceRepositoryService.getRemittanceByRemittanceId(14365L);
        Country fromCountry = countryService.getCountry(remittance.getFromCd());
        Country toCountry = countryService.getCountry(remittance.getToCd());

        notificationService.sendEmailConfirmRemittance(user, traveler, remittance, fromCountry, toCountry);
    }

    @Disabled
    @Test
    public void 약관발송테스트() {
        Stream.of(1019978, 1028085).map(Integer::longValue).forEach(id -> {
            User user = userRepositoryService.getUserByUserId(id);
            notificationService.sendEmailPrivacyPolicy(user);
        });
    }
}