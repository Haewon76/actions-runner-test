package com.cashmallow.api.config;

import com.cashmallow.api.config.converter.*;
import com.cashmallow.api.domain.model.coupon.vo.DiscountType;
import com.cashmallow.api.domain.model.coupon.vo.ExpireType;
import com.cashmallow.api.domain.model.coupon.vo.SendType;
import com.cashmallow.api.domain.model.coupon.vo.ServiceType;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
public class GsonConfig {

    private final DiscountTypeConverter discountTypeConverter;

    private final ExpireTypeConverter expireTypeConverter;

    private final SendTypeConverter sendTypeConverter;

    private final ServiceTypeConverter serviceTypeConverter;

    // private final TargetTypeConverter targetTypeConverter;

    public static final String PATTERN_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZ";

    @Bean("gsonPretty")
    public Gson gsonPretty() {
        return new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().setPrettyPrinting().create();
    }

    @Bean("gsonSnakeCase")
    public Gson gsonSnakeCase() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Bean("gson")
    public Gson gson() {
        return new GsonBuilder().registerTypeAdapter(DiscountType.class, discountTypeConverter)
                .registerTypeAdapter(ExpireType.class, expireTypeConverter)
                .registerTypeAdapter(SendType.class, sendTypeConverter)
                .registerTypeAdapter(ServiceType.class, serviceTypeConverter)
                // .registerTypeAdapter(TargetType.class, targetTypeConverter)
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter().nullSafe())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter().nullSafe())
                .create();
    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if(value != null)
                out.value(value.format(format));
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            return LocalDateTime.parse(in.nextString(), format);
        }
    }

    static class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {
        DateTimeFormatter format = DateTimeFormatter.ofPattern(PATTERN_DATETIME);

        @Override
        public void write(JsonWriter out, ZonedDateTime value) throws IOException {
            if(value != null)
                out.value(value.format(format));
        }

        @Override
        public ZonedDateTime read(JsonReader in) throws IOException {
            return ZonedDateTime.parse(in.nextString(), format);
        }
    }
}
