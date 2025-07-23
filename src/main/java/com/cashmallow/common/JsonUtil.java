package com.cashmallow.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {

    private final ObjectMapper mapper;

    //  리스트 형식의 경우 TypeReference를 사용
    public <T> T fromJson(String jsonStr, TypeReference<T> typeReference) {
        T result = null;
        try {
            result = mapper.readValue(jsonStr, typeReference);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return result;
    }

    //  일반 클래스 형식의 경우 Class를 사용하도록 처리
    public <T> T fromJson(String jsonStr, Class<T> clazz) {
        T result = null;
        try {
            result = mapper.readValue(jsonStr, clazz);
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return result;
    }

    public String toJson(Object vo) {

        String json = "";

        try {
            json = mapper.writeValueAsString(vo);
        } catch (IOException e) {
            log.info(e.getMessage(), e);
        }
        return json;
    }

    public String toJsonPretty(Object vo) {

        String json = "";

        try {
            json = mapper.writeValueAsString(vo);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return json;
    }

}
