package com.cashmallow.api.domain.model.memo;

import com.cashmallow.common.CommDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.sql.Timestamp;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Memo {

    private Long id;

    private Long refId;

    private String type;

    private String memo;

    private Timestamp createdAt;


    private String createdAtString;

    public String getCreatedAtString() {
        return createdAt != null ? CommDateTime.toString(createdAt) : null;
    }

    private Timestamp updatedAt;

    private Long creatorId;

    private String creatorName;

    private String firstName;

    private String lastName;


}
