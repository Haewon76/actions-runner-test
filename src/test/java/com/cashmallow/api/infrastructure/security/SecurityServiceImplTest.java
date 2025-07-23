package com.cashmallow.api.infrastructure.security;

import com.cashmallow.config.EnableDevLocal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@EnableDevLocal
class SecurityServiceImplTest {

    @Autowired
    private SecurityServiceImpl securityService;

    @DisplayName(" 유닛테스트")
    @Test
    public void given_when_than() {

        //given - precondition or setup
        final String s = securityService.encryptSHA2("tiger001!");
        System.out.println(s);
        // CB9AB7BCCC4A01AD21F6477466FE82BD3289B1ACB4D0E0798FEB48FE50FDC1FE2662F8DCF3ECF42F7BA64766892E79AD15FD4698A57A44E49897F2C8C4974E61
        // A99E7B73043726E8E0E1696B1E30C2FF94F102F06E32F205B4FD28C4F5324EABE3385DBA1ABFDB41F31CB3CFF78D026930DA2027FA4D892ED0A0BA22A3CDC99D
        // when - action or the behaviour that we are going test

        // then - verify the output

        String s1 = securityService.encryptAES256("M123A4567");
        System.out.println("s1 = " + s1);

    }

    @Test
    public void 일본어_성_이름_암호화() {
        List.of("なら","あか").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
        System.out.println("=================");
        List.of("喔去","欸天").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
        System.out.println("=================");
        List.of("平八郎","東郷").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
        System.out.println("=================");
        List.of("文雄","岸き").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
        System.out.println("=================");
        List.of("次郎","小泉進").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
        System.out.println("=================");
        List.of("の","の").forEach(f -> {
            String s1 = securityService.encryptAES256(f);
            System.out.println("s1 = " + s1);
        });
    }

    @Test
    public void null_암호화_테스트() {
        String str = "null";
        String s = securityService.encryptAES256(str);
        System.out.println("s = " + s);

    }
    @Test
    public void null_복호화_테스트() {
        String str = "c71363c8bed55e35d2635fe774c165f2";
        String s = securityService.decryptAES256(str);
        String s1 = securityService.encryptAES256("+8523338864"); // 534e2d1dee98d0c6ce346fbc372bc70e
        System.out.println("s = " + s);
        System.out.println("s = " + s1);

    }
}