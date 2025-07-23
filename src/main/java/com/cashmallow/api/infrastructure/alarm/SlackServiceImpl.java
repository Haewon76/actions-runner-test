package com.cashmallow.api.infrastructure.alarm;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.application.UserAdminService;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.interfaces.CryptAES;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.EnvUtil;
import com.cashmallow.common.geoutil.GeoUtil;
import com.cashmallow.interceptor.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.cashmallow.common.CommonUtil.*;

@Slf4j
@Primary
@RequiredArgsConstructor
@Service
class SlackServiceImpl implements AlarmService {

    private static final Logger logger = LoggerFactory.getLogger(SlackServiceImpl.class);
    private final SlackProperties slackProperties;

    private final EnvUtil envUtil;
    private final TowerLampServiceImpl towerLampService;
    private final UserAdminService userAdminService;

    private final TaskExecutor taskExecutor;
    private final GeoUtil geoUtil;

    private final String LINE_SEPARATOR = "\n-----------------------------------";

    // @link - https://github.com/CashmallowCorp/you-go-we-go/issues/323
    private static final Map<String, List<String>> ADMIN_MESSAGE_KEYWORDS = new HashMap<>();

    static {
        // 인출관련 알림
        ADMIN_MESSAGE_KEYWORDS.put("인출신청",
                List.of(
                        "인출 거래번호", "가맹점이름"
                )
        );
        ADMIN_MESSAGE_KEYWORDS.put("인출완료", List.of("인출 거래번호"));
        ADMIN_MESSAGE_KEYWORDS.put("인출취소", List.of("인출 거래번호"));
        ADMIN_MESSAGE_KEYWORDS.put("인출롤백", List.of("인출 거래번호"));

        // 신분증/계좌 승인 알림
        ADMIN_MESSAGE_KEYWORDS.put("승인",
                List.of(
                        "[ADMIN] 여행자 등록 처리 승인",
                        "[ADMIN] 통장 등록 처리 승인"
                )
        );
        ADMIN_MESSAGE_KEYWORDS.put("보류",
                List.of(
                        "[ADMIN] 통장 요청 처리 완료",
                        "통장 KYC 만료 처리 완료",
                        "여행자 KYC 만료 처리 완료",
                        "여행자 요청 처리 완료, 사용자ID",
                        "[ADMIN] 여행자 요청 처리 완료"
                )
        );
        ADMIN_MESSAGE_KEYWORDS.put("요청", List.of("[ADMIN] 여행자 등록 처리 요청"));


        // 매핑관련 알림
        ADMIN_MESSAGE_KEYWORDS.put("PAYGATE", List.of("[ADMIN] 여행자 송금 완료"));
        ADMIN_MESSAGE_KEYWORDS.put("영수증 재등록(DR)", List.of("[ADMIN] 송금 거래번호"));

        // 환불알림
        ADMIN_MESSAGE_KEYWORDS.put("ADMIN", List.of("여행자 환불 완료 (user_id)"));
    }

    private void postMessage(SlackChannel channel, String kind, String message) {
        String sendMessage = "[" + kind + "] " + message;
        sendSlack(channel, sendMessage);
    }

    private void sendSlack(SlackChannel channel, String message) {
        // INFO, ERROR 채널이면 요청자 정보를 포함
        if (isInfoOrError(channel)) {
            String userId = "-1";
            if (StringUtils.isNotBlank(MDC.get("userId"))) {
                userId = MDC.get("userId");
            }
            message = getDefaultSlackMessageTemplate(message, userId);
        }

        sendSlackAsync(channel, appendWorkerName(message));
    }

    private void sendSlackAsync(SlackChannel channel, String message) {
        logger.info("sending Slack {}: {}", channel.toString(), message);

        // jp 면 jp 채널로
        final CountryCode sendCountry;
        String countryName = MDC.get(AuthInterceptor.SERVICE_COUNTRY);
        if (StringUtils.isNotBlank(countryName) && CountryCode.JP == CountryCode.valueOf(countryName)) {
            sendCountry = CountryCode.JP;
        } else {
            sendCountry = CountryCode.HK;
        }

        taskExecutor.execute(() -> {
            if (envUtil.isPrd() && SlackChannel.ERROR.equals(channel)) {
                towerLampService.turnOnRed();
            }

            requestSendSlackMessage(channel, sendCountry, message);
        });
    }

