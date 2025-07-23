package com.cashmallow.api.interfaces.memo;

import com.cashmallow.api.domain.model.memo.Memo;

import java.util.List;
import java.util.Map;

public interface MemoService {

    int saveMemo(Map<String, Object> memoParams);

    int createMemo(Map<String, Object> memoParams);

    int modifyMemo(Map<String, Object> memoParams);

    List<Memo> getMemoList(Map<String, Object> memoParams);

}