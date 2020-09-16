package com.server.demo.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class PrintOutVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {
        log.info("C start");
        vertx.eventBus().consumer("address.c", this::handler);
        startFuture.complete();
    }

    private void handler(Message<String> msg) {
        String reply = msg.body() + "C";
        try {
            msg.reply(reply);
            log.info(reply);
        } catch (RuntimeException e) {
            msg.fail(404, e.getMessage());
        }
    }
}