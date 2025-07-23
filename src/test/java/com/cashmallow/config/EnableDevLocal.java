package com.cashmallow.config;

import org.springframework.test.context.junit.jupiter.EnabledIf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@EnabledIf(
        expression = "#{environment.acceptsProfiles('dev-local')}",
        loadContext = true
)
public @interface EnableDevLocal {
}