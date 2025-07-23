package com.cashmallow.api.interfaces;

import com.cashmallow.api.domain.shared.Const;
import com.cashmallow.api.domain.shared.MsgCode;
import com.cashmallow.common.JsonStr;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
class ApiResultVOTest {

    @Test
    void successMsg() {
        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setSuccessInfo();

        String jsonString = JsonStr.toJsonString(apiResultVO);
        System.out.println("jsonString = " + jsonString);
    }

    @Test
    void failMsg() {
        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setFailInfo(Const.ALREADY_COMPLETE);
        apiResultVO.setStatus(Const.ALREADY_COMPLETE);

        String jsonString = JsonStr.toJsonString(apiResultVO);
        System.out.println("jsonString = " + jsonString);
    }

    @Test
    void errorMsg() {
        ApiResultVO apiResultVO = new ApiResultVO();
        apiResultVO.setFailInfo(MsgCode.INTERNAL_SERVER_ERROR);

        String jsonString1 = JsonStr.toJsonString(apiResultVO);
        System.out.println("jsonString1 = " + jsonString1);
    }

}