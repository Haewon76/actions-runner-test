package com.cashmallow.api.interfaces.aml.complyadvantage.dto;

import com.cashmallow.api.interfaces.aml.complyadvantage.enums.ComplyAdvantageCaseStateCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public record ComplyAdvantageCustomerCasesResponse(@JsonProperty("total_count") Long totalCount,
                                                   List<ComplyAdvantageCases> cases
) {
    @Getter
    @Setter
    public static class ComplyAdvantageCases {
        @JsonProperty("identifier")
        private String casesId;
        private String type;
        private ComplyAdvantageCaseStateCode state;
        @JsonProperty("review_items_count")
        private int reviewItemsCount;
    }
}
