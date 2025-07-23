package com.cashmallow.common;

import org.junit.jupiter.api.Test;

import static com.cashmallow.common.DateUtil.isDate;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateUtilTest {

    @Test
    public void 날짜_형식이_올바른_경우() {
        String date = "2022-06-28";
        assertTrue(isDate(date));
    }

    @Test
    public void 날짜_형식이_올바르지_않은_경우() {
        String date = "05-80";
        assertFalse(isDate(date));
    }

    @Test
    public void 날짜_형식이_올바르지_않은_경우_다른_형식() {
        String date = "21 AUG 21";
        assertFalse(isDate(date));
    }

    @Test
    public void 날짜_형식이_null_인_경우() {
        String date = null;
        assertFalse(isDate(date));
    }

    @Test
    public void 날짜_형식이_빈_문자열_인_경우() {
        String date = "";
        assertFalse(isDate(date));
    }

}