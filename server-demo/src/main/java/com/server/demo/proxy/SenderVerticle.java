package com.server.demo.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
public class SenderVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> startFuture) {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/api/sync").handler(this::syncEvent);
        router.get("/api/async").handler(this::aSyncEvent);

        Promise<Void> promise = Promise.promise();
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router);
        server.listen(config().getInteger("http.port", 8000),
                result -> {
                    if (result.succeeded()) {
                        log.info("server started at port 8000");
                        promise.complete();
                    } else {
                        promise.fail(result.cause());
                    }
                });
    }

    private void syncEvent(RoutingContext ctx) {
        String msg = ctx.getBodyAsString();
        callB(msg).future()
                .onComplete(h -> {
                    log.info("B " + h.result());
                    callC(h.result()).future()
                            .onComplete(r -> ctx.request().response().setStatusCode(200).end(r.result()))
                            .onSuccess(x -> log.info("kkk: {}", x))
                            .onFailure(e -> {
                                log.error(e.getCause().getMessage());
                                ctx.request().response().end(e.getMessage());
                            });
                })
                .onSuccess(s -> log.info("ka ka ka"))
                .onFailure(e -> {
                    log.error(e.getCause().getMessage());
                    ctx.request().response().end(e.getMessage());
                });

    }

    public void aSyncEvent(RoutingContext ctx) {
        String msg = ctx.getBodyAsString();

        CompositeFuture.all(callB(msg).future(), callC(msg).future())
                .map(data -> {
                    List<String> result = data.list();
                    return true;
                });

        callB(msg).future().onSuccess(b -> {
            callC(b).future().onSuccess(c -> {

            });
        });


    }

    private Promise<String> callB(String msg) {
        Promise<String> promise = Promise.promise();
        vertx.eventBus().request("address.b", msg, reply -> {
            if (reply.succeeded()) {
                log.info(reply.result().body().toString());
                promise.complete(reply.result().body().toString());
            } else {
                promise.fail(reply.cause());
            }
        });
        return promise;
    }

    private Promise<String> callC(String msg) {
        Promise<String> promise = Promise.promise();
        vertx.eventBus().request("address.c", msg, reply -> {
            if (reply.succeeded()) {
                log.info(reply.result().body().toString());
                promise.complete(reply.result().body().toString());
            } else {
                promise.fail(reply.cause());
            }
        });
        return promise;
    }

    @Override
    public void stop(Future<Void> stopFuture) {
    }
}
