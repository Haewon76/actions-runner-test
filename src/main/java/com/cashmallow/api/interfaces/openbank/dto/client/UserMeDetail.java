package com.cashmallow.api.interfaces.openbank.dto.client;


import com.cashmallow.common.CustomStringUtil;
import lombok.Data;

@Data
public class UserMeDetail {
    private String fintech_use_num;     // 핀테크 이용번호
    private String account_alias;
    private String bank_code_std;       // 은행코드
    private String bank_code_sub;
    private String bank_name;           //
    private String account_num_masked;  //
    private String account_holder_name; //
    private String account_holder_type;
    private String inquiry_agree_yn;
    private String inquiry_agree_dtime;
    private String transfer_agree_yn;
    private String transfer_agree_dtime;
    private String payer_num;
    private String savings_bank_name;
    private String account_seq;
    private String account_type;
    private String user_ci;             // CI
    private String traveler_id;         //

    @Override
    public String toString() {
        return "UserMeDetail{" +
                "fintech_use_num='" + fintech_use_num + '\'' +
                ", account_alias='" + account_alias + '\'' +
                ", bank_code_std='" + bank_code_std + '\'' +
                ", bankName='" + bank_name + '\'' +
                ", account_num_masked='" + account_num_masked + '\'' +
                ", account_holder_name='" + CustomStringUtil.maskingName(account_holder_name) + '\'' +
                ", account_holder_type='" + account_holder_type + '\'' +
                ", transfer_agree_yn='" + transfer_agree_yn + '\'' +
                ", transfer_agree_dtime='" + transfer_agree_dtime + '\'' +
                ", savings_bank_name='" + savings_bank_name + '\'' +
                '}';
    }
}
