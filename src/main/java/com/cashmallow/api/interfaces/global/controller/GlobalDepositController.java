package com.cashmallow.api.interfaces.global.controller;

import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.global.dto.InsertDepositBulkRequest;
import com.cashmallow.api.interfaces.global.dto.InsertDepositRequest;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("global")
@RequiredArgsConstructor
public class GlobalDepositController {

    private final PaygateServiceImpl paygateService;

    @PostMapping("/jp/deposits")
    public void insertDepositJp(@RequestBody InsertDepositRequest request) throws CashmallowException {
        log.debug("request:{}", request);

        if (!request.country().equals(CountryCode.JP) || !request.currency().equalsIgnoreCase(CountryCode.JP.getCurrency())) {
            throw new CashmallowException("Country code must be JP And Currency must be JPY");
        }
        paygateService.insertDeposit(request);
    }

    @PostMapping(value = "/jp/deposits", params = "type=bulk")
    public void insertDepositBulkJp(@RequestBody InsertDepositBulkRequest request) throws CashmallowException {
        log.debug("request:{}", request);

        if (!request.country().equals(CountryCode.JP) || !request.currency().equalsIgnoreCase(CountryCode.JP.getCurrency())) {
            throw new CashmallowException("Country code must be JP And Currency must be JPY");
        }
        paygateService.insertDepositBulk(request);
    }

    @PostMapping("/kr/deposits")
    public void insertDepositKr(@RequestBody InsertDepositRequest request) throws CashmallowException {
        log.debug("request:{}", request);

        if (!request.country().equals(CountryCode.KR) || !request.currency().equalsIgnoreCase(CountryCode.KR.getCurrency())) {
            throw new CashmallowException("Country code must be KR And Currency must be KRW");
        }
        paygateService.insertDeposit(request);
    }

    @PostMapping(value = "/kr/deposits", params = "type=bulk")
    public void insertDepositBulkKr(@RequestBody InsertDepositBulkRequest request) throws CashmallowException {
        log.debug("request:{}", request);

        if (!request.country().equals(CountryCode.KR) || !request.currency().equalsIgnoreCase(CountryCode.KR.getCurrency())) {
            throw new CashmallowException("Country code must be KR And Currency must be KRW");
        }
        paygateService.insertDepositBulk(request);
    }

}
