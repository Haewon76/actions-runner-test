package com.cashmallow.api.interfaces.slack.web;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.impl.WebhookServiceImpl;
import com.cashmallow.api.application.impl.WebhookServiceImpl.Keyword;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.slack.web.model.EmailWebhook;
import com.google.gson.Gson;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;


/**
 * Slack outgoing webhook
 * 슬랙 앱에서 통합 앱 설정
 * - 체널 변경으로 체널 변경.
 * - URL로 서버 변경.
 * <p>
 * 동작 채널
 * prd : 신청확인
 * dev : sys_dev_admin_alert
 */
@Controller
public class SlackController {

    private static final Logger logger = LoggerFactory.getLogger(SlackController.class);


    @Value("${slack.webhook.token}")
    private String webhookToken;


    @Autowired
    private WebhookServiceImpl webhookService;

    @Autowired
    private AlarmService alarmService;

    @Autowired
    private Gson gsonPretty;


    @PostMapping(value = {"/slack/webhook", "/json/command/slackCommand"}, produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getStatistics(@RequestParam String token,
                                @RequestParam String text, @RequestParam(value = "user_id") String userId,
                                HttpServletRequest request, HttpServletResponse response) {

        String method = "getStatistics()";

        logger.info("{}: text={}, userId={}", method, text, userId);

        JSONObject json = new JSONObject();

        if (!webhookToken.equals(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return null;
        }

        text = text.toUpperCase();

        // 호출이 무한반복되는걸 방지한다
        if (userId != null && "USLACKBOT".equals(userId)) {
            json.put("text", "ok");
            return json.toString();
        }


        String answer = null;

        if ("?".equals(text)) {
            answer = webhookService.getHelp();

        } else if (text.startsWith(Keyword.EX_SERVICE.toString()) && text.split("\\s+").length == 4) {
            String[] splited = text.split("\\s+");
            String fromCd = splited[1];
            String toCd = splited[2];
            String enabled = splited[3].toUpperCase();

            answer = webhookService.setExchangeServiceStatus(fromCd, toCd, enabled);

        } else if (text.startsWith(Keyword.RM_SERVICE.toString()) && text.split("\\s+").length == 4) {
            String[] splited = text.split("\\s+");
            String fromCd = splited[1];
            String toCd = splited[2];
            String enabled = splited[3].toUpperCase();

            answer = webhookService.setRemittanceServiceStatus(fromCd, toCd, enabled);
        } else if (text.startsWith(Keyword.COUNT_NEW_USERS.toString())) {
            if (text.split("\\s+").length == 3) {
                String[] splited = text.split("\\s+");
                String startDate = splited[1];
                String endDate = splited[2];
                logger.debug("COUNT_NEW_USERS startDate:{} endDate:{}", startDate, endDate);
                answer = webhookService.getCountNewUsers(startDate, endDate);
            } else {
                answer = "[사용법]\n" +
                        "count_new_users 시작일 종료일\n" +
                        "날짜는 yyyy-mm-dd 형식\n" +
                        "eg. count_new_users 2023-03-31 2023-04-06\n";
            }
        } else {
            try {
                answer = webhookService.getStatistics(Keyword.valueOf(text));
            } catch (IllegalArgumentException e) {
                answer = "해당 키워드는 서비스되지 않습니다.";
            }
        }

        json.put("text", answer);

        return json.toString();

    }

    @PostMapping(value = "/slack/email/webhook", produces = GlobalConst.PRODUCES)
    @ResponseBody
    public String getEmailInboundToSlack(@RequestBody Object obj) {
        final String json = gsonPretty.toJson(obj);
        EmailWebhook emailWebhook = gsonPretty.fromJson(json, EmailWebhook.class);

        String result = MessageFormat.format("Event: {0}\nUserAgent: {1}\nto: {2}\nSubject: {3}",
                emailWebhook.getEvent(),
                emailWebhook.getPayload().getUserAgent(),
                emailWebhook.getPayload().getMessage().getTo(),
                emailWebhook.getPayload().getMessage().getSubject());

        alarmService.i("emailWebhook", result);
        return null;
    }

}
