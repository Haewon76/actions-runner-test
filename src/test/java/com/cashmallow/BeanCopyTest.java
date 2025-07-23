package com.cashmallow;

import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;

@Slf4j
public class BeanCopyTest {

    @Test
    public void BeanUtils_copyProperties_TEST() {
        AClass orign = new AClass();
        orign.setA("1");
        orign.setA_2("Q2");
        orign.setA_3("1asd");
        orign.setA_4("155");
        BClassVo dest = new BClassVo();

        BeanUtils.copyProperties(orign, dest, "a2");

        BClassVo bClass = new Gson().fromJson(new Gson().toJson(orign), BClassVo.class);
        log.info(new Gson().toJson(dest));
    }

    @Data
    class AClass {
        private String a;
        private String a2;
        private String a_2;
        private String a_3;
        private String a_4;
    }

    @Data
    class BClassVo {
        private String a;
        private String a2;
        private String a4;
    }
}
