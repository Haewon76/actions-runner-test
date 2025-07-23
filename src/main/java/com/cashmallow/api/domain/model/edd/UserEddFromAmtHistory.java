package com.cashmallow.api.domain.model.edd;

import com.cashmallow.common.CommDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEddFromAmtHistory {

    private Long id;

    private String type;

    private BigDecimal fromAmt;

    private Timestamp createdAt;

    private String createdAtString;

    public String getCreatedAtString() {
        return createdAt != null ? CommDateTime.toString(createdAt) : null;
    }

    private String korName;
    private String receiverName;

    private String receiverLastName;
    private String receiverFirstName;

}
