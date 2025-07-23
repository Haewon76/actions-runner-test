package com.cashmallow.api.interfaces.admin.dto;

import com.cashmallow.api.domain.shared.Const;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
public class DatatablesResponse<T> {
    private int status;
    private String code;
    private String message;

    private int draw;
    private long recordsTotal;
    private long recordsFiltered;
    private List<T> data;

    public DatatablesResponse() {}

    public DatatablesResponse(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public DatatablesResponse(long recordsTotal, long recordsFiltered, List<T> data) {
        this.recordsTotal = recordsTotal;
        this.recordsFiltered = recordsFiltered;
        this.data = data;
    }

    @Builder
    public DatatablesResponse(int status, String code, String message, List<T> data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public DatatablesResponse(String message, String status) {
        this.message = message;
        this.status = Integer.parseInt(status);
        this.data = Collections.emptyList();
    }

    public void setSuccess(List<T> data, String message) {
        this.message = message;
        this.status = Integer.parseInt(Const.CODE_SUCCESS);
        this.code = Const.STATUS_SUCCESS;
        if (data.isEmpty()) {
            this.data = Collections.emptyList();
        } else {
            this.data = data;
        }

    }
}