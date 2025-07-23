package com.cashmallow.api.interfaces.bank;

import com.cashmallow.api.auth.AuthService;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.bank.dto.BankInfoResVO;
import com.cashmallow.api.interfaces.bank.dto.BankInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BankController {


    private final BankServiceImpl bankService;
    private final AuthService authService;

    @GetMapping("/banks")
    public ResponseEntity<ApiResultVO> getBankLists(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String token,
            @RequestParam CountryInfo iso3166,
            @RequestParam(required = false) BigDecimal amount) {

        long userId = authService.getUserId(token);
        if (userId == Const.NO_USER_ID && !authService.isHexaStr(token)) {
            log.info("Invalid token");
            return ResponseEntity.badRequest().body(null);
        }

        if (iso3166.equals(CountryInfo.ID) && amount == null) {
            String msg = "조회 국가(iso3166) 인도네시아(ID)이면 금액(amount)가 필수 입니다.";
            log.error(msg);
            ApiResultVO errorResult = new ApiResultVO(Const.CODE_INVALID_PARAMS);
            errorResult.setMessage(msg);
            return ResponseEntity.badRequest().body(errorResult);
        }

        List<BankInfoVO> moreBankInfo = new ArrayList<>();
        if (iso3166.equals(CountryInfo.HK)) {
            moreBankInfo = bankService.getBankInfos(CountryInfo.MO, amount);
        }
        // swallowCopy시 원본에 영향이 가서 DeepCopy
        List<BankInfoVO> bankInfos = bankService.getBankInfos(iso3166, amount).stream().map(BankInfoVO::new).collect(Collectors.toList());
        bankInfos.addAll(moreBankInfo);

        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setSuccessInfo(new BankInfoResVO(bankInfos));
        return ResponseEntity.ok().body(apiResultVO);
    }
}
