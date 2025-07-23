package com.cashmallow.api.interfaces.devoffice.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.BundleService;
import com.cashmallow.api.application.EmailService;
import com.cashmallow.api.application.impl.MLSCBWebhookService;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.auth.impl.AuthServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.OtpService;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.infrastructure.security.SecurityServiceImpl;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.CryptAES;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.aml.complyadvantage.ComplyAdvantageAmlService;
import com.cashmallow.api.interfaces.authme.AuthMeService;
import com.cashmallow.api.interfaces.coatm.facade.CoatmServiceImpl;
import com.cashmallow.api.interfaces.devoffice.ContingencyServiceImpl;
import com.cashmallow.api.interfaces.devoffice.web.dto.AddPartnerMaintenancesRequest;
import com.cashmallow.api.interfaces.devoffice.web.dto.GetPartnerMaintenancesResponse;
import com.cashmallow.api.interfaces.mallowlink.remittance.dto.BankData;
import com.cashmallow.api.interfaces.mallowlink.withdrawal.MallowlinkWithdrawalServiceImpl;
import com.cashmallow.api.interfaces.paygate.facade.PaygateServiceImpl;
import com.cashmallow.api.interfaces.scb.model.dto.inbound.SCBInboundRequest;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.api.interfaces.statistics.MoneyTransferStatisticsService;
import com.cashmallow.api.interfaces.traveler.web.address.AddressEnglishServiceImpl;
import com.cashmallow.api.interfaces.traveler.web.address.dto.GoogleAddressResultResponse;
import com.cashmallow.common.CommonUtil;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static com.cashmallow.api.application.impl.TravelerServiceImpl.TRAVELER_LOGIN_DEVICE_RESET;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Controller
@Slf4j
@RequestMapping("/devoffice/contingency")
public class ContingencyController {
    private final Logger logger = LoggerFactory.getLogger(ContingencyController.class);

    @Autowired
    private TravelerServiceImpl travelerService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private AuthMeService authMeService;

    @Autowired
    private SevenBankServiceImpl sevenBankService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private ContingencyServiceImpl contingencyService;

    @Autowired
    private EnvUtil envUtil;

    @Autowired
    private Gson gson;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SecurityServiceImpl securityService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private MoneyTransferStatisticsService moneyTransferStatisticsService;

    @Autowired
    private CoatmServiceImpl coatmService;

    @Autowired
    private AddressEnglishServiceImpl englishService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private MLSCBWebhookService mlScbWebhookService;

    @Autowired
    private SevenBankServiceImpl sevenbankService;

    @Autowired
    private PaygateServiceImpl paygateService;

    @Autowired
    private MallowlinkWithdrawalServiceImpl mallowlinkWithdrawalService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private ComplyAdvantageAmlService complyAdvantageAmlService;

    @Autowired
    private BundleService rnBundleService;

    @Value("${cashmallow.homepage.whitelist}")
    private Set<String> homepageWhitelist;

