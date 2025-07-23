package com.cashmallow.api.domain.model.memo;

import java.util.List;
import java.util.Map;

public interface MemoMapper {

    List<Memo> getMemoList(Map<String, Object> memoParams);

    int registerMemo(Memo memo);

    int updateMemo(Memo memo);

}
