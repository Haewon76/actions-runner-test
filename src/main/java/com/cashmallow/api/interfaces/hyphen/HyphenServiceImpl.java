package com.cashmallow.api.interfaces.hyphen;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.ExchangeServiceImpl;
import com.cashmallow.api.application.impl.TravelerServiceImpl;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.enums.CertificationType;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.api.infrastructure.RedisService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.api.interfaces.hyphen.dto.*;
import com.cashmallow.common.EnvUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import static com.cashmallow.api.infrastructure.RedisService.REDIS_KEY_HYPHEN;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class HyphenServiceImpl {

    @Value("${hyphen.url}")
    private String hyphenUrl;
    @Value("${hyphen.user-id}")
    private String hyphenId;
    @Value("${hyphen.hKey}")
    private String hKey;

    private final TravelerServiceImpl travelerService;
    private final TravelerRepositoryService travelerRepositoryService;
    private final HyphenClient hyphenClient;
    private final RedisService redisService;
    private final ExchangeServiceImpl exchangeService;
    private final MessageSource messageSource;
    private final AlarmService alarmService;

    private final Gson gsonPretty;
    private final Gson gson;

    private final EnvUtil envUtil;

    private final String HYPHEN_REDIS_KEY = "hyphen_redis_key";
    private final String BANK_ACCOUNT_INFO = "BankAccountInfo";
    private final String BANK_ACCOUNT_COUNT = "BankAccountCount";

    private final int MAX_REQUEST_COUNT = 5;


    /**
     * 주민등록증 진위 확인
     *
     * @param name      이름
     * @param juminNo   주민등록번호(- 없이)
     * @param issueDate 발행 일자
     * @return
     */
    public boolean checkIDCard(String name, String juminNo, String issueDate) {

        String method = "checkIDCard()";

        Header[] headers = new Header[3];
        headers[0] = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader("user-id", hyphenId);
        headers[2] = new BasicHeader("Hkey", hKey);
        // headers[3] = new BasicHeader("hyphen-gustation", "Y");

        JSONObject body = new JSONObject();

        body.put("ownerNm", name);
        body.put("juminNo", juminNo);
        body.put("issueDt", issueDate);

        String responseStringBody = httpPost(method, body, hyphenUrl + "/in0005000233", headers);

        JSONObject json = new JSONObject(responseStringBody);

        if (!json.getString("resCd").equals("0000")) {
            log.info("resMsg:{}", json.getString("resMsg"));
            throw new RuntimeException("CHECK_ID_CARD_FAIL");
        }

        JSONObject data = json.getJSONObject("data");

        if (!data.getString("truthYn").equalsIgnoreCase("Y")) {
            String truthMsg = data.getString("truthMsg");
            log.info("truthMsg: {}", truthMsg);
            return false;
        }
        return true;
    }

    /**
     * 운전면허증 진위 확인
     *
     * @param name      이름
     * @param birthDay  생년월일 6자리
     * @param licenceNo 면허증 번호
     * @param serialNo  면허증 시리얼
     * @return
     */
    public boolean checkDriverLicence(String name, String birthDay, String licenceNo, String serialNo) {

        String method = "checkDriverLicence()";

        Header[] headers = new Header[3];
        headers[0] = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader("user-id", hyphenId);
        headers[2] = new BasicHeader("Hkey", hKey);
        // headers[3] = new BasicHeader("hyphen-gustation", "Y");

        String[] licence = new String[4];
        licence[0] = licenceNo.substring(0, 2);
        licence[1] = licenceNo.substring(2, 4);
        licence[2] = licenceNo.substring(4, 10);
        licence[3] = licenceNo.substring(10, 12);

        JSONObject body = new JSONObject();
        body.put("ownerNm", name);
        body.put("juminNo", birthDay);
        body.put("licence01", licence[0]);
        body.put("licence02", licence[1]);
        body.put("licence03", licence[2]);
        body.put("licence04", licence[3]);
        body.put("serialNo", serialNo);

        String responseStringBody = httpPost(method, body, hyphenUrl + "/in0072000230", headers);

        JSONObject json = new JSONObject(responseStringBody);
        if (!json.getString("resCd").equals("0000")) {
            log.info("resMsg:{}", json.getString("resMsg"));
            throw new RuntimeException("CHECK_DRIVER_LICENCE_FAIL");
        }

        JSONObject out = json.getJSONObject("out");
        JSONObject outB0001 = out.getJSONObject("outB0001");

        boolean serialNoTruthYn = outB0001.getString("serialNoTruthYn").equalsIgnoreCase("Y");
        boolean licenceTruthYn = outB0001.getString("licenceTruthYn").equalsIgnoreCase("Y");

        JSONObject data = json.getJSONObject("data");

        if (!serialNoTruthYn || !licenceTruthYn) {
            String licenceTruthMsg = data.getString("licenceTruthMsg");
            String serialNoTruthMsg = data.getString("serialNoTruthMsg");
            log.info("licenceTruthMsg:{}", licenceTruthMsg);
            log.info("serialNoTruthMsg:{}", serialNoTruthMsg);
            return false;
        }
        return true;
    }

    /**
     * 외국인등록증 진위 확인
     *
     * @param name        이름
     * @param foreignerNo 외국인 등록 번호
     * @param issueDate   발행 일자
     * @return
     */
    public boolean checkResidenceCard(String name, String foreignerNo, String issueDate) {

        String method = "checkResidenceCard()";

        Header[] headers = new Header[3];
        headers[0] = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader("user-id", hyphenId);
        headers[2] = new BasicHeader("Hkey", hKey);
        // headers[3] = new BasicHeader("hyphen-gustation", "Y");

        JSONObject body = new JSONObject();
        body.put("foreignerNo", foreignerNo);
        body.put("issueDt", issueDate);

        String responseStringBody = httpPost(method, body, hyphenUrl + "/in0074000236", headers);

        JSONObject json = new JSONObject(responseStringBody);

        JSONObject common = json.getJSONObject("common");
        if (common.getString("errYn").equals("N")) {
            log.info("errMsg:{}", common.getString("errMsg"));
            throw new RuntimeException("CHECK_RESIDENCE_CARD_FAIL");
        }

        JSONObject data = json.getJSONObject("data");
        if (!data.getString("truthYn").equalsIgnoreCase("Y")) {
            String truthMsg = data.getString("truthMsg");
            log.info("truthMsg:{}", truthMsg);
            return false;
        }
        return true;
    }

    /**
     * 1원 인증 1/2, 1원 보내기
     *
     * @param travelerId
     * @param bankAccountInfoVo
     * @return ApiResultVO
     */
    @Transactional(readOnly = false, rollbackFor = CashmallowException.class)
    public ApiResultVO checkAccount1(long travelerId, BankAccountInfoVo bankAccountInfoVo) {
        ApiResultVO voResult = new ApiResultVO();

        String accountCountKey = redisService.generateRedisKey(BANK_ACCOUNT_COUNT, String.valueOf(travelerId), REDIS_KEY_HYPHEN);
        String bankAccountCountStr = redisService.get(accountCountKey);
        int bankAccountCount = bankAccountCountStr == null ? 0 : Integer.parseInt(bankAccountCountStr);
        if (bankAccountCount > MAX_REQUEST_COUNT) {
            log.info("계좌 인증 시도 횟수 초과 travelerId:{}", travelerId);
            Locale locale = LocaleContextHolder.getLocale();
            String accountVerificationLimit = messageSource.getMessage("ACCOUNT_VERIFICATION_LIMIT", new Integer[]{MAX_REQUEST_COUNT}, locale);
            voResult.setFailInfo(accountVerificationLimit);
        }

        String bankCode = bankAccountInfoVo.getBankCode();
        String accountNumber = bankAccountInfoVo.getAccountNumber();
        CheckAccount1VO.Request request = new CheckAccount1VO.Request(bankCode, accountNumber);

        CheckAccount1VO.Response response;
        if (envUtil.isPrd()) {
            response = hyphenClient.checkAccount1(request);

            if (!response.getSuccessYn().equalsIgnoreCase("Y")) {
                log.error(gsonPretty.toJson(response));
                voResult.setFailInfo("SEND_1WON_FAIL");
                return voResult;
            }
        } else {
            Random random = new Random();
            response = new CheckAccount1VO.Response(
                    "0000",
                    "Y",
                    "",
                    "1234",
                    String.format("%03d", random.nextInt(1000)),
                    "",
                    String.valueOf(random.nextInt(10000)),
                    ""
            );
            alarmService.i("하이픈 계좌인증", gsonPretty.toJson(response));
        }

        String redisKey = redisService.generateRedisKey(BANK_ACCOUNT_INFO, String.valueOf(travelerId), REDIS_KEY_HYPHEN);
        BankCertificationInfo bankCertificationInfo = new BankCertificationInfo(bankAccountInfoVo, response);
        redisService.put(redisKey, gson.toJson(bankCertificationInfo), 5);
        redisService.put(accountCountKey, String.valueOf(bankAccountCount + 1), 60 * 23);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("oriSeqNo", response.getOriSeqNo());
        resultMap.put("accountName", bankAccountInfoVo.getAccountName());
        voResult.setSuccessInfo(resultMap);
        return voResult;
    }

    /**
     * 고객이 입력한 적요를 검사하여 계좌 인증 완료.
     *
     * @param oriSeqNo     인증 검증 번호(1 Tr에서 얻은 값)
     * @param printContent 인증 고객 입력 적요 (ex 파란하늘)
     * @return
     */
    public boolean checkAccount2(String oriSeqNo, String printContent) {
        String method = "checkAccount2()";

        CheckAccount2VO.Request request = new CheckAccount2VO.Request(oriSeqNo, printContent);
        CheckAccount2VO.Response response = hyphenClient.checkAccount2(request);
        log.debug("{}", response);

        return response.getSuccessYn().equalsIgnoreCase("Y");
    }

    public boolean notValidPrintContent(String oriSeqNo, String printContent) {
        return !checkAccount2(oriSeqNo, printContent);
    }


    @Transactional(readOnly = false, rollbackFor = CashmallowException.class)
    public ApiResultVO registerBankAccount(long travelerId, String oriSeqNo, String printContent) {
        ApiResultVO resultVO = new ApiResultVO();

        String redisKey = redisService.generateRedisKey(BANK_ACCOUNT_INFO, String.valueOf(travelerId), REDIS_KEY_HYPHEN);
        BankCertificationInfo bankCertificationInfo = gson.fromJson(redisService.get(redisKey), BankCertificationInfo.class);

        try {
            // 계좌 OTP 검증
            checkPrintContent(bankCertificationInfo, oriSeqNo, printContent);

            // 계좌 등록
            Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
            BankAccountInfoVo bankAccountInfoVo = bankCertificationInfo.getBankAccountInfoVo();
            traveler.setAccountName(bankAccountInfoVo.getAccountName());
            traveler.setAccountNo(bankAccountInfoVo.getAccountNumber());
            traveler.setBankCode(bankAccountInfoVo.getBankCode());
            traveler.setBankName(bankAccountInfoVo.getBankName());
            traveler.setAccountOk("N");
            traveler = travelerService.updateBankAccount(traveler, null);

            // 계좌 승인
            if (!traveler.getAccountOk().equals("Y")
                    || Boolean.TRUE.equals(exchangeService.isPossibleToCancelBankBookverified(travelerId))) {
                travelerService.verifyBankAccountByAdmin(traveler, "Y", null);
                resultVO.setSuccessInfo();
            }
        } catch (CashmallowException e) {
            log.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return resultVO;
    }

    private void checkPrintContent(BankCertificationInfo bankCertificationInfo, String oriSeqNo, String printContent) throws CashmallowException {
        if (bankCertificationInfo == null || bankCertificationInfo.getResponse() == null || bankCertificationInfo.getBankAccountInfoVo() == null) {
            throw new CashmallowException(MsgCode.INTERNAL_SERVER_ERROR);
        }
        if (!bankCertificationInfo.getResponse().getOriSeqNo().equals(oriSeqNo)) {
            throw new CashmallowException("WRONG_ORI_SEQ_NO");
        }
        if (!StringUtils.equals(bankCertificationInfo.getResponse().getInPrintContent(), printContent)) {
            throw new CashmallowException("WRONG_PRINT_CONTENT");
        }
    }

    /**
     * 여행자의 이름과 계좌주 확인
     *
     * @param travelerId
     * @param bankCode
     * @param accountNumber
     * @return 일치하면 여행자의 이름 반환. 일치하지 않으면 빈스트링.
     */
    public String checkFCS(long travelerId, String bankCode, String accountNumber) {

        String method = "checkFCS()";

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(travelerId);
        if (traveler == null) {
            log.info("{}: Traveler id null", method);
            throw new RuntimeException("TRAVELER_CANNOT_FIND");
        }
        String localName = traveler.getLocalLastName() + traveler.getLocalFirstName();

        FCS.request request = new FCS.request(bankCode, accountNumber);

        FCS.response response = hyphenClient.postFCS(request);

        String name = response.getName();
        String reply = response.getReply();
        String reply_msg = response.getReplyMsg();

        if (!reply.equals("0000")) {
            log.info("{}: reply_msg={}", method, reply_msg);
            throw new RuntimeException("WRONG_ACCOUNT_INFO");
        }

        if (!localName.equals(name)) {
            throw new RuntimeException("MISMATCHED_TRAVELER_NAME_AND_ACCOUNT_NAME");
        }
        return name;
    }

    private String httpPost(String method, JSONObject body, String apiUrl, Header[] headers) {

        String result = "";
        String bodyStr = body.toString();
        byte[] bodyByte = bodyStr.getBytes(StandardCharsets.UTF_8);

        log.info("{}: body={}", method, body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // API url
            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(bodyByte);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            log.info("{}: stateCode={} ", method, resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                log.error("{}: failure (reason={}) ", method, resSL.getReasonPhrase());
                throw new RuntimeException(resSL.getReasonPhrase());
            }

            log.info("{}: success", method);

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            log.info("{}: entity.toString()={}", method, resBody);

            result = resBody;
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        return result;
    }

    public boolean certification(CertificationReqVO certificationReqVO) {
        final String localName = certificationReqVO.getLocalName();
        final String identificationNumber = certificationReqVO.getIdentificationNumber();
        final String issueDate = certificationReqVO.getIssueDate();
        final String birthDay = certificationReqVO.getBirthDay();
        final String licenceNo = certificationReqVO.getLicenceNo();
        final String serialNo = certificationReqVO.getSerialNo();

        CertificationType certificationType = certificationReqVO.getCertificationType();
        switch (certificationType) {
            case ID_CARD:
                return checkIDCard(localName, identificationNumber, issueDate);
            case DRIVER_LICENSE:
                return checkDriverLicence(localName, birthDay, licenceNo, serialNo);
            case RESIDENCE_CARD:
                return checkResidenceCard(localName, identificationNumber, issueDate);
            default:
                throw new RuntimeException("INVALID_CERTIFICATION_TYPE");
        }
    }
}
