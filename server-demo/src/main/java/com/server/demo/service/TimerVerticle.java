package com.server.demo.service;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimerVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startFuture) {
        log.info("B start");
        vertx.eventBus().consumer("address.b", this::handler);
        startFuture.complete();
    }

    private void handler(Message<String> msg) {
        long sleepTime = Long.valueOf(msg.body());

        try {
            vertx.setTimer(sleepTime, id -> {
                msg.reply("I'm sleep " + sleepTime);
                log.info(sleepTime);
                vertx.cancelTimer(id);
            });
        } catch (RuntimeException e) {
            msg.fail(404, e.getMessage());
        }
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        super.stop(stopFuture);
    }
}
