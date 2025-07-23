package com.cashmallow.api.interfaces.aml.complyadvantage.dto;

public record ComplyAdvantageCreateCustomerResponse(
        Long userId,
        String workflowInstanceId,
        String workflowType,
        String workflowStatus,
        String customerId
) {
}
