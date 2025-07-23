package com.cashmallow.api.domain.model.refund;

import com.cashmallow.api.domain.model.coupon.vo.CouponNewRefund;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.traveler.dto.JpRefundAccountInfoRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
public class RefundRepositoryService {

    private final RefundMapper refundMapper;


    public NewRefund getNewRefundById(Long id) {
        return refundMapper.getNewRefundById(id);
    }

    public List<NewRefund> getNewRefundListByTravelerId(Long travelerId) {
        return refundMapper.getNewRefundListByTravelerId(travelerId);
    }

    public List<NewRefund> getNewRefundListInStandbyByTravelerId(long travelerId) {
        return refundMapper.getNewRefundListInStandbyByTravelerId(travelerId);
    }

    public List<NewRefund> getNewRefundListInProgressByTravelerId(long travelerId) {
        return refundMapper.getNewRefundInProgressByTravelerId(travelerId);
    }

    public List<NewRefund> getNewRefundByCountry(String countryCode, NewRefund.RefundStatusCode refundStatus) {
        Map<String, Object> params = new HashMap<>();
        params.put("toCd", countryCode);
        params.put("refundStatus", refundStatus);
        return refundMapper.getNewRefundByCountry(params);
    }

    // OP, MP(수동전송), AP(자동전송)중 상태
    public NewRefund getNewRefundInProgressByWalletId(long walletId, long travelerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("walletId", walletId);
        params.put("travelerId", travelerId);
        return refundMapper.getNewRefundInProgressByWalletId(params);
    }

    public NewRefund getNewRefundNotCancelByRemitId(long remitId, long travelerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("remitId", remitId);
        params.put("travelerId", travelerId);
        return refundMapper.getNewRefundNotCancelByRemitId(params);
    }

    public List<NewRefund> getNewRefundNotCancelByExchangeId(String exchangeId, Long travelerId) {
        Map<String, Object> params = new HashMap<>();
        params.put("exchangeId", exchangeId);
        params.put("travelerId", travelerId);
        return refundMapper.getNewRefundNotCancelByExchangeId(params);
    }

    public void updateNewRefundList(List<NewRefund> newRefundList) {
        refundMapper.updateNewRefundList(newRefundList);
    }

    public int updateNewRefund(NewRefund newRefund) {
        return refundMapper.updateNewRefund(newRefund);
    }

    public JpRefundAccountInfo registerJpRefundAccountInfo(JpRefundAccountInfoRequest accountRequest, Long travelerId) throws CashmallowException {
        JpRefundAccountInfo jpRefundAccountInfo = new JpRefundAccountInfo(accountRequest, travelerId);
        refundMapper.insertJpRefundAccountInfo(jpRefundAccountInfo);
        recordJpRefundAccountInfoHistory(jpRefundAccountInfo);
        return jpRefundAccountInfo;
    }

    @Transactional
    public void updateJpRefundAccountInfo(JpRefundAccountInfo jpRefundAccountInfo) {
        refundMapper.updateJpRefundAccountInfo(jpRefundAccountInfo);
        recordJpRefundAccountInfoHistory(jpRefundAccountInfo);
    }

    public JpRefundAccountInfo getJpRefundAccountInfoById(Long jpRefundAccountInfoId) {
        return refundMapper.getJpRefundAccountInfoById(jpRefundAccountInfoId);
    }

    public JpRefundAccountInfo getJpRefundAccountInfoByTravelerId(Long travelerId) {
        return refundMapper.getJpRefundAccountInfoByTravelerId(travelerId);
    }

    private void recordJpRefundAccountInfoHistory(JpRefundAccountInfo jpRefundAccountInfo) {
        refundMapper.insertJpRefundAccountInfoHistory(jpRefundAccountInfo);
    }

    // 쿠폰 환불용 조회
    // 송금일 경우 remit_id 컬럼에, 화전일 경우 exchange_id/wallet_id 컬럼에서 가져와야하고 필요한 값만 가져오기 위해 추가함
    public CouponNewRefund getNewRefundExchangeOrRemitById(Long refundId) {
        return refundMapper.getNewRefundExchangeOrRemitById(refundId);
    }
}
