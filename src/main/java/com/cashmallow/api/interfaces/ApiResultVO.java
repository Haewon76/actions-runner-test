package com.cashmallow.api.interfaces;

import com.cashmallow.api.domain.shared.Const;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 가능: API 호출 결과를 위한 class.
@JsonPropertyOrder({"code", "status", "message", "obj"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Slf4j
@ToString
public class ApiResultVO {

    private static final Logger logger = LoggerFactory.getLogger(ApiResultVO.class);

    private String code;
    private String status;
    private String message;
    private Object obj;

    private String opt;         // Option Value

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public String getOpt() {
        return opt;
    }

    public void setOpt(String opt) {
        this.opt = opt;
    }

    public ApiResultVO() {
        this.code = "";
        this.status = "";
        this.message = "";
        this.obj = null;
        this.opt = null;
    }

    public ApiResultVO(String code, String status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
        this.obj = null;
        this.opt = null;
    }

    public ApiResultVO(String code) {
        this.code = code;
        this.obj = null;
        this.opt = null;

        if (code.equals(Const.CODE_INVALID_PARAMS)) {
            this.code = Const.CODE_FAILURE;
            this.status = Const.STATUS_INVALID_PARAMS;
            this.message = Const.MSG_INVALID_PARAMS;
        } else if (code.equals(Const.CODE_INVALID_TOKEN)) {
            this.code = Const.CODE_FAILURE;
            this.status = Const.STATUS_INVALID_TOKEN;
            this.message = Const.MSG_INVALID_TOKEN;
        } else if (code.equals(Const.CODE_INVALID_USER_ID)) {
            this.code = Const.CODE_FAILURE;
            this.status = Const.STATUS_INVALID_USER_ID;
            this.message = Const.MSG_INVALID_USER_ID;
        } else if (code.equals(Const.CODE_TEST_MODE)) {
            this.code = Const.CODE_FAILURE;
            this.status = Const.STATUS_TEST_MODE;
            this.message = Const.MSG_TEST_MODE;
        } else {
            this.status = "";
            this.message = "";
        }
    }

    // 기능: 처리 결과를 설정한다.
    public void setResult(String code, String status, String msg) {
        this.code = code;
        this.status = status;
        this.message = msg;
    }

    // 기능: 처리 결과를 설정한다.
    public void setResult(String code, String status, String msg, Object obj) {
        this.code = code;
        this.status = status;
        this.message = msg;
        this.obj = obj;
    }

    // 기능: 성공시 값을 설정한다.
    public ApiResultVO setSuccessInfo() {
        this.code = Const.CODE_SUCCESS;
        this.status = Const.STATUS_SUCCESS;
        this.message = Const.MSG_SUCCESS;
        this.obj = null;
        return this;
    }

    // 기능: 성공시 값을 설정한다.
    public void setSuccessInfo(Object obj) {
        this.code = Const.CODE_SUCCESS;
        this.status = Const.STATUS_SUCCESS;
        this.message = Const.MSG_SUCCESS;
        this.obj = obj;
    }

    // 성공시 기본 메세지값이 비어있지 않는다
    public void setSuccessInfoWithDefaultMessage(Object obj) {
        this.code = Const.CODE_SUCCESS;
        this.status = Const.STATUS_SUCCESS;
        this.message = Const.STATUS_SUCCESS;
        this.obj = obj;
    }

    // 기능: 성공시 값과 상태값을 설정한다.
    public void setSuccessInfo(Object obj, String status) {
        this.code = Const.CODE_SUCCESS;
        this.status = status;
        this.message = Const.MSG_SUCCESS;
        this.obj = obj;
    }

    // 기능: 실패시 값을 설정한다.
    public void setFailInfo(String message) {
        this.code = Const.CODE_FAILURE;
        this.status = Const.STATUS_FAILURE;
        this.message = message;
        this.obj = null;
        logger.info("setFailInfo(): message={}", message);
    }

    // 기능: 실패시 값을 설정한다.
    public void setFailInfo(String message, Object obj) {
        this.code = Const.CODE_FAILURE;
        this.status = Const.STATUS_FAILURE;
        this.message = message;
        this.obj = obj;
        logger.error("setFailInfo(): message={}", message);
    }

    // 기능: 파라미터 오류
    public void setInvalidParams() {
        this.code = Const.CODE_FAILURE;
        this.status = Const.STATUS_INVALID_PARAMS;
        this.message = Const.MSG_INVALID_PARAMS;
        this.obj = null;
        logger.info("setInvalidParams(): message={}", message);
    }

    // 기능: 권한이 부족하여 처리할 수 없음을 설정한다.
    public void setFailInfo2NeedAuth() {
        this.code = Const.CODE_FAILURE;
        this.status = Const.STATUS_NEED_AUTH;
        this.message = Const.MSG_NEED_AUTH;
        this.obj = null;
        logger.info("setFailInfo2NeedAuth(): message={}", message);
    }

    // 기능: obj != null 또는 error가 존재하지 않을 경우 성공 값을 설정하고 그렇지 않을 경우 실패 값을 설정한다.
    public void setSuccessOrFail(boolean flagSetResult, Object obj, String error) {
        if (flagSetResult) {
            if (obj != null || StringUtils.isEmpty(error)) {
                setSuccessInfo(obj);
            } else {
                setFailInfo(!StringUtils.isEmpty(error) ? error : message);
            }
        }
    }

    // 기능: code == Const.CODE_SUCCESS인지 검사한다.
    public boolean chkOk() {
        return code.equals(Const.CODE_SUCCESS);
    }

    public boolean chkSuccess() {
        return code.equals(Const.CODE_SUCCESS) && status.equals(Const.STATUS_SUCCESS);
    }

}
