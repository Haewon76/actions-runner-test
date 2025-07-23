package com.cashmallow.api.domain.model.openbank;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class OpenbankLogMapperTest {

    @Autowired
    OpenbankLogMapper openbankLogMapper;

    @Test
    void saveRequestAndResponse() {
        // given
        OpenbankLogRequest request = new OpenbankLogRequest(
                OpenbankRequestType.ISSUE_TOKEN,
                "1234",
                "requestText"
        );

        // when
        openbankLogMapper.saveRequest(request);

        openbankLogMapper.saveResponse(new OpenbankLogResponse(
                request.getId(),
                "200 OK",
                "12341234",
                "responseText",
                "A0000",
                ""
        ));

        // then
        Assertions.assertThat(request.getId()).isNotEqualTo(0L);
        OpenbankLog openbankLog = openbankLogMapper.getOpenbankLogById(request.getId());
        Assertions.assertThat(openbankLog.getResponseCode()).isEqualTo("200 OK");
    }
}