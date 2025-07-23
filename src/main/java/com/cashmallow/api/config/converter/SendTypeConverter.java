package com.cashmallow.api.config.converter;

import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class SendTypeConverter implements Converter<String, SendType>, JsonDeserializer<SendType> {

    @Override
    public SendType convert(@NotNull String source) {
        return SendType.fromString(source);
    }

    @Override
    public SendType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String code = json.getAsString();
        return SendType.fromString(code);
    }
}
