package com.cashmallow.api.interfaces.coupon.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CouponUserInviteCodeRequest {

    private Long id;
    private Long userId;
    private String inviteCode;
    private String abbreviation;
    private String iso3166;

    public CouponUserInviteCodeRequest() {}

    @Builder
    public CouponUserInviteCodeRequest(Long userId, String inviteCode, String abbreviation, String iso3166) {
        this.userId = userId;
        this.inviteCode = inviteCode;
        this.abbreviation = abbreviation;
        this.iso3166 = iso3166;
    }

    public static CouponUserInviteCodeRequest inviteCodeRequest(String inviteCode) {
        CouponUserInviteCodeRequest request = new CouponUserInviteCodeRequest();
        request.setInviteCode(inviteCode);
        return request;
    }
}
