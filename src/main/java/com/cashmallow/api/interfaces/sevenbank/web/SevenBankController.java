package com.cashmallow.api.interfaces.sevenbank.web;

import com.cashmallow.api.domain.model.cashout.CashOut;
import com.cashmallow.api.domain.model.cashout.CashoutRepositoryService;
import com.cashmallow.api.domain.shared.CashmallowException;
import com.cashmallow.api.interfaces.GlobalConst;
import com.cashmallow.api.interfaces.sevenbank.facade.SevenBankServiceImpl;
import com.cashmallow.common.CommNet;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Controller
@RequestMapping("/sevenbank")
public class SevenBankController {
    private final Logger logger = LoggerFactory.getLogger(SevenBankController.class);

    // Seven Bank API Result code to be received
    //    private static final String RESULT_FUNC_NOTIFY = "NotifyResult";

    // Seven Bank API Result code to be returned.
    //    private static final String RESULT_VERIFICATION_ERROR = "E002";

    @Autowired
    private SevenBankServiceImpl sevenBankService;

    @Autowired
    private CashoutRepositoryService cashoutRepositoryService;

    @Autowired
    private MessageSource messageSource;


    @PostMapping(value = "/cashout/notify", produces = GlobalConst.PRODUCES)
    public void receiveWithdrawalNotify(@RequestHeader("SignH") String signH,
                                        HttpServletRequest request, HttpServletResponse response) throws IOException {

        String method = "receiveWithdrawalNotify()";

        JSONObject result = new JSONObject();
        String body = CommNet.extractPostRequestBody(request);
        logger.info("receiveWithdrawalNotify(): body={}", body);

        byte[] bodyByte = body.getBytes(StandardCharsets.UTF_8);

        String bodyStr = new String(Base64.decodeBase64(bodyByte));

        String signHFromBody = sevenBankService.makeSignH(bodyByte);
        //        String signHFromBodyUpperCase = new String(signHFromBody).toUpperCase();
        logger.info("receiveWithdrawalNotify(): signH={}, sevenBankService.makeSignH(bodyByte)={}, bodyStr={}", signH, signHFromBody, bodyStr);

        if (!signH.equals(signHFromBody)) {

            logger.info("signH={}, signHFromBody{}", signH, signHFromBody, bodyStr);

            result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_ERROR);
            result.put(SevenBankServiceImpl.KEY_RESULT_CODE, SevenBankServiceImpl.ERROR_INVALID_DIGI_SIGN);

            byte[] resultBodyByte = Base64.encodeBase64(result.toString().getBytes(StandardCharsets.UTF_8));
            response.addHeader("SignH", sevenBankService.makeSignH(resultBodyByte.toString().getBytes()));
            response.getOutputStream().write(resultBodyByte);
            return;
        }

        JSONObject pJson = new JSONObject(bodyStr);
        String func = pJson.getString("func");
        JSONObject data = pJson.getJSONObject("args");

        if (!SevenBankServiceImpl.FUNC_NOTIFY_RESULT.equals(func)) {
            result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_ERROR);
            result.put(SevenBankServiceImpl.KEY_RESULT_CODE, SevenBankServiceImpl.ERROR_FUNCTION_NOT_FOUND);
            logger.error("{}: Known function. result={}", method, result);

            byte[] resultBodyByte = Base64.encodeBase64(result.toString().getBytes(StandardCharsets.UTF_8));
            response.addHeader("SignH", sevenBankService.makeSignH(resultBodyByte.toString().getBytes()));
            response.getOutputStream().write(resultBodyByte);

            return;
        }

        if (SevenBankServiceImpl.STATUS_DONE.equals(data.getString("result"))) {

            String remittanceId = data.getString("remittanceId");
            Locale locale = new Locale("en");

            try {
                sevenBankService.completeCashOutSevenBank(remittanceId);
                result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_SUCCESS);
            } catch (CashmallowException e) {
                logger.error(e.getMessage(), e);
                result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_SUCCESS);
                result.put(SevenBankServiceImpl.KEY_RESULT_CODE, e.getMessage());
            }

        } else if (SevenBankServiceImpl.STATUS_EXPIRED.equals(data.getString("result"))) {

            String remittanceId = data.getString("remittanceId");
            Long cashoutId = sevenBankService.makeCashoutId(remittanceId);
            CashOut cashOut = cashoutRepositoryService.getCashOut(cashoutId);
            Locale locale = new Locale("en");

            try {

                // todo 4132건 임시 우회 나중에 제거 할 것.
                Set<String> errorRemittanceIds = new HashSet<>();
                errorRemittanceIds.add("4132");
                errorRemittanceIds.add("4154");
                errorRemittanceIds.add("4155");

                if (!errorRemittanceIds.contains(remittanceId)) {
                    sevenBankService.cancelCashOutSevenBankByTimeout(cashOut, CashOut.CoStatus.CC.name());
                }
                result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_SUCCESS);
            } catch (CashmallowException e) {
                logger.error(e.getMessage(), e);
                result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_SUCCESS);
                result.put(SevenBankServiceImpl.KEY_RESULT_CODE, e.getMessage());
            }

        } else {
            // 아직 인출이 안되고 신청만 되어 있는 경우 등..
            logger.info("{}: The withdrawal is not yet complete. pJson={}", method, pJson);
            result.put(SevenBankServiceImpl.KEY_RESULT, SevenBankServiceImpl.RESULT_ERROR);
            result.put(SevenBankServiceImpl.KEY_RESULT_CODE, SevenBankServiceImpl.ERROR_REMITTANCE_NOT_FOUND);

        }

        String resultStr = result.toString();
        logger.info("result={}, resultStr={}", result, resultStr);

        byte[] resultBodyByte = Base64.encodeBase64(result.toString().getBytes(StandardCharsets.UTF_8));
        response.addHeader("SignH", sevenBankService.makeSignH(resultBodyByte));
        response.getOutputStream().write(resultBodyByte);
    }
}
