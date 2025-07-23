package com.cashmallow.api.interfaces.edd;

import com.cashmallow.api.application.AlarmService;
import com.cashmallow.api.domain.model.country.Country;
import com.cashmallow.api.domain.model.country.ExchangeConfig;
import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import com.cashmallow.api.domain.model.edd.*;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerRepositoryService;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.infrastructure.fileserver.FileUploadValidator;
import com.cashmallow.api.infrastructure.security.CashmallowEncrypt;
import com.cashmallow.api.interfaces.traveler.dto.TravelerEddValidationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

import static com.cashmallow.api.interfaces.global.GlobalQueueService.GLOBAL_JP_TOPIC;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserEddServiceImpl implements UserEddService {
    private static final Logger logger = LoggerFactory.getLogger(UserEddServiceImpl.class);

    private final UserEddMapper userEddMapper;

    private final UserEddImageMapper userEddImageMapper;

    private final UserEddLogMapper userEddLogMapper;

    private final UserRepositoryService userRepositoryService;

    private final TravelerRepositoryService travelerRepositoryService;

    private final AlarmService alarmService;

    private final RabbitTemplate rabbitTemplate;

    private final FileUploadValidator fileUploadValidator;

    private final MessageSource messageSource;

    /**
     * User EDD Validation 체크 및 송금, 환전 할 수 없도록 막음.
     *
     * @param fromCountry
     * @param fromMoney
     * @param userId
     * @param travelerId
     * @return
     */
    @Override
    public TravelerEddValidationVO verificationUserEdd(Country fromCountry, BigDecimal fromMoney, Long userId, Long travelerId,
                                                       ExchangeConfig exchangeConfig, Locale locale) throws CashmallowException {

        TravelerEddValidationVO eddValidationVO = new TravelerEddValidationVO();

        // LIMIT 금액 및 count
        BigDecimal maxLimitAmt = exchangeConfig.getFromEddAmountLimit();
        Integer maxLimitCnt = exchangeConfig.getFromEddCountLimit();

        // 일 한도 금액 설정이 없으면 무조건 패스
        if ((maxLimitAmt == null || maxLimitAmt.intValue() == 0) && (maxLimitCnt == null || maxLimitCnt == 0)) {
            eddValidationVO.setStatus(Const.STATUS_SUCCESS);
            return eddValidationVO;
        }

        Calendar cal = Calendar.getInstance();
        Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());
        String toDate = toDayTimestamp.toString();

        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);

        List<UserEdd> userEddList = userEddMapper.getUserEddList(params);

        cal.add(Calendar.DATE, -7);

        String fromDate = new Timestamp(cal.getTime().getTime()).toString();

        if (!CollectionUtils.isEmpty(userEddList)) {
            UserEdd userEdd = userEddList.get(0);

            // limited Y 이면 EDD 제한, InitAt 정보가 7일 이후 추가 되었을 경우 initAt으로 fromDate 셋팅
            if (StringUtils.equals(Const.USER_EDD_LIMITED_Y, userEdd.getLimited())) {
                eddValidationVO.SetFailInfo(messageSource.getMessage("USER_EDD_MESSAGE", null, "Please contact the customer service center. (Code:  E404-D)", locale),
                        messageSource.getMessage("USER_EDD_TITLE", null, "Notice", locale),
                        messageSource.getMessage("USER_EDD_BUTTON", null, "Confirm", locale));
                return eddValidationVO;
            } else if (userEdd.getInitAt() != null) {
                Timestamp ts = userEdd.getInitAt();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(ts);

                if (cal.before(calendar)) {
                    fromDate = new Timestamp(userEdd.getInitAt().getTime()).toString();
                }
            }
        }

        params.put("fromDate", fromDate);
        params.put("toDate", toDate);
        params.put("travelerId", travelerId);
        params.put("fromCd", fromCountry.getCode());

        Map<String, Object> eddLimitAmt = userEddMapper.getUserEddLimit(params);

        // 기간내 총 환전 및 송금 금액 및 Cnt
        BigDecimal fromAmtSum = (BigDecimal) eddLimitAmt.get("fromAmtSum");
        Integer fromCnt = ((BigDecimal) eddLimitAmt.get("cnt")).intValue();

        // 총 인출 금액 + 요청 금액
        BigDecimal totalAmt = fromAmtSum.add(fromMoney);

        // 제한 금액이 total 금액 보다 작으면, 실패
        if (maxLimitAmt.compareTo(totalAmt) <= 0 || maxLimitCnt < fromCnt) {
            eddValidationVO.SetFailInfo(messageSource.getMessage("USER_EDD_MESSAGE", null, "Please contact the customer service center. (Code:  E404-D)", locale),
                    messageSource.getMessage("USER_EDD_TITLE", null, "Notice", locale),
                    messageSource.getMessage("USER_EDD_BUTTON", null, "Confirm", locale));

            log.info("verificationUserEdd() : maxLimitAmt : {}, maxLimitCnt : {}", maxLimitAmt, maxLimitCnt);

            UserEdd userEdd = UserEdd.builder()
                    .userId(userId)
                    .amount(fromAmtSum)
                    .count(fromCnt)
                    .limited(Const.USER_EDD_LIMITED_Y)
                    .createdAt(toDayTimestamp)
                    .searchStartAt(fromDate)
                    .searchEndAt(toDate)
                    .build();

            userEddMapper.registerUserEdd(userEdd);

            alarmService.aEdd("EDD등록", "EDD에 사용자가 자동으로 등록되었습니다. userId = " + userEdd.getUserId() + ", amount = " + userEdd.getAmount());

            try {
                UserEddLog userEddLog = new UserEddLog();
                BeanUtils.copyProperties(userEdd, userEddLog);
                userEddLog.setId(null);
                userEddLog.setUserEddId(userEdd.getId());

                userEddLogMapper.registerUserEddLog(userEddLog);

            } catch (Exception e) {
                logger.error("verificationUserEdd registerUserEdd error", e);
            }

            logger.info("verificationUserEdd registerUserEdd {}", userEdd);

            sendUserEddToGlobalTraveler(fromCountry, userEdd);

            return eddValidationVO;
        }

        eddValidationVO.setStatus(Const.STATUS_SUCCESS);
        return eddValidationVO;
    }

    /**
     * EDD 사용자 정보를 Global Traveler로 전송, ex) JP, KR
     *
     * @param fromCountry
     * @param userEdd
     */
    public void sendUserEddToGlobalTraveler(Country fromCountry, UserEdd userEdd) {
        try {
            String countryCode = fromCountry.getIso3166(); // alpha-2 code, ex) KR, JP
            if (CountryInfo.JP.name().equals(countryCode)) {
                String routingKey = "global-jp-traveler-edd-add-routing";
                rabbitTemplate.convertAndSend(GLOBAL_JP_TOPIC, routingKey, userEdd);
            }
        } catch (Exception e) {
            logger.error("verificationUserEdd rabbitTemplate error {}", e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserEdd> getUserEddList(Map<String, Object> eddParams) {
        return userEddMapper.getUserEddList(eddParams);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserEdd> getUserEddJoinList(Map<String, Object> eddParams) {

        List<UserEdd> userEddList = userEddMapper.getUserEddJoinList(eddParams);

        List<UserEdd> userEddListResult = new ArrayList<>();

        for (UserEdd userEdd : userEddList) {
            userEdd.setCreatorName(userEdd.getCreatorLastName() + " " + userEdd.getCreatorFirstName());
            userEddListResult.add(userEdd);
        }


        return userEddListResult;
    }

    @Transactional(rollbackFor = CashmallowException.class)
    @Override
    public void updateEdd(Long managerId, Long userEddId, String limited, String ip) throws CashmallowException {
        HashMap<String, Object> params = new HashMap<>();
        params.put("id", userEddId);

        List<UserEdd> userEddList = userEddMapper.getUserEddList(params);

        if (userEddList.isEmpty()) {
            log.error("userEddId = " + userEddId);
            throw new CashmallowException("해당 id가 존재하지 않습니다.");
        }

        Calendar cal = Calendar.getInstance();
        Timestamp updatedAt = new Timestamp(cal.getTime().getTime());

        UserEdd userEdd = userEddList.get(0);
        userEdd.setLimited(limited);
        userEdd.setCreatorId(managerId);
        userEdd.setUpdatedAt(updatedAt);
        userEdd.setInitIp(ip);
        if (Const.USER_EDD_LIMITED_Y.equals(limited)) {
            userEdd.setInitAt(null);
        } else {
            userEdd.setInitAt(updatedAt);
        }

        userEddMapper.updateUserEdd(userEdd);

        try {
            UserEddLog userEddLog = new UserEddLog();
            BeanUtils.copyProperties(userEdd, userEddLog);
            userEddLog.setId(null);
            userEddLog.setUserEddId(userEdd.getId());
            userEddLog.setCreatedAt(updatedAt);
            userEddLog.setCreatorId(managerId);
            userEddLog.setInitIp(ip);

            userEddLogMapper.registerUserEddLog(userEddLog);

        } catch (Exception e) {
            logger.error("verificationUserEdd registerUserEdd error {}", e);
        }
    }

    @Transactional
    @Override
    public int registerUserEddImage(Long managerId, Long userEddId, List<MultipartFile> pictureLists) throws CashmallowException, IOException {

        Calendar cal = Calendar.getInstance();
        Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());

        List<UserEddImage> userEddImageList = new ArrayList<>();

        for (MultipartFile multipartFile : pictureLists) {
            // 파일 확장자 체크 - 2024.06 웹 취약점 대응(안랩)
            String originalFilename = multipartFile.getOriginalFilename();
            fileUploadValidator.validateFile(multipartFile);

            try {
                UserEddImage userEddImage = UserEddImage.builder()
                        .userEddId(userEddId)
                        .image(CashmallowEncrypt.encryptAES256(multipartFile.getBytes()))
                        .creatorId(managerId)
                        .createdAt(toDayTimestamp)
                        .type("DEFAULT")
                        .contentType(multipartFile.getContentType())
                        .fileSize(multipartFile.getSize())
                        .build();
                userEddImageList.add(userEddImage);

            } catch (Exception e) {
                log.error("이미지 저장중에 에러 발생하였습니다. userEddId = " + userEddId, e);
                throw new CashmallowException(e.getMessage(), e);
            }
        }

        return userEddImageMapper.registerUserEddImage(userEddImageList);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Long> getUserEddImageList(Long userEddId) {
        return userEddImageMapper.getUserEddImageIdList(userEddId);
    }

    @Transactional(readOnly = true)
    @Override
    public UserEddImage getUserEddImage(Long userEddImageId) throws Exception {

        Map<String, Object> params = new HashMap<>();
        params.put("id", userEddImageId);

        List<UserEddImage> userEddImageList = userEddImageMapper.getUserEddImageList(params);

        if (userEddImageList.isEmpty()) {
            return null;
        }

        UserEddImage userEddImage = userEddImageList.get(0);
        User user = userRepositoryService.getUserByUserId(userEddImage.getCreatorId());

        userEddImage.setImage(CashmallowEncrypt.decryptAES256(userEddImage.getImage()));
        userEddImage.setUser(user);

        return userEddImage;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserEddFromAmtHistory> getFromAmtHistory(Long userEddId) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", userEddId);

        List<UserEdd> userEddList = userEddMapper.getUserEddList(params);

        if (!CollectionUtils.isEmpty(userEddList)) {
            UserEdd userEdd = userEddList.get(0);
            User user = userRepositoryService.getUserByUserId(userEdd.getUserId());
            Traveler traveler = travelerRepositoryService.getTravelerByUserId(userEdd.getUserId());

            params.clear();
            params.put("travelerId", traveler.getId());
            params.put("fromCd", user.getCountry());
            params.put("fromDate", userEdd.getSearchStartAt());
            params.put("toDate", userEdd.getSearchEndAt());

            List<UserEddFromAmtHistory> userEddFromAmtHistoryList = userEddMapper.getFromAmtHistory(params);

            List<UserEddFromAmtHistory> userEddFromAmtHistoryListResult = new ArrayList<>();

            for (UserEddFromAmtHistory userEddFromAmtHistory : userEddFromAmtHistoryList) {
                userEddFromAmtHistory.setReceiverName(userEddFromAmtHistory.getReceiverLastName() + " " + userEddFromAmtHistory.getReceiverFirstName());

                userEddFromAmtHistoryListResult.add(userEddFromAmtHistory);
            }
            return userEddFromAmtHistoryListResult;
        }

        return null;
    }

    /**
     * 수동 edd user 등록
     *
     * @param userEdd
     * @param managerId
     * @param ip
     * @return
     * @throws CashmallowException
     */
    @Override
    public int registerUserEdd(UserEdd userEdd, Long managerId, String ip) throws CashmallowException {
        int result = userEddMapper.registerUserEdd(userEdd);

        alarmService.aEdd("EDD등록", "관리자에 의해서 EDD에 사용자가 수동으로 등록되었습니다. userId = " + userEdd.getUserId() + ", managerId = " + managerId);

        try {
            UserEddLog userEddLog = new UserEddLog();
            BeanUtils.copyProperties(userEdd, userEddLog);
            userEddLog.setId(null);
            userEddLog.setUserEddId(userEdd.getId());
            userEddLog.setCreatorId(managerId);
            userEddLog.setInitIp(ip);

            userEddLogMapper.registerUserEddLog(userEddLog);

        } catch (Exception e) {
            logger.error("verificationUserEdd registerUserEdd error {}", e);
        }

        return result;
    }
}