    private void requestSendSlackMessage(final SlackChannel channel,
                                         final CountryCode sendCountry,
                                         final String message) {
        try {
            SlackProperties.ChannelIdAndToken channelIdAndToken = slackProperties.getChannelIdAndToken(channel, sendCountry);

            RestTemplate restTemplate = new RestTemplate();
            String url = slackProperties.apiUrl();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + channelIdAndToken.token());

            Map<String, Object> map = new HashMap<>();
            map.put("channel", channelIdAndToken.channelId());
            map.put("username", slackProperties.senderName());
            map.put("text", message + LINE_SEPARATOR);
            map.put("parse", "none");
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(map, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
            if (response.getStatusCode() == HttpStatus.OK) {
                log.debug("Slack Message Request Successful. {}", response.getBody());
            } else {
                log.debug("Slack Message Request Failed. status: {}, {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            log.error("Slack Message Request Failed. {}", e.getMessage(), e);
        }
    }

    private static boolean isInfoOrError(SlackChannel channel) {
        return channel.equals(SlackChannel.INFO) || channel.equals(SlackChannel.ERROR);
    }


    @Override
    public void e(String kind, String message) {
        postMessage(SlackChannel.ERROR, kind, message);
    }

    @Override
    public void i(String kind, String message) {
        postMessage(SlackChannel.INFO, kind, message);
    }

    @Override
    public void ie(String kind, String message) {
        String uuid = UUID.randomUUID().toString();
        String header = message.split("\n")[0] + "\nuuid: " + uuid;
        String errorData = CryptAES.encode(uuid, message);
        i(kind, header + "\n-----------\n" + errorData);
    }

    @Override
    public boolean aAlert(String kind, String message, User user) {
        MDC.put(AuthInterceptor.SERVICE_COUNTRY, user != null ? user.getCountryCode().name() : CountryCode.HK.name());
        // 불필요한 메세지는 #admin_massage 채널로 보냄
        if (ADMIN_MESSAGE_KEYWORDS
                .getOrDefault(kind, List.of())
                .stream()
                .anyMatch(message::contains)) {
            log.debug("aMessage:: kind={}, message={}", kind, message);
            postMessage(SlackChannel.ADMIN_MESSAGE, kind, message);
            return false;
        }

        // 관리자가 봐야하는 중요한 메세지는 #admin_alert 채널로 보냄
        log.debug("aAlert:: kind={}, message={}", kind, message);
        postMessage(SlackChannel.ADMIN_ALERT, kind, message);
        MDC.remove(AuthInterceptor.SERVICE_COUNTRY);
        return true;
    }

    @Override
    public void sAlert(String kind, String message) {
        postMessage(SlackChannel.SYSTEM_ALERT, kind, message);
    }

    @Override
    public void aMsg(String kind, String message) {
        postMessage(SlackChannel.ADMIN_MESSAGE, kind, message);
    }

    @Override
    public void aEdd(String kind, String message) {
        postMessage(SlackChannel.ADMIN_EDD, kind, message);
    }

    /**
     * 어드민 알람시 사용
     * 메세지에 어드민이 포함되어있다면 작업자를 추가
     *
     * @return
     */
    private String appendWorkerName(String message) {
        try {
            if (!message.toUpperCase().contains("ADMIN")) {
                // 어드민이 아닌경우 기기 정보를 슬랙으로 전송
                // info: A_676_33_Pixel 4 XL
                // I_599_16.6_iPhone 14 Pro
                return message + appDebugInfo();
            }

            final String mdcUserId = MDC.get("userId");
            if (StringUtils.isEmpty(mdcUserId)) {
                return message;
            }

            String workerName = userAdminService.getAdminName(Long.parseLong(mdcUserId));
            return message + ", worker:" + workerName;

        } catch (Exception e) {
            log.error("getAdminName:: " + e.getMessage(), e);
        }

        return message;
    }

    private String getDefaultSlackMessageTemplate(String message, String userId) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (servletRequestAttributes == null) {
            return message;
        }

        final HttpServletRequest request = servletRequestAttributes.getRequest();
        StringBuilder sb = new StringBuilder();
        sb.append("* uri: " + request.getMethod() + " " + getUri(request) + "\n");
        if (StringUtils.isNotEmpty(userId) && !"-1".equals(userId)) {
            sb.append("* userId: " + userId + "\n");
        }
        sb.append("* ip: " + getRemoteAddr(request) + "\n");

        String location = geoUtil.getMyLocation(getRemoteAddr(request));
        if (StringUtils.isNotEmpty(location)) {
            sb.append("* location: " + location + "\n");
        }

        // body를 가져 올 수 있으면 출력.
        final String requestJsonBody = getRequestBodyString(request);
        if (StringUtils.isNotEmpty(requestJsonBody)) {
            String token = request.getHeader("Authorization");
            String json = CustomStringUtil.decode(token, requestJsonBody);
            String uuid = UUID.randomUUID().toString();
            sb.append(String.format("* key: %s\n", uuid));
            if (StringUtils.isNotEmpty(json)) {
                sb.append(String.format("* encrypted body: %s\n", envUtil.isPrd() ? CryptAES.encode(uuid, json) : json));
            } else {
                sb.append(String.format("* plain body: %s\n", envUtil.isPrd() ? CryptAES.encode(uuid, requestJsonBody) : requestJsonBody));
            }
        }

        getHeaders().forEach((key, value) -> {
            if (key.startsWith("cm-")
                    // || key.startsWith("authorization")
                    || key.startsWith("accept-language")
                    // || key.startsWith("x-request-id")
                    || key.startsWith("user-agent")) {
                sb.append("* ").append(key).append(" : ").append(value).append("\n");
            }
        });

        sb.append("message: ").append(message);
        return sb.toString();
    }

}
