package com.cashmallow.api.config.converter;

import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class ExpireTypeConverter implements Converter<String, ExpireType>, JsonDeserializer<ExpireType> {

    @Override
    public ExpireType convert(@NotNull String source) {
        return ExpireType.fromString(source);
    }

    @Override
    public ExpireType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String code = json.getAsString();
        return ExpireType.fromString(code);
    }
}
