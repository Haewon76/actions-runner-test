package com.cashmallow.api.interfaces.authme.dto;

import com.cashmallow.common.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TravelerImage {

    private Long travelerId;
    private String fileName;
    private String type;
    private String createdAt;

    public static TravelerImage from(Long travelerId,
                                     String fileName,
                                     String type) {
        return new TravelerImage(
                travelerId,
                fileName,
                type,
                DateUtil.getTimestampToKst(new Timestamp(new Date().getTime()))
        );
    }
}