    /**
     * 사용자 토큰을 리턴한다
     * ex) http://localhost:10000/api/devoffice/contingency/user/token?email=tiger002@ruu.kr&password=tiger002!
     *
     * @param email
     * @param password
     * @return
     * @throws CashmallowException
     */
    @GetMapping(value = "/user/token", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public ApiResultVO login(@RequestParam String email,
                             @RequestParam String password) throws CashmallowException {

        ApiResultVO resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);
        String accessToken = null;
        try {
            String userId = email.replaceAll("@", "").replaceAll("\\.", "");
            String refreshToken = travelerService.travelerLogin(userId, password, "fake", null, null, null, null, null, null, null);
            accessToken = authService.issueAccessToken(refreshToken);
            log.info(accessToken);
            resultVO.setSuccessInfo(accessToken);
        } catch (CashmallowException e) {
            if (INTERNAL_SERVER_ERROR.equals(e.getMessage())) {
                logger.error(e.getMessage(), e);
            } else {
                logger.warn(e.getMessage());
            }

            // error message localization

            // 기기 변경시 오류 코드 처리
            if (TRAVELER_LOGIN_DEVICE_RESET.equalsIgnoreCase(e.getMessage())) {
                resultVO.setResult(
                        Const.CODE_FAILURE,
                        e.getMessage(),
                        e.getMessage(),
                        travelerService.getResetDeviceEmailToken(Long.parseLong(e.getOption()))
                );
            } else {
                resultVO.setFailInfo(e.getMessage());
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultVO = new ApiResultVO(Const.CODE_INVALID_PARAMS);
        }

        return resultVO;
    }

    /**
     * 복호화 api
     *
     * @param token       인코딩 할 때 토큰 값
     * @param encodeValue 해당 api의 결과 값
     * @return 복호화 값
     */
    @PostMapping("/decode")
    @ResponseBody
    public String getDecodeValue(@RequestHeader("Authorization") String token,
                                 @RequestBody String encodeValue) {
        log.debug("token={}", token);
        log.debug("encodeValue={}", encodeValue);

        return CustomStringUtil.decode(token, encodeValue);
    }


    @PostMapping("/encode")
    @ResponseBody
    public String getEncodeValue(String token,
                                 @RequestBody String json) {
        log.debug("token={}", token);
        log.debug("jsonBsody={}", json);

        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setSuccessInfo(json);
        return CryptAES.encode(token, json);
    }


    /**
     * 세븐뱅크 인바운드 처리
     *
     * @param bodyStr
     * @return
     * @throws CashmallowException
     */
    @PostMapping(value = "/sevenbank/inbound", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String receiveWithdrawalNotify(@RequestBody Object bodyStr) throws CashmallowException {
        /*
        {
        "func": "NotifyResult",
        "args": {
                "receptionId": "7D6UCBP7JLS0",
                "remittanceId": "3BAE",
                "result": "done",
                "agentCode": "B001",
                "datetime": "20230429213357"
            }
        }
        */
        logger.info("sevenbank/inbound, bodyStr={}", bodyStr);

        JSONObject pJson = new JSONObject(gson.toJson(bodyStr));
        JSONObject data = pJson.getJSONObject("args");

        if (SevenBankServiceImpl.STATUS_DONE.equals(data.getString("result"))) {
            String remittanceId = data.getString("remittanceId");
            contingencyService.forceCashoutOp(remittanceId);
            sevenBankService.completeCashOutSevenBank(remittanceId);
            return "[completeCashOutSevenBank]" + bodyStr;
        } else if (SevenBankServiceImpl.STATUS_EXPIRED.equals(data.getString("result"))) {
            String remittanceId = data.getString("remittanceId");
            Long cashoutId = sevenBankService.makeCashoutId(remittanceId);
            CashOut cashOut = cashoutRepositoryService.getCashOut(cashoutId);
            sevenBankService.cancelCashOutSevenBankByTimeout(cashOut, CashOut.CoStatus.CC.name());
            return "[cancelCashOutSevenBankByTimeout]" + bodyStr;
        }

        return "[ERROR]" + bodyStr;
    }

    @GetMapping(value = "/atms", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public List<WithdrawalPartnerCashpoint> getAtmLocations(
            @RequestParam(required = false, defaultValue = "193") Long withdrawalPartnerId,
            @RequestParam(value = "lat", required = false, defaultValue = "13.7603838") Double lat,
            @RequestParam(value = "lng", required = false, defaultValue = "100.4929992") Double lng
    ) {
        return mallowlinkWithdrawalService.getAtmList(withdrawalPartnerId, lat, lng);
    }

    @GetMapping(value = "/{partner}/banks", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public List<BankData> getBanks(
            @PathVariable(value = "partner") String partner,
            @RequestParam(value = "toCountry", required = false, defaultValue = "JP") String toCountry) throws CashmallowException {
        // todo mallowlink bank와 연결
        return List.of();
    }


    @GetMapping(value = "/coatms", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public List<WithdrawalPartnerCashpoint> getCoAtmLocations(@RequestParam(value = "lat", defaultValue = "37.5059885") String lat,
                                                              @RequestParam(value = "lng", defaultValue = "127.0499224") String lng) throws CashmallowException {
        return coatmService.getAtmList(185L, Double.parseDouble(lat), Double.parseDouble(lng));
    }

    @GetMapping(value = "/email", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String sendEmailTest() throws IOException, MessagingException, ExecutionException, InterruptedException {
        log.info("첨부파일 없는 이메일 발송 시작");
        emailService.sendMail("jd@cashmallow.com", UUID.randomUUID().toString(), "Test", null).get();
        log.info("이메일 발송 완료");
        log.info("----첨부파일 있는 이메일 발송 시작----");

        final File testFile = new File(FileUtils.getTempDirectory() + "/aaa.txt");
        FileUtils.writeStringToFile(testFile, "test", "UTF-8");
        emailService.sendMail("jd@cashmallow.com", UUID.randomUUID().toString(), "Test", testFile).get();
        testFile.delete();
        log.info("이메일 발송 완료");

        return "OK";
    }

    @GetMapping("/password")
    @ResponseBody
    public String password(@RequestParam("password") String password) {
        return securityService.encryptSHA2(password);
    }

    @GetMapping("/otp/valid")
    @ResponseBody
    public boolean addOtpValid(@RequestParam("email") String email,
                               @RequestParam("code") String code) {
        return otpService.isValidOtp(email, code);
    }

    @GetMapping("/otp")
    @ResponseBody
    public String addOtp(@RequestParam("email") String email) {
        return otpService.addOtpKey(email);
    }

    // http://localhost:10000/api/devoffice/contingency/redis/scb/20231010/3
    @PostMapping("/redis/scb/{yyyyMMdd}/{value}")
    @ResponseBody
    public String updateRedisScbIncrementValue(@PathVariable("yyyyMMdd") String yyyyMMdd,
                                               @PathVariable("value") Long value) {
        String key = "SCB:" + yyyyMMdd;
        // 20231010
        // 1 (현재 카운트)

        return redisService.increaseAndGetCount(key, value).toString();
    }


    @GetMapping("/money-transfer-statistics")
    @ResponseBody
    public ResponseEntity<String> addMoneyTransferStatistics(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
                                                             @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
                                                             @RequestParam CountryCode fromCd) {
        while (startDate.isBefore(endDate)) {
            moneyTransferStatisticsService.addMoneyTransferStatistics(fromCd, startDate);
            startDate = startDate.plusDays(1);
        }
        if (startDate.isEqual(endDate)) {
            moneyTransferStatisticsService.addMoneyTransferStatistics(fromCd, startDate);
        }
        return new ResponseEntity<>("success", org.springframework.http.HttpStatus.OK);
    }

    @GetMapping("/address")
    @ResponseBody
    public List<GoogleAddressResultResponse> getAddressByGoole(@RequestParam("address") String address) {
        return englishService.getSearchResultForGlobal(address);
    }

    @GetMapping(value = "/exception")
    public ResponseEntity<String> exceptionTest() {
        log.error("normal ERROR");
        try {
            throw new CashmallowException("WTF?");
        } catch (Exception e) {
            log.info("catch INFO");
            log.debug("catch DEBUG");
            log.error("catch ERROR", e);
        }
        alarmService.e("EXCEPTION", "ERROR");
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("maintenance")
    public ResponseEntity<String> addPartnerMaintenances(@RequestBody AddPartnerMaintenancesRequest request) {

        try {
            partnerService.addPartnerMaintenance(request);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok().body("");
    }

    @PostMapping("scb/inbound/test")
    public ResponseEntity<String> scbInboundTest(@RequestBody SCBInboundRequest request) {

        if (envUtil.isDev()) {
            try {
                mlScbWebhookService.send(request);
                return ResponseEntity.ok().body("");
            } catch (RuntimeException e) {
                log.info(e.getMessage());
            }
        }

        return ResponseEntity.badRequest().body("Not support");
    }

    @GetMapping("maintenance")
    public ResponseEntity<Object> getPartnerMaintenances(@RequestParam String kindOfStorekeeper) {
        try {
            GetPartnerMaintenancesResponse partnerMaintenances = partnerService.getPartnerMaintenances(kindOfStorekeeper);
            return ResponseEntity.ok().body(partnerMaintenances);
        } catch (RuntimeException e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/sevenbank/balance")
    public ResponseEntity<Object> getSevenBankBalance() {
        try {
            return ResponseEntity.ok().body(sevenbankService.getBalance());
        } catch (Exception e) {
            log.info(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/crypto")
    public String crypto() {
        return "crypto/crypto";
    }

    @PostMapping("/crypto/decrypt")
    @ResponseBody
    public String decrypt(@RequestParam String token,
                          @RequestBody String requestJsonBody) {
        String json = CustomStringUtil.decode(token, requestJsonBody);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(json)) {
            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                return gson.toJson(gson.fromJson(json, Object.class));
            } catch (Exception ignored) {
            }
            return json;
        }

        return "복호화 실패";
    }

    @GetMapping("/userId")
    @ResponseBody
    public String getUserId(@RequestParam Long travelerId) {
        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        if (traveler == null) {
            return "존재하지 않는 TravelerId";
        }
        return traveler.getUserId().toString();
    }

    @GetMapping("/requestUrl")
    @ResponseBody
    public String getRequestURL(HttpServletRequest request) {
        return request.getRequestURL().toString();
    }

    @PostMapping("/authme/forceUpdate/{customerId}")
    @ResponseBody
    public String authmeCustomerForceUpdate(@PathVariable String customerId) {
        authMeService.checkTimeoutAndUpdateStatus(customerId);
        return "SUCCESS";
    }

    @PostMapping("/travelers/complyadvantage")
    @ResponseBody
    public String createComplyAdvantageCustomer() {
        try {
            List<Traveler> verifiedTravlerList = travelerRepositoryService.getVerifiedTravelListById();
            for (Traveler traveler : verifiedTravlerList) {
                complyAdvantageAmlService.getComplyAdvantageCustomerId(traveler, traveler.getBirthDate());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        try {
            List<Traveler> verifiedTravlerList = travelerRepositoryService.getVerifiedTravelListByPassport();
            for (Traveler traveler : verifiedTravlerList) {
                complyAdvantageAmlService.getComplyAdvantageCustomerId(traveler, traveler.getBirthDate());
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return "SUCCESS";
    }

    @PostMapping("/admin/bundle")
    @ResponseBody
    public ResponseEntity<Object> registerBundle(@RequestPart("file") MultipartFile bundleFile, HttpServletRequest request) {
        try {
            String method = "configure()";
            log.info("{}", method);
            String userIp = CommonUtil.getRemoteAddr(request);

            // ip가 whitelist에 있는지 확인
            if (!homepageWhitelist.contains(userIp)) {
                logger.error("{}: Not allowed IP address. userIp={}", method, userIp);
                return ResponseEntity.status(403).body("유효하지 않은 IP 주소입니다.");
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

            long result = rnBundleService.registerBundle(bundleFile, request, -1L);

            if (result > 0) {
                return ResponseEntity.ok().body("Bundle 등록에 성공하였습니다.");
            }
            return ResponseEntity.status(500).body("Bundle 등록에 실패하였습니다.");
        }
        catch (Exception e) {
            return ResponseEntity.status(505).body(e.getMessage());
        }
    }
}