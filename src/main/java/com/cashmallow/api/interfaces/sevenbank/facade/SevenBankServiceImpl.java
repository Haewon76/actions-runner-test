package com.cashmallow.api.interfaces.sevenbank.facade;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.NotificationService;
import com.cashmallow.api.application.impl.CashOutServiceImpl;
import com.cashmallow.api.application.impl.PartnerServiceImpl;
import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashOut.CoStatus;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.model.partner.WithdrawalPartner;
import com.cashmallow.api.domain.model.partner.WithdrawalPartnerCashpoint;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.traveler.TravelerWallet;
import com.cashmallow.api.domain.model.traveler.WalletRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.interfaces.sevenbank.dto.BalanceResponse;
import com.cashmallow.api.interfaces.traveler.dto.RequestCashOutVO;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.JsonStr;
import com.cashmallow.common.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.codec.binary.Base64;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

import static com.cashmallow.api.domain.shared.MsgCode.*;

@Service
public class SevenBankServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(SevenBankServiceImpl.class);

    private static final String DATA_NOT_FOUND_ERROR = "DATA_NOT_FOUND_ERROR";

    // Seven Bank API Result function values
    public static final String FUNC_WITHDRAWAL = "Withdrawal";
    public static final String FUNC_CANCEL_WITHDRAWAL = "CancelWithdrawal";
    public static final String FUNC_QUERY_WITHDRAWAL = "QueryWithdrawalStatus";
    public static final String FUNC_BALANCE = "GetBalance";
    public static final String FUNC_NOTIFY_RESULT = "NotifyResult";
    public static final String FUNC_GET_NAVI_INFO = "GetNaviInfo";

    // Seven Bank API Result code to be returned.
    public static final String ERROR_INVALID_DIGI_SIGN = "E001";
    public static final String ERROR_REMITTANCE_NOT_FOUND = "B001"; // Remittance not found.
    public static final String ERROR_FUNCTION_NOT_FOUND = "D004";

    public static final String KEY_RESULT = "result";
    public static final String KEY_RESULT_CODE = "resultCode";
    public static final String KEY_RESULT_DATA = "resultData";

    public static final String RESULT_SUCCESS = "OK";
    public static final String RESULT_ERROR = "ERROR";

    public static final String STATUS_RECEIVED = "received";
    public static final String STATUS_DONE = "done";
    public static final String STATUS_CANCELED = "canceled";
    public static final String STATUS_EXPIRED = "expired";

    // ATM list source : "QBC" or "NAV"
    @Value("${sevenbank.api.atmListSource}")
    private String atmListSource;

    @Autowired
    private EnvUtil envUtil;

    // ATM API from QBC
    private static final String ATM_RADIUS_LIMIT_QBC = "10"; // distance limit km

    // ATM API from Navitime
    @Value("${sevenbank.api.atmListUrlNav}")
    private String atmListUrlNavitime;
    private static final int ATM_COUNT_LIMIT_NAVITIME = 20;
    private static final int ATM_RADIUS_LIMIT_NAVITIME = 50000; // distance limit

    private static Random random = new SecureRandom();

    // requestTime (use Tokyo timezone). Header data 체크용으로만 사용함. 
    private final TimeZone timeZone = TimeZone.getTimeZone("Asia/Tokyo");
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Value("${sevenbank.api.apiUrl}")
    private String apiUrl;

    @Value("${sevenbank.api.agentCode}")
    private String agentCode;

    @Value("${sevenbank.api.companyId}")
    private String cstId;

    @Value("${sevenbank.api.password}")
    private String password;

    @Value("${sevenbank.api.confirmCode}")
    private String confirmCode;

    @Autowired
    private CashOutServiceImpl cashOutService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private PartnerServiceImpl partnerService;

    @Autowired
    private TravelerRepositoryService travelerRepositoryService;

    @Autowired
    private WalletRepositoryService walletRepositoryService;

    @Autowired
    private UserRepositoryService userRepositoryService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private JsonUtil jsonUtil;


    /**
     * Seven bank withdrawal reserve
     *
     * @param cashOutId
     * @param userId
     * @param passportFirstName
     * @param passportLastName
     * @param requestTime       : yyyyMMddHHmmss
     * @param amount
     * @return Map. keys : {"reception_id", "expired_datetime", "partner_code", "customer_no", "confirm_no"}
     * @throws CashmallowException
     */
    public Map<String, Object> withdrawal(Long cashOutId, String customerId, String passportFirstName, String passportLastName, String email, BigDecimal amount)
            throws CashmallowException {

        Map<String, Object> result = new HashMap<>();

        String method = "withdrawal()";

        // requestTime (use Tokyo timezone). Header data 체크용으로만 사용함. 
        dateFormat.setTimeZone(timeZone);
        String requestTime = dateFormat.format(new Date());

        // ticketNo
        String remittanceId = makeRemittanceId(cashOutId);

        // body
        JSONObject body = new JSONObject();
        JSONObject args = new JSONObject();

        args.put("remittanceId", remittanceId);

        args.put("customerId", customerId);
        args.put("familyName", passportLastName);
        args.put("givenName", passportFirstName);
        args.put("tel", "0");
        args.put("email", email);

        args.put("agentCode", agentCode);
        args.put("amount", String.valueOf(amount.intValue()));
        args.put("expiredInterval", "10");
        args.put("serviceInfo", "Welcome to Cashmallow.");

        body.put("func", FUNC_WITHDRAWAL);
        body.put("args", args);

        String bodyStr = body.toString();
        byte[] bodyByte = Base64.encodeBase64(bodyStr.getBytes(StandardCharsets.UTF_8));

        Header[] headers = makeHeader(requestTime, bodyByte);

        logger.info("{}: headers={}", method, headers);
        logger.info("{}: body={}", method, body);
        alarmService.i("SevenBankServiceImpl.withdrawal", "body=" + body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // API url
            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(bodyByte);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            logger.info("{}: stateCode={} ", method, resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                logger.error("{}: failure (reason={}) ", method, resSL.getReasonPhrase());
                throw new CashmallowException(resSL.getReasonPhrase());
            }

            logger.info("{}: success", method);

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            logger.info("{}: entity.toString()={}", method, entity);

            Header[] resHeader = response.getAllHeaders();
            checkResponseHeader(requestTime, resBody, resHeader);

            resBody = new String(Base64.decodeBase64(resBody));
            JSONObject returnValue = new JSONObject(resBody);
            logger.info("{}: returnValue={}", method, returnValue);

            String returnCode = returnValue.getString(KEY_RESULT);
            if (!RESULT_SUCCESS.equals(returnCode)) {
                logger.error("{}: Seven Bank API error. return={}", method, returnValue);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            JSONObject data = returnValue.getJSONObject("resultData");

            logger.info("{}: data={}", method, data);

            result.put("reception_id", data.getString("receptionId"));
            // 10 분간 유효함.
            result.put("expired_datetime", data.getString("expiredDatetime"));
            result.put("partner_code", data.getString("cashCode01"));
            result.put("customer_no", data.getString("cashCode02"));
            result.put("confirm_no", data.getString("cashCode03"));

        } catch (IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        return result;
    }

    public JSONObject cancelWithdrawal(Long cashOutId) throws CashmallowException {
        JSONObject result = new JSONObject();

        String method = "cancelWithdrawal()";

        // requestTime (use Tokyo timezone). Header data 체크용으로만 사용함. 
        dateFormat.setTimeZone(timeZone);
        String requestTime = dateFormat.format(new Date());

        // ticketNo
        String remittanceId = makeRemittanceId(cashOutId);

        // body
        JSONObject body = new JSONObject();
        JSONObject args = new JSONObject();

        args.put("remittanceId", remittanceId);

        body.put("func", FUNC_CANCEL_WITHDRAWAL);
        body.put("args", args);

        byte[] bodyByte = Base64.encodeBase64(body.toString().getBytes(StandardCharsets.UTF_8));

        Header[] headers = makeHeader(requestTime, bodyByte);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // API url
            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(bodyByte);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            logger.info("{}: stateCode={} ", method, resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                logger.error("{}: failure (reason={}) ", method, resSL.getReasonPhrase());
                throw new CashmallowException(resSL.getReasonPhrase());
            }

            logger.info("{}: success", method);

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            logger.info("{}: entity.toString()={}", method, entity);

            Header[] resHeader = response.getAllHeaders();
            checkResponseHeader(requestTime, resBody, resHeader);

            resBody = new String(Base64.decodeBase64(resBody));
            result = new JSONObject(resBody);

            logger.info("{}: result={}", method, result);

            String resultCode = result.getString(KEY_RESULT);
            if (!RESULT_SUCCESS.equals(resultCode)) {
                logger.error("{}: failed to cancel withdrawal. result={}", method, result);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        return result.getJSONObject(KEY_RESULT_DATA);
    }

    /**
     * Get status of withdrawal for Seven Bank
     *
     * @param cashOutId
     * @return status (received(Can be canceled), done, canceled, expired)
     * @throws CashmallowException
     */
    public JSONObject queryWithdrawalStatus(Long cashOutId) throws CashmallowException {

        JSONObject result = new JSONObject();

        String method = "queryWithdrawalStatus()";

        // requestTime (use Tokyo timezone). Header data 체크용으로만 사용함.
        dateFormat.setTimeZone(timeZone);
        String requestTime = dateFormat.format(new Date());

        // ticketNo
        String remittanceId = makeRemittanceId(cashOutId);

        // body
        JSONObject body = new JSONObject();
        JSONObject args = new JSONObject();

        args.put("remittanceId", remittanceId);

        body.put("func", FUNC_QUERY_WITHDRAWAL);
        body.put("args", args);

        String bodyStr = body.toString();
        byte[] bodyByte = Base64.encodeBase64(bodyStr.getBytes(StandardCharsets.UTF_8));

        Header[] headers = makeHeader(requestTime, bodyByte);

        logger.info("{}: headers={}", method, headers);
        logger.info("{}: body={}", method, body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(bodyByte);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            logger.info("{}: stateCode={} ", method, resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                logger.error("{}: failure (reason={}) ", method, resSL.getReasonPhrase());
                throw new CashmallowException(resSL.getReasonPhrase());
            }

            logger.info("{}: success", method);

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            logger.info("{}: entity.toString()={}", method, entity);

            Header[] resHeader = response.getAllHeaders();
            checkResponseHeader(requestTime, resBody, resHeader);

            resBody = new String(Base64.decodeBase64(resBody));
            result = new JSONObject(resBody);

            logger.info("{}: result={}", method, result);

            //            String resultCode = result.getString(KEY_RESULT);
            //            if (!RESULT_SUCCESS.equals(resultCode)) {
            //                logger.info("{}: failed to query withdrawal status. result={}", method, result);
            //                throw new CashmallowException("Failure Seven Bank WithdrawalReserve");
            //            }

        } catch (IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        return result;
        //        return result.getJSONObject(KEY_RESULT_DATA).getString("status");
    }

    /**
     * Get ATM list
     *
     * @param withdrawalPartnerCashpointId
     * @param lat
     * @param lng
     * @return
     * @throws CashmallowException
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<WithdrawalPartnerCashpoint> getAtmList(Long withdrawalPartnerCashpointId, Double lat, Double lng) throws CashmallowException {

        List<WithdrawalPartnerCashpoint> WithdrawalPartnerCashpoints;

        if (atmListSource.equals("NAV")) {
            WithdrawalPartnerCashpoints = getAtmListNavitime(withdrawalPartnerCashpointId, lat, lng);
        } else {
            WithdrawalPartnerCashpoints = getAtmListQBC(withdrawalPartnerCashpointId, lat, lng);
        }

        return WithdrawalPartnerCashpoints;
    }

    /**
     * Get ATM list from QBC
     *
     * @param storekeeperId
     * @param lat
     * @param lng
     * @return
     * @throws CashmallowException
     */
    public List<WithdrawalPartnerCashpoint> getAtmListQBC(Long storekeeperId, Double lat, Double lng) throws CashmallowException {
        String method = "getAtmList()";
        List<WithdrawalPartnerCashpoint> atmList = new ArrayList<>();

        // requestTime (use Tokyo timezone). Header data 체크용으로만 사용함. 
        dateFormat.setTimeZone(timeZone);
        String requestTime = dateFormat.format(new Date());

        // body
        JSONObject body = new JSONObject();
        JSONObject args = new JSONObject();

        args.put("latitude", String.valueOf(lat));
        args.put("longitude", String.valueOf(lng));
        args.put("radius", ATM_RADIUS_LIMIT_QBC);
        args.put("agentCode", agentCode);

        body.put("func", FUNC_GET_NAVI_INFO);
        body.put("args", args);

        String bodyStr = body.toString();
        byte[] bodyByte = Base64.encodeBase64(bodyStr.getBytes(StandardCharsets.UTF_8));

        Header[] headers = makeHeader(requestTime, bodyByte);

        logger.info("{}: headers={}", method, headers);
        logger.info("{}: body={}", method, body);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            // API url
            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(bodyByte);
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            logger.info("{}: stateCode={} ", method, resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                logger.error("{}: failure (reason={}) ", method, resSL.getReasonPhrase());
                throw new CashmallowException(resSL.getReasonPhrase());
            }

            logger.info("{}: success", method);

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            logger.info("{}: entity.toString()={}", method, entity);

            Header[] resHeader = response.getAllHeaders();
            checkResponseHeader(requestTime, resBody, resHeader);

            String decoded = new String(Base64.decodeBase64(resBody.getBytes()));
            JSONObject data = new JSONObject(decoded);

            logger.info("{}: returnValue={}", method, data);

            String returnCode = data.getString(KEY_RESULT);
            if (!RESULT_SUCCESS.equals(returnCode)) {
                logger.error("{}: Seven Bank API error. returnCode={}", method, returnCode);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            JSONArray ja = data.getJSONArray("resultData");

            Iterator<Object> iterator = ja.iterator();
            String staticUrl = envUtil.getStaticUrl();
            while (iterator.hasNext()) {
                WithdrawalPartnerCashpoint atm = new WithdrawalPartnerCashpoint();

                JSONObject item = (JSONObject) iterator.next();
                atm.setId(Long.valueOf(item.getString("storeNo")));
                atm.setWithdrawalPartnerId(storekeeperId);
                atm.setPartnerCashpointId(item.getString("storeNo"));
                atm.setPartnerCashpointName(item.getString("storeName"));
                atm.setPartnerCashpointAddr(item.getString("address"));
                atm.setPartnerCashpointLat(item.getDouble("latitude"));
                atm.setPartnerCashpointLng(item.getDouble("longitude"));
                atm.setCashOutHours(item.getString("openingTime"));

                // ref - https://drive.google.com/drive/u/0/folders/1OXLkpQQrLv9ADb9bHK0lpIpWKsW8vFIQ
                /*
                String logoUrl = item.getString("logoUrl");
                if (CommonUtil.isValidURL(logoUrl)) {
                    atm.setIconImagePath(logoUrl);
                } else {
                    atm.setDefaultIconImagePath(hostUrl + "/images/atm/cashmallow_atm_icon.png");
                }
                */
                atm.setIconImagePath(staticUrl + "/images/atm/7bank.png");
                atm.setDefaultIconImagePath(staticUrl + "/images/atm/cashmallow_atm_icon.png");

                atmList.add(atm);
            }

            logger.info("{}: atmList={}", method, atmList);

        } catch (IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        return atmList;
    }

    /**
     * Get ATM list from Navitime (https://pkg.navitime.co.jp)
     *
     * @param storekeeperId
     * @param lat
     * @param lng
     * @return
     * @throws CashmallowException
     */
    public List<WithdrawalPartnerCashpoint> getAtmListNavitime(Long storekeeperId, Double lat, Double lng) throws CashmallowException {
        String method = "getAtmList()";
        List<WithdrawalPartnerCashpoint> atmList = new ArrayList<>();

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.now());

        try {
            StringBuilder urlString = new StringBuilder(atmListUrlNavitime);
            urlString.append("?datum=wgs84&add=detail&c_d1=1&sort=admin_jis&lang=en&ignore-i18n=true&exclude-i18n=detail-text");
            urlString.append("&limit=" + ATM_COUNT_LIMIT_NAVITIME);
            urlString.append("&coord=" + lat + "," + lng);
            urlString.append("&radius=" + ATM_RADIUS_LIMIT_NAVITIME);
            urlString.append("&timestamp=" + timestamp.getTime());
            urlString.append("&random-seed=" + random.nextInt(1000000000));

            URL url = new URL(urlString.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // add request header 
            conn.setRequestProperty("Content-Type", "application/json;charset=utf8");

            int responseCode = conn.getResponseCode();

            if (responseCode != 200) {
                logger.error("{}: responseCode={}", method, responseCode);
                return atmList;
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();

            logger.debug("{}: response={}", method, response);
            JSONObject jo = new JSONObject(response.toString());

            JSONArray ja = jo.getJSONArray("items");

            Iterator<Object> iterator = ja.iterator();

            while (iterator.hasNext()) {
                WithdrawalPartnerCashpoint atm = new WithdrawalPartnerCashpoint();

                JSONObject item = (JSONObject) iterator.next();
                atm.setId(Long.valueOf(item.getString("code")));
                atm.setWithdrawalPartnerId(storekeeperId);
                atm.setPartnerCashpointId(item.getString("code"));
                atm.setPartnerCashpointName(item.getString("name"));
                atm.setPartnerCashpointAddr(item.getString("address_name"));

                JSONObject coord = item.getJSONObject("coord");
                atm.setPartnerCashpointLat(coord.getDouble("lat"));
                atm.setPartnerCashpointLng(coord.getDouble("lon"));

                JSONArray details = item.getJSONArray("details");
                Iterator<Object> dIterator = details.iterator();
                while (dIterator.hasNext()) {
                    JSONObject detail = (JSONObject) dIterator.next();
                    JSONArray texts = detail.getJSONArray("texts");
                    Iterator<Object> tIterator = texts.iterator();
                    while (tIterator.hasNext()) {
                        JSONObject text = (JSONObject) tIterator.next();
                        if ("00003".equals(text.getString("code"))) {
                            atm.setCashOutHours(text.getString("value"));
                        }
                    }
                }

                JSONArray categories = item.getJSONArray("categories");
                Iterator<Object> cIterator = categories.iterator();
                String staticUrl = envUtil.getStaticUrl();
                while (cIterator.hasNext()) {
                    JSONObject category = (JSONObject) cIterator.next();
                    /*
                    String imagePath = category.getString("image_path");
                    if (CommonUtil.isValidURL(imagePath)) {
                        atm.setIconImagePath(imagePath);
                    } else {
                        atm.setIconImagePath(hostUrl + "/images/atm/cashmallow_atm_icon.png");
                    }
                    */
                    atm.setIconImagePath(staticUrl + "/images/atm/7bank.png");
                    atm.setDefaultIconImagePath(staticUrl + "/images/atm/cashmallow_atm_icon.png");
                }

                atmList.add(atm);
            }

            logger.info("{}: atmList={}", method, atmList);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        return atmList;
    }

    /**
     * Check validation for Response header
     *
     * @param requestTime
     * @param resBody
     * @param resHeader
     * @throws CashmallowException
     */
    private void checkResponseHeader(String requestTime, String resBody, Header[] resHeader)
            throws CashmallowException {
        for (Header header : resHeader) {
            switch (header.getName()) {
                case "CstID":
                    if (!header.getValue().equals(cstId)) {
                        throw new CashmallowException("The CompanyID of the responce is inconsistent.");
                    }
                    break;

                case "ReqDt":
                    if (!header.getValue().equals(requestTime)) {
                        throw new CashmallowException("The RequestTime of the responce is inconsistent.");
                    }
                    break;

                case "SignH":
                    String decSignH = resBody + "#" + confirmCode;
                    String signH = encodeMD5(decSignH.getBytes());

                    if (!header.getValue().equals(signH)) {
                        throw new CashmallowException("The SignB of the responce is inconsistent.");
                    }
                    break;

                default:
                    break;
            }

        }
    }

    public Header[] makeHeader(String requestTime, byte[] bodyByte) {

        String authH = makeAuthH(requestTime);

        String signH = makeSignH(bodyByte);

        //        Header[] headers = {
        //                new BasicHeader(HTTP.CONTENT_TYPE, "application/json"),
        //                new BasicHeader("Accept-Charset", "UTF-8"),
        //                new BasicHeader("CompanyId", companyId),
        //                new BasicHeader("RequestTime", requestTime),
        //                new BasicHeader("SignH", signH),
        //                new BasicHeader("SignB", signB)
        //        };

        Header[] headers = new Header[6];
        headers[0] = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader("Accept-Charset", StandardCharsets.UTF_8.name());
        headers[2] = new BasicHeader("ReqDt", requestTime);
        headers[3] = new BasicHeader("CstID", cstId);
        headers[4] = new BasicHeader("AuthH", authH);
        headers[5] = new BasicHeader("SignH", signH);

        return headers;
    }

    /**
     * Generate AuthH
     *
     * @param time : yyyyMMddHHmmss
     * @return
     */
    public String makeAuthH(String time) {
        String decSignH = cstId + "#" + time + "#" + password;
        return encodeMD5(decSignH.getBytes());
    }

    /**
     * Generate SignH
     *
     * @param body : JSON string
     * @return
     */
    public String makeSignH(byte[] bodyByte) {
        String encodeBody = new String(bodyByte);
        String decSignB = encodeBody + "#" + confirmCode;

        logger.info("encodeBody={}", encodeBody);
        logger.info("decSignB={}", decSignB);

        return encodeMD5(decSignB.getBytes());
    }

    /**
     * Encode MD5
     *
     * @param bytes
     * @return
     */
    public String encodeMD5(byte[] bytes) {
        String result = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(bytes);
            byte[] byteData = md.digest();

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

            result = sb.toString();

        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * make Remittance ID
     *
     * @param cashOutId
     * @return
     */
    public String makeRemittanceId(Long cashOutId) {
        // Just make Hexadecimal
        return Long.toHexString(cashOutId).toUpperCase();
    }

    /**
     * Make cashoutId from remittanceId
     *
     * @param remittanceId
     * @return
     */
    public Long makeCashoutId(String remittanceId) {
        // Convert hexadecimal string to long value
        return new BigInteger(remittanceId, 16).longValue();
    }

    /**
     * Seven Bank 인출 시 사용할 customerNo 생성. 8자리 이하의 number string.
     *
     * @param userId
     * @param travelerId
     * @return
     */
    public static String makeCustomerId(Long userId, Long travelerId) {

        // id 의 뒤 4자리 숫자만 사용해서 조합한다.
        // 10000 으로 나누어 나머지를 사용한다.
        long diveder = 10000;

        return String.format("%d%04d", userId % diveder, travelerId % diveder);

    }

    public static String makeCustomerId(Long userId) {
        // 새로운 유니크 아이디를 생성한다
        return "CM" + userId;
    }

    // 기능: 12.0. 여행자 인출 신청

    /**
     * @param userId
     * @param withdrawalPartner
     * @param rCashOutVO
     * @return Map. keys : {"reception_id", "expired_datetime", "partner_code", "customer_no", "confirm_no", "cash_out_id"}
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public Map<String, Object> requestCashOut(Long userId, WithdrawalPartner withdrawalPartner, RequestCashOutVO rCashOutVO) throws CashmallowException {
        String method = "requestCashOut()";

        Map<String, Object> result;

        if (rCashOutVO == null) {
            logger.error("{}: Invalid parameters", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null || withdrawalPartner == null) {
            logger.error("{}: userId={}, storekeeperId={}", method, userId, rCashOutVO.getWithdrawal_partner_id());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        // 1. userId의 travelerId 구하기
        if (traveler == null) {
            logger.error("{}: userId로 여행자 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        //        BigDecimal cashoutFee = cashOutService.calcCashoutFee(rCashOutVO.getCountry(), rCashOutVO.getTraveler_cash_out_amt());
        //        rCashOutVO.setStorekeeper_cash_out_amt(rCashOutVO.getTraveler_cash_out_amt());
        //        rCashOutVO.setStorekeeper_cash_out_fee(cashoutFee);
        //        rCashOutVO.setStorekeeper_total_cost(rCashOutVO.getTraveler_cash_out_amt().add(cashoutFee));

        TravelerWallet travelerWallet = null;
        // 여행자 지갑에서 인출 신청한 국가의 보유 금액 조회
        List<TravelerWallet> wallets = walletRepositoryService.getTravelerWalletList(userId);
        for (TravelerWallet w : wallets) {
            if (w.getCountry().equals(rCashOutVO.getCountry())) {
                travelerWallet = w;
                break;
            }
        }

        if (rCashOutVO.getWallet_id() != null) {
            travelerWallet = walletRepositoryService.getTravelerWallet(rCashOutVO.getWallet_id());
        }

        if (travelerWallet == null) {
            logger.error("{}: 인출 가능한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cashOutService.checkCashOutRequest(traveler, rCashOutVO, travelerWallet, withdrawalPartner);

        walletRepositoryService.updateWalletForWithdrawal(rCashOutVO, travelerWallet);

        if (withdrawalPartner.getShopName().contains("Seven")) {

            BigDecimal cashoutFee = cashOutService.calcCashoutFee(rCashOutVO.getCountry(), rCashOutVO.getTraveler_cash_out_amt(), withdrawalPartner.getId());

            CashOut cashOut = new CashOut(traveler.getId(), rCashOutVO.getWithdrawal_partner_id(), rCashOutVO.getCountry(),
                    rCashOutVO.getTraveler_cash_out_amt(), rCashOutVO.getTraveler_cash_out_fee(),
                    rCashOutVO.getTraveler_cash_out_amt(), cashoutFee, CustomStringUtil.generateQrCode(), "Temporary QR code to avoid DB exception",
                    travelerWallet.getExchangeIds(), travelerWallet.getId());

            cashOut.setCashoutReservedDate(rCashOutVO.getCashout_reserved_date());
            cashOut.setFlightArrivalDate(rCashOutVO.getFlight_arrival_date());
            cashOut.setFlightNo(rCashOutVO.getFlight_no());
            cashOut.setPrivacySharingAgreement(rCashOutVO.isPrivacy_sharing_agreement());

            cashOut.setCoStatus(CoStatus.OP.name());
            cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

            long cashOutId = cashOutService.registerCashOut(cashOut);

            String firstName = traveler.getEnFirstName();
            String lastName = traveler.getEnLastName();

            // Call seven bank API
            result = withdrawal(cashOutId, makeCustomerId(userId, traveler.getId()),
                    firstName, lastName, user.getEmail(),
                    rCashOutVO.getTraveler_cash_out_amt());

            result.put("cash_out_id", cashOutId);

            logger.info("{}: result={}", method, result);

            // Update confirmNo in CashOut
            String seperator = "-";
            StringBuilder qrCodeValue = new StringBuilder("");
            qrCodeValue.append((String) result.get("reception_id") + seperator);
            qrCodeValue.append((String) result.get("expired_datetime") + seperator);
            qrCodeValue.append((String) result.get("partner_code") + seperator);
            qrCodeValue.append((String) result.get("customer_no") + seperator);
            qrCodeValue.append((String) result.get("confirm_no"));

            String qrCodeSource = "Seven Bank confirm number";

            cashOutService.updateCashoutQrCode(traveler, cashOutId, qrCodeValue.toString(), qrCodeSource);

        } else {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + rCashOutVO.getCountry() + ", 금액:"
                + rCashOutVO.getTraveler_cash_out_amt();
        msg += "\n가맹점ID:" + withdrawalPartner.getUserId();
        msg += ", 가맹점이름:" + withdrawalPartner.getShopName();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }

        alarmService.aAlert("인출신청", msg, user);

        return result;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    public Map<String, Object> requestCashOutV2(Long userId, WithdrawalPartner withdrawalPartner, BigDecimal travelerCashoutAmt,
                                                Long travelerWalletId, String countryCode, String cashoutReservedDate, Integer requestTime) throws CashmallowException {
        String method = "requestCashOutV2()";

        Map<String, Object> result;

        User user = userRepositoryService.getUserByUserId(userId);
        if (user == null || withdrawalPartner == null) {
            logger.error("{}: userId={}, storekeeperId={}", method, userId, withdrawalPartner.getId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);

        // 1. userId의 travelerId 구하기
        if (traveler == null) {
            logger.error("{}: userId로 여행자 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        String customerId = getCustomerId(userId, traveler.getId());
        logger.info("{}: customerId={}, travelerId={}, userId={}", method, customerId, traveler.getId(), userId);

        TravelerWallet travelerWallet = walletRepositoryService.getTravelerWallet(travelerWalletId);

        if (travelerWallet == null) {
            logger.error("{}: 인출 가능한 여행자 지갑을 찾지 못했습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cashOutService.checkCashOutRequestV2(traveler, cashoutReservedDate, travelerWallet, withdrawalPartner, travelerCashoutAmt, requestTime, countryCode);

        walletRepositoryService.updateWalletForWithdrawalV2(travelerCashoutAmt, travelerWallet);

        if (!withdrawalPartner.getShopName().contains("Seven")) {
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }


        BigDecimal cashoutFee = cashOutService.calcCashoutFee(countryCode, travelerCashoutAmt, withdrawalPartner.getId());

        CashOut cashOut = new CashOut(traveler.getId(), withdrawalPartner.getId(), countryCode,
                travelerCashoutAmt, BigDecimal.ZERO,
                travelerCashoutAmt, cashoutFee, CustomStringUtil.generateQrCode(),
                "Temporary QR code to avoid DB exception", travelerWallet.getExchangeIds(), travelerWallet.getId());

        cashOut.setCashoutReservedDate(cashoutReservedDate);
        cashOut.setPrivacySharingAgreement(user.getAgreePrivacy().equalsIgnoreCase("Y"));

        cashOut.setCoStatus(CoStatus.OP.name());
        cashOut.setCoStatusDate(Timestamp.valueOf(LocalDateTime.now()));

        long cashOutId = cashOutService.registerCashOut(cashOut);

        String firstName = traveler.getEnFirstName();
        String lastName = traveler.getEnLastName();

        logger.info("{}: cashOutId={}", method, cashOutId);

        // Call seven bank API
        result = withdrawal(cashOutId, customerId, firstName, lastName, user.getEmail(),
                travelerCashoutAmt);

        result.put("cash_out_id", cashOutId);

        logger.info("{}: result={}", method, result);

        // Update confirmNo in CashOut
        String seperator = "-";
        StringBuilder qrCodeValue = new StringBuilder("");
        qrCodeValue.append((String) result.get("reception_id") + seperator);
        qrCodeValue.append((String) result.get("expired_datetime") + seperator);
        qrCodeValue.append((String) result.get("partner_code") + seperator);
        qrCodeValue.append((String) result.get("customer_no") + seperator);
        qrCodeValue.append((String) result.get("confirm_no"));

        String qrCodeSource = "Seven Bank confirm number";

        cashOutService.updateCashoutQrCode(traveler, cashOutId, qrCodeValue.toString(), qrCodeSource);

        // send slack message 
        String msg = "유저ID:" + userId + ", 신청국:" + countryCode + ", 금액:" + travelerCashoutAmt;
        msg += "\n가맹점ID:" + withdrawalPartner.getUserId();
        msg += ", 가맹점이름:" + withdrawalPartner.getShopName();
        if (withdrawalPartner.getAbout() != null) {
            msg += "\n가맹점정보:" + withdrawalPartner.getAbout();
        }

        alarmService.aAlert("인출신청", msg, user);

        return result;
    }

    public String getCustomerId(Long userId, Long travelerId) {
        boolean cmCustomer = cashoutRepositoryService.isCmCustomer(travelerId);

        /**
         * 1번 또는 2번거래에 해당하는 경우 'CM' + userId 로 customerId 생성
         *
         * 1. 거래를 1번도 안한경우
         * 2. 2024-12-24 일 이후 최초 거래인 경우
         */
        if (userId == 52531L || cmCustomer) {
           return makeCustomerId(userId);
        }

        // 기존유저는 customerId 기존 형식 유지
        return makeCustomerId(userId, travelerId);
    }

    /**
     * Seven Bank 인출 예약 취소. Timeout 에 의한 취소 처리.
     *
     * @param userId
     * @param cashOutId
     * @param coStatus
     * @throws CashmallowException
     */
    @Transactional(rollbackFor = CashmallowException.class)
    public void cancelCashOutSevenBankByTimeout(CashOut cashout, String coStatus) throws CashmallowException {
        String method = "cancelCashOutSevenBank()";

        switch (cashout.getCoStatusEnum()) {
            case OP -> {
                // pass
                logger.debug("cashout id:{}, coStatus:{} to coStatus:{}", cashout.getId(), cashout.getCoStatus(), coStatus);
            }
            case CC, TC, SC -> {
                logger.warn("ALREADY_CANCELD cashoutId={}, coStatus={}", cashout.getId(), cashout.getCoStatus());
                alarmService.i("인출 취소", "ALREADY_CANCELD cashoutId=%s, coStatus=%s".formatted(cashout.getId(), cashout.getCoStatus()));
                return;
            }
            default -> {
                logger.warn("{}: 진행 중인 인출이 아니므로 취소할 수 없습니다. cashoutId={}, coStatus={}", method, cashout.getId(), cashout.getCoStatus());
                throw new CashmallowException(CASHOUT_NOT_IN_PROGRESS_UNAVAILABLE_CANCEL);
            }
        }

        Traveler traveler = travelerRepositoryService.getTravelerByTravelerId(cashout.getTravelerId());
        if (traveler == null) {
            logger.error("{}: traveler 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        cancelCashoutSevenBank(traveler.getUserId(), cashout.getId(), coStatus);

        // Seven Bank는 ATM 이므로 가맹점에 FCM을 보내지 않는다.
        // 여행자에게 FCM을 보낸다.
        User user = userRepositoryService.getUserByUserId(traveler.getUserId());
        notificationService.sendFcmNotificationMsgAsync(user, FcmEventCode.CO, FcmEventValue.CC, cashout.getId());
    }

    /**
     * Seven Bank 인출 예약 취소. 사용자 요청에 의한 취소 처리.
     *
     * @param userId
     * @param cashOutId
     * @param coStatus
     * @throws CashmallowException
     */
    public void cancelCashoutSevenBank(Long userId, Long cashoutId, String coStatus) throws CashmallowException {
        String method = "cancelCashOutSevenBank()";

        CashOut cashout = cashoutRepositoryService.getCashOut(cashoutId);
        if (cashout == null) {
            logger.error("{}: 인출정보가 올바르지 않습니다. cashout=null", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        Traveler traveler = travelerRepositoryService.getTravelerByUserId(userId);
        if (traveler == null) {
            logger.error("{}: traveler 정보를 찾을 수 없습니다.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 소유권 체크
        if (!traveler.getId().equals(cashout.getTravelerId())) {
            logger.error("{}: 인출 소유자가 아닙니다. traveler.getId:{}, cashout.getTravelerId:{}", method, traveler.getId(), cashout.getTravelerId());
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        switch (cashout.getCoStatusEnum()) {
            case OP -> {
                // pass
                logger.debug("cashout id:{}, coStatus:{} to coStatus:{}", cashout.getId(), cashout.getCoStatus(), coStatus);
            }
            case CC, TC, SC -> {
                logger.warn("ALREADY_CANCELD cashoutId={}, coStatus={}", cashout.getId(), cashout.getCoStatus());
                alarmService.i("인출 취소", "ALREADY_CANCELD cashoutId=%s, coStatus=%s".formatted(cashout.getId(), cashout.getCoStatus()));
                return;
            }
            default -> {
                logger.warn("{}: 진행 중인 인출이 아니므로 취소할 수 없습니다. cashoutId={}, coStatus={}", method, cashout.getId(), cashout.getCoStatus());
                throw new CashmallowException(CASHOUT_NOT_IN_PROGRESS_UNAVAILABLE_CANCEL);
            }
        }

        JSONObject statusResult = queryWithdrawalStatus(cashoutId);
        logger.info("queryWithdrawalStatus response={}", JsonStr.toJson(statusResult));

        String sResult = statusResult.getString(KEY_RESULT);
        String sResultStatus = null;
        if (!RESULT_SUCCESS.equals(sResult)) {
            logger.info("{}: failed to query withdrawal status. result={}", method, statusResult);
            // 상태 체크 실패한 경우 에러 처리 한다.
            throw new CashmallowException("Failed to check status for withdrawal cancel in Seven Bank. status=" + sResultStatus);
        } else {
            // 상태 체크 성공한 경우 상태값을 가져온다.
            sResultStatus = statusResult.getJSONObject(KEY_RESULT_DATA).getString("status");
        }

        logger.info("{}: Before CancelWithdrawal status={}", method, statusResult);
        // STATUS_RECEIVED -> 인출 신청 중 상태이므로 Seven Bank 에 인출취소 처리 요청 
        if (STATUS_RECEIVED.equals(sResultStatus)) {

            // Seven Bank에 인출 취소 요청 
            cancelWithdrawal(cashoutId);

            // 취소 요청 후 상태를 다시 확인한다.
            statusResult = queryWithdrawalStatus(cashoutId);
            logger.info("{}: After CancelWithdrawal status={}", method, statusResult);
        }

        sResult = statusResult.getString(KEY_RESULT);

        if (RESULT_SUCCESS.equals(sResult)) {
            sResultStatus = statusResult.getJSONObject(KEY_RESULT_DATA).getString("status");
        }

        String sResultCode = statusResult.getString(KEY_RESULT_CODE);

        if (STATUS_DONE.equals(sResultStatus)) {
            // Withdrawal has been completed. ATM 에서 이미 인출했으므로 인출 완료로 처리한다.
            String remittanceId = makeRemittanceId(cashoutId);
            completeCashOutSevenBank(remittanceId);
        } else if (STATUS_CANCELED.equals(sResultStatus) || STATUS_EXPIRED.equals(sResultStatus)
                || ERROR_REMITTANCE_NOT_FOUND.equals(sResultCode)) {
            // canceled, expired 인 경우 or remittanceId not found 경우 DB 업데이트. 취소 요청 API 호출 이후에 DB 업데이트 해야 함.
            cashOutService.processCancelCashout(cashoutId, CoStatus.valueOf(coStatus));
        } else {
            // 이외의 경우 에러 처리 한다.
            throw new CashmallowException("Failed to delete withdrawal in Seven Bank. status=" + sResultStatus);
        }

    }

    // 여행자 인출 완료 with ticketNo, amount
    @Transactional(rollbackFor = CashmallowException.class)
    public void completeCashOutSevenBank(String remittanceId) throws CashmallowException {

        Long cashoutId = makeCashoutId(remittanceId);
        CashOut cashOut = cashoutRepositoryService.getCashOut(cashoutId);

        WithdrawalPartner withdrawalPartner = null;
        if (cashOut != null) {
            withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        }

        if (cashOut == null || !CoStatus.OP.equals(CoStatus.valueOf(cashOut.getCoStatus()))
                || withdrawalPartner == null || !withdrawalPartner.getShopName().contains("Seven")) {
            if (cashOut != null && CoStatus.CC.name().equalsIgnoreCase(cashOut.getCoStatus())) {
                // CC 상태의 cashout을 OP로 만들어서 강제로 출금 로직으로 복귀하게 함.
                cashOut.setCoStatus(CoStatus.OP.name());
                TravelerWallet wallet = walletRepositoryService.getTravelerWallet(cashOut.getWalletId());
                walletRepositoryService.updateWalletForWithdrawalV2(cashOut.getTravelerCashOutAmt(), wallet);
            } else {
                throw new CashmallowException(DATA_NOT_FOUND_ERROR);
            }
        }

        cashOutService.completeCashOutConfirm(cashOut);

    }

    @Profile("dev-local")
    @Transactional(rollbackFor = CashmallowException.class)
    public void completeCashOutSevenBank(Long cashoutId) throws CashmallowException {

        CashOut cashOut = cashoutRepositoryService.getCashOut(cashoutId);

        WithdrawalPartner withdrawalPartner = null;
        if (cashOut != null) {
            withdrawalPartner = partnerService.getWithdrawalPartnerByWithdrawalPartnerId(cashOut.getWithdrawalPartnerId());
        }

        if (cashOut == null || !CoStatus.OP.equals(CoStatus.valueOf(cashOut.getCoStatus()))
                || withdrawalPartner == null || !withdrawalPartner.getShopName().contains("Seven")) {
            if (cashOut != null && CoStatus.CC.name().equalsIgnoreCase(cashOut.getCoStatus())) {
                // CC 상태의 cashout을 OP로 만들어서 강제로 출금 로직으로 복귀하게 함.
                cashOut.setCoStatus(CoStatus.OP.name());
                TravelerWallet wallet = walletRepositoryService.getTravelerWallet(cashOut.getWalletId());
                walletRepositoryService.updateWalletForWithdrawalV2(cashOut.getTravelerCashOutAmt(), wallet);
            } else {
                throw new CashmallowException(DATA_NOT_FOUND_ERROR);
            }
        }

        cashOutService.completeCashOutConfirm(cashOut);

    }


    public BalanceResponse<HashMap<String, String>> getBalance() throws CashmallowException {
        dateFormat.setTimeZone(timeZone);

        // body
        JSONObject body = new JSONObject();
        body.put("func", FUNC_BALANCE);

        Header[] headers = makeHeader(dateFormat.format(new Date()), Base64.encodeBase64(body.toString().getBytes(StandardCharsets.UTF_8)));

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            HttpPost request = new HttpPost(apiUrl);
            request.setHeaders(headers);

            ByteArrayEntity params = new ByteArrayEntity(Base64.encodeBase64(body.toString().getBytes(StandardCharsets.UTF_8)));
            request.setEntity(params);

            CloseableHttpResponse response = httpClient.execute(request);

            StatusLine resSL = response.getStatusLine();
            logger.info("stateCode={} ", resSL.getStatusCode());

            if (resSL.getStatusCode() != 200) {
                logger.error("failure (reason={}) ", resSL.getReasonPhrase());
                throw new CashmallowException(resSL.getReasonPhrase());
            }

            HttpEntity entity = response.getEntity();
            String resBody = EntityUtils.toString(entity);
            logger.info("entity.toString()={}", entity);

            Header[] resHeader = response.getAllHeaders();
            checkResponseHeader(dateFormat.format(new Date()), resBody, resHeader);

            String jsonResult = new String(Base64.decodeBase64(resBody));

            logger.info("result={}", jsonResult);
            BalanceResponse<HashMap<String, String>> result = jsonUtil.fromJson(jsonResult, new TypeReference<BalanceResponse<HashMap<String, String>>>() {
            });
            return result;

        } catch (IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }

}
