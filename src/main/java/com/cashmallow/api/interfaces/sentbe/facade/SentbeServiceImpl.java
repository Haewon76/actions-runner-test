package com.cashmallow.api.interfaces.sentbe.facade;

import com.cashmallow.api.domain.shared.CashmallowException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;

@Service
public class SentbeServiceImpl {
    Logger logger = LoggerFactory.getLogger(SentbeServiceImpl.class);

    private static ScheduledExecutorService scheduledService = Executors.newScheduledThreadPool(10);

    @Value("${sentbe.url}")
    private String sentbeUrl;

    @Value("${sentbe.APIKey}")
    private String sentbeAPIKey;

    @Value("${sentbe.secret}")
    private String sentbeSecret;

    public Map<String, Object> getSentbeBalance() throws CashmallowException {
        String method = "getSentbeBalance()";

        Map<String, Object> result = new HashMap<>();
        URIBuilder builder;
        try {
            builder = new URIBuilder(sentbeUrl + "/v1/service/balance");

            String responseBody = httpGet(method, builder);
            logger.info("{} : {} ", method, responseBody);

            JSONObject json = new JSONObject(responseBody);

            if (!json.get("code").toString().equals("200")) {
                logger.error("{} : code={}, message={}", method, json.get("code"), json.get("message"));
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

            JSONArray bankArray = json.getJSONObject("data").getJSONArray("list");
            result.put("USD", BigDecimal.ZERO);
            result.put("KRW", BigDecimal.ZERO);
            result.put("SGD", BigDecimal.ZERO);

            for (Object ob : bankArray) {
                JSONObject balanceJson = (JSONObject) ob;

                if (balanceJson.getString("currency").equals("USD")) {
                    result.put("USD", balanceJson.getBigDecimal("balance"));
                } else if (balanceJson.getString("currency").equals("KRW")) {
                    result.put("KRW", balanceJson.getBigDecimal("balance"));
                } else if (balanceJson.getString("currency").equals("SGD")) {
                    result.put("SGD", balanceJson.getBigDecimal("balance"));
                }
            }

            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    private String generateHMAC(String data, String key) throws SignatureException, NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);
        byte[] hash = mac.doFinal(data.getBytes());
        Encoder encoder = Base64.getEncoder();
        String encodedBytes = encoder.encodeToString(hash);
        return encodedBytes;
    }

    private String httpGet(String method, URIBuilder urlBuilder) throws CashmallowException {

        String result = "";

        logger.info("{}: body={}", method, urlBuilder);

        String nowTimeStamp = "";
        String date = "";
        String hmac = "";
        try {
            nowTimeStamp = String.valueOf(System.currentTimeMillis());

            Instant instant = Instant.now();
            date = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'UTC'").withZone(ZoneOffset.UTC)
                    .withLocale(Locale.US).format(instant);

            String data = sentbeAPIKey + nowTimeStamp + date;
            hmac = generateHMAC(data, sentbeSecret);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            Header[] headers = new Header[4];
            headers[0] = new BasicHeader("x-sentbe-apikey", sentbeAPIKey);
            headers[1] = new BasicHeader("x-sentbe-nonce", nowTimeStamp);
            headers[2] = new BasicHeader("x-sentbe-hmac", hmac);
            headers[3] = new BasicHeader("x-sentbe-date", date);

            URL url = urlBuilder.build().toURL();
            // API url
            HttpGet request = new HttpGet(url.toString());
            request.setHeaders(headers);

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

            result = resBody;
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }

        return result;
    }

    @PreDestroy
    public void destroy() {
        logger.info("destroy() executorService.isShutdown={}", scheduledService.isShutdown());
        if (!scheduledService.isShutdown()) {
            scheduledService.shutdown();
        }
    }
}
