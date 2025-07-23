package com.cashmallow.common;

import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpApi {
    private static final Logger logger = LoggerFactory.getLogger(HttpApi.class);

    // HTTP Content-Type 정리: http://hbesthee.tistory.com/45
    public static ApiResultVO httpGet(String url, HashMap<String, String> params, String encoding) {
        boolean flagSetResult = true;
        String method = "httpGet(): ";
        String error = "";
        Object obj = null;
        ApiResultVO result = new ApiResultVO(Const.CODE_INVALID_PARAMS);

        synchronized (HttpApi.class.getSimpleName()) {

            if (url != null && !url.isEmpty()) {
                try {
                    if (params != null && params.size() >= 1) {
                        List<NameValuePair> paramList = convertParam(params);
                        String paramString = URLEncodedUtils.format(paramList, encoding);

                        if (!paramString.isEmpty()) {
                            url += "?" + paramString;
                        }
                    }

                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    logger.info(method + url);
                    HttpGet httpGet = new HttpGet(url);
                    // httpGet.setHeader("Content-Type", "text/xml");
                    CloseableHttpResponse response = httpClient.execute(httpGet);

                    try {
                        logger.info(method + response.getStatusLine().toString());

                        // API서버로부터 받은 결과
                        org.apache.http.HttpEntity entity = response.getEntity();
                        obj = EntityUtils.toString((org.apache.http.HttpEntity) entity);
                        EntityUtils.consume(entity);
                    } finally {
                        response.close();
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    error = e.toString();
                }
            } else {
                error = "URL이 비었습니다.";
            }

            result.setSuccessOrFail(flagSetResult, obj, error);
        }
        return result;
    }

    private static List<NameValuePair> convertParam(HashMap<String, String> params) {
        List<NameValuePair> paramList = new ArrayList<>();

        if (params != null) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                paramList.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        return paramList;
    }

    // 기능:
    public static String httpPostWithJson(String url, HashMap<String, String> hashMap) {
        String result = "";

        try {
            // 전달하고자 하는 Parameter를 JSON 문자열로 변환한다.
            ObjectMapper objMapper = new ObjectMapper();
            String jsonParam = objMapper.writeValueAsString(hashMap);
            result = httpPostWithJson(url, jsonParam);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result = e.toString();
        }

        return result;
    }

    // 기능:
    public static String httpPostWithJson(String url, String jsonParam) {
        String result = "";

        if (url != null && !url.isEmpty() && jsonParam != null && !jsonParam.isEmpty()) {
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setHeader("Content-Type", "application/json");

                // logger.info("httpPostWithJson(): jsonParam={}", jsonParam);

                StringEntity params = new StringEntity(jsonParam);
                httpPost.setEntity(params);

                // List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                // nvps.add(new BasicNameValuePair("username", id)); // request.getParameter("t1cmcom")));
                // nvps.add(new BasicNameValuePair("password", pw)); // request.getParameter("1111")));

                // // UTF-8은 한글
                // httpPost.setEntity(new UrlEncodedFormEntity(nvps,"UTF-8"));
                CloseableHttpResponse response = httpclient.execute(httpPost);

                try {
                    // logger.info("httpPostWithJson(): statusLine={}", response.getStatusLine().toString());

                    // API서버로부터 받은 JSON 문자열 데이터
                    org.apache.http.HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString((org.apache.http.HttpEntity) entity);

                    logger.debug("httpPostWithJson(): result={}", result);

                    EntityUtils.consume(entity);
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }


    // 기능:
    public static String httpPostWithJson(String url, String token, String json) {
        String result = "";

        if (url != null && !url.isEmpty() && json != null && !json.isEmpty()) {
            try {
                CloseableHttpClient httpclient = HttpClients.createDefault();
                HttpPost httpPost = new HttpPost(url);
                //              httpPost.addHeader("Content-Type", "text/xml; charset=utf-8");
                httpPost.setHeader("Content-Type", "application/json; charset=utf-8");
                httpPost.setHeader("Authorization", token);

                logger.info("httpPostWithJson(): json={}", json);

                //                StringEntity tokenParam = new StringEntity(token, Const.DEF_ENCODING);
                //                httpPost.setEntity(tokenParam);
                //
                StringEntity jsonParam = new StringEntity(json, Const.DEF_ENCODING);
                httpPost.setEntity(jsonParam);

                //                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                //                nameValuePairs.add(new BasicNameValuePair("token", token));
                //                nameValuePairs.add(new BasicNameValuePair("jsonStr", json));

                //                // UTF-8은 한글
                //                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Const.DEF_ENCODING));
                CloseableHttpResponse response = httpclient.execute(httpPost);

                try {
                    logger.info("httpPostWithJson(): statusLine={}", response.getStatusLine().toString());

                    // API서버로부터 받은 JSON 문자열 데이터
                    org.apache.http.HttpEntity entity = response.getEntity();
                    result = EntityUtils.toString((org.apache.http.HttpEntity) entity);

                    logger.debug("httpPostWithJson(): result={}", result);

                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    result = e.toString();
                } finally {
                    response.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result = e.toString();
            }
        }

        return result;
    }
}
