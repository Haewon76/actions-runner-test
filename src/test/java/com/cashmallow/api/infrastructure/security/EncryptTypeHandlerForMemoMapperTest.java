package com.cashmallow.api.infrastructure.security;

import com.cashmallow.api.domain.model.memo.Memo;
import com.cashmallow.api.domain.model.memo.MemoMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class EncryptTypeHandlerForMemoMapperTest {


    @Autowired
    MemoMapper memoMapper;

    @Test
    @Transactional
    @Disabled
    void TypeHandler_암복호화_getMemoList() {

        Map<String, Object> params = new HashMap<>();
        params.put("refId", 80);
        List<Memo> obj = memoMapper.getMemoList(params);

        assertNotNull(obj);


    }


}