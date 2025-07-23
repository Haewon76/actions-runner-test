package com.cashmallow.common;

import com.cashmallow.api.domain.shared.Const;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jackson.JsonComponentModule;
import org.springframework.data.geo.GeoModule;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class JsonStr {
    private static final Logger logger = LoggerFactory.getLogger(JsonStr.class);

    @Getter
    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new Jdk8Module())
            .registerModule(new JavaTimeModule().addSerializer(ZonedDateTime.class, new ZonedDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"))))
            .registerModule(new ParameterNamesModule())
            .registerModule(new JsonComponentModule())
            .registerModule(new GeoModule())
            .registerModule(new SimpleModule().addSerializer(Number.class, new ToStringSerializer()))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private JsonStr() {
        throw new IllegalStateException("Utility Class");
    }

    // 기능: JSON 문자열을 HashMap형태로 응답한다.
    public static Map<String, Object> toHashMap(String jsonParamStr) {

        HashMap<String, Object> map = new HashMap<>();

        try {
            ObjectMapper objMapper = new ObjectMapper();
            map = objMapper.readValue(jsonParamStr, new TypeReference<HashMap<String, Object>>() {
            });
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return map;
    }

    // 기능: JSON 문자열을 Object 형태로 변환한다.
    // - Jackson JSON Generic Serializing and Deserializing Methods :
    // https://morgankenyon.wordpress.com/2015/08/08/jackson-json-generic-serializing-and-deserializing-methods/
    // - Unmarshall to Collection: http://www.baeldung.com/jackson-collection-array
    // - Generic type을 변환하려면 좀 더 검색을 ....: Deserializing Generic Type Jackson
    // - Json deserialization with Jackson and Super type tokens:
    // https://www.javacodegeeks.com/2013/01/json-deserialization-with-jackson-and-super-type-tokens.html
    public static Object toObject(String className, String jsonStr) {

        Object result = null;

        try {
            ObjectMapper objMapper = new ObjectMapper();
            result = objMapper.readValue(jsonStr, Class.forName(className));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }

    // 기능: Object를 JSON으로 응답한다.
    public static String toJsonString(Object vo, HttpServletResponse response) {
        String json = toJsonString(vo);
        if (response != null) {
            response.setStatus(Const.RESPONSE_CODE_OK); // 응답할 수 있는 경우는 무조건 "200"을 응답하기로 함.
        }
        return json;
    }


    // 기능: Object를 JSON으로 응답한다.

    /**
     * Object를 JSON으로 응답한다. in snake case
     *
     * @param vo
     * @param response
     * @return
     */
    public static String toJsonStringSnake(Object vo, HttpServletResponse response) {
        String json = toJsonStringSnake(vo);

        if (response != null) {
            response.setStatus(Const.RESPONSE_CODE_OK); // 응답할 수 있는 경우는 무조건 "200"을 응답하기로 함.
        }
        //        logger.debug("toJsonString(): {}", json);
        return json;
    }

    public static String toJsonString(Object vo) {

        String json = "";
        ObjectMapper mapper = new ObjectMapper();

        try {
            json = mapper.writeValueAsString(vo);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //		logger.debug("toJsonString(): json={}",  json);
        return json;
    }

    public static String toJson(Object vo) {
        String json = "";
        try {
            json = mapper.writeValueAsString(vo);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return json;
    }

    public static String toJsonStringSnake(Object vo) {

        String json = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);

        try {
            json = mapper.writeValueAsString(vo);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //        logger.debug("toJsonString(): json={}",  json);
        return json;
    }
}
