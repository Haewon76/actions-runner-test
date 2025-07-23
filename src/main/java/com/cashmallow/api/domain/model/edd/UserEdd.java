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
public class UserEdd {

    private Long id;

    private Long userId;

    private BigDecimal amount;

    private Integer count;

    private String limited;

    private String firstName;
    private String lastName;
    private String email;
    private String korName;

    private Long creatorId;

    private String creatorLastName;
    private String creatorFirstName;
    private String creatorName;

    private Timestamp initAt;

    private Timestamp createdAt;

    private Timestamp updatedAt;

    private Integer memoCount;

    private Integer imageCount;

    private String searchStartAt;
    private String searchEndAt;

    private String initIp;

    private String initAtString;

    private String createdAtString;

    private String updatedAtString;

    public String getInitAtString() {
        return initAt != null ? CommDateTime.toString(initAt) : null;
    }

    public String getCreatedAtString() {
        return createdAt != null ? CommDateTime.toString(createdAt) : null;
    }

    public String getUpdatedAtString() {
        return updatedAt != null ? CommDateTime.toString(updatedAt) : null;
    }
}
