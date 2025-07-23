package com.cashmallow.api.domain.model.memo;

import java.util.List;
import java.util.Map;

public interface MemoLogMapper {

    List<MemoLog> getMemoLogList(Map<String, Object> memoLogParams);

    int registerMemoLog(MemoLog memoLog);

}
