package com.cashmallow.api.domain.model.refund;


import com.cashmallow.api.domain.model.coupon.vo.CouponNewRefund;

import java.util.List;
import java.util.Map;

public interface RefundMapper {

    NewRefund getNewRefundById(Long id);

    List<NewRefund> getNewRefundListByTravelerId(Long travelerId);

    List<NewRefund> getNewRefundInProgressByTravelerId(Long travelerId);

    List<NewRefund> getNewRefundListInStandbyByTravelerId(Long travelerId);

    int countSearchNewRefundList(Map<String, Object> params);

    List<Object> searchNewRefundList(Map<String, Object> params);

    NewRefund getNewRefundInProgressByWalletId(Map<String, Object> params);
    List<NewRefund> getNewRefundByCountry(Map<String, Object> params);

    List<NewRefund> getNewRefundNotCancelByExchangeId(Map<String, Object> params);

    NewRefund getNewRefundNotCancelByRemitId(Map<String, Object> params);

    Map<String, Object> getRefundAmountByCountry(String country);

    // NewRefund 생성
    int insertNewRefund(NewRefund newRefund);
    // NewRefund 업데이트
    int updateNewRefund(NewRefund newRefund);

    int setRefundTidOutId(NewRefund newRefund);

    void updateNewRefundList(List<NewRefund> newRefundList);

    int insertRefundStatus(RefundStatus refundStatus);
    void insertRefundStatusList(List<RefundStatus> refundStatusList);

    int insertJpRefundAccountInfo(JpRefundAccountInfo jpRefundAccountInfo);
    int updateJpRefundAccountInfo(JpRefundAccountInfo jpRefundAccountInfo);

    int insertJpRefundAccountInfoHistory(JpRefundAccountInfo jpRefundAccountInfo);

    JpRefundAccountInfo getJpRefundAccountInfoById(Long id);
    JpRefundAccountInfo getJpRefundAccountInfoByTravelerId(Long travelerId);

    // 쿠폰 환불용 조회
    // 송금일 경우 remit_id 컬럼에, 화전일 경우 exchange_id/wallet_id 컬럼에서 가져와야하고 필요한 값만 가져오기 위해 추가함
    CouponNewRefund getNewRefundExchangeOrRemitById(Long refundId);
}
