package com.cashmallow.api.interfaces.memo;

import com.cashmallow.api.domain.model.memo.Memo;
import com.cashmallow.api.domain.model.memo.MemoLog;
import com.cashmallow.api.domain.model.memo.MemoLogMapper;
import com.cashmallow.api.domain.model.memo.MemoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemoServiceImpl implements MemoService {
    private static final Logger logger = LoggerFactory.getLogger(MemoServiceImpl.class);


    final private MemoMapper memoMapper;

    final private MemoLogMapper memoLogMapper;

    @Override
    public int saveMemo(Map<String, Object> memoParams) {
        Map<String, Object> params = new HashMap<>();
        params.put("refId", memoParams.get("refId"));

        List<Memo> memoList = memoMapper.getMemoList(params);

        List<Memo> memoListResult = new ArrayList<>();

        for (Memo memo : memoList) {
            memo.setCreatorName(memo.getLastName() + " " + memo.getFirstName());
            memoListResult.add(memo);
        }

        if (!CollectionUtils.isEmpty(memoListResult)) {
            return modifyMemo(memoParams);
        } else {
            int results = createMemo(memoParams);
            return results;
        }

    }

    @Override
    public int createMemo(Map<String, Object> memoParams) {
        Calendar cal = Calendar.getInstance();
        Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());

        Memo memo = Memo.builder()
                .refId(Long.valueOf((String) memoParams.get("refId")))
                .memo((String) memoParams.get("memo"))
                .type((String) memoParams.get("type"))
                .creatorId((Long) memoParams.get("creatorId"))
                .createdAt(toDayTimestamp)
                .updatedAt(toDayTimestamp)
                .build();
        int results = memoMapper.registerMemo(memo);

        try {
            MemoLog memoLogParam = new MemoLog();
            BeanUtils.copyProperties(memo, memoLogParam);
            memoLogParam.setId(null);
            memoLogParam.setMemoId(memo.getId());
            memoLogMapper.registerMemoLog(memoLogParam);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return results;
    }

    @Override
    public int modifyMemo(Map<String, Object> memoParams) {
        Calendar cal = Calendar.getInstance();
        Timestamp toDayTimestamp = new Timestamp(cal.getTime().getTime());

        Map<String, Object> params = new HashMap<>();
        params.put("refId", memoParams.get("refId"));

        List<Memo> memoList = memoMapper.getMemoList(params);

        List<Memo> memoListResult = new ArrayList<>();

        for (Memo memo : memoList) {
            memo.setCreatorName(memo.getLastName() + " " + memo.getFirstName());
            memoListResult.add(memo);
        }

        if (!CollectionUtils.isEmpty(memoListResult)) {
            Memo memo = memoListResult.get(0);
            memo.setUpdatedAt(toDayTimestamp);
            memo.setMemo((String) memoParams.get("memo"));
            memo.setRefId(Long.valueOf((String) memoParams.get("refId")));

            try {
                MemoLog memoLogParam = new MemoLog();
                BeanUtils.copyProperties(memo, memoLogParam);
                memoLogParam.setId(null);
                memoLogParam.setMemoId(memo.getId());
                memoLogParam.setCreatedAt(toDayTimestamp);
                memoLogParam.setCreatorId((Long) memoParams.get("creatorId"));

                memoLogMapper.registerMemoLog(memoLogParam);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            return memoMapper.updateMemo(memo);
        }

        return 0;
    }

    @Override
    public List<Memo> getMemoList(Map<String, Object> memoParams) {

        List<Memo> memoList = memoMapper.getMemoList(memoParams);

        List<Memo> memoListResult = new ArrayList<>();

        for (Memo memo : memoList) {
            memo.setCreatorName(memo.getLastName() + " " + memo.getFirstName());
            memoListResult.add(memo);
        }

        return memoListResult;
    }
}
