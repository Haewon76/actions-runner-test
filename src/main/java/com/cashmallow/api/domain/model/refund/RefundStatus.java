package com.cashmallow.api.domain.model.refund;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefundStatus {
    private Long refundId;
    private NewRefund.RefundStatusCode refundStatus;
    private Long userId;
    private String message;

    public static RefundStatus Of(NewRefund newRefund, Long userId, String message) {
        RefundStatus returnValue = new RefundStatus();
        returnValue.setRefundId(newRefund.getId());
        returnValue.setRefundStatus(newRefund.getRefundStatus());
        returnValue.setUserId(userId);
        returnValue.setMessage(message);

        return returnValue;
    }
}
