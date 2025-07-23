package com.cashmallow.api.config.converter;

import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class DiscountTypeConverter implements Converter<String, DiscountType>, JsonDeserializer<DiscountType> {

    @Override
    public DiscountType convert(@NotNull String source) {
        return DiscountType.fromString(source);
    }

    @Override
    public DiscountType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String code = json.getAsString();
        return DiscountType.fromString(code);
    }
}
