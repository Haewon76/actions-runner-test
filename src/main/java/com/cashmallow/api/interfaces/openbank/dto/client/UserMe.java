package com.cashmallow.api.interfaces.openbank.dto.client;

import com.cashmallow.common.CustomStringUtil;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/*
{
    "api_tran_id": "f61b5d17-e00d-469f-b322-c845763a7861",
    "rsp_code": "A0000",
    "rsp_message": "",
    "api_tran_dtm": "20230410144838159",
    "user_seq_no": "1101026667",
    "user_ci": "0Q/ZGQfQm6j84pbqsntjZiU4OcAdi7/8nvqEXfZ9BvwMa0cg/ZgsetgV88a8DJr6sh59NGUWpAqmC7IAKe4//w==",
    "user_name": "홍길동",
    "res_cnt": "2",
    "res_list": [{
        "fintech_use_num": "120220187388941172640333",
        "account_alias": "JD",
        "bank_code_std": "003",
        "bank_code_sub": "0000000",
        "bankName": "IBK기업은행",
        "account_num_masked": "010211588***",
        "account_holder_name": "홍길동",
        "account_holder_type": "P",
        "inquiry_agree_yn": "N",
        "inquiry_agree_dtime": "",
        "transfer_agree_yn": "Y",
        "transfer_agree_dtime": "20230410144727",
        "payer_num": "20230410319808135955",
        "savings_bank_name": "",
        "account_seq": "",
        "accountType": "1"
    }, {
        "fintech_use_num": "120220187388941172649341",
        "account_alias": "테스트",
        "bank_code_std": "002",
        "bank_code_sub": "0000000",
        "bankName": "KDB산업은행",
        "account_num_masked": "01027511***",
        "account_holder_name": "홍길동",
        "account_holder_type": "P",
        "inquiry_agree_yn": "N",
        "inquiry_agree_dtime": "",
        "transfer_agree_yn": "Y",
        "transfer_agree_dtime": "20230410144057",
        "payer_num": "20230410319808126595",
        "savings_bank_name": "",
        "account_seq": "",
        "accountType": "1"
    }],
    "inquiry_card_cnt": "0",
    "inquiry_card_list": [],
    "inquiry_pay_cnt": "0",
    "inquiry_pay_list": [],
    "inquiry_insurance_cnt": "0",
    "inquiry_insurance_list": [],
    "inquiry_loan_cnt": "0",
    "inquiry_loan_list": []
}
*/
@Data
public class UserMe {
    private String api_tran_id;
    private String rsp_code;
    private String rsp_message;
    private String api_tran_dtm;
    private String user_seq_no;
    private String user_ci;
    private String user_name;
    private String res_cnt;
    private String inquiry_card_cnt;
    private Object inquiry_card_list;
    private String inquiry_pay_cnt;
    private Object inquiry_pay_list;
    private String inquiry_insurance_cnt;
    private Object inquiry_insurance_list;
    private String inquiry_loan_cnt;
    private Object inquiry_loan_list;
    List<UserMeDetail> res_list;

    @JsonIgnore
    public boolean isSuccess() {
        return StringUtils.equals("A0000", this.rsp_code);
    }

    @JsonIgnore
    public boolean isNotMatchUserName(String nameByTravelerId) {
        return !CustomStringUtil.matchMaskedString(this.getUser_name(), nameByTravelerId);
    }

    @Override
    public String toString() {
        return "UserMe{" +
                "api_tran_id='" + api_tran_id + '\'' +
                ", rsp_code='" + rsp_code + '\'' +
                ", rsp_message='" + rsp_message + '\'' +
                ", api_tran_dtm='" + api_tran_dtm + '\'' +
                ", user_seq_no='" + user_seq_no + '\'' +
                ", user_name='" + CustomStringUtil.maskingName(user_name) + '\'' +
                ", res_cnt='" + res_cnt + '\'' +
                ", res_list=" + res_list +
                '}';
    }
}
