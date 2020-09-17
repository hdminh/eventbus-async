package com.server.demo;

import com.server.demo.server.RabbitServer;
import io.vertx.core.Vertx;

public class RabbitServerMain {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new RabbitServer());
    }
}
