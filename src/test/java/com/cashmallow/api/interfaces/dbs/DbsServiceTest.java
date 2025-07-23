package com.cashmallow.api.interfaces.dbs;

import com.cashmallow.api.application.impl.CompanyServiceImpl;
import com.cashmallow.api.domain.model.company.PaygateRecord;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.refund.RefundRepositoryService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.AdminDbsRemittanceAskVO;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import com.cashmallow.api.interfaces.dbs.client.DbsClient;
import com.cashmallow.api.interfaces.dbs.model.dto.DbsBalanceResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@Slf4j
@SpringBootTest
@Disabled
public class DbsServiceTest {
    @Autowired
    private DbsClient dbsClient;
    @Autowired
    private DbsService dbsService;
    @Autowired
    private TravelerRepositoryService travelerRepositoryService;
    @Autowired
    private CompanyServiceImpl companyService;
    @Autowired
    private RefundRepositoryService refundRepositoryService;

    @Test
    void 잔액체크_테스트() {
        // String json = "{\"json\":\"1\"}";
        //
        // String re =  abstractJsonString(json);
        // String nomal = abstractJsonString("일반 메세지");
        // log.info(re);
        // log.info(nomal);

        final String errorMessage = dbsService.getDbsFxQuotationApprove(51L, 120L);
        if (StringUtils.isEmpty(errorMessage)) {
            log.info("suc");
        } else {
            log.info(errorMessage);
        }

    }

    @Test
    void 자동환불_테스트() throws CashmallowException {
        NewRefund refund = refundRepositoryService.getNewRefundById(13340L);
        dbsService.tryAutoRefund(refund);
    }

    @Test
    void healthCheck() {
        String result = dbsClient.checkHealth();
        log.info(result);
    }

    @Test
    void DBS_API_송금_테스트() throws CashmallowException {
        Traveler traveler = travelerRepositoryService.getTravelerByUserId(946718L);
        PaygateRecord pr = companyService.getPaygateRecord("TEMP9622231");
        Long managerUserId = 51L;
        dbsService.requestRemittance(traveler, pr, new BigDecimal("44.19"), managerUserId, null, null);
    }

    @Test
    void DBS_API_잔액조회_테스트() {
        DbsBalanceResponse response =  dbsService.getDbsAccountBalance(51L);
        log.info(response.toString());
    }

    @Test
    void DBS_REMITTANCE_내역_조회_테스트() {
        AdminDbsRemittanceAskVO pvo = new AdminDbsRemittanceAskVO();
        SearchResultVO result = companyService.getAdminDbsRemittance(pvo);
        log.info(result.toString());
    }

}
