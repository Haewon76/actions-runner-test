package com.cashmallow.api.application;


import org.springframework.http.HttpStatus;

import java.util.concurrent.Future;

public interface MLWebhookService<T> {

    Future<HttpStatus> send(T t);


}