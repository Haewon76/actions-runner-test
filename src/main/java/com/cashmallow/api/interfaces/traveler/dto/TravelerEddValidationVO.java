package com.cashmallow.api.interfaces.traveler.dto;

import com.cashmallow.api.domain.shared.Const;
import lombok.Data;

@Data
public class TravelerEddValidationVO {
    private String status;
    private String message; // 모바일 전달 메세지
    private String title; // 모바일 팝업창 title
    private String buttonName; // 모바일 팝업창 buttonName

    public void SetFailInfo(String message, String title, String buttonName) {
        this.message = message;
        this.title = title;
        this.buttonName = buttonName;
        this.status = Const.STATUS_FAILURE;
    }
}
