package com.gateway.demo;

import com.gateway.demo.client.RabbitClient;
import io.vertx.core.Vertx;

public class RabbitClientMain {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RabbitClient());

    }
}
