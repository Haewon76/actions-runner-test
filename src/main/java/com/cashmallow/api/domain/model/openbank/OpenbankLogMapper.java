package com.cashmallow.api.domain.model.openbank;

import java.util.List;

public interface OpenbankLogMapper {

    List<OpenbankLog> getOpenbankLogs();

    OpenbankLog getOpenbankLogById(long id);

    int saveRequest(OpenbankLogRequest request);

    int saveResponse(OpenbankLogResponse response);
}
