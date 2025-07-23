package com.cashmallow.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegularExpressionUtilTest {

    @Test
    public void 연속된_숫자_테스트() {
        assertTrue(RegularExpressionUtil.sequenceNumbers("323421"));
        assertTrue(RegularExpressionUtil.sequenceNumbers("187623"));
    }

    @Test
    public void 숫자_빈도수_테스트() {
        assertTrue(RegularExpressionUtil.frequencyNumber("423343", 3));
    }

    @Test
    public void 핀코드_핸드폰번호_테스트() {
        assertFalse(RegularExpressionUtil.isPinCodeContainsPhoneNumber("423232", null));

        assertFalse(RegularExpressionUtil.isPinCodeContainsPhoneNumber("423231", "+85201072661662"));

        assertTrue(RegularExpressionUtil.isPinCodeContainsPhoneNumber("427266", "+85201072661662"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsPhoneNumber("726633", "+85201072661662"));

        assertTrue(RegularExpressionUtil.isPinCodeContainsPhoneNumber("166233", "+85201072661662"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsPhoneNumber("416624", "+85201072661662"));

        assertFalse(RegularExpressionUtil.isPinCodeContainsPhoneNumber("416624", "+85222232"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsPhoneNumber("416624", "+85226624"));

    }

    @Test
    public void 핀코드_생년월일_테스트() {
        assertFalse(RegularExpressionUtil.isPinCodeContainsBirthDate("423232", null));

        assertFalse(RegularExpressionUtil.isPinCodeContainsBirthDate("419712", "19780217"));

        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("419782", "19780217"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("402172", "19780217"));

        assertFalse(RegularExpressionUtil.isPinCodeContainsBirthDate("478122", "780217"));

        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("478022", "780217"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("402172", "780217"));

        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("802122", "19780217"));
        assertTrue(RegularExpressionUtil.isPinCodeContainsBirthDate("407802", "19780217"));

    }


}