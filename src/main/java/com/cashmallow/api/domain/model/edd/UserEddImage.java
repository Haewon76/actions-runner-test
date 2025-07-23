package com.cashmallow.api.domain.model.edd;

import com.cashmallow.api.domain.model.user.User;
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
public class UserEddImage {

    private Long id;

    private Long userEddId;

    private byte[] image;

    private Long creatorId;

    private Timestamp createdAt;

    private String type;

    private String contentType;

    private Long fileSize;

    private User user;
}
