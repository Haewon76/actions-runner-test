package com.cashmallow.api.domain.model.exchange;

import com.cashmallow.api.application.SecurityService;
import com.cashmallow.api.domain.model.traveler.Traveler;
import com.cashmallow.api.domain.model.traveler.TravelerMapper;
import com.cashmallow.api.domain.model.user.User;
import com.cashmallow.api.domain.model.user.UserRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.admin.dto.SearchResultVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.cashmallow.api.domain.shared.MsgCode.REJECT_EXCHANGE_REMITTANCE_ERROR;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class ExchangeRepositoryService {

    private final ExchangeMapper exchangeMapper;
    private final TravelerMapper travelerMapper;

    private final UserRepositoryService userRepositoryService;

    private final SecurityService securityService;

    private final MessageSource messageSource;

    public Exchange getExchangeByExchangeId(Long exchangeId) {
        return exchangeMapper.getExchangeByExchangeId(exchangeId);
    }

    public Map<String, Object> getExchangeFromAmtSumByPeriod(Map<String, Object> params) {
        return exchangeMapper.getExchangeFromAmtSumByPeriod(params);
    }

    /**
     * Get exchange list by traveler id
     *
     * @param travelerId
     * @return
     */
    public List<Exchange> getExchangeListByTravelerId(Long travelerId) {
        return exchangeMapper.getExchangeListByTravelerId(travelerId);
    }

    /**
     * Get exchange amount statistics by country
     *
     * @param country
     * @param beginDate
     * @param endDate
     * @return : totalCnt, totalFee, totalTotal, hkdAmt, hkdFee, hkdTotal, ...,
     * comCnt, comFee, comTotal, comHkdAmt, comHkdFee, comHkdTotal, ...,
     * reqCnt, reqFee, reqTotal, reqHkdAmt, reqHkdFee, reqHkdTotal, ...,
     * canCnt, canFee, canTotal, canHkdAmt, canHkdFee, canHkdTotal, ...
     */
    public Map<String, Object> getExchangeAmountByCountry(String country, Date beginDate, Date endDate) {

        Map<String, Object> params = new HashMap<>();
        params.put("country", country);

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:dd");
        params.put("beginDate", df.format(beginDate));
        params.put("endDate", df.format(endDate));

        return exchangeMapper.getExchangeAmountByCountry(params);
    }

    /**
     * Update exchange
     *
     * @param exchange
     * @return
     */
    @Transactional
    public int updateExchange(Exchange exchange) {
        int affectedRow = exchangeMapper.updateExchange(exchange);
        exchangeMapper.insertExchangeStatus(new ExchangeStatus(exchange));
        return affectedRow;
    }

    @Transactional
    public int updateExchangeTrAccountInfo(Exchange exchange) {
        int affectedRow = exchangeMapper.updateExchangeTrAccountInfo(exchange);
        exchangeMapper.insertExchangeStatus(new ExchangeStatus(exchange));
        return affectedRow;
    }

    public int insertExchange(Exchange exchange) {
        int affectedRow = exchangeMapper.insertExchange(exchange);
        exchangeMapper.insertExchangeStatus(new ExchangeStatus(exchange));
        return affectedRow;
    }


    /**
     * travelerId의 마지막 환전 신청 정보를 읽는다.
     *
     * @param userId
     * @return
     * @throws CashmallowException
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Exchange getLatestExchangeInProgress(Long userId) {
        String method = "getLatestExchangeInProgress()";
        log.info("{}: userId={}", method, userId);
        Traveler traveler = travelerMapper.getTravelerByUserId(userId);
        if (traveler == null) {
            log.info("{}: traveler 정보가 등록되지 않았습니다. userId={}", method, userId);
            return null;
        }

        return getLatestExchangeInProgress(traveler);
    }

    /**
     * travelerId의 마지막 환전 신청 정보를 읽는다.
     *
     * @param userId
     * @return
     * @throws CashmallowException
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Exchange getLatestExchangeInProgress(Traveler traveler) {
        String method = "getLatestExchangeInProgress()";
        Exchange exchange = null;

        if (traveler == null) {
            log.info("{}: traveler 정보가 등록되지 않았습니다.", method);
            return null;
        }

        Long travelerId = traveler.getId();
        // 1. 여행자의 마지막 환전 환전 신청 정보를 읽는다.
        List<Exchange> exchanges = exchangeMapper.getLastestExchangeInProgressByTravelerId(travelerId);

        if (exchanges != null && !exchanges.isEmpty()) {
            exchange = exchanges.get(0);

            List<ExchangeDepositReceipt> receipts = exchangeMapper.getExchangeDepositReceiptList(exchange.getId());
            if (receipts != null && !receipts.isEmpty()) {
                String trReceiptPhoto = receipts.get(receipts.size() - 1).getReceiptPhoto();
                exchange.setTrReceiptPhoto(trReceiptPhoto);
            }

            // DR 상태인경우 reject_message를 추가한다
            if (Exchange.ExStatus.DR.name().equalsIgnoreCase(exchange.getExStatus())) {
                final String remittanceRejectMessage = exchangeMapper.getExchangeRejectMessage(exchange.getId(), exchange.getExStatus());
                exchange.setRejectMessage(remittanceRejectMessage);

                if (StringUtils.isEmpty(remittanceRejectMessage)) {
                    User user = userRepositoryService.getUserByTravelerId(travelerId);
                    Locale locale = user.getCountryLocale();
                    String message = messageSource.getMessage(REJECT_EXCHANGE_REMITTANCE_ERROR, null, "Please contact the cs-center.", locale);
                    exchange.setRejectMessage(message);
                }
            }
        }

        return exchange;
    }

    /**
     * traveler의 환전 내역 조회
     *
     * @param token
     * @param page
     * @param size
     * @param sort
     * @return
     */
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    @SuppressWarnings("unchecked")
    public SearchResultVO getExchangeByTravelerId(Long userId, int page, int size)
            throws CashmallowException {

        String method = "getExchangeByUserId(): ";

        String sort = "id DESC";
        SearchResultVO searchResult = new SearchResultVO(page, size, sort);

        Traveler traveler = travelerMapper.getTravelerByUserId(userId);

        if (traveler == null) {
            log.info("{}: can not find traveler. userId={}", method, userId);
            return searchResult;
        }

        Long travelerId = traveler.getId();

        Map<String, Object> params = new HashMap<>();
        params.put("traveler_id", travelerId);
        params.put("start_row", searchResult.getPage() * searchResult.getSize());
        params.put("size", searchResult.getSize());
        params.put("sort", searchResult.getSort());

        int totalCount = exchangeMapper.countExchangeByTravelerId(params);
        List<Object> vos = exchangeMapper.getExchangeByTravelerId(params);

        List<Object> exchangeResult = new ArrayList<>();

        for (Object obj : vos) {
            Map<String, String> exchange = (Map) obj;
            exchange.put("tr_account_name", securityService.decryptAES256(exchange.get("tr_account_name")));
            exchange.put("tr_account_no", securityService.decryptAES256(exchange.get("tr_account_no")));
            exchange.put("tr_address", securityService.decryptAES256(exchange.get("tr_address")));
            exchange.put("tr_address_secondary", securityService.decryptAES256(exchange.get("tr_address_secondary")));
            exchange.put("tr_phone_number", securityService.decryptAES256(exchange.get("tr_phone_number")));
            exchangeResult.add(exchange);
        }

        searchResult.setResult(exchangeResult, totalCount, page);

        return searchResult;

    }

    @Transactional
    public void updateExchangeBankAccountId(Exchange exchange) {
        exchangeMapper.updateExchangeBankAccountId(exchange);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public Exchange getExchangeBeforeMapping(long id) {
        return exchangeMapper.getExchangeBeforeMapping(id);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<Exchange> getExchangeBeforeMappingList(String accountNo) {
        return exchangeMapper.getExchangeBeforeMappingList(accountNo);
    }

    public int countExchangeListByTravelerIds(List<Long> travelerIds) {
        return exchangeMapper.countExchangeListByTravelerIds(travelerIds);
    }

}
