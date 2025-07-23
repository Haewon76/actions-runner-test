package com.cashmallow.api.config.converter;

import com.cashmallow.api.domain.model.country.enums.CountryInfo;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CountryInfoConverter implements Converter<String, CountryInfo> {

    @Override
    public CountryInfo convert(String source) {
        return CountryInfo.valueOf(source.toUpperCase());
    }
}
