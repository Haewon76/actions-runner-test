package com.cashmallow.api.infrastructure.aml;

public interface OctaService<T, R> {

    R execute(T request);

    String getURL();

}
