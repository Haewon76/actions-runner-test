package com.cashmallow.api.interfaces.batch.web;

import com.cashmallow.api.application.SystemService;
import com.cashmallow.api.interfaces.ApiResultVO;
import com.cashmallow.common.CustomStringUtil;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controller for cashmallow-batch system
 *
 * @author swshin
 */
@Controller
@RequestMapping("/batch")
public class BatchController {

    private static final Logger logger = LoggerFactory.getLogger(BatchController.class);

    @Autowired
    private SystemService systemService; // root-context.xml에 정의된 bean 이름과 mapping됨.

    /**
     * Return system status
     *
     * @param token
     * @param request
     * @param response
     * @return Normal -> obj:"OK", Failure -> message:<error message>
     */
    @GetMapping(value = "/system-status", produces = "application/json; charset=utf8")
    @ResponseBody
    public String getSystemStatus(@RequestHeader("Authorization") String token, HttpServletRequest request, HttpServletResponse response) {

        logger.info("getSystemStatus(): system health check");

        ApiResultVO resultVO = new ApiResultVO();
        try {
            JSONObject status = systemService.getSystemStatus();

            resultVO.setSuccessInfo(status.toString());

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultVO.setFailInfo(e.getMessage());
        }

        return CustomStringUtil.encryptJsonString(token, resultVO, response);
    }

}
