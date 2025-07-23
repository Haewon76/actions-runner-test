package com.cashmallow.api.application.impl;

import com.cashmallow.api.application.*;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.enums.Country3;
import com.cashmallow.api.domain.model.country.enums.CountryCode;
import com.cashmallow.api.domain.model.exchange.Exchange;
import com.cashmallow.api.domain.model.geo.GeoLocation;
import com.cashmallow.api.domain.model.notification.*;
import com.cashmallow.api.domain.model.refund.NewRefund;
import com.cashmallow.api.domain.model.remittance.Remittance;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserMapper;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.email.EmailServiceImpl;
import com.cashmallow.api.infrastructure.fcm.FcmEventCode;
import com.cashmallow.api.infrastructure.fcm.FcmEventValue;
import com.cashmallow.api.infrastructure.fcm.dto.FcmMessageDto;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.common.CustomStringUtil;
import com.cashmallow.common.geoutil.GeoUtil;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.openhtmltopdf.extend.FSSupplier;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.helper.W3CDom;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import java.awt.*;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

import static com.cashmallow.api.domain.model.country.enums.CountryCode.*;
import static com.cashmallow.api.domain.shared.MsgCode.INTERNAL_SERVER_ERROR;
import static com.cashmallow.common.CommonUtil.*;
import static java.util.stream.Collectors.*;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private Random random = new SecureRandom();

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Value("${host.url}")
    private String hostUrl;

    @Value("${host.file.path.home}")
    private String hostFilePathHome;

    @Autowired
    private FileService fileService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private FcmService fcmService;

    @Autowired
    private UserVerifyEmailService userVerifyEmailService;

    @Autowired
    private SecurityService securityService;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private GeoUtil geoUtil;

    @Autowired
    private MessageSource messageSource;

    //-------------------------------------------------------------------------------
    // 70. FCM
    //-------------------------------------------------------------------------------

    // 기능: 70.1. Client의 token을 등록/갱신한다.
    @Override
    @Transactional(rollbackFor = CashmallowException.class)
    public void addFcmToken(long userId, String fcmToken, String devType) throws CashmallowException {

        String method = "addFcmClientToken()";

        logger.info("{}: userId={}, devType={}", method, userId, devType);

        if (userId != Const.NO_USER_ID && fcmToken != null && !fcmToken.isEmpty() && devType != null && !devType.isEmpty()) {

            HashMap<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("fcmToken", fcmToken);
            params.put("devType", devType);

            int affectedRow = notificationMapper.addFcmToken(params);

            if (affectedRow <= 0) {
                logger.warn("{}: Failed to update fcm_token table. affectedRow={}", method, affectedRow);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }
        } else {
            logger.error("{}: Invalid parameters. fcmToken={}, devType={}", method, fcmToken, devType);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

    }

    private String getFcmTitle(String langKey, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId) {
        String method = "getFcmTitle()";

        String title = "";
        String messageCode = "FCM_" + eventCode + "_" + eventValue;

        // orgId 에 의미가 있는 경우. 본인 또는 계좌 인증 보류 인 경우만 타이틀을 수정한다.
        if (FcmEventCode.AU == eventCode) {
            messageCode += "_" + orgId;
        }

        messageCode += "_TITLE";

        Locale locale = new Locale(langKey.substring(0, 2));

        title = messageSource.getMessage(messageCode, null, "", locale);

        if (StringUtils.isEmpty(title)) {
            locale = new Locale("en");
            title = messageSource.getMessage(messageCode, null, "", locale);
        }

        if (StringUtils.isEmpty(title)) {
            logger.warn("{}: Cannot find message. messageCode={}", method, messageCode);
        }

        return title;
    }

    private String getFcmMessage(String langKey, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, String message) {
        String method = "getFcmMessage()";

        if (!StringUtils.isEmpty(message)) {
            return message;
        }

        String messageCode = "FCM_" + eventCode + "_" + eventValue;

        // orgId 에 의미가 있는 경우
        if (FcmEventCode.AU == eventCode) {
            messageCode += "_" + orgId;
        }

        Locale locale = new Locale(langKey.substring(0, 2));

        message = messageSource.getMessage(messageCode, null, "", locale);

        if (StringUtils.isEmpty(message)) {
            locale = new Locale("en");
            message = messageSource.getMessage(messageCode, null, "", locale);
        }

        if (StringUtils.isEmpty(message)) {
            logger.error("{}: annot find message. messageCode={}", method, messageCode);
            return null;
        }

        return message;
    }

    @Override
    public void sendFcmNotificationMsgAsync(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, String message) {
        String method = "sendFcmNotificationMsgAsync()";

        if (user == null || ObjectUtils.isEmpty(eventCode) || ObjectUtils.isEmpty(eventValue)) {
            logger.error("{} : Invalid parameters. eventCode={}, eventValue={}", method, eventCode, eventValue);
            return;
        }

        logger.info("{} : userId={}, eventCode={}, eventValue={}, orgId={}", method, user.getId(), eventCode, eventValue, orgId);

        // set body message
        String body = getFcmMessage(user.getLangKey(), eventCode, eventValue, orgId, message);

        // get FCM token for user
        Map<String, String> tokenMap = notificationMapper.getFcmTokenByUserId(user.getId());

        if (tokenMap == null || tokenMap.isEmpty()) {
            logger.warn("{}: Cann't find fcm_token. userId={}", method, user.getId());
            return;
        }

        String token = tokenMap.get("fcm_token");
        String devType = tokenMap.get("dev_type");
        String title = getFcmTitle(user.getLangKey(), eventCode, eventValue, orgId);

        // Reference : https://firebase.google.com/docs/cloud-messaging/android/receive#restricted
        if ("A".equals(devType)) {
            fcmService.postMsgToFcmV1Android(token, title, body, eventCode, eventValue, String.valueOf(orgId));
        } else {
            fcmService.postMsgToFcmV1Ios(token, title, body, eventCode, eventValue, String.valueOf(orgId));
        }
    }

    @Override
    public void sendFcmNotification(List<User> users, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId, String message) throws FirebaseMessagingException {
        String method = "sendFcmNotification()";

        if (users == null || users.isEmpty() || eventCode == null || eventValue == null) {
            logger.error("{} : Invalid parameters. eventCode={}, eventValue={}", method, eventCode, eventValue);
            return;
        }

        logger.info("{} : eventCode={}, eventValue={}, orgId={}", method, eventCode, eventValue, orgId);

        Map<Long, FcmToken> userTokenMap = getUserTokens(users);
        Map<String, Map<String, List<User>>> langDeviceGroupMap = users.stream()
                .filter(u -> userTokenMap.containsKey(u.getId()))
                .collect(groupingBy(User::getLangKey,
                        groupingBy(u -> userTokenMap.get(u.getId()).getDevType())));

        for (String lang : langDeviceGroupMap.keySet()) {
            String title = getFcmTitle(lang, eventCode, eventValue, orgId);
            String body = getFcmMessage(lang, eventCode, eventValue, orgId, message);
            FcmMessageDto fcmMessageDto = FcmMessageDto.of(title, body, eventCode, eventValue);

            for (String device : langDeviceGroupMap.get(lang).keySet()) {
                List<String> userTokens = langDeviceGroupMap.get(lang).get(device).stream()
                        .map(u -> userTokenMap.get(u.getId()).getFcmToken())
                        .collect(toList());

                if ("A".equals(device)) {
                    fcmService.sendFcmMessageMulticastAndroid(fcmMessageDto, userTokens);
                } else {
                    fcmService.sendFcmMessageMulticastIos(fcmMessageDto, userTokens);
                }
            }
        }
    }

    private Map<Long, FcmToken> getUserTokens(List<User> users) {
        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(toList());
        return notificationMapper.getFcmTokensByUserIds(userIds)
                .stream()
                .collect(toMap(FcmToken::getUserId, fcmToken -> fcmToken));
    }

    @Override
    public void sendFcmNotificationMsgAsync(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId) {
        sendFcmNotificationMsgAsync(user, eventCode, eventValue, orgId, null);
    }

    @Override
    public void addFcmNotificationMsg(User user, FcmEventCode eventCode, FcmEventValue eventValue, Long orgId) {
        String method = "addFcmNotificationMsg()";

        if (user == null || ObjectUtils.isEmpty(eventCode) || ObjectUtils.isEmpty(eventValue)) {
            logger.error("{} : Invalid parameters", method);
            return;
        }

        logger.info("{}: userId={}, eventCode={}, eventValue={}, orgId={}", method, user.getId(), eventCode, eventValue, orgId);

        try {
            HashMap<String, Object> params = new HashMap<>();
            params.put("user_id", user.getId());
            params.put("event_code", eventCode); // event 코드
            params.put("event_value", eventValue); // event 값
            params.put("org_id", orgId); // event 연관 id
            int queryResult = notificationMapper.addFcmNotificationMsg(params);

            logger.info("{}: addFcmNotificationMsg result={}", method, queryResult);

            Map<String, String> tokenMap = notificationMapper.getFcmTokenByUserId(user.getId());

            if (tokenMap == null || tokenMap.isEmpty()) {
                logger.warn("{}: Cann't find fcm_token. userId={}", method, user.getId());
                return;
            }

            String token = tokenMap.get("fcm_token");
            String devType = tokenMap.get("dev_type");

            String title = getFcmTitle(user.getLangKey(), eventCode, eventValue, orgId);

            String message = getFcmMessage(user.getLangKey(), eventCode, eventValue, orgId, null);
            if (StringUtils.isEmpty(message)) {
                logger.error("{}: cannot find message. eventCode={}, eventValue={}, orgId={}", method, eventCode, eventValue, orgId);
                return;
            }

            if ("A".equals(devType)) {
                fcmService.postMsgToFcmV1Android(token, title, message, eventCode, eventValue, String.valueOf(orgId));
            } else {
                fcmService.postMsgToFcmV1Ios(token, title, message, eventCode, eventValue, String.valueOf(orgId));
            }
        } catch (Exception e) {
            logger.error("addFcmNotificationMsg: " + e.getMessage(), e);
        }
    }

    // 기능: 오래된 알림 메시지 삭제 처리
    @Override
    public ApiResultVO removeFcmNotification() {
        boolean flagSetResult = true;
        String method = "removeFcmNotification(): ";
        String error = "";
        Object obj = null;
        logger.info(method);
        ApiResultVO result = new ApiResultVO(Const.CODE_FAILURE);

        try {
            int affectedRow = notificationMapper.removeFcmNotification();
            logger.info("{}: deletedRows={}", method, affectedRow);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            error = "오래된 알림 메시지 삭제 중 오류가 발생했습니다.";
        }

        result.setSuccessOrFail(flagSetResult, obj, error);
        return result;
    }

    @Override
    public String sendEmailToAuth(String emailTo, CountryCode countryCode) throws CashmallowException {
        String method = "sendEMailToAuth()";

        String value = "";

        if (StringUtils.isEmpty(emailTo)) {
            logger.error("{}: parameter is empty. emailTo={}", method, emailTo);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {
            // 메일 내용
            value = String.valueOf(this.random.nextInt(900000) + 100000);

            String subject = String.format("%s %s : %s",
                    getMessageByCountry(messageSource, countryCode, "MAIL_SUBJECT_PREFIX"),
                    getMessageByCountry(messageSource, countryCode, "MAIL_SUBJECT_EMAIL_AUTH"),
                    value);

            Context context = new Context();
            context.setVariable("value", value);

            String text = templateEngine.process(getTemplateByCountry(countryCode, "email_auth"), context);

            // insert email auth code
            userVerifyEmailService.addEmailCertNum(emailTo, value);
            emailService.sendMail(emailTo, subject, text);

        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

        return value;
    }

    @Override
    public String sendEmailToResetPassword(User user, String emailTo, Locale locale) throws CashmallowException {
        String method = "sendEmailToResetPassword()";

        if (StringUtils.isEmpty(emailTo)) {
            logger.error("{}: parameter is empty. emailTo={}", method, emailTo);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        final String randomToken = getRandomToken();
        String resetUrl = hostUrl + "/auth/email/verify-captcha";

        try {
            // 메일 제목
            String subject = String.format("%s %s",
                    getMessageByCountry(messageSource, user.getCountryCode(), "MAIL_SUBJECT_PREFIX"),
                    getMessageByCountry(messageSource, user.getCountryCode(), "MAIL_SUBJECT_RESET_PASSWORD"));

            // 메일 내용
            Context context = new Context();

            String text;

            // 임시 토큰 생성 후 DB에 저장 + 로그인 실패 횟수 저장 + 로그인 시도 상태(BLOCK, NORMAL) 체크

            userVerifyEmailService.addVerifiedEmailPassword(new EmailTokenVerity(randomToken, user.getId(), user.getLoginFailCount(), EmailVerityType.findByType(user.getLoginFailCount())));

            context.setVariable("email", emailTo);
            context.setVariable("token", randomToken);
            context.setVariable("resetUrl", resetUrl);

            text = templateEngine.process(getTemplateByCountry(user.getCountryCode(), "reset_password_request"), context);

            emailService.sendMail(emailTo, subject, text);

        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("메일 템플릿 생성 실패 : " + e.getMessage(), e);
            throw new CashmallowException(e.getMessage(), e);
        }

        return resetUrl + "?token=" + randomToken;
    }

    @Override
    public boolean isNotAvailableEmailVerify(Long userId) {
        return !notificationMapper.isAvailableEmailVerify(userId);
    }

    @Override
    public boolean isNotAvailablePassword(EmailTokenVerity verity) {
        return notificationMapper.isNotAvailablePassword(verity);
    }

    @Override
    public String getBankAccountConfirmView(User user, Locale locale) {
        Context context = new Context();
        if (user != null) {
            context.setVariable("isSuccess", "Y");
        } else {
            context.setVariable("isSuccess", "N");
        }

        return templateEngine.process(getTemplateByCountry(user.getCountryCode(), "bank_account_confirm"), context);
    }

    /**
     * 환전 완료 후 이메일 발송
     *
     * @param user
     * @return
     * @throws Exception
     */
    @Override
    public void sendEmailConfirmExchange(User user, Traveler traveler, Exchange exchange, Country fromCountry, Country toCountry)
            throws CashmallowException {

        final String method = "sendEmailConfirmExchange()";

        String emailTo = user.getEmail();
        Locale locale = Locale.ENGLISH;

        String langKey = user.getLangKey();
        if (!StringUtils.isEmpty(langKey) && langKey.length() >= 2) {
            locale = new Locale(langKey.substring(0, 2));
        }

        if (StringUtils.isEmpty(emailTo)) {
            logger.error("{}: parameter is empty. emailTo={}", method, emailTo);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {

            // 메일 내용
            String subject = String.format("%s %s",
                    getMessageByCountry(messageSource, user.getCountryCode(), "MAIL_SUBJECT_PREFIX"),
                    getMessageByCountry(messageSource, user.getCountryCode(), "MAIL_SUBJECT_CONFIRM_EXCHANGE"));

            Context mailContext = new Context();

            String text = templateEngine.process(getTemplateByCountry(user.getCountryCode(), "confirm_exchange"), mailContext);

            if (exchange.getFromCd().equals(HK.getCode()) || exchange.getFromCd().equals(KR.getCode()) || exchange.getFromCd().equals(JP.getCode())) {
                sendExchangeReceiptMail(traveler, exchange, fromCountry, toCountry, method, emailTo, subject, text);
            }

        } catch (MessagingException | FontFormatException | IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailConfirmRemittance(User user, Traveler traveler, Remittance remittance, Country fromCountry, Country toCountry)
            throws CashmallowException {

        final String method = "sendEmailConfirmRemittance()";

        String userEmail = user.getEmail();
        Locale locale = user.getCountryLocale();

        final String counrtyCode = remittance.getFromCd();

        if (StringUtils.isEmpty(userEmail)) {
            logger.error("{}: parameter is empty. emailTo={}", method, userEmail);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {
            // 메일 내용
            String subject = String.format("%s %s",
                    messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                    messageSource.getMessage("MAIL_SUBJECT_CONFIRM_REMITTANCE", null, "", locale));

            Context mailContext = new Context();

            String text = templateEngine.process(getTemplateByCountry(CountryCode.of(counrtyCode), "remittance_confirm"), mailContext);

            if (counrtyCode.equals(HK.getCode()) || counrtyCode.equals(JP.getCode())) {
                sendRemittanceReceiptMail(traveler, remittance, fromCountry, toCountry, method, userEmail, subject, text);
            }

        } catch (MessagingException | FontFormatException | IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailPrivacyPolicy(User user) {
        String emailTo = user.getEmail();
        // emailTo = "jd@cashmallow.com";

        if (StringUtils.isEmpty(emailTo)) {
            return;
        }

        try {
            // 메일 내용
            String subject = String.format("%s %s",
                    messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", Locale.ENGLISH),
                    "有關個人資料處理方針的修訂通知 Notice of Amendment on Privacy Policy");

            Context context = new Context();
            String text = templateEngine.process("mail-templates/en/privacy_policy", context);

            emailService.sendMail(emailTo, subject, text);
            logger.info("sent email - userId: {}, email: {}", user.getId(), user.getEmail());
        } catch (Exception e) {
            logger.error("Failed to send email for privacy policy: {}, userId: {}, email: {}", e.getMessage(), user.getId(), user.getEmail());
        }
    }

    private void sendExchangeReceiptMail(Traveler traveler, Exchange exchange, Country fromCountry, Country toCountry,
                                         final String method, String userEmail, String subject, String text)
            throws FontFormatException, IOException, InterruptedException, MessagingException, CashmallowException {

        Graphics2D g;
        ClassLoader classLoader = EmailServiceImpl.class.getClassLoader();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());

        String name = "";
        if ("Y".equals(fromCountry.getIsFamilyNameAfterFirstName())) {
            name = traveler.getEnFirstName() + " " + traveler.getEnLastName();
        } else {
            name = traveler.getEnLastName() + " " + traveler.getEnFirstName();
        }

        DecimalFormat fromDF = new DecimalFormat(
                fromCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");
        DecimalFormat toDF = new DecimalFormat(
                toCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");

        int p = 2;
        double d = Math.floor(Math.log10(exchange.getExchangeRate().doubleValue())) + 1;
        if (d < 2) {
            // Set significant digits to 4 digits
            p = (int) (-d + 4);
        }

        BigDecimal fromAmtWithSpread = exchange.getFromAmt().subtract(exchange.getFeePerAmt());

        BigDecimal exchangeRateWithSpread = fromAmtWithSpread.divide(exchange.getToAmt(), 6, RoundingMode.HALF_UP);

        String exchangeRate = exchangeRateWithSpread.setScale(p, RoundingMode.HALF_UP).toString();
        String discountAmount = ObjectUtils.isEmpty(exchange.getCouponDiscountAmt()) ? "0" : fromDF.format(exchange.getCouponDiscountAmt());

        Context context = new Context();
        context.setVariable("receiptNo", "E" + exchange.getId());
        context.setVariable("date", date);
        context.setVariable("travelerName", name);
        context.setVariable("travelerAddress", traveler.getAddress() + " " + traveler.getAddressSecondary());
        context.setVariable("email", userEmail);
        context.setVariable("fromPrincipal", fromDF.format(exchange.getFromAmt().subtract(exchange.getFeePerAmt())));
        context.setVariable("fromAmt", fromDF.format(exchange.getFromAmt()));
        context.setVariable("toIso4217", toCountry.getIso4217());
        context.setVariable("toAmt", toDF.format(exchange.getToAmt()));
        context.setVariable("discountAmt", discountAmount);
        context.setVariable("fee", fromDF.format(exchange.getFeePerAmt()));
        context.setVariable("exchange_rate", exchangeRate);
        context.setVariable("cashmallowCiImage", classLoader.getResource("images/cashmallow_simbol.png"));

        String receipt = null;
        String receiptFileName = "RECEIPT_No_";

        if (HK.getCode().equals(exchange.getFromCd())) {

            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_hk.png"));

            receipt = templateEngine.process("receipt-templates/hkg/confirm_exchange_receipt_v2", context);

        } else if (JP.getCode().equals(exchange.getFromCd())) {
            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_jp.png"));

            receipt = templateEngine.process("receipt-templates/jpn/confirm_exchange_receipt_v2", context);
        }

        // logger.info("{}: receipt={}", method, receipt);

        Document document = Jsoup.parse(receipt);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        logger.debug("document = {}", document);

        // get the byte array of the image (as jpeg)
        String fileServerDirPath = hostFilePathHome + Const.FILE_SERVER_CMRECEIPT;
        String filePath = fileServerDirPath + File.separator + receiptFileName + "E" + exchange.getId() + ".pdf";
        File attacedFile = new File(filePath);

        try (OutputStream os = new FileOutputStream(attacedFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withFile(attacedFile);
            builder.toStream(os);
            FSSupplier<InputStream> droidFssSupplier = () -> {
                logger.info("{}: Requesting font DroidSansFallback", method);

                return getClass().getClassLoader().getResourceAsStream("fonts/DroidSansFallback.ttf");
            };
            builder.useFont(droidFssSupplier, "Droid Sans Fallback");
            String baseUrl = getClass().getClassLoader().getResource("").toString();
            builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
            builder.run();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        File parentPath = attacedFile.getParentFile();
        if (parentPath.exists() || parentPath.mkdirs()) {
            sendEmailWithCmReceipt(userEmail, subject, text, attacedFile);
        } else {
            logger.error("{}: Failed to create file.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

    }

    private void sendEmailWithCmReceipt(String emailTo, String subject, String text,
                                        File attachedFile) throws IOException, MessagingException,
            CashmallowException {

        String method = "sendEmailWithCmReceipt()";

        logger.info("{}: emailTo={}, subject={}", method, emailTo, subject);

        // Send email
        emailService.sendMail(emailTo, subject, text, attachedFile);

        // Upload file
        fileService.upload(attachedFile, Const.FILE_SERVER_CMRECEIPT);

    }

    private void sendRemittanceReceiptMail(Traveler traveler, Remittance remittance, Country fromCountry, Country toCountry,
                                           final String method, String userEmail, String subject, String text)
            throws FontFormatException, IOException, InterruptedException, MessagingException,
            CashmallowException {

        ClassLoader classLoader = EmailServiceImpl.class.getClassLoader();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());

        String travelerName = "";
        String receiverName = "";
        if ("Y".equals(fromCountry.getIsFamilyNameAfterFirstName())) {
            travelerName = traveler.getEnFirstName() + " " + traveler.getEnLastName();
            receiverName = remittance.getReceiverFirstName() + " " + remittance.getReceiverLastName();
        } else {
            travelerName = traveler.getEnLastName() + " " + traveler.getEnFirstName();
            receiverName = remittance.getReceiverLastName() + " " + remittance.getReceiverFirstName();
        }

        DecimalFormat fromDF = new DecimalFormat(
                fromCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");
        DecimalFormat toDF = new DecimalFormat(
                toCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");

        int p = 2;
        double d = Math.floor(Math.log10(remittance.getExchangeRate().doubleValue())) + 1;
        if (d < 2) {
            // Set significant digits to 4 digits
            p = (int) (-d + 4);
        }

        BigDecimal fromAmtWithSpread = remittance.getFromAmt().subtract(remittance.getFeePerAmt());

        BigDecimal exchangeRateWithSpread = fromAmtWithSpread.divide(remittance.getToAmt(), 6, RoundingMode.HALF_UP);

        String exchangeRate = exchangeRateWithSpread.setScale(p, RoundingMode.HALF_UP).toString();
        String discountAmount = ObjectUtils.isEmpty(remittance.getCouponDiscountAmt()) ? "0" : fromDF.format(remittance.getCouponDiscountAmt());
        String receiverPhoneNo = Country3.ofAlpha3(remittance.getReceiverPhoneCountry()).getCalling() + remittance.getReceiverPhoneNo().replaceFirst("^0", "");

        String receiverBankName = getReceiverBankNameForReceipt(remittance);

        Context context = new Context();
        context.setVariable("travelerName", travelerName);
        context.setVariable("travelerAddress", traveler.getAddress() + " " + traveler.getAddressSecondary());
        context.setVariable("date", date);
        context.setVariable("email", userEmail);
        context.setVariable("receiptNo", "RM" + remittance.getId());
        context.setVariable("toIso4217", toCountry.getIso4217());
        context.setVariable("fromPrincipal", fromDF.format(remittance.getFromAmt().subtract(remittance.getFeePerAmt())));
        context.setVariable("discountAmt", discountAmount);
        context.setVariable("fromAmt", fromDF.format(remittance.getFromAmt()));
        context.setVariable("toAmt", toDF.format(remittance.getToAmt()));
        context.setVariable("exchange_rate", exchangeRate);
        context.setVariable("fee", fromDF.format(remittance.getFeePerAmt()));

        context.setVariable("receiverName", receiverName);
        context.setVariable("receiverPhoneNo", receiverPhoneNo);
        context.setVariable("receiverAddress", remittance.getReceiverAddressSecondary() + ", " + remittance.getReceiverAddress());
        context.setVariable("toCountryName", toCountry.getEngName());

        context.setVariable("receiverEmail", remittance.getReceiverEmail());
        context.setVariable("receiverBankName", getReceiverBankNameForReceipt(remittance));
        context.setVariable("receiverAccountName", receiverName);
        context.setVariable("receiverAccountNo", remittance.getReceiverBankAccountNo());
        context.setVariable("cashmallowCiImage", classLoader.getResource("images/cashmallow_simbol.png"));

        String receipt = null;
        String receiptFileName = "RECEIPT_No_";

        if (HK.getCode().equals(remittance.getFromCd())) {
            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_hk.png"));
            context.setVariable("purpose", remittance.getRemitPurpose().getEnDecription());
            context.setVariable("fundSource", remittance.getRemitFundSource().getEnDecription());

            receipt = templateEngine.process("receipt-templates/hkg/remittance_confirm_receipt_v2", context);
        } else if (JP.getCode().equals(remittance.getFromCd())) {
            String relationship = "";
            if (ObjectUtils.isNotEmpty(remittance.getRemitRelationship())) {
                relationship =  messageSource.getMessage(remittance.getRemitRelationship().getMessageCode(), null,  new Locale("ja"));
            }

            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_jp.png"));
            context.setVariable("purpose", remittance.getRemitPurpose().getJpDecription());
            context.setVariable("fundSource", remittance.getRemitFundSource().getJpDecription());
            context.setVariable("relationship", relationship);

            receipt = templateEngine.process("receipt-templates/jpn/remittance_confirm_receipt_v2", context);
        }

        // logger.info("{}: receipt={}", method, receipt);

        Document document = Jsoup.parse(receipt);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        // logger.info("document = {}", document);

        // get the byte array of the image (as jpeg)
        String fileServerDir = hostFilePathHome + Const.FILE_SERVER_CMRECEIPT;
        File attacedFile = new File(fileServerDir + File.separator + receiptFileName + "RM" + remittance.getId() + ".pdf");

        try (OutputStream os = new FileOutputStream(attacedFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withFile(attacedFile);
            builder.toStream(os);
            FSSupplier<InputStream> droidFssSupplier = () -> {
                logger.info("{}: Requesting font DroidSansFallback", method);

                return getClass().getClassLoader().getResourceAsStream("fonts/DroidSansFallback.ttf");
            };
            builder.useFont(droidFssSupplier, "Droid Sans Fallback");
            String baseUrl = getClass().getClassLoader().getResource("").toString();
            builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
            builder.run();
            logger.info("파일 저장 완료 receiptFileName={}", receiptFileName);
        } catch (Exception e) {
            logger.info("document = {}", document);
            logger.error(e.getMessage(), e);
        }

        File parentPath = attacedFile.getParentFile();
        if (parentPath.exists() || parentPath.mkdirs()) {
            sendEmailWithCmReceipt(userEmail, subject, text, attacedFile);
        } else {
            logger.error("{}: Failed to create file.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    private String getReceiverBankNameForReceipt(Remittance remittance) {
        if (!StringUtils.isEmpty(remittance.getReceiverRoutingNumber())) {
            return remittance.getReceiverRoutingNumber();
        } else if (!StringUtils.isEmpty(remittance.getReceiverSwiftCode())) {
            return remittance.getReceiverSwiftCode();
        } else if (!StringUtils.isEmpty(remittance.getReceiverIfscCode())) {
            return remittance.getReceiverIfscCode();
        } else if (!StringUtils.isEmpty(remittance.getReceiverCardNumber())) {
            return remittance.getReceiverCardNumber();
        } else if (remittance.getToCountry().equals(AU) && !StringUtils.isEmpty(remittance.getReceiverBankCode())) {
            String[] bankCode = remittance.getReceiverBankCode().split("-");
            if (bankCode.length <= 1) {
                return remittance.getReceiverBankName();
            }
            return bankCode[1];
        }

        return remittance.getReceiverBankName();
    }

    /**
     * Send email to warn new device
     *
     * @param user
     * @param deviceInfo
     * @param ip
     */
    @Override
    public void sendEmailToWarnNewDevice(User user, String deviceInfo, String ip) throws CashmallowException {

        final String method = "sendEmailToWarnNewDevice()";

        Locale locale = user.getCountryLocale();
        String emailTo = user.getEmail();       // 이메일 수신자, 보조 이메일이 추가되면 여기에 추가한다.
        String accountEmail = user.getEmail();  // 사용자 계정

        Map<String, String> accessInfo = getAccessInfo(ip);

        logger.info("{}: accessInfo={}", method, accessInfo);

        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_WARN_NEW_DEVICE", null, "", locale));

        Context context = new Context();
        context.setVariable("accountEmail", accountEmail);
        context.setVariable("deviceInfo", deviceInfo);
        context.setVariable("location", accessInfo.get("location"));
        context.setVariable("accessTime", accessInfo.get("accessTime"));
        context.setVariable("ip", ip);

        try {
            String text = templateEngine.process(getTemplateByCountry(user.getCountryCode(), "warn_new_device"), context);
            emailService.sendMail(emailTo, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

    }

    @Transactional(rollbackFor = CashmallowException.class)
    public void sendEmailToResetDevice(User user, String deviceInfo, String ip) throws CashmallowException {
        final boolean isValidPinCodeDeviceReset = isValidPinCodeDeviceReset();

        final String method = "sendMailToResetDevice()";

        String emailTo = user.getEmail();       // 이메일 수신자, 보조 이메일이 추가되면 여기에 추가한다.
        String accountEmail = user.getEmail();  // 사용자 계정
        Locale locale = user.getCountryLocale();

        String accountToken = CustomStringUtil.randomUuidStr();

        EmailToken emailToken = new EmailToken(user.getId(), user.getCls(), accountToken);

        removeEmailTokenByUserId(user.getId());
        int affectedRow = addEmailToken(emailToken);

        if (affectedRow != 1) {
            throw new CashmallowException("Error! The email token did not be created.");
        }

        Map<String, String> accessInfo = getAccessInfo(ip);

        logger.info("{}: accessInfo={}", method, accessInfo);

        StringBuilder url = new StringBuilder(hostUrl);
        url.append(URLs.CONFIRM_DEVICE_RESET_CAPTCHA);

        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_RESET_DEVICE", null, "", locale)
        );

        if (isValidPinCodeDeviceReset) {
            subject = String.format("%s %s : %s",
                    messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                    messageSource.getMessage("MAIL_SUBJECT_RESET_DEVICE", null, "", locale),
                    emailToken.getCode()
            );
        }

        Context context = new Context();
        context.setVariable("resetURL", url);
        context.setVariable("id", user.getId());
        context.setVariable("accountToken", accountToken);
        context.setVariable("accountEmail", accountEmail);
        context.setVariable("deviceInfo", deviceInfo);
        context.setVariable("location", accessInfo.get("location"));
        context.setVariable("accessTime", accessInfo.get("accessTime"));
        context.setVariable("ip", ip);
        context.setVariable("code", emailToken.getCode());

        try {
            String text = templateEngine.process(getTemplateByCountry(user.getCountryCode(), isValidPinCodeDeviceReset ? "reset_device_code" : "reset_device"), context);
            emailService.sendMail(emailTo, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new CashmallowException(e.getMessage(), e);
        }

    }

    /**
     * 기기 변경 초기화 성공, 실패 메시지, User의 가입국가 언어 설정을 따라감.
     *
     * @param isSuccess
     * @param user
     * @return
     */
    @Override
    public String getResetDeviceConfirmView(boolean isSuccess, User user) {
        Context context = new Context();

        CountryCode countryCode = HK;
        long userId = -1;
        if (user != null) {
            countryCode = user.getCountryCode();
            userId = user.getId();
        }

        logger.info("getResetDeviceConfirmView(): userId={}, isSuccess={}, locale={}", userId, isSuccess, countryCode.getName());

        if (isSuccess) {
            context.setVariable("isSuccess", "Y");
        } else {
            context.setVariable("isSuccess", "N");
        }
        return templateEngine.process(getTemplateByCountry(countryCode, "reset_device_confirm"), context);
    }

    /**
     * Send email to warn new device
     *
     * @param user
     * @param deviceInfo
     * @param ip
     */
    @Override
    public void sendEmailToNotifyDormantUser(long userId, String email) throws CashmallowException {

        final String method = "sendEmailToNotifyDormantUser()";

        // 휴면계정 처리는 한국 거주자만 대상으로 하므로 한국어 메일만 전송함.
        Locale locale = Locale.KOREAN;

        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_NOTIFY_DORMANT_USER", null, "", locale));

        Context context = new Context();

        String text = templateEngine.process(getTemplateByCountry(KR, "notify_dormant_user"), context);

        try {
            emailService.sendMail(email, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("{}: Failed to send email. userId={}, email={}", method, userId, email);
            throw new CashmallowException(e.getMessage(), e);
        }

    }

    /**
     * Get user's location and localized access time by access IP address
     *
     * @param ip
     * @return Map<String, String> : location, accessTime
     */
    public Map<String, String> getAccessInfo(String ip) {
        Map<String, String> map = new HashMap<>();

        String location = geoUtil.getMyLocation(ip);
        String timeZone = "+00:00";
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));

        final GeoLocation myCountryCode = geoUtil.getMyCountryCode(ip);
        if (myCountryCode != null && !"-".equals(myCountryCode.getCity()) && StringUtils.isNotBlank(myCountryCode.getCountryLong())) {
            timeZone = myCountryCode.getTimezone();
            now = ZonedDateTime.now(ZoneId.of(timeZone));
        }

        final String accessTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " [" + timeZone + "]";

        map.put("location", location);
        map.put("accessTime", accessTime);

        return map;
    }

    @Transactional
    public EmailToken getEmailToken(Long userId) {
        return notificationMapper.getEmailToken(userId);
    }

    @Override
    @Transactional
    public int addEmailToken(EmailToken emailToken) {
        return notificationMapper.insertEmailToken(emailToken);
    }

    @Override
    @Transactional
    public int removeEmailTokenByUserId(Long userId) {
        return notificationMapper.deleteEmailTokenByUserId(userId);
    }

    @Override
    @Transactional
    public int removeEmailToken(EmailToken emailToken) {
        return notificationMapper.deleteEmailToken(emailToken);
    }


    @Override
    @Transactional
    public EmailTokenVerity getVerifiedEmailPassword(String token) {
        return notificationMapper.getVerifiedEmailPassword(token);
    }

    @Override
    public EmailTokenVerity getVerifiedEmailCode(String token) {
        return notificationMapper.getVerifiedEmailCode(token);
    }

    @Transactional
    @Override
    public EmailTokenVerity passwordResetAndVerity(String token) {
        final EmailTokenVerity emailTokenVerity = notificationMapper.getVerifiedEmailPassword(token);
        if (emailTokenVerity == null) {
            return null;
        }

        emailTokenVerity.setPassword(securityService.decryptAES256(emailTokenVerity.getPassword()));

        // http://localhost:10000/api/auth/email/verify?token=2c36731b655eeb96461c9a5db23f7d19
        // 이미 비밀번호가 입력되어있는 경우 새로운 비밀번호로 갱신하지 않음
        if (StringUtils.isEmpty(emailTokenVerity.getPassword())) {
            // expire token and update user password
            final String password = generateResetRandomPassword();
            emailTokenVerity.setToken(token);

            // email_verify table에서는 복호화 할 수 있도록 SHA256으로 저장한다
            // 이메일 인증 여러번 클릭하더라도 비밀번호가 계속 바뀌지 않도록 한다
            emailTokenVerity.setPassword(securityService.encryptAES256(password));
            notificationMapper.updateVerifiedEmailPassword(emailTokenVerity);

            // update login_fail_count = 0, password_hash in user table
            emailTokenVerity.setPassword(securityService.encryptSHA2(password));
            userMapper.updateUserPassword(emailTokenVerity);

            emailTokenVerity.setPassword(password);
        }

        return emailTokenVerity;
    }

    @Override
    public void sendEmailReRegisterReceipt(User user) throws CashmallowException {
        final String method = "sendEmailConfirmRemittance()";

        String email = user.getEmail();

        if (StringUtils.isEmpty(email)) {
            logger.error("{}: parameter is empty. email={}", method, email);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 메일 내용
        String subject = "[Cashmallow] Account reconciliation failed, please contact customer service center.";

        Context mailContext = new Context();
        String text = templateEngine.process("mail-templates/en/reregister_receipt", mailContext);

        try {
            emailService.sendMail(email, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("{}: Failed to send email. userId={}, email={}", method, user.getId(), email);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    private CountryCode getUserCountryByEmail(String email, Locale locale) {
        final User userByEmail = userMapper.getUserByEmail(email);
        if (userByEmail == null) {
            if (Locale.KOREAN.getLanguage().equals(locale.getLanguage())) {
                return CountryCode.KR;
            }

            return CountryCode.HK;
        }

        return CountryCode.of(userByEmail.getCountry());
    }

    @Override
    public void sendEmailConfirmNewRefundForRemittance(User user, Traveler traveler, NewRefund refund,
                                                       Country fromCountry, Country toCountry, Remittance remittance) throws CashmallowException {

        final String method = "sendEmailConfirmNewRefundForRemittance()";

        ClassLoader classLoader = EmailServiceImpl.class.getClassLoader();

        String emailTo = user.getEmail();
        Locale locale = user.getCountryLocale();

        if (!StringUtils.isNotBlank(emailTo)) {
            logger.error("{}: parameter is empty. emailTo={}", method, emailTo);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        try {

            // 메일 내용
            String subject = String.format("%s %s",
                    messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                    messageSource.getMessage("MAIL_SUBJECT_CONFIRM_REFUND", null, "", locale));

            Context context = new Context();

            String text = "";

            if (refund.getToCd().equals(HK.getCode())) {
                text = templateEngine.process(getTemplateByCountry(HK, "confirm_refund"), context);
            }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String date = sdf.format(new Date());

            String name = "";
            // 환불은 toCountry가 서비스하는 국가
            if ("Y".equals(toCountry.getIsFamilyNameAfterFirstName())) {
                name = traveler.getEnFirstName() + " " + traveler.getEnLastName();
            } else {
                name = traveler.getEnLastName() + " " + traveler.getEnFirstName();
            }

            DecimalFormat fromDF = new DecimalFormat(
                    fromCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");
            DecimalFormat toDF = new DecimalFormat(
                    toCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");

            int p = 2;
            double d = Math.floor(Math.log10(refund.getExchangeRate().doubleValue())) + 1;
            if (d < 2) {
                // Set significant digits to 4 digits
                p = (int) (-d + 4);
            }

            BigDecimal toAmtWithSpread = refund.getToAmt().add(refund.getFeePerAmt());
            BigDecimal exchangeRateWithSpread = refund.getFromAmt().divide(toAmtWithSpread, 6, RoundingMode.HALF_UP);

            String exchangeRate = exchangeRateWithSpread.setScale(p, RoundingMode.HALF_UP).toString();

            context = new Context();
            context.setVariable("name", name);
            context.setVariable("identificationNo", securityService.decryptAES256(traveler.getIdentificationNumber()));
            context.setVariable("receiptNo", "R" + refund.getId());
            context.setVariable("emailId", emailTo);
            context.setVariable("date", date);
            context.setVariable("fromIso4217", fromCountry.getIso4217());
            context.setVariable("toAmt", toDF.format(refund.getToAmt()));
            context.setVariable("exchange_rate", exchangeRate);
            context.setVariable("toPrincipal", toDF.format(refund.getToAmt().add(refund.getFeePerAmt())));
            context.setVariable("fee", toDF.format(refund.getFeePerAmt()));
            context.setVariable("fromAmt", fromDF.format(refund.getFromAmt()));
            context.setVariable("cashmallowCiImage", classLoader.getResource("images/cashmallow_simbol.png"));

            String receipt = null;
            String receiptFileName = "RECEIPT_No_";

            if (HK.getCode().equals(refund.getToCd())) {
                context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_hk.png"));
                receipt = templateEngine.process("receipt-templates/hkg/confirm_refund_receipt_v2", context);
            } else if (JP.getCode().equals(refund.getToCd())) {
                context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_jp.png"));
                receipt = templateEngine.process("receipt-templates/hkg/confirm_refund_receipt_v2", context);
            }

            logger.info("{}: receipt={}", method, receipt);

            Document document = Jsoup.parse(receipt);
            document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

            logger.info("document = {}", document);

            // get the byte array of the image (as jpeg)
            String fileServerDir = hostFilePathHome + Const.FILE_SERVER_CMRECEIPT;
            File attacedFile = new File(fileServerDir + File.separator + receiptFileName + "R" + refund.getId() + ".pdf");

            try (OutputStream os = new FileOutputStream(attacedFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.withFile(attacedFile);
                builder.toStream(os);
                FSSupplier<InputStream> droidFssSupplier = () -> {
                    logger.info("{}: Requesting font DroidSansFallback", method);

                    return getClass().getClassLoader().getResourceAsStream("fonts/DroidSansFallback.ttf");
                };
                builder.useFont(droidFssSupplier, "Droid Sans Fallback");
                String baseUrl = getClass().getClassLoader().getResource("").toString();
                builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
                builder.run();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            File parentPath = attacedFile.getParentFile();
            if (parentPath.exists() || parentPath.mkdirs()) {
                sendEmailWithCmReceipt(emailTo, subject, text, attacedFile);
            } else {
                logger.error("{}: Failed to create file.", method);
                throw new CashmallowException(INTERNAL_SERVER_ERROR);
            }

        } catch (MessagingException | IOException e) {
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailConfirmNewRefundForExchange(User user, Traveler traveler, NewRefund refund, Country fromCountry, Country toCountry)
            throws CashmallowException {

        final String method = "sendEmailConfirmNewRefundForExchange()";

        ClassLoader classLoader = EmailServiceImpl.class.getClassLoader();

        String userEmail = user.getEmail();
        Locale locale = user.getCountryLocale();

        if (!StringUtils.isNotBlank(userEmail)) {
            logger.error("{}: parameter is empty. emailTo={}", method, userEmail);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }

        // 메일 내용
        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_CONFIRM_REFUND", null, "", locale));

        Context context = new Context();

        String text = templateEngine.process(getTemplateByCountry(user.getCountryCode(), "confirm_refund"), context);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(new Date());

        String name = "";
        // 환불은 toCountry가 서비스하는 국가
        if ("Y".equals(toCountry.getIsFamilyNameAfterFirstName())) {
            name = traveler.getEnFirstName() + " " + traveler.getEnLastName();
        } else {
            name = traveler.getEnLastName() + " " + traveler.getEnFirstName();
        }

        DecimalFormat fromDF = new DecimalFormat(
                fromCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");
        DecimalFormat toDF = new DecimalFormat(
                toCountry.getMappingInc().compareTo(BigDecimal.ONE) < 0 ? "#,##0.00" : "#,##0");

        int p = 2;
        double d = Math.floor(Math.log10(refund.getExchangeRate().doubleValue())) + 1;
        if (d < 2) {
            // Set significant digits to 4 digits
            p = (int) (-d + 4);
        }

        BigDecimal toAmtWithSpread = refund.getToAmt().add(refund.getFeePerAmt());
        BigDecimal exchangeRateWithSpread = refund.getFromAmt().divide(toAmtWithSpread, 6, RoundingMode.HALF_UP);

        String exchangeRate = exchangeRateWithSpread.setScale(p, RoundingMode.HALF_UP).toString();
        String discountAmount = ObjectUtils.isEmpty(refund.getCouponDiscountAmount()) ? "0" : fromDF.format(refund.getCouponDiscountAmount());

        context = new Context();
        context.setVariable("travelerName", name);
        context.setVariable("travelerAddress", traveler.getAddress() + " " + traveler.getAddressSecondary());
        context.setVariable("receiptNo", "R" + refund.getId());
        context.setVariable("email", userEmail);
        context.setVariable("date", date);
        context.setVariable("fromIso4217", fromCountry.getIso4217());
        context.setVariable("fromAmt", fromDF.format(refund.getFromAmt())); // 환불 신청한 외화금액
        context.setVariable("toAmt", toDF.format(refund.getToAmt())); // 환불 지급액
        context.setVariable("discountAmt", discountAmount);
        context.setVariable("fee", toDF.format(refund.getFeePerAmt())); // 건당 수수료
        context.setVariable("exchange_rate", exchangeRate); // 환율
        context.setVariable("cashmallowCiImage", classLoader.getResource("images/cashmallow_simbol.png"));

        // context.setVariable("toPrincipal", toDF.format(refund.getToAmt().add(refund.getFeePerAmt()))); // 환불 신청 원금

        String receipt = null;
        String receiptFileName = "RECEIPT_No_";

        if (HK.getCode().equals(refund.getToCd())) {

            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_hk.png"));

            receipt = templateEngine.process("receipt-templates/hkg/confirm_refund_receipt_v2", context);
        } else if (JP.getCode().equals(refund.getToCd())) {

            context.setVariable("stampImage", classLoader.getResource("images/cashmallow_stamp_jp.png"));

            receipt = templateEngine.process("receipt-templates/jpn/confirm_refund_receipt_v2", context);
        }

        logger.info("{}: receipt={}", method, receipt);

        Document document = Jsoup.parse(receipt);
        document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);

        logger.info("document = {}", document);

        String fileServerDir = hostFilePathHome + Const.FILE_SERVER_CMRECEIPT;
        String fileName = fileServerDir + File.separator + receiptFileName + "R" + refund.getId() + ".pdf";
        File attacedFile = new File(fileName);

        try (OutputStream os = new FileOutputStream(attacedFile)) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withFile(attacedFile);
            builder.toStream(os);
            FSSupplier<InputStream> droidFssSupplier = () -> {
                logger.info("{}: Requesting font DroidSansFallback", method);

                return getClass().getClassLoader().getResourceAsStream("fonts/DroidSansFallback.ttf");
            };
            builder.useFont(droidFssSupplier, "Droid Sans Fallback");
            String baseUrl = getClass().getClassLoader().getResource("").toString();
            builder.withW3cDocument(new W3CDom().fromJsoup(document), baseUrl);
            builder.run();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        File parentPath = attacedFile.getParentFile();
        if (parentPath.exists() || parentPath.mkdirs()) {
            try {
                sendEmailWithCmReceipt(userEmail, subject, text, attacedFile);
            } catch (IOException | MessagingException | CashmallowException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error("{}: Failed to create file.", method);
            throw new CashmallowException(INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void sendEmailExpiredWalletBefore7Day(User user) throws CashmallowException {
        String method = "sendEmailWalletExpired7Day()";

        String email = user.getEmail();
        Locale locale = user.getCountryLocale();

        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_WALLET_EXPIRE_BEFORE_7DAY", null, "", locale));

        Context context = new Context();
        String text = templateEngine.process(getTemplateByCountry(HK, "wallet_expire_before_7day"), context);

        if (CountryCode.JP.equals(user.getCountryCode())) {
            text = templateEngine.process(getTemplateByCountry(JP, "wallet_expire_before_7day"), context);
        }

        try {
            emailService.sendMail(email, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("{}: Failed to send email. userId={}, email={}", method, user.getId(), email);
            throw new CashmallowException(e.getMessage(), e);
        }
    }

    @Override
    public void sendEmailExpiredWallet(User user) throws CashmallowException {
        String method = "sendEmailExpiredWallet()";

        String email = user.getEmail();
        Locale locale = user.getCountryLocale();

        String subject = String.format("%s %s",
                messageSource.getMessage("MAIL_SUBJECT_PREFIX", null, "", locale),
                messageSource.getMessage("MAIL_SUBJECT_WALLET_EXPIRE", null, "", locale));

        Context context = new Context();

        String text = templateEngine.process(getTemplateByCountry(HK, "wallet_expire"), context);

        if (CountryCode.JP.equals(user.getCountryCode())) {
            text = templateEngine.process(getTemplateByCountry(JP, "wallet_expire"), context);
        }

        try {
            emailService.sendMail(email, subject, text);
        } catch (MessagingException | UnsupportedEncodingException e) {
            logger.error("{}: Failed to send email. userId={}, email={}", method, user.getId(), email);
            throw new CashmallowException(e.getMessage(), e);
        }
    }
}
