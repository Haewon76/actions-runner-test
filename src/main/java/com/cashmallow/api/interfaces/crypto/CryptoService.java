package com.cashmallow.api.interfaces.crypto;

import com.cashmallow.common.JsonUtil;
import com.cashmallow.common.CustomStringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unchecked")
public class CryptoService {

    private final JsonUtil jsonUtil;

    public Object decrypt(String token, String requestJsonBody) {
        String json = CustomStringUtil.decode(token, requestJsonBody);
        if (StringUtils.isNotEmpty(json)) {
            try {
                Map<String, String> map = jsonUtil.fromJson(json, Map.class);
                String decode = CustomStringUtil.decode(map.get("key"), map.get("text"));
                try {
                    if (StringUtils.isBlank(decode)) {
                        decode = CustomStringUtil.decode(token, requestJsonBody);
                    }
                    // json이 맞는지 확인
                    Object o = jsonUtil.fromJson(decode, Object.class);
                    if(o == null) {
                        return decode;
                    }
                    return o;
                } catch (Exception e) {
                    // json이 아닌 경우
                    return decode;
                }
            } catch (Exception ignored) {
                log.error("Failed to decrypt json: {}", json);
            }
            return json;
        }

        return null;
    }
}
