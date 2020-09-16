package com.server.demo;

import com.server.demo.server.RabbitServer;
import io.vertx.core.Vertx;

public class RabbitServerMain {
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
//        vertx.deployVerticle(new TimerVerticle());
//        vertx.deployVerticle(new PrintOutVerticle());
//        vertx.deployVerticle(new SenderVerticle());
        vertx.deployVerticle(new RabbitServer());
    }
}
