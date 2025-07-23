package com.cashmallow.api.config.converter;

import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class ServiceTypeConverter implements Converter<String, ServiceType>, JsonDeserializer<ServiceType> {

    @Override
    public ServiceType convert(@NotNull String source) {
        return ServiceType.fromString(source);
    }

    @Override
    public ServiceType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String code = json.getAsString();
        return ServiceType.fromString(code);
    }
}
