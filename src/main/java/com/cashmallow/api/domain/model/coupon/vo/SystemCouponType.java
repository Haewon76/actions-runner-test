package com.cashmallow.api.domain.model.coupon.vo;

public enum SystemCouponType {

    thankYouMyFriend("ThankYouMyFriend", "FR"),
    ThankYouMyFriendHK("ThankYouMyFriend(HK)", "FR"),
    ThankYouMyFriendJP("ThankYouMyFriend(JP)", "FR"),

    thankYouToo("ThankYouToo", "FT"),
    ThankYouTooHK("ThankYouToo(HK)", "FT"),
    ThankYouTooJP("ThankYouToo(JP)", "FT"),

    welcome("Welcome", "WC"),
    WelcomeHK("Welcome(HK)", "WC"),
    WelcomeJP("Welcome(JP)", "WC"),

    birthday("Birthday", "BD"),
    BirthdayHK("Birthday(HK)", "BD"),
    BirthdayJP("Birthday(JP)", "BD"),

    influencer("IN", "IN"),
    event("EV", "EV");

    private final String code;

    private final String abbreviation;

    private SystemCouponType(String code, String abbreviation) {
        this.code = code;
        this.abbreviation = abbreviation;
    }

    public String getCode() {
        return code;
    }

    public String getAbbreviation() {
        return abbreviation;
    }


}
